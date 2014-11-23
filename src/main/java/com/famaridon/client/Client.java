package com.famaridon.client;

import com.famaridon.Main;
import com.famaridon.server.beans.Action;
import com.famaridon.server.beans.Query;
import com.famaridon.server.beans.UnwatchQuery;
import com.famaridon.server.beans.WatchQuery;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by rolfone on 11/11/14.
 */
public class Client {

	private final InetAddress host;
	private final int port;

	public Client() {
		this(InetAddress.getLoopbackAddress(), Main.DEFAULT_PORT);
	}

	public Client(int port) {
		this(InetAddress.getLoopbackAddress(), port);
	}

	public Client(String host) throws UnknownHostException {
		this(InetAddress.getByName(host), Main.DEFAULT_PORT);
	}

	public Client(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}

	public Client(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public void stopServer() throws IOException {
		this.send(Action.STOP, null);
	}

	public void watch(WatchQuery query) throws IOException {
		this.send(Action.WATCH, query);
	}

	public void watch(UnwatchQuery query) throws IOException {
		this.send(Action.UNWATCH, query);
	}

	private void send(Action action, Query query) throws IOException {
		Socket socket = new Socket(host, port);

		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
			objectOutputStream.writeObject(action);
			if (query != null) {
				objectOutputStream.writeObject(query);
			}
		}

	}
}
