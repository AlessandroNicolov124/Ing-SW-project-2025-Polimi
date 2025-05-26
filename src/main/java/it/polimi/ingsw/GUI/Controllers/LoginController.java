package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField connTypeField;

    @FXML
    private TextField ipAddrField;

    @FXML
    private TextField srvPortField;


    private AppContext context;

    @FXML
    private void handleLogin(ActionEvent event) {
        // Dummy authentication: accept any non-empty username/password
        if (!usernameField.getText().isEmpty() && !connTypeField.getText().isEmpty() && !ipAddrField.getText().isEmpty() && !srvPortField.getText().isEmpty()) {
            try {
                String username = usernameField.getText();
                String serverAddress = ipAddrField.getText();
                int serverPort = Integer.parseInt(srvPortField.getText());


                if ("socket".equals(connTypeField.getText())) {
                    SocketClient socketClient= new SocketClient(serverAddress, serverPort, context);
                    context.setClient(socketClient);
                    Thread t = new Thread(socketClient);
                    t.start();

                    context.setPlayerId(context.getClient().addPlayer(username));


                }
                else if ("rmi".equals(connTypeField.getText())) {
                    RmiClient rmiClient = RmiClient.connectTo(serverAddress, context);
                    if (rmiClient == null) {
                        throw new IllegalStateException("Failed to connect to RMI server.");
                    }
                }

                context.setUsername(String.valueOf(usernameField));
                context.setServerAddress(ipAddrField.getText());
                context.setServerPort(Integer.parseInt(srvPortField.getText()));
                context.setConnectionType(connTypeField.getText());



                //GUIApp.showDashboardPage();
            } catch (Exception e) {
                final Stage dialog = new Stage();
                VBox dialogVbox = new VBox(20);
                dialogVbox.getChildren().add(new Text("Something went wrong, please try again."));
                dialogVbox.getChildren().add(new Text("Make sure you insert valid username, password and ip address and server port"));
                Scene dialogScene = new Scene(dialogVbox, 300, 100);
                dialog.setScene(dialogScene);
                dialog.show();

                e.printStackTrace();
            }
        } else {
            // Optionally show error message
            usernameField.setPromptText("Enter username");
            connTypeField.setPromptText("Enter connection type (rmi/socket))");
            ipAddrField.setPromptText("Enter ip address");
            srvPortField.setPromptText("Enter port (socket: 7777; rmi: 1099)");

        }
    }

    public LoginController getController() {
        return this;
    }

    public void setContext(AppContext context) {
        this.context = context;
    }
}