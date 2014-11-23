package com.famaridon.server.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by rolfone on 20/11/14.
 */
public class WatcherGUI extends Application {

	protected final TabPane tabPane = new TabPane();
	protected final MenuBar menuBar = new MenuBar();
	protected final BorderPane root = new BorderPane();

	@Override
	public void start(Stage primaryStage) {

		Scene scene = new Scene(root, 800, 600);
		primaryStage.setTitle("Project watcher (server)");
		primaryStage.setScene(scene);

		// build the layout
		root.setTop(menuBar);
		root.setCenter(tabPane);

		menuBar.setUseSystemMenuBar(true);

		Menu fileMenu = new Menu("Fichier");
		Menu helpMenu = new Menu("Aide");

		menuBar.getMenus().setAll(fileMenu,helpMenu);

		primaryStage.show();
	}

	public void addProject(String project){
		Tab projectTab = new Tab(project);
		tabPane.getTabs().setAll(projectTab);
	}

	public static void main(String[] args) {
		launch(args);
	}
}