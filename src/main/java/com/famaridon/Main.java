package com.famaridon;

import ch.qos.logback.classic.Level;
import com.famaridon.client.Client;
import com.famaridon.folder.sync.ProjectAutoDeployScannerThread;
import com.famaridon.folder.sync.event.listener.impl.VDocHostDeployerEventListener;
import com.famaridon.server.ServerThread;
import com.famaridon.server.beans.UnwatchQuery;
import com.famaridon.server.beans.WatchQuery;
import com.famaridon.server.ui.WatcherGUI;
import javafx.application.Application;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static final int DEFAULT_PORT = 32000;

	public static void main(String[] args) throws InterruptedException, IOException {

		CommandLine commandLine = getCommandLine(args);

		Main main = new Main(commandLine);

		main.run();

	}

	private final CommandLine commandLine;

	public Main(CommandLine commandLine) {
		this.commandLine = commandLine;
		if (commandLine.hasOption('d')) {
			ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			root.setLevel(Level.DEBUG);
		}
	}

	public void run() throws IOException {
		int port = Integer.parseInt(commandLine.getOptionValue("port", Integer.toString(DEFAULT_PORT)));

		if (commandLine.hasOption("server")) {

			if(commandLine.hasOption("gui")){
				Application.launch(WatcherGUI.class);
			}

			ServerThread serverThread = new ServerThread(port);
			serverThread.run();
			return;
		}

		Client client = new Client(port);

		if (commandLine.hasOption("stop")) {
			client.stopServer();
		} else {
			String project = resolveProject(commandLine);

			if (commandLine.hasOption("watch")) {
				Path source = resolveSourceCustom(commandLine);
				Path target = resolveTarget(commandLine);

				WatchQuery query = new WatchQuery(project,source.toString(),target.toString());
				if (commandLine.hasOption("exclude")) {
					String[] excludeMatcherArray = commandLine.getOptionValues("exclude");
					for (String excludeMatcher : excludeMatcherArray) {
						query.addExclude(excludeMatcher);
					}
				}
				client.watch(query);
			} else if (commandLine.hasOption("unwatch")) {
				UnwatchQuery query = new UnwatchQuery(project);
				client.watch(query);
			}
		}
	}

	private String resolveProject(CommandLine commandLine) {
		if (commandLine.hasOption('p')) {
			return commandLine.getOptionValue('p');
		}
		Path source = resolveSource(commandLine);
		return source.toAbsolutePath().getParent().getFileName().toString();
	}

	private Path resolveTarget(CommandLine commandLine) {
		Path target = null;
		if (commandLine.hasOption('t')) {
			target = Paths.get(commandLine.getOptionValue('t'));
		} else {
			Path propertiesFile = resolveSource(commandLine).resolve("home.properties");
			if (Files.exists(propertiesFile)) {
				try (FileReader fileReader = new FileReader(propertiesFile.toFile())) {
					Properties properties = new Properties();
					properties.load(fileReader);
					String vdocHome = properties.getProperty("VDOC_HOME");
					if (vdocHome != null && !"".equals(vdocHome)) {
						target = Paths.get(vdocHome);
					}
				} catch (IOException e) {
					throw new IllegalArgumentException("home.properties file can't be parsed!", e);
				}
			}
		}

		if (target == null) {
			throw new IllegalArgumentException("No target folder configuration found!");
		} else if (!Files.exists(target)) {
			throw new IllegalArgumentException("Target folder not found!");
		}
		return target;
	}

	private Path resolveSource(CommandLine commandLine) {
		Path source = Paths.get(commandLine.getOptionValue('s',"."));
		if (!Files.exists(source)) {
			throw new IllegalArgumentException("Source folder not found!");
		}
		return source;
	}

	private Path resolveSourceCustom(CommandLine commandLine) {
		return resolveSource(commandLine).resolve("src/main/custom");
	}

	private static CommandLine getCommandLine(String[] args) {
		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption("h", "help", false, "print this message");
		options.addOption("d", "debug", false, "set the logger as debug mode");

		// server options
		options.addOption(Option.builder()
						.longOpt("server")
						.desc("start the server").build()
		);
		options.addOption(Option.builder()
						.longOpt("port")
						.hasArg()
						.desc("start the server listening port").build()
		);
		options.addOption(Option.builder()
						.longOpt("gui")
						.desc("start the GUI mode").build()
		);

		// client command
		options.addOption(Option.builder()
						.longOpt("stop")
						.desc("stop server").build()
		);
		options.addOption(Option.builder()
						.longOpt("watch")
						.desc("configure server to watch project (source, target, is required)").build()
		);
		options.addOption(Option.builder()
						.longOpt("unwatch")
						.desc("configure server to unwatch project").build()
		);

		// client options
		options.addOption(Option.builder("p")
						.longOpt("project")
						.hasArg()
						.desc("the project name used as unique identifier").build()
		);
		options.addOption(Option.builder("s")
						.longOpt("source")
						.hasArg()
						.type(File.class)
						.argName("file")
						.desc("the project source folder to watch").build()
		);
		options.addOption(Option.builder("t")
						.longOpt("target")
						.hasArg()
						.type(File.class)
						.argName("file")
						.desc("the target vdoc folder if not set try to read home.properties file for 'VDOC_HOME' property").build()
		);

		options.addOption(Option.builder()
						.longOpt("exclude")
						.hasArgs()
						.desc("Exclude file or folder pattern ").build()
		);

		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("watcher", options, true);
			LOGGER.error("Command line can't be parsed.",e);
			System.exit(1);
		}
		return commandLine;
	}
}


