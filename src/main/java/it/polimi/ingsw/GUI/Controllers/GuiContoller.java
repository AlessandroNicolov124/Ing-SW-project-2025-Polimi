package it.polimi.ingsw.GUI.Controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import it.polimi.ingsw.GUI.AppContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;


public abstract class GuiContoller {
    protected AppContext context;

    public GuiContoller getController() {
        return this;
    }

    public void setContext(AppContext context) {
        this.context = context;
    }

    protected static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showColorSelectionDialog(Consumer<String> onColorSelected) {
        Platform.runLater(() -> {
            List<String> colors = Arrays.asList("BLUE", "GREEN", "YELLOW", "RED");
            AtomicBoolean accepted = new AtomicBoolean(false);

            do {

                ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(colors.get(0), colors);
                choiceDialog.setTitle("Select cabin colour");
                choiceDialog.setHeaderText("Choose the colour of your starting cabin");
                choiceDialog.setContentText("Available colours:");

                choiceDialog.getDialogPane().getButtonTypes().clear();
                ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
                choiceDialog.getDialogPane().getButtonTypes().add(selectButtonType);

                VBox content = new VBox(10);
                content.getChildren().addAll(
                    new Label("Choose the colour of your starting cabin"),
                    choiceDialog.getDialogPane().getContent()
                );
                choiceDialog.getDialogPane().setContent(content);

                Button selectButton = (Button) choiceDialog.getDialogPane().lookupButton(selectButtonType);
                selectButton.addEventFilter(ActionEvent.ACTION, event -> {
                    event.consume();

                    String selectedColor = choiceDialog.getSelectedItem();
                    if (selectedColor != null) {
                        accepted.set(true);
                        onColorSelected.accept(selectedColor);
                        choiceDialog.close();
                    }
                });

                choiceDialog.initOwner(null);
                choiceDialog.setResizable(false);
                
                choiceDialog.showAndWait();

            } while (!accepted.get());
        });
    }

    protected void showValidationErrorDialog(String errors) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ship Validation Failed");
            alert.setHeaderText("Your ship has the following issues:");
            
            TextArea textArea = new TextArea(errors);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });
    }

    protected void ShowInfoDialogWindow(String Title, String Header, String Message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(Title);
            alert.setHeaderText(Header);

            TextArea textArea = new TextArea(Message);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });
    }



    protected String showPlayerSelectionDialog() {
        List<String> players = new ArrayList<>(context.getPlayersInMatchList());
        players.removeIf(player -> player.equals(context.getUsername()));
        
        if (players.isEmpty()) {
            showErrorDialog("No Players", "No other players in the match to view");
            return null;
        }
        
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(players.get(0), players);
        choiceDialog.setTitle("Select Player");
        choiceDialog.setHeaderText("Choose a player to view their ship");
        choiceDialog.setContentText("Player:");

        return choiceDialog.showAndWait().orElse(null);
    }
    
}
