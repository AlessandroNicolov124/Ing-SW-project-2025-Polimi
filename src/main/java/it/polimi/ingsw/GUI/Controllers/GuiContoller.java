package it.polimi.ingsw.GUI.Controllers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import it.polimi.ingsw.GUI.AppContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public abstract class GuiContoller {
    protected AppContext context;

    public GuiContoller getController() {
        return this;
    }

    public void setContext(AppContext context) {
        this.context = context;
    }

    protected static void showErrorDialog(String title, String message) {
        try {
            final Stage dialog = new Stage();
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text(title));
            dialogVbox.getChildren().add(new Text(message));
            Scene dialogScene = new Scene(dialogVbox, 300, 150);
            dialog.setScene(dialogScene);
            dialog.setTitle(title);
            dialog.show();
        } catch (Exception e) {
            System.err.println("Failed to show error dialog: " + e.getMessage());
        }
    }


    protected void showColorSelectionDialog(Consumer<String> onColorSelected) {
        Platform.runLater(() -> {
            List<String> colors = Arrays.asList("BLUE", "GREEN", "YELLOW", "RED");
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
                    onColorSelected.accept(selectedColor);
                    choiceDialog.close();
                }
            });

            choiceDialog.initOwner(null);
            choiceDialog.setResizable(false);
            
            choiceDialog.showAndWait();
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
    
}
