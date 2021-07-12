//So far I have the basics down and I can basically build the village by hand if necessary.
//This means that I can move villagers to coordinates, I can create buildings, and I can set it up so the villager can go between their jobs and their homes based on a day-night cycle.
//This is close to a normal life simulator right now, and I should use this as a backup.
//There's really not that much work left, if you think about it as the number of steps I have left.
//I need to implement automatic villages and player control, as well as the plot.
//Because projects are no fun without a plot, I added one in where you effectively act as a god for the villagers
//So you can be a good god or bad god, with the good god getting certain perks and bad god getting other perks.
//Both gods are at "war" with the other, with the bad god spawning monsters and the good god controlling the village.
//And the "life simulator" mode has both gods acting automatically, while the other modes has you taking the place of a god.
//So my goal for the next few days is going to be to add in things like divine punishment or blessings
//I'll consider this as part of player control, or at least the things that the player does are player control.
//There's only 2 major steps that I need to do, so I would say I'm about halfway done (2/3rds if you squint).
//So the next step I'm going to take is to automate building the village. It'll probably be node-based generation, where everything's centered around the keep.
//At the keep, it chooses a direction to build roads. At the road, there's an option to continue building or not.
//Everything's decided by the number of villagers and the resources available.
//It'll also try to create "districts" by mapping things out. Grid system is necessary for everything.
//Houses should be generated with 1 house per villager. Then, road access is vital, so every house needs to have a road.
//So houses are built in a group of 4 with roads surrounding. This can be done by setting locations.
//Or, the idea of a town radius might be interesting. The town starts with a radius of 1, so the houses and farms are centered around that.
//Then, roads are built on the cardinal directions until it's as long as the radius.
//This creates a
//| - - | - - |
//| f f | t t |
//| f f | t t |
//- - - * - - -
//| h h | s s |
//| h h | s s |
//| - - | - - |
//type of structure, where the radius is 2. This creates a nice road structure. Houses then want to be built SW, farms NW, storage SE, and general NE
//Then, everything needs to access at least one road. So, generate a radius of 3, then generate a radius of 6, and go on 3 at a time
//After 2 days, a new villager will be born per 2 villagers (the main focus is jobs, not reproduction)
//Every villager = 1 radius increase but no village restructure until villageSize % 3 == 0
//Each house has 2 villagers, so houses will only be built per 2 villagers.
//Each farm can feed 2 villagers, so that means that you can have 1 "idle" villager
//This "idle" villager will be cutting down trees and storing them in the storehouse or hunting
//So current variables:
//villageSize: radius of village growth & expansion
//resourcesNeeded: Array of what resources are needed such as food and wood
//So starting jobs are farmer, woodcutter, and hunter
//Hunter looks for animals (dijkstra's good here), farmer stays at their farm, and woodcutter looks for food.
//Buildings magically appear because who needs a realistic world.
//An int array to keep track of happiness is vital, villager priority is happiness to max
//Think "Kingdoms and Castles"

package sample;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;

public class Controller {
    //FXML declarations
    @FXML
    private AnchorPane aPane;
    @FXML
    private GridPane gPane;
    @FXML
    private ListView<String> list1, list2, list3, list4;
    @FXML
    private Slider slider1;
    @FXML
    private Button start, b1, b2, b3, b5;
    @FXML
    private ToggleButton tb1, tb2;
    @FXML
    private Label l1;
    @FXML
    private Rectangle rect1;
    @FXML
    private Circle circle1;
    @FXML
    private LineChart<Number, Number> line1;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    //Bounds for grid
    int x = 20;
    int y = 30;
    //Buttons 2D and 1D array to keep track of which one I'm clicking
    private final Button[][] btn = new Button[x][y];
    private final ArrayList<Button> buttonTracker = new ArrayList<>();
    //Arrays for keeping track of information
    private final int[][] gameGrid = new int[x][y];
    private final ArrayList<Villager> villagers = new ArrayList<>();
    //This is an abomination which keeps track of building coordinates by type of building (2, house) = (1, 1)
    private final ArrayList<ArrayList<int[]>> targets = new ArrayList<>();
    //Town, which contains and deals with the villagers
    private final Town town = new Town(villagers, new int[]{x/2-1,y/2-1}, gameGrid, targets);
    //Keeps track of building names
    static final ArrayList<String> buildings = new ArrayList<>(Arrays.asList("Blank", "Villager", "Monster", "Animal", "Target", "House", "Farm", "Hunter's Lodge", "Barracks"));
    //This comes in handy later
    private int[] selectedCoords;
    //Everything for the line chart
    private final ArrayList<XYChart.Series<Number, Number>> seriesArrayList = new ArrayList<>(Arrays.asList(new XYChart.Series<>(), new XYChart.Series<>(), new XYChart.Series<>()));
    int count = 0;
    int[] total = new int[]{0, 0, 0};
    private final ArrayList<ArrayList<Integer>> populations = new ArrayList<>();

