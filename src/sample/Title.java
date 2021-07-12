package sample;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;

public class Title {
    @FXML
    AnchorPane aPane;
    @FXML
    Button b2;
    @FXML
    Label l1, l2;

    private final ArrayList<String> posCol = new ArrayList<>(Arrays.asList("#ff0000", "#00b500", "#0000ff", "#b5b500", "#b500b5", "#00b5b5"));
    private String backgroundColor = posCol.get(Controller.randInt(0, 5));
    private String destinationColor = posCol.get(Controller.randInt(0, 5));
    private int tracker = 0;

    @FXML
    void initialize(){
        aPane.setStyle("-fx-background-color: #00b5b5");
        timer();
    }

    public void timer(){
        //It's the timer!
        new AnimationTimer() {
            double last = System.nanoTime();
            @Override
            public void handle(long now) {
                if(System.nanoTime() - last >= 100000.0*50){
                    last = System.nanoTime();
                    setBackgroundColor();
                    tracker++;
                }
            }
        }.start();
    }

    public void setBackgroundColor(){
        //https://stackoverflow.com/questions/27532/generating-gradients-programmatically
        //The way this works is by randomly selecting a color from the library (just basic colors)
        ArrayList<String> possibleColors = (ArrayList<String>) posCol.clone();
        possibleColors.remove(destinationColor);
        if(backgroundColor.equals(destinationColor)){
            if(tracker % 50 == 0) {
                destinationColor = possibleColors.get(Controller.randInt(0, possibleColors.size() - 1));
            }
        }else{
            var num = Integer.parseInt(backgroundColor.substring(1), 16);
            var num2 = Integer.parseInt(destinationColor.substring(1), 16);
            int step = 1;
            int r = (num >> 16);
            if((num2 >> 16) > r){
                r += step;
            }else if((num2 >> 16) < r){
                r -= step;
            }
            if(r < 0) r = 0;
            if(r > 255) r = 255;
            int b = ((num >> 8) & 0x00FF);
            if(((num2 >> 8) & 0x00FF) > b){
                b += step;
            }else if(((num2 >> 8) & 0x00FF) < b){
                b -= step;
            }
            if(b < 0) b = 0;
            if(b > 255) b = 255;
            int g = (num & 0x0000FF);
            if((num2 & 0x0000FF) > g){
                g += step;
            }else if((num2 & 0x0000FF) < g){
                g -= step;
            }
            if(g < 0) g = 0;
            if(g > 255) g = 255;
            var newColor = g | (b<<8) | (r << 16);
            StringBuilder stringColor = new StringBuilder(Integer.toString(newColor, 16));
            while(stringColor.length() != 6){
                if(stringColor.length() > 6){
                    stringColor = new StringBuilder(stringColor.substring(1));
                }else{
                    stringColor.insert(0, "0");
                }
            }
            stringColor.insert(0, "#");
            backgroundColor = stringColor.toString();
            aPane.setStyle("-fx-background-color: " + backgroundColor);
            l1.setTextFill(Color.web(backgroundColor));
            l2.setTextFill(Color.web(backgroundColor));
            b2.setTextFill(Color.web(backgroundColor));
            //step++;
        }
    }

    @FXML
    private void b2Click(){
        try {
            switchScreen();
        } catch (Exception ignored) {}
    }

    //https://gist.github.com/pethaniakshay/302072fda98098a24ce382a361bdf477s
    private void switchScreen() throws Exception{
        //Javafx works by putting "scenes" (everything that's described in the FXML file) on a "stage".
        //The way this works is by getting a new scene (through an intermediary known as a parent) and putting it on the stage.
        Stage stage = (Stage) b2.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
