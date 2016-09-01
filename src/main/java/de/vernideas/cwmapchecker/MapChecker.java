package de.vernideas.cwmapchecker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class MapChecker extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Clausewitz Map Checker");

		Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
		Scene scene = new Scene(root, 1024, 800);
		scene.getStylesheets().add("de/vernideas/cwmapchecker/controls/titled_border.css");
		scene.getStylesheets().add("de/vernideas/cwmapchecker/style.css");

		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
