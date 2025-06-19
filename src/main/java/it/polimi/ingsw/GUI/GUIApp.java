package it.polimi.ingsw.GUI;



import it.polimi.ingsw.GUI.Controllers.LobbyController;
import it.polimi.ingsw.GUI.Controllers.LoginController;
import it.polimi.ingsw.GUI.Controllers.ShipBuilderController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {

    private static Stage primaryStage;
    private static AppContext context = new AppContext();


    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLoginPage();
    }

    public static void showLoginPage() throws Exception {
        FXMLLoader loader = new FXMLLoader(GUIApp.class.getResource("/FXML/LoginView.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setContext(context);

        primaryStage.setScene(new Scene(root, 1000, 1000));
        primaryStage.setWidth(1000);
        primaryStage.setHeight(1000);
        primaryStage.setTitle("Login page");
        primaryStage.show();
    }

    public static void showShipBuilder() throws Exception {
        FXMLLoader loader = new FXMLLoader(GUIApp.class.getResource("/FXML/ShipBuilderView.fxml"));
        Parent root = loader.load();

        ShipBuilderController shipBuilderController = loader.getController();
        shipBuilderController.setContext(context);
        // Switch scene
        primaryStage.setScene(new Scene(root, 1000, 1000));
        primaryStage.setTitle("Ship Builder");
        primaryStage.show();

    }

    public static void showLobbyPage() throws Exception {
        FXMLLoader loader = new FXMLLoader(GUIApp.class.getResource("/FXML/lobby.fxml"));
        Parent root = loader.load();

        LobbyController lobbyController = loader.getController();
        lobbyController.setContext(context);
        // Switch scene
        primaryStage.setScene(new Scene(root, 1000, 1000));
        primaryStage.setTitle("Lobby");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}