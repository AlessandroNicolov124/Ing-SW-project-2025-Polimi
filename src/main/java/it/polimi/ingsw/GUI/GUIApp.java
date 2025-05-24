package it.polimi.ingsw.GUI;


import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

//TODO implement GameListener if (Here or somewwhere else inside GUI...)
public class GUIApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Welcome in galaxy trucker GUI");
        Scene scene = new Scene(label, 1000, 1000);

        Image image = new Image("file:Images/Icons/app_icon.png");
        primaryStage.getIcons().add(image);
        primaryStage.setTitle("Galaxy trucker GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

}