    //My old buddy randInt
    public static int randInt(int min, int max){
        return (int)(Math.random()*(max + 1 - min)) + min;
    }

    //My new buddy indexOf, invented because the ArrayList.indexOf() function doesn't care about my feelings
    //When searching for an object they have to be the exact same object
    //So a new int[]{1,2} inside the array =/= new int[]{1, 2} outside, and the indexOf returns -1
    //So I'm just gonna make my own and deal with it
    public static int indexOf(ArrayList<int[]> arrayList, int[] element){
        for (int i = 0; i < arrayList.size(); i++) {
            int[] temp = arrayList.get(i);
            if(temp.getClass().isArray()) {

                if (Arrays.equals(temp, element)) return i;
            }
        }
        return -1;
    }

    @FXML
    private void handleStart(){
        town.setOutputList(list4);
        if(start.getLayoutX() == 929) return;
        start.setLayoutX(929);
        start.setLayoutY(30);
        for (Node n:aPane.getChildren()) {
            n.setVisible(true);
        }
        list2.setVisible(false);
        b3.setVisible(false);
        //Initialize grid with 2D buttons
        for(int i=0; i<btn.length; i++){
            for(int j=0; j<btn[0].length;j++){
                btn[i][j] = new Button();
                btn[i][j].setStyle("-fx-background-color:#d3d3d3");
                btn[i][j].setPrefWidth(25);
                gPane.add(btn[i][j], j, i);
                buttonTracker.add(btn[i][j]);
                gameGrid[i][j]=0;
            }
        }
        gPane.setGridLinesVisible(true);
        gPane.setVisible(true);
        for (int i = 0; i < y; i++) {
            Label l = new Label(i + "");
            l.setLayoutX(44 + i*25);
            l.setLayoutY(12);
            l.setPrefHeight(15);
            l.setPrefWidth(25);
            l.setAlignment(Pos.CENTER);
            aPane.getChildren().add(l);
        }
        for (int i = 0; i < x; i++) {
            Label l = new Label(i + "");
            l.setLayoutX(25);
            l.setLayoutY(30 + i*25);
            l.setPrefHeight(25);
            l.setPrefWidth(15);
            l.setAlignment(Pos.CENTER_RIGHT);
            aPane.getChildren().add(l);
        }
        //Right now, I assign posts by clicking, since I didn't automate it with Town and Job yet
        //So this deals with the clicking
        EventHandler<MouseEvent> z = t -> {
            //By all accounts, this should not work. And yet, it does. It's magic.
            int ind = buttonTracker.indexOf((Button)t.getSource());
            int squareInfo = gameGrid[ind/y][ind%y];
            list1.getItems().clear();
            //I screwed up my coordinate system, so I use a (y, x) system in the code and display an (x, y) because that's what humans use.
            //I'm not sure if I overcomplicated things or if I was just confused, but it works and I don't touch it.
            //It's also really hard to put it back once I used it for everything, so I'm ignoring the problem.
            list1.getItems().addAll("Coordinates: (" + ind%y + ", " + ind/y + ")", "Square Type: " + buildings.get(squareInfo));
            int[] currentCoords = new int[]{ind/y, ind%y};
            switch(buildings.get(squareInfo)){
                case "Villager" ->{
                    //list2 is the list of buildings that you can build, and b3 is the confirmation button
                    list2.setVisible(false);
                    b3.setVisible(false);
                    //Prints villager information
                    Villager v = town.getVillagerByCoords(currentCoords);
                    if(v != null) {
                        list1.getItems().add("Name: " + v.getName());
                        if (!Arrays.equals(new int[]{-1, -1}, v.getHome())) {
                            list1.getItems().add("Home: " + Arrays.toString(new int[]{v.getHome()[1], v.getHome()[0]}));
                        } else {
                            list1.getItems().add("Home: None");
                        }
                        list1.getItems().add("Job Site: " + Arrays.toString(new int[]{v.getJob().getJobSite()[1], v.getJob().getJobSite()[0]}));
                    }
                }
                case "Monster" -> {
                    list2.setVisible(false);
                    b3.setVisible(false);
                    list1.getItems().add("Name: " + town.getMonster(currentCoords).getName());
                }
                case "Animal" -> {
                    list2.setVisible(false);
                    b3.setVisible(false);
                    Animal a = town.getAnimalByCoords(currentCoords);
                    if(a != null){
                        list1.getItems().add("Name: " + a.getName());
                        //these one liners are really convenient
                        list1.getItems().add(a.isGender() ? "Gender: Male" : "Gender: Female");
                        list1.getItems().add("Nice Fur: " + a.getGenes()[0]);
                        list1.getItems().add("Better Senses: " + a.getGenes()[1]);
                        list1.getItems().add("Nocturnal: " + a.getGenes()[2]);
                    }
                }
                case "House" -> {
                    list2.setVisible(false);
                    b3.setVisible(false);
                    //When clicked on a house, assign the last clicked villager to it
                    if(town.getOwner(currentCoords) == null){
                        Villager v = town.getVillagerByCoords(selectedCoords);
                        if(v != null){
                            v.setHome(currentCoords);
                        }
                    }
                    if(town.getOwner(currentCoords) != null) list1.getItems().add("Owned By: " + town.getOwner(currentCoords).getName());
                }
                case "Farm" -> {
                    list2.setVisible(false);
                    b3.setVisible(false);
                    //When clicked on a farm/workshop, assign the last clicked villager to it
                    if(town.getOwner(currentCoords) == null){
                        Villager v = town.getVillagerByCoords(selectedCoords);
                        if(v != null){
                            v.setJob(new Job("Farmer", currentCoords, indexOf(targets.get(5), currentCoords), targets));
                        }
                    }
                    if(town.getOwner(currentCoords) != null) list1.getItems().add("Owned By: " + town.getOwner(currentCoords).getName());
                }
                case "Hunter's Lodge" -> {
                    list2.setVisible(false);
                    b3.setVisible(false);
                    if(town.getOwner(currentCoords) == null){
                        Villager v = town.getVillagerByCoords(selectedCoords);
                        if(v != null){
                            v.setJob(new Job("Hunter", currentCoords, indexOf(targets.get(6), currentCoords), targets));
                        }
                    }
                    if(town.getOwner(currentCoords) != null) list1.getItems().add("Owned By: " + town.getOwner(currentCoords).getName());
                }
                case "Barracks" -> {
                    list2.setVisible(false);
                    b3.setVisible(false);
                    if(town.getOwner(currentCoords) == null){
                        Villager v = town.getVillagerByCoords(selectedCoords);
                        if(v != null){
                            v.setJob(new Job("Soldier", currentCoords, indexOf(targets.get(7), currentCoords), targets));
                        }
                    }
                    if(town.getOwner(currentCoords) != null) list1.getItems().add("Owned By: " + town.getOwner(currentCoords).getName());
                }
                default -> {
                    //When blank, try to build something
                    list2.setVisible(true);
                    b3.setVisible(true);
                }
            }
            //Last selected coords are always saved
            selectedCoords = new int[]{ind / y, ind % y};
//            for (ArrayList<int[]> target:targets) {
//                System.out.print(Arrays.deepToString(target.toArray()) + ", ");
//            }
//            System.out.println("\n");
        };
        //Assigns the handler to every button
        for (Button[] buttons : btn) {
            for (int j = 0; j < btn[0].length; j++) {
                buttons[j].setOnMouseClicked(z);
            }
        }
        //Adds the buildings and deals with the UI a bit
        //"Blank", "Villager", "Monster", "Target", "House", "Farm"
        ArrayList<String> availableBuildings = new ArrayList<>(buildings);
        availableBuildings.remove(0);
        ArrayList<String> availablePowerUps = new ArrayList<>(Arrays.asList("Divine Alarm", "Blessing", "Strengthen Monster", "Target Villager", "Deus VULT", "Calamity"));
        list2.getItems().add("Buildings: ");
        list2.getItems().addAll(availableBuildings);
        list3.getItems().add("Powers: ");
        list3.getItems().addAll(availablePowerUps);
        for (String s : buildings) {
            if(!s.equals("Blank")) {
                targets.add(new ArrayList<>());
            }
        }
        slider1.setValue(timeCoeff/10.0);
        //Creates the first target spot for the villager to go to
        l1.setText("Time: 0.0 Day: 0");
        l1.setText(l1.getText() + "\n" + "It is currently day time.");
        //Sets up the line graph
        line1.setAnimated(false);
        xAxis.setAutoRanging(false);
        xAxis.setLabel("Day");
        yAxis.setLabel("Population");
        xAxis.setLowerBound(count - 10);
        xAxis.setUpperBound(count + 2);
        populations.add(new ArrayList<>());
        populations.get(0).add(villagers.size());
        populations.add(new ArrayList<>());
        populations.get(1).add(town.monsterList().size());
        populations.add(new ArrayList<>());
        populations.get(2).add(town.animalList().size());
        town.buildTown();
        town.initialLifeforms();
        //targets.get(3).add(new int[]{randInt(0, 19), randInt(0, 29)});
        timer();
    }

