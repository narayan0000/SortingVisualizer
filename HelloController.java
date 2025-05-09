package com.example.hellofx;

import java.util.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class HelloController implements Initializable {

    private boolean isSorting = false;

    private Timeline sortingTimeline;

    private static final Duration SWAP_DELAY = Duration.millis(300); // Adjust this value as needed to control speed of swaps

    private double rectangleWidth; // Add this at the class level

    @FXML
    private AnchorPane rootPane;

    @FXML
    private  HBox myHBox;

    @FXML
    private ChoiceBox<String> myChoiceBox;

    private String[] sortMethod = {"Bubble Sort","Insertion Sort","Selection Sort"};

    @FXML
    private Slider mySlider;
    int noOfElements;

    @FXML
    private VBox myVBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        noOfElements=10;
        Platform.runLater(() -> rectgeneration(noOfElements));

       // Set left and right anchors to center the HBox horizontally in the AnchorPane
        double hbWidth = myHBox.getPrefWidth();  // Get the width of the HBox
        double parentWidth = rootPane.getPrefWidth();  // Get the width of the AnchorPane

        // Center the HBox by setting the left anchor and calculating the right anchor
        double leftAnchor = (parentWidth - hbWidth) / 2.0;
        AnchorPane.setLeftAnchor(myHBox, leftAnchor);
        AnchorPane.setRightAnchor(myHBox, null); // Remove the right anchor to let it adjust automatically

        myHBox.setFillHeight(true);

        myChoiceBox.getItems().addAll(sortMethod);

        mySlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                noOfElements = (int) mySlider.getValue();
                myHBox.getChildren().clear();
                rectgeneration(noOfElements);
            }
        });
    }

    public void sort(ActionEvent e){

        String method = myChoiceBox.getValue();

        if(method.equals("Bubble Sort")){
            bubble();
        }

        if(method.equals("Selection Sort")){
            selection();
        }

        if(method.equals("Insertion Sort")){
            insertion();
        }
    }

    public void reshuffle(ActionEvent e) {
        if (sortingTimeline != null) {
            sortingTimeline.stop();
            isSorting = false;
        }
        myHBox.getChildren().clear();
        rectgeneration(noOfElements);
    }

    public  void rectgeneration(int value) {
        myHBox.getChildren().clear();

        myHBox.setPrefWidth(482);
        myHBox.setMaxWidth(482);
        myHBox.setMinWidth(482);

        myHBox.setSpacing(-1); // Slight overlap to hide rounding gaps
        rectangleWidth = (myHBox.getPrefWidth() - 2) / value;


            for (int i = 1; i <= value; i++) {
                int height = rand();
                Rectangle rectangle = new Rectangle();
                rectangle.setHeight(height);
                rectangle.setWidth(rectangleWidth);
                rectangle.setFill(Color.CORNFLOWERBLUE);
                rectangle.setStroke(Color.BLACK);
                rectangle.setArcWidth(5);
                rectangle.setArcHeight(5);

                myHBox.getChildren().add(rectangle);

            }
    }


    public  int  rand(){
        int val=0;
        double hbheight = myHBox.getPrefHeight();
        Random rand = new Random();
        val =  new Random().nextInt(189) + 1;
        return val;
    }

    private void resetColors() {
        for (Node node : myHBox.getChildren()) {
            ((Rectangle) node).setFill(Color.CORNFLOWERBLUE);
        }
    }

    private void swapNodes(int index1, int index2) {
        if (index1 == index2) return;

        ObservableList<Node> children = myHBox.getChildren();
        Node node1 = children.get(index1);
        Node node2 = children.get(index2);

        // Only swap heights - preserve all visual properties
        double tempHeight = ((Rectangle)node1).getHeight();
        ((Rectangle)node1).setHeight(((Rectangle)node2).getHeight());
        ((Rectangle)node2).setHeight(tempHeight);
    }

    public void bubble() {
        if (isSorting) return;
        isSorting = true;

        sortingTimeline = new Timeline();
        AtomicInteger i = new AtomicInteger(0);
        AtomicInteger currentBubble = new AtomicInteger(0); // Tracks the bubbling element
        boolean[] isBubbling = {false};

        KeyFrame frame = new KeyFrame(SWAP_DELAY, e -> {
            ObservableList<Node> children = myHBox.getChildren();
            int n = children.size();

            // Completion check
            if (i.get() >= n-1) {
                resetColors();
                isSorting = false;
                sortingTimeline.stop();
                return;
            }

            resetColors();

            if (!isBubbling[0]) {
                // Start new bubble from beginning
                currentBubble.set(0);
                isBubbling[0] = true;
            }

            // Continue current bubble
            if (currentBubble.get() < n-1-i.get()) {
                Rectangle rect1 = (Rectangle) children.get(currentBubble.get());
                Rectangle rect2 = (Rectangle) children.get(currentBubble.get()+1);

                // Highlight the traveling element
                rect1.setFill(Color.PURPLE); // The element that's bubbling up
                rect2.setFill(Color.YELLOW); // The element being compared with

                if (rect1.getHeight() > rect2.getHeight()) {
                    swapNodes(currentBubble.get(), currentBubble.get()+1);
                    rect1.setFill(Color.RED);
                    rect2.setFill(Color.RED);
                }
                currentBubble.getAndIncrement();
            } else {
                // Bubble reached end
                i.getAndIncrement();
                isBubbling[0] = false;
            }
        });

        sortingTimeline.getKeyFrames().add(frame);
        sortingTimeline.setCycleCount((Animation.INDEFINITE));
        sortingTimeline.play();
    }


    public void selection() {
        if (isSorting) return;
        isSorting = true;

        sortingTimeline = new Timeline();
        AtomicInteger i = new AtomicInteger(0);       // Start of unsorted portion
        AtomicInteger minIndex = new AtomicInteger(0); // Current minimum index
        AtomicInteger j = new AtomicInteger(0);       // Scanning pointer
        boolean[] findingMin = {true};                // State flag

        KeyFrame frame = new KeyFrame(SWAP_DELAY, e -> {
            ObservableList<Node> children = myHBox.getChildren();
            resetColors(); // Reset all to blue at start of each step

            // PHASE 1: Highlight unsorted portion
            for (int idx = i.get(); idx < children.size(); idx++) {
                ((Rectangle) children.get(idx)).setFill(Color.CORNFLOWERBLUE);
            }

            if (i.get() < children.size() - 1) {
                if (findingMin[0]) {
                    // PHASE 2: Find minimum
                    if (j.get() < children.size()) {
                        Rectangle current = (Rectangle) children.get(j.get());
                        Rectangle minRect = (Rectangle) children.get(minIndex.get());

                        // Highlight comparison
                        current.setFill(Color.ORANGE);
                        minRect.setFill(Color.YELLOW);

                        if (current.getHeight() < minRect.getHeight()) {
                            minIndex.set(j.get());
                        }
                        j.getAndIncrement();
                    } else {
                        findingMin[0] = false;
                    }
                } else {
                    // PHASE 3: Swap and prepare next pass
                    if (minIndex.get() != i.get()) {
                        swapNodes(i.get(), minIndex.get()); // Data-only swap
                    }

                    // Move to next unsorted element
                    i.getAndIncrement();
                    minIndex.set(i.get());
                    j.set(i.get() + 1);
                    findingMin[0] = true;
                }
            } else {
                // PHASE 4: Finalize
                isSorting = false;
                sortingTimeline.stop();
            }
        });

        sortingTimeline.getKeyFrames().add(frame);
        sortingTimeline.setCycleCount(Animation.INDEFINITE);
        sortingTimeline.play();
    }

    public void insertion() {
        if (isSorting) return;
        isSorting = true;
        resetColors(); // Reset all to BLUE

        sortingTimeline = new Timeline();
        AtomicInteger i = new AtomicInteger(1);
        AtomicInteger j = new AtomicInteger(0);
        Rectangle[] currentPair = {null, null}; // Track both rectangles in current comparison

        KeyFrame frame = new KeyFrame(Duration.millis(800), e -> {
            if (i.get() >= myHBox.getChildren().size()) {
                isSorting = false;
                resetColors();
                sortingTimeline.stop();
                return;
            }

            // Reset previous pair if exists
            if (currentPair[0] != null) {
                currentPair[0].setFill(Color.CORNFLOWERBLUE);
                currentPair[1].setFill(Color.CORNFLOWERBLUE);
            }

            if (j.get() < 0) {
                // Move to next element
                i.getAndIncrement();
                j.set(i.get() - 1);
                return;
            }

            // Get current pair
            currentPair[0] = (Rectangle) myHBox.getChildren().get(j.get());
            currentPair[1] = (Rectangle) myHBox.getChildren().get(j.get() + 1);

            // Highlight comparison (ORANGE)
            currentPair[0].setFill(Color.ORANGE);
            currentPair[1].setFill(Color.ORANGE);

            if (currentPair[0].getHeight() > currentPair[1].getHeight()) {
                // Flash RED during swap
                currentPair[0].setFill(Color.RED);
                currentPair[1].setFill(Color.RED);

                swapNodes(j.get(), j.get() + 1);
                j.getAndDecrement();
            } else {
                // Move to next element
                currentPair[0].setFill(Color.CORNFLOWERBLUE);
                currentPair[1].setFill(Color.CORNFLOWERBLUE);
                i.getAndIncrement();
                j.set(i.get() - 1);
            }
        });

        sortingTimeline.getKeyFrames().add(frame);
        sortingTimeline.setCycleCount(Animation.INDEFINITE);
        sortingTimeline.play();
    }
}


