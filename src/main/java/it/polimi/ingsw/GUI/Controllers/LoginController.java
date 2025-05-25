package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.socket.client.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.network.socket.messages.RotationType;

public class LoginController {
    @FXML
    private TextField usernameField;
    private TextField connTypeField;
    private TextField ipAddrField;
    private TextField SrvPortField;


    private AppContext context;

    @FXML
    private void handleLogin(ActionEvent event) {
        // Dummy authentication: accept any non-empty username/password
        if (!usernameField.getText().isEmpty() && !connTypeField.getText().isEmpty() && !ipAddrField.getText().isEmpty() && !SrvPortField.getText().isEmpty()) {
            try {
                String username = usernameField.getText();
                String serverAddress = ipAddrField.getText();
                int serverPort = Integer.parseInt(SrvPortField.getText());


                if ("socket".equals(connTypeField.getText())) {
                    SocketClient socketClient= new SocketClient(serverAddress, serverPort, context);
                    context.setClient(socketClient);
                    new Thread(socketClient).start();

                    context.setPlayerId(context.getClient().addPlayer(username));


                }
                else if ("rmi".equals(connTypeField.getText())) {

                }

                context.setUsername(String.valueOf(usernameField));
                context.setServerAddress(ipAddrField.getText());
                context.setServerPort(Integer.parseInt(SrvPortField.getText()));
                context.setConnectionType(connTypeField.getText());



                //GUIApp.showDashboardPage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Optionally show error message
            usernameField.setPromptText("Enter username");
            connTypeField.setPromptText("Enter connection type (rmi/socket))");
            ipAddrField.setPromptText("Enter ip address");
            SrvPortField.setPromptText("Enter port (socket: 7777; rmi: 1099)");

        }
    }

    public LoginController getController() {
        return this;
    }

    public void setContext(AppContext context) {
        this.context = context;
    }
}