package com.famaridon.server;

import com.famaridon.folder.sync.ProjectAutoDeployScannerThread;
import com.famaridon.folder.sync.event.listener.impl.LessEventListener;
import com.famaridon.folder.sync.event.listener.impl.VDocHostDeployerEventListener;
import com.famaridon.server.beans.Action;
import com.famaridon.server.beans.UnwatchQuery;
import com.famaridon.server.beans.WatchQuery;
import com.famaridon.server.exception.QueryException;
import com.famaridon.server.ui.WatcherGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rolfone on 09/11/14.
 */
public class ServerThread implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerThread.class);
	private int port;
	private boolean running = true;
	private Map<String, ProjectAutoDeployScannerThread> runningThread = new HashMap<>();
	private WatcherGUI gui;

	public ServerThread(int port) {
		this.port = port;
	}

	public ServerThread(int port, WatcherGUI gui) {
		this.port = port;
		this.gui = gui;
	}

	@Override
	public void run() {

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			LOGGER.info("Server starting on port {}",port);
			while (running) {
				Socket socket = serverSocket.accept();
				LOGGER.debug("Accept connexion from {}:{}", socket.getInetAddress(), socket.getLocalPort());
				try {
					handlingQuery(socket);
				} catch (QueryException e) {
					LOGGER.error("Invalid query sequence!", e);
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						LOGGER.error("Client socket close fail. ", e);
					}
				}
			}
			stopWatchers();

		} catch (IOException e) {
			LOGGER.error("IO fail : ", e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					LOGGER.error("Server socket close fail. ", e);
				}
			}
		}
	}


	private void handlingQuery(Socket socket) throws QueryException {
		try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
			Action queryAction = readAction(inputStream);
			switch (queryAction) {
				case STOP:
					handleStop();
					break;
				case WATCH:
					WatchQuery watchQuery = readWatchQuery(inputStream);
					handleWatchQuery(watchQuery);
					break;
				case UNWATCH:
					UnwatchQuery unwatchQuery = readUnwatchQuery(inputStream);
					handleUnwatchQuery(unwatchQuery);
					break;
				default:
					LOGGER.warn("Query {} actually not implemented!", queryAction);
			}

		} catch (IOException e) {
			LOGGER.error("Socket can't be read correctly!", e);
		}

	}

	private void handleUnwatchQuery(UnwatchQuery unwatchQuery) {
		LOGGER.debug("UNWATCH Query received.");
		this.stopWatcher(unwatchQuery.getProject());
	}

	private void handleWatchQuery(WatchQuery query) {
		LOGGER.debug("WATCH Query received.");
		if (this.runningThread.containsKey(query.getProject())) {
			LOGGER.warn("Project already watched!");
			return;
		}
		Path source = Paths.get(query.getProjectPath());
		Path target = Paths.get(query.getVdocPath());
		ProjectAutoDeployScannerThread projectAutoDeployScanner = new ProjectAutoDeployScannerThread(source);
		projectAutoDeployScanner.addFolderEventListener(new VDocHostDeployerEventListener(source, target));
		projectAutoDeployScanner.addFolderEventListener(new LessEventListener(source, target));

		for (String excludeMatcher : query.getExcludeList()) {
			projectAutoDeployScanner.addExcludeMatcher(excludeMatcher);
		}


		// start the thread
		Thread t = new Thread(projectAutoDeployScanner);
		t.setName(query.getProject());
		t.start();
		this.runningThread.put(query.getProject(), projectAutoDeployScanner);
	}

	private void handleStop() {
		LOGGER.debug("STOP Query received.");
		synchronized (this) {
			Thread.yield();
			this.running = false;
		}
	}

	private void stopWatcher(String projectName) {
		ProjectAutoDeployScannerThread thread = this.runningThread.get(projectName);
		if (thread == null) {
			LOGGER.warn("No thread found for project {}", projectName);
			return;
		}
		// stop
		thread.stopWatching();

		this.runningThread.remove(projectName);
	}

	private void stopWatchers() {
		// stop all
		for (ProjectAutoDeployScannerThread thread : runningThread.values()) {
			thread.stopWatching();
		}

		// join all
		for (ProjectAutoDeployScannerThread thread : runningThread.values()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				LOGGER.warn("Can't join thread {}", thread.getName(), e);
			}
		}
	}

	private WatchQuery readWatchQuery(ObjectInputStream inputStream) throws QueryException {
		try {
			return (WatchQuery) inputStream.readObject();
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			throw new QueryException("WatchQuery can't be read!", e);
		}
	}

	private Action readAction(ObjectInputStream inputStream) throws QueryException {
		try {
			return (Action) inputStream.readObject();
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			throw new QueryException("Query Action can't be read!", e);
		}
	}

	private UnwatchQuery readUnwatchQuery(ObjectInputStream inputStream) throws QueryException {
		try {
			return (UnwatchQuery) inputStream.readObject();
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			throw new QueryException("UnwatchQuery can't be read!", e);
		}
	}

}
