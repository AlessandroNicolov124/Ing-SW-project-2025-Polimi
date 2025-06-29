package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GameStateUpdateListener.FlightBoardListener;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.RotationType;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlightBoardController extends GuiContoller implements FlightBoardListener {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button PickCard;

    private List<Circle> positionCircles = new ArrayList<>();

    private static final float ELLIPSE_CENTER_X = 521.0f;
    private static final float ELLIPSE_CENTER_Y = 318.0f;
    private static final float ELLIPSE_RADIUS_X = 400.0f;
    private static final float ELLIPSE_RADIUS_Y = 220.0f;
    private static final int CORRECTION_ADDENDUM = 37;
    private static final int STARTING_ANGLE = 132;


    @FXML
    public void initialize() {
        setupBackground();
        drawEllipse();
        drawCircles();
    }

    private void setupBackground() {
        Image bgImage = new Image(getClass().getResource("/Images/cardboard/cardboard-5.png").toExternalForm());
        anchorPane.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundImage(
                        bgImage,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new javafx.scene.layout.BackgroundSize(
                                1055, 639, false, false, true, false
                        )
                )
        ));
    }

    private void initPositions() {
        try {
            context.getClient().getPlayersOrderForFlightboard(context.getCurrentMatchId(), context.getPlayerId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void drawEllipse(){
        Ellipse ellipse = new Ellipse();
        ellipse.setCenterX(ELLIPSE_CENTER_X); // Center X of the pane
        ellipse.setCenterY(ELLIPSE_CENTER_Y); // Center Y of the pane
        ellipse.setRadiusX(ELLIPSE_RADIUS_X); // Horizontal radius
        ellipse.setRadiusY(ELLIPSE_RADIUS_Y); // Vertical radius

        ellipse.setFill(Color.TRANSPARENT);      // Make the ellipse hollow
        ellipse.setStroke(Color.WHITESMOKE);           // Use a bright color for the outline
        ellipse.setStrokeWidth(3);               // Make the line thick enough to be visible
        //ellipse.getStrokeDashArray().addAll(20d, 15d);

        anchorPane.getChildren().add(ellipse);
    }

    private void drawCircles(){
        int numCircles = 24;
        double circumference = estimateEllipseCircumference();
        double arcLengthPerPoint = circumference / numCircles;
        // Angle offset to align the first circle with the board's starting position.
        // I've adjusted this slightly to better match the visual layout.
        double x,y;
        double angleOffset = -Math.toRadians(STARTING_ANGLE);
        double angle = angleOffset;

        for (int i = 0; i < numCircles; i++) {
            // Calculate the angle for each circle. We subtract to go counter-clockwise.
            //double angle = angleOffset + (i * (2 * Math.PI / numCircles));

            // The formula for a point on an ellipse
            x = ELLIPSE_CENTER_X + ELLIPSE_RADIUS_X * Math.cos(angle);
            y = ELLIPSE_CENTER_Y + ELLIPSE_RADIUS_Y * Math.sin(angle);

            // Create the circle for the position
            Circle positionCircle = new Circle(x, y, 15); // 20 is the radius of the circle
            positionCircle.setFill(Color.web("transparent", 0.4)); // Semi-transparent red
            positionCircle.setStroke(Color.WHITE);
            positionCircle.setStrokeWidth(2);

            // Add the circle to our tracking list
            positionCircles.add(positionCircle);

            // Create a text label with the position number (1-24)
            Text positionText = new Text(String.valueOf(i + 1));
            positionText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            positionText.setFill(Color.WHITE);

            // Center the text inside the circle
            // We need to query the text's layout bounds to center it accurately
            positionText.applyCss(); // Apply CSS to get accurate bounds
            positionText.setX(x - positionText.getLayoutBounds().getWidth() / 2);
            positionText.setY(y + positionText.getLayoutBounds().getHeight() / 4);


            anchorPane.getChildren().addAll(positionCircle, positionText);

            double currentArc = 0;
            double lastAngle = angle;
            //Point2D arcStatringPoint= new Point2D(ELLIPSE_CENTER_X + ELLIPSE_RADIUS_Y * Math.cos(lastAngle), ELLIPSE_CENTER_Y + ELLIPSE_RADIUS_Y * Math.sin(lastAngle));
            while (currentArc < arcLengthPerPoint) {
                angle += Math.toRadians(0.1); // Move angle by a small step (clockwise)
                Point2D arcStatringPoint= new Point2D(ELLIPSE_CENTER_X + ELLIPSE_RADIUS_X * Math.cos(lastAngle), ELLIPSE_CENTER_Y + ELLIPSE_RADIUS_Y * Math.sin(lastAngle));
                currentArc += arcStatringPoint.distance(new Point2D(ELLIPSE_CENTER_X + ELLIPSE_RADIUS_X * Math.cos(angle), ELLIPSE_CENTER_Y + ELLIPSE_RADIUS_Y * Math.sin(angle)));
                lastAngle = angle;
            }
        }
    }

    private double estimateEllipseCircumference() {
        //double h = Math.pow(ELLIPSE_RADIUS_X - ELLIPSE_RADIUS_Y, 2) / Math.pow(ELLIPSE_RADIUS_X + ELLIPSE_RADIUS_Y, 2);
        //return Math.PI * (ELLIPSE_RADIUS_X + ry) * (1 + (3 * h) / (10 + Math.sqrt(4 - 3 * h)));
        return (2 * Math.PI * (Math.sqrt((Math.pow(ELLIPSE_RADIUS_X, 2) + Math.pow(ELLIPSE_RADIUS_Y, 2))/2)) - CORRECTION_ADDENDUM);
    }

    @FXML
    public void PickAdventureCardFromSrv() {

        positionCircles.get(5).setFill(Color.web("BLUE", 0.7));
        try {
            context.getClient().pickAdventureCard(context.getPlayerId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setContext(AppContext context) {
        super.setContext(context);
        context.addFlightboardListener(this);
    }

    @Override
    public void flightOrderRecieved(UUID matchId, int playerId, List<Player> list, boolean success) {
        if(matchId == context.getCurrentMatchId() && success)
        {

        }
    }

    @Override
    public void adventureCardPicketFromsrv(int playerId, AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn, Map<Integer, Integer> updatedPositions) {

    }
}