    //List Selection can vary, but the names do stay the same.
    //town.build assumes that villager is 0, monster is 1, target is 2, and so on
    //And buildings has the indexes of villager being 1, monster 2, target 3, and so on
    @FXML
    private void b3Click(){
        //Has the town build whatever was selected
        int selected = buildings.indexOf(list2.getSelectionModel().getSelectedItem());
        if(selected <= 0) return;
        //In case I don't want duplicate buildings, I can click the button to move it around
        if(tb1.isSelected()){
            clearScreen(new ArrayList<>(Arrays.asList(0, 1, 2)));
            targets.get(selected - 1).clear();
        }
        town.build(selectedCoords, selected);
    }

    @FXML
    private void b4Click(){
        //availablePowerUps.addAll(Arrays.asList("Divine Alarm", "Deus VULT", "Strengthen Monster", "Target Villager", "Deus VULT", "Calamity"));
        switch(list3.getSelectionModel().getSelectedItem()){
            case "Divine Alarm" -> {
                list4.getItems().add("All soldiers are focusing on " + town.getMonster(selectedCoords).getName());
                for (Villager v:villagers) {
                    if(v.getJob().getName().equals("Soldier")){
                        v.getJob().targetJobSite(town.getMonster(selectedCoords).getIndex());
                    }
                }
            }
            case "Blessing" -> {
                list4.getItems().add("It's truly a life saver! All villagers now have 1 extra life.");
                for (Villager v:villagers) {
                    v.increaseLives();
                }
            }
            case "Strengthen Monster" -> {
                if(selectedCoords != null){
                    list4.getItems().add(town.getMonster(selectedCoords).getName() + " is now stronger!");
                    if(town.getMonster(selectedCoords) != null){
                        town.getMonster(selectedCoords).strengthen();
                    }else{
                        ArrayList<Monster> monsterList = town.monsterList();
                        if(monsterList.size() > 0) monsterList.get(randInt(0, monsterList.size() - 1)).strengthen();
                    }
                }
            }
            case "Target Villager" -> {
                ArrayList<Monster> monsterList = town.monsterList();
                if(town.getVillagerByCoords(selectedCoords) != null){
                    list4.getItems().add(town.getVillagerByCoords(selectedCoords).getName() + " is now being chased by monsters!");
                    for (Monster m:monsterList) {
                        m.screwThatGuy(town.getVillagerByCoords(selectedCoords), gameGrid, targets.get(0));
                    }
                }
            }
            case "Deus VULT" -> {
                list4.getItems().add("Divine Intervention! Square " + Arrays.toString(selectedCoords) + " has been devastated!");
                town.lightning(selectedCoords);
            }
            case "Calamity" -> {
                list4.getItems().add("Run away! A horde of monsters attacks!");
                ArrayList<int[]> spawnCoords = new ArrayList<>();
                for (int i = 0; i < 25; i++) {
                    int[] posCoords = new int[]{randInt(0, x - 1), randInt(0, y - 1)};
                    while(spawnCoords.contains(posCoords)) posCoords = new int[]{randInt(0, x - 1), randInt(0, y - 1)};
                    spawnCoords.add(posCoords);
                    town.spawnMonster(targets.get(1), posCoords);
                }
            }
        }
    }

