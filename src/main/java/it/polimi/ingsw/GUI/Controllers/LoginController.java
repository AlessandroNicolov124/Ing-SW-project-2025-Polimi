package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.TextField;


public class LoginController extends GuiContoller{
    @FXML
    private TextField usernameField;

    @FXML
    private TextField connTypeField;

    @FXML
    private TextField ipAddrField;

    @FXML
    private TextField srvPortField;

    @FXML
    private void handleLogin(ActionEvent event) {
        // Dummy authentication: accept any non-empty username/password
        if (!usernameField.getText().isEmpty() && !connTypeField.getText().isEmpty() && !ipAddrField.getText().isEmpty() && !srvPortField.getText().isEmpty()) {
            try {
                String username = usernameField.getText();
                String serverAddress = ipAddrField.getText();
                int serverPort = Integer.parseInt(srvPortField.getText());
                String connectionType = connTypeField.getText().toLowerCase();



                if (!connectionType.equals("socket") && !connectionType.equals("rmi")) {
                    showErrorDialog("Invalid Connection Type", 
                        "Connection type must be either 'socket' or 'rmi'");
                    return;
                }

                context.setUsername(username);
                context.setServerAddress(ipAddrField.getText());
                context.setServerPort(Integer.parseInt(srvPortField.getText()));
                context.setConnectionType(connTypeField.getText());

                if ("socket".equals(connectionType)) {
                    SocketClient socketClient= new SocketClient(serverAddress, serverPort, context);
                    context.setClient(socketClient);
                    Thread t = new Thread(socketClient);
                    t.start();

                    context.getClient().createPlayer(username);
                    //methods to add a new player
                }
                else {
                    RmiClient rmiClient = RmiClient.connectTo(serverAddress, serverPort, context);
                    if (rmiClient == null) {
                        throw new IllegalStateException("Failed to connect to RMI server.");
                    }
                    context.setClient(rmiClient);
                    context.getClient().createPlayer(username);

                }

                //System.out.println("Connected as player " + context.getPlayerId() + " with username: " + context.getUsername());


                //GUIApp.showShipBuilder();
                //GUIApp.showLobbyPage();

            } catch (Exception e) {
                showErrorDialog("Connection Failed", 
                    "Could not connect to server.\n" + "Error: " + e.getMessage());
                    e.printStackTrace();                
            }
        } else {
            // Optionally show error message
            if (usernameField.getText().isEmpty()) {
                usernameField.setPromptText("Enter username");
            }
            if (connTypeField.getText().isEmpty()) {
                connTypeField.setPromptText("Enter connection type (rmi/socket)");
            }
            if (ipAddrField.getText().isEmpty()) {
                ipAddrField.setPromptText("Enter ip address");
            }
            if (srvPortField.getText().isEmpty()) {
                srvPortField.setPromptText("Enter port (socket: 7777; rmi: 1099)");
            }
            
            showErrorDialog("Missing Information", "Please fill in all fields");

        }
    }

    public LoginController getController() {
        return this;
    }

    public void setContext(AppContext context) {
        this.context = context;
    }
}