package org.ghost.chat_examen;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("login-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 900, 520);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}