    @FXML
    private void addNode(){
        //Has the town add a villager
        town.addVillager();
        town.refreshJobSites();
    }

    @FXML
    private void animalNode(){
        town.spawnAnimal(targets.get(2));
        town.refreshJobSites();
    }

    @FXML
    private void monsterNode(){
        town.spawnMonster(targets.get(1));
        town.refreshJobSites();
    }

    //Speed up or slow down
    private double timeCoeff = 10;
    @FXML
    private void b2Click(){
        timeCoeff -= 5;
        if(timeCoeff == 0){
            timeCoeff = 15;
        }
        slider1.setValue(timeCoeff/10.0);
    }

    private final ArrayList<Background> backgrounds = new ArrayList<>(Arrays.asList(
            new Background(new BackgroundImage(new Image(   "resources/background.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/villager.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/monster.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/animal.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/target.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/house.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/farm.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/huntersLodge.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/barracks.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)),
            new Background(new BackgroundImage(new Image("resources/hurtVillager.png"),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT))
    ));

    public void updateScreen(){
        //For every building, make sure that its values are set
        for (int i = 0; i < targets.size(); i++) {
            ArrayList<int[]> buildingTypes = targets.get(i);
            for (int[] coords : buildingTypes) {
                gameGrid[coords[0]][coords[1]] = i + 1;
            }
        }
        //Then go through the grid and change the colors
        for(int i=0; i<btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                if(town.getVillagerByCoords(new int[]{i, j}) != null) {
                    if (town.getVillagerByCoords(new int[]{i, j}).tookDamage() && gameGrid[i][j] == 1) {
                        btn[i][j].setBackground(backgrounds.get(9));
                    }else {
                        btn[i][j].setBackground(backgrounds.get(gameGrid[i][j]));
                    }
                }else{
                    btn[i][j].setBackground(backgrounds.get(gameGrid[i][j]));
                }
            }
        }
    }

    public void clearScreen(ArrayList<Integer> typeExclusions){
        //Clears the screen except for the buildings that I want to keep
        for (int i = 0; i < gameGrid.length; i++) {
            for (int j = 0; j < gameGrid[0].length; j++) {
                if(!typeExclusions.contains(gameGrid[i][j])) {
                    gameGrid[i][j] = 0;
                    btn[i][j].setStyle("-fx-background-color: #d3d3d3");
                }
            }
        }
    }

    public void timer(){
        //It's the timer!
        new AnimationTimer() {
            double last = System.nanoTime();
            double nanoSecondsSuckButAreReallySmoothForCalculations = 90.0;
            double fastNight = 1.0;
            boolean agedUp = false;
            boolean recordedLine = false;
            @Override
            public void handle(long now) {
                if (!tb2.isSelected()) {
                    //It's easier to just do everything second by second instead of timing everything perfectly
                    //Doing something every 7 seconds and something else every second is easier when you go second by second
                    if (System.nanoTime() - last >= 100000000.0 * timeCoeff) {
                        last = System.nanoTime();
                        town.refreshJobSites();
                        town.move();
                        town.timeUp();
                        l1.setText("Time: " + town.getTime() + " Day: " + town.getDay());
                        if (town.isNight()) {
                            l1.setText(l1.getText() + "\n" + "It is currently night time.");
                            if (!agedUp) {
                                town.ageUp();
                                town.villagerReproduction();;
                                agedUp = true;
                            }
                            recordedLine = false;
                        } else {
                            l1.setText(l1.getText() + "\n" + "It is currently day time.");
                            agedUp = false;
                            if (!recordedLine) {
                                populations.get(0).add(villagers.size());
                                populations.get(1).add(town.monsterList().size());
                                populations.get(2).add(town.animalList().size());
                                xAxis.setLowerBound(count - 10);
                                xAxis.setUpperBound(count + 2);
                                for (int i = 0; i < populations.size(); i++) {
                                    total[i]++;
                                    if (total[i] >= 10) {
                                        seriesArrayList.get(i).getData().remove(0);
                                    }
                                    seriesArrayList.get(i).getData().add(new XYChart.Data<>(count, populations.get(i).get(count)));
                                }
                                for (XYChart.Series<Number, Number> s : seriesArrayList) {
                                    if (!line1.getData().contains(s)) {
                                        line1.getData().add(s);
                                    }
                                }
                                count++;
                                town.reproduce();
                                recordedLine = true;
                            }
                        }
                        if (town.isNight() && fastNight != 2.3) {
                            nanoSecondsSuckButAreReallySmoothForCalculations = 90.0;
                            fastNight = 2.3;
                            circle1.setFill(Color.SILVER);
                            rect1.setFill(Color.BLACK);
                        } else if (!town.isNight() && fastNight != 1) {
                            nanoSecondsSuckButAreReallySmoothForCalculations = 90.0;
                            fastNight = 1;
                            circle1.setFill(Color.GOLD);
                            rect1.setFill(Color.DODGERBLUE);
                        }
                    }
                    if (System.nanoTime() - last >= timeCoeff / 5) {
                        circle1.setCenterX(-80 * Math.sin(Math.toRadians(nanoSecondsSuckButAreReallySmoothForCalculations)) + 82);
                        circle1.setCenterY(50 * Math.cos(Math.toRadians(nanoSecondsSuckButAreReallySmoothForCalculations)));
                        nanoSecondsSuckButAreReallySmoothForCalculations += 0.3 * fastNight / (timeCoeff / 5);
                    }
                }
                updateScreen();
            }
        }.start();
    }
}
