//Change of plans, I want to put in player control first because it's the easiest thing to do.
//Since the best projects have a plot line, I'm going to implement a story into the game.
//Let's go with a "good god, bad god" approach, where you can "choose your alignment".
//I can say "as a being from a higher dimension, you can influence the lives of these beings. Will you bring prosperity or cruelty?"
//Then you have a counteracting god. If you play good god, you have a bad god spawning in monsters, and vice versa.
//This god just chooses targets randomly, so they'll go through the targets array and choose something.
//The main thing this changes is the list of actions you can do.
//The player is able to spontaneously create houses, villagers, farms, and animals, so we have the "good" god all set.
//By creating villagers and farms, we can have the player sponsor the growth of the village.
//They can only spend so many points however, and the opposing god can earn points too.
//You start off with 500 points.
//Prices for good god are as follows:
//Villager: 100pts
//All buildings: 50pts
//Divine Alarm: 100pts (All soldiers focus on one monster)
//Deus VULT: 500pts (All villagers + 1 life)
//Prices for bad god are:
//Monster: 100pts
//Monster life + 1: 50pts
//Screw that guy: 100pts (All monsters focus on one soldier)
//Deus VULT: 100pts (set a square to 0, remove from targets, and kill the villagers)
//Monsters generate 10 point each per day, villagers generate 20
//Disaster: 1000pts (A fire or monster horde, orange if fire and purple if monster horde. Max 100 population, reproduces from 1)
//However, monsters are able to constantly attack and don't need food, along with being cheaper.

package sample;

import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Town {
    private int radius = 3;
    private final ArrayList<Villager> villagers;
    private final ArrayList<Monster> monsters = new ArrayList<>();
    private final ArrayList<Animal> animals = new ArrayList<>();
    private int time = 0;
    private final int[] centralCoords;
    private final int[][] gameGrid;
    private final ArrayList<ArrayList<int[]>> targets;
    private ListView<String> list4;
    private ArrayList<int[]> forbiddenCoords;
    //private final String[] buildings = Controller.buildings;

    private final double timePerDay = 30.0;

    public Town(ArrayList<Villager> startingPop, int[] cc, int[][] gameGrid, ArrayList<ArrayList<int[]>> target){
        this.villagers = startingPop;
        this.centralCoords = cc;
        this.gameGrid = gameGrid;
        this.targets = target;
        this.forbiddenCoords = availableDirections(cc, 2);
    }

    public void initialLifeforms(){
        for (int i = 0; i < 3; i++) {
            Villager v = addVillager();
            v.setHome(targets.get(4).get(i));
            switch (5 + i){
                case 5 -> v.setJob(new Job("Farmer", targets.get(5).get(0), 0, targets));
                case 6 -> v.setJob(new Job("Hunter", targets.get(6).get(0), 0, targets));
                case 7 -> v.setJob(new Job("Soldier", targets.get(7).get(0), 0, targets));
            }
        }
        for (int i = 0; i < 2; i++) {
            spawnMonster(targets.get(1), randomPosition(centralCoords));
        }
        for (int i = 0; i < 4; i++) {
            spawnAnimal(targets.get(2), randomPosition(centralCoords));
        }
    }

    public int[] randomPosition(int[] avoid) {
        int[] posCoords = new int[]{Controller.randInt(0, gameGrid.length - 1), Controller.randInt(0, gameGrid[0].length - 1)};
        while(gameGrid[posCoords[0]][posCoords[1]] != 0 && Arrays.equals(posCoords, avoid)) posCoords = new int[]{Controller.randInt(0, gameGrid.length - 1), Controller.randInt(0, gameGrid[0].length - 1)};
        return posCoords;
    }

    private final ArrayList<Integer> districtOrder = new ArrayList<>();
    public void buildTown(){
        this.forbiddenCoords.add(centralCoords);
        ArrayList<Integer> temp = new ArrayList<>(Arrays.asList(5, 6, 7, 8));
        while(temp.size() > 0) districtOrder.add(temp.remove(Controller.randInt(0, temp.size() - 1)));
        ArrayList<int[]> buildHere = new ArrayList<>();
        for (int i = -1; i <= 1; i+=2) {
            for (int j = -1; j <= 1; j+=2) {
                buildHere.add(new int[]{centralCoords[0] + i, centralCoords[1] + j});
            }
        }
        //buildHere is organized like this: NW, NE, SW, SE
        //It's a lot of prep for just one thing but my mind is broken and loops are fun.
        for (int i = 0; i < buildHere.size(); i++) {
            build(buildHere.get(i), districtOrder.get(i));
            if(districtOrder.get(i) == 5){
                ArrayList<int[]> newHouses = availableDirections(buildHere.get(i), 1);
                for (int j = 0; j < newHouses.size(); j++) {
                    for (int[] coords:this.forbiddenCoords) {
                        if(Arrays.equals(coords, newHouses.get(j))){
                            newHouses.set(j, null);
                        }
                    }
                }
                for (int[] coords:newHouses) {
                    if(coords != null) build(coords, 5);
                }
            }
        }
    }

    public void increaseSize(){
        ArrayList<int[]> centerCoords = new ArrayList<>();
        int initRadius = 3;
        for (int i = -1*radius; i <= radius; i+=initRadius) {
            for (int j = -1*radius; j <= radius; j+=initRadius) {
                if(!(i==0&&j==0)) {
                    if ((centralCoords[0] + i < gameGrid.length && centralCoords[0] + i >= 0) &&
                            (centralCoords[1] + j < gameGrid[0].length && centralCoords[1] + j >= 0)
                            /*&& gameGrid[centralCoords[0] + i][centralCoords[1] + j] == 0*/) {
                        centerCoords.add(new int[]{centralCoords[0] + i, centralCoords[1] + j});
                    }
                }
            }
        }
        for (int i = 0; i < gameGrid.length; i++) {
            for (int j = 0; j < gameGrid[0].length; j++) {
                if(gameGrid[i][j] != 6 && gameGrid[i][j] != 7 && gameGrid[i][j] != 8 && gameGrid[i][j] != 9){
                    gameGrid[i][j] = 0;
                }
            }
        }
        for (int[] centerCoord:centerCoords) {
            this.forbiddenCoords.addAll(availableDirections(centerCoord, 2));
            this.forbiddenCoords.add(centerCoord);
        }
        for (int i = 4; i < 8; i++) {
            ArrayList<int[]> buildingCoords = targets.get(i);
            ArrayList<int[]> buildHere = new ArrayList<>();
            for (int[] buildingCoord : buildingCoords) {
                ArrayList<int[]> temp = new ArrayList<>(availableDirections(buildingCoord, 1));
                temp = validateBuildCoords(temp, buildingCoord, i);
                temp.removeIf(Predicate.isEqual(null));
                if(temp.size() == 0){
                    temp = new ArrayList<>(availableDirections(buildingCoord, 2, true));
                    temp = validateBuildCoords(temp, buildingCoord, i);
                }
                buildHere.addAll(temp);
            }
            for (int[] coords:buildHere) {
                if(coords != null) build(coords, i+1);
            }
        }
        radius += 3;
    }

    public ArrayList<int[]> validateBuildCoords(ArrayList<int[]> buildCoords, int[] direction, int i){
        ArrayList<int[]> buildHere = new ArrayList<>(buildCoords);
        for (int j = 0; j < buildHere.size(); j++) {
            for (int[] forbiddenCC : forbiddenCoords) {
                if (Arrays.equals(forbiddenCC, buildHere.get(j))) {
                    buildHere.set(j, null);
                }
            }
            if(buildHere.get(j) != null) {
                if (((direction[0] - centralCoords[0] >= 0 && buildHere.get(j)[0] - centralCoords[0] <= 0) ||
                        (direction[0] - centralCoords[0] <= 0 && buildHere.get(j)[0] - centralCoords[0] >= 0)) ||
                        ((direction[1] - centralCoords[1] >= 0 && buildHere.get(j)[1] - centralCoords[1] <= 0) ||
                        (direction[1] - centralCoords[1] <= 0 && buildHere.get(j)[1] - centralCoords[1] >= 0))) {
                    buildHere.set(j, null);
                }
            }
        }
        if(i != 4) {
            for (int k = 0; k < buildCoords.size(); k++) {
                for (int[] coords2 : targets.get(4)) {
                    if (Arrays.equals(buildCoords.get(k), coords2)) {
                        targets.get(4).set(k, null);
                    }
                }
            }
        }
        return buildHere;
    }

    //General Section

    public void setOutputList(ListView<String> list){
        this.list4 = list;
    }

    public ArrayList<int[]> availableDirections(int[] coords, int searchRadius){
        ArrayList<int[]> output = new ArrayList<>();
        for (int i = -1*searchRadius; i <= searchRadius; i++) {
            if(i != 0) {
                if ((coords[0] + i < gameGrid.length && coords[0] + i >= 0) && gameGrid[coords[0] + i][coords[1]] == 0) {
                    output.add(new int[]{coords[0] + i, coords[1]});
                }
                if ((coords[1] + i < gameGrid[0].length && coords[1] + i >= 0) && gameGrid[coords[0]][coords[1] + i] == 0) {
                    output.add(new int[]{coords[0], coords[1] + i});
                }
            }
        }
        return output;
    }

    public ArrayList<int[]> availableDirections(int[] coords, int searchRadius, boolean ignored){
        ArrayList<int[]> output = new ArrayList<>();
        for (int i = -1*searchRadius; i <= searchRadius; i+=searchRadius) {
            if(i != 0) {
                if ((coords[0] + i < gameGrid.length && coords[0] + i >= 0) && gameGrid[coords[0] + i][coords[1]] == 0) {
                    output.add(new int[]{coords[0] + i, coords[1]});
                }
                if ((coords[1] + i < gameGrid[0].length && coords[1] + i >= 0) && gameGrid[coords[0]][coords[1] + i] == 0) {
                    output.add(new int[]{coords[0], coords[1] + i});
                }
            }
        }
        return output;
    }

    public void villagerReproduction(){
        int size = villagers.size()/2 + 1;
        list4.getItems().add(0, "The village grows! 1 traveler and " + villagers.size()/2 + " babies have arrived!");
        if(villagers.size() + size > targets.get(4).size()){
            increaseSize();
        }
        ArrayList<Villager> saveForLater = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            addVillager(centralCoords);
            Villager v = villagers.get(villagers.size() - 1);
            if(targets.get(4).size() >= villagers.size()) {
                v.setHome(targets.get(4).get(villagers.size() - 1));
            }else{
                saveForLater.add(v);
            }
            switch (villagers.size()%3){
                case 0 -> v.setJob(new Job("Farmer", targets.get(5).get(0), 0, targets));
                case 1 -> v.setJob(new Job("Hunter", targets.get(6).get(0), 0, targets));
                case 2 -> v.setJob(new Job("Soldier", targets.get(7).get(0), 0, targets));
            }
        }
        for (Villager v:saveForLater) {
            v.setHome(targets.get(4).get(villagers.indexOf(v)));
        }
    }

    public void reproduce(){
        int monsterSize = monsters.size()/2 + 1;
        while(monsters.size() <= (2 + monsterSize)){
            spawnMonster(targets.get(1), randomPosition(centralCoords));
        }
        int animalSize = animals.size()/2 + 1;
        while(animals.size() <= (2 + animalSize)){
            spawnAnimal(targets.get(2), randomPosition(centralCoords));
        }
    }

    public static ArrayList<int[]> getAvailableSquares(int[] target, int[] coords, int[][] gameGrid, int searchRadius) {
        ArrayList<int[]> availableSquares = new ArrayList<>();
        for (int i = -1*searchRadius; i <= searchRadius; i++) {
            for (int j = -1*searchRadius; j <= searchRadius; j++) {
                if(i == 0 && j == 0) j++;
                if ((coords[0] + i < gameGrid.length && coords[0] + i >= 0) &&
                        (coords[1] + j < gameGrid[0].length && coords[1] + j >= 0)){
                    if(gameGrid[coords[0] + i][coords[1] + j] == 0 ||
                            Arrays.equals(new int[]{coords[0] + i, coords[1] + j}, target)) {
                        availableSquares.add(new int[]{coords[0] + i, coords[1] + j});
                    }
                }
            }
        }
        if(availableSquares.size() == 0) return null;
        return availableSquares;
    }

    public static ArrayList<int[]> getAvailableSquares(int[] coords, int[][] gameGrid, int searchRadius) {
        ArrayList<int[]> availableSquares = new ArrayList<>();
        for (int i = -1*searchRadius; i <= searchRadius; i++) {
            for (int j = -1*searchRadius; j <= searchRadius; j++) {
                if(i == 0 && j == 0) j++;
                if ((coords[0] + i < gameGrid.length && coords[0] + i >= 0) &&
                        (coords[1] + j < gameGrid[0].length && coords[1] + j >= 0)){
                    if(gameGrid[coords[0] + i][coords[1] + j] == 0) {
                        availableSquares.add(new int[]{coords[0] + i, coords[1] + j});
                    }
                }
            }
        }
        if(availableSquares.size() == 0) return null;
        return availableSquares;
    }

    public void build(int[] coords, int type){
        //Builds a building on specific coordinates
        if(gameGrid[coords[0]][coords[1]] != 0){
            switch(gameGrid[coords[0]][coords[1]]){
                case 1 -> {
                    int[] newCoords = randomPosition(coords);
                    getVillagerByCoords(coords).changeCoords(newCoords, gameGrid);
                }
                case 2 -> {
                    int[] newCoords = randomPosition(coords);
                    getMonster(coords).setCoords(newCoords[0], newCoords[1], targets.get(1), gameGrid);
                    gameGrid[coords[0]][coords[1]] = 0;
                }
                case 3 -> {
                    int[] newCoords = randomPosition(coords);
                    getAnimalByCoords(coords).setCoords(newCoords[0], newCoords[1], targets.get(2), gameGrid);
                    gameGrid[coords[0]][coords[1]] = 0;
                }
            }
        }
        this.gameGrid[coords[0]][coords[1]] = type + 1;
        if(type - 1 > 2) {
            this.targets.get(type - 1).add(coords.clone());
        }else if(type - 1 == 2){
            spawnAnimal(this.targets.get(2), coords.clone());
        }else if(type - 1 == 1){
            spawnMonster(this.targets.get(1), coords.clone());
        }else if (type - 1 == 0){
            addVillager(coords.clone());
        }
    }

    public void move(){
        //This moves all the things that need to move
        for (Monster m:monsters) {
            if(!isNight()){
                m.move(this.gameGrid, this.targets.get(1), this);
            }
        }
        for (int i = 0; i < animals.size(); i++) {
            Animal a = animals.get(i);
            if((a.getGenes()[2] && isNight()) || (!a.getGenes()[2] && !isNight())){
                a.move(gameGrid, targets.get(2), targets.get(0));
                Animal kid = a.reproduce(gameGrid, targets.get(2));
                if(kid != null) {
                    animals.add(kid);
                    this.targets.get(2).add(kid.getCoords());
                    this.gameGrid[kid.getCoords()[0]][kid.getCoords()[1]] = 3;
                }
            }
        }
        //Distinguishes between night and day and tells the villagers where to move based on their job
        for (Villager v : villagers) {
            if (!isNight()) {
                //Day time
                v.dijkstra(this.gameGrid, v.getJob().getJobSite());
            } else {
                //Night time
                if (!Arrays.equals(v.getHome(), new int[]{-1, -1})) {
                    v.dijkstra(this.gameGrid, v.getHome());
                }
            }
        }
    }

    public void lightning(int[] coords){
        this.gameGrid[coords[0]][coords[1]] = 0;
        for (int j = 0; j < this.targets.size(); j++) {
            ArrayList<int[]> buildingType = this.targets.get(j);
            for (int i = 0; i < buildingType.size(); i++) {
                if(Arrays.equals(buildingType.get(i), coords.clone())){
                    buildingType.remove(i);
                    if(this.targets.indexOf(buildingType) == 0){
                        getMonster(coords).die(monsters, this.targets.get(1));
                    }
                }
            }
        }
        if(getHomeOwner(coords) != null){
            killVillager(getHomeOwner(coords));
        }
        if(getJobOwner(coords) != null){
            killVillager(getJobOwner(coords));
        }
        if(getVillagerByCoords(coords) != null){
            killVillager(getVillagerByCoords(coords));
        }
    }

    public void ageUp(){
        for (int i = 0; i < animals.size(); i++) {
            animals.get(i).ageUp();
        }
        for (int i = 0; i < villagers.size(); i++) {
            villagers.get(i).ageUp();
        }
    }

//    public void setRadius(int radius) {
//        this.radius = radius;
//    }

    public double getTime() {
        return time%timePerDay;
    }

    public int getDay(){
        return time/(int)timePerDay;
    }

    public boolean isNight(){
        int timePerNight = 10;
        return getTime() > timePerDay - timePerNight;
    }

    public void timeUp(){
        this.time++;
    }

    //Villager Section

    public Villager getVillagerByCoords(int[] coords){
        //Gets a villager if it's at a certain location
        for (Villager villager : villagers) {
            if (Arrays.equals(villager.getCoords(), coords)) return villager;
        }
        return null;
    }

    public Villager addVillager(){
        //Adds a villager to the family
        Villager v = new Villager(this.centralCoords[0], this.centralCoords[1], villagers.size(), "None", this.targets,this, list4);
        villagers.add(v);
        targets.get(0).add(this.centralCoords.clone());
        this.gameGrid[villagers.get(villagers.size() - 1).getX()][villagers.get(villagers.size() - 1).getY()] = 1;
        return v;
    }

    public void addVillager(int[] coords){
        //Adds a villager to the family
        Villager v = new Villager(coords[0], coords[1], villagers.size(), "None", this.targets,this, list4);
        villagers.add(v);
        targets.get(0).add(coords);
        this.gameGrid[villagers.get(villagers.size() - 1).getX()][villagers.get(villagers.size() - 1).getY()] = 1;
    }

    public void killVillager(Villager v){
        this.gameGrid[v.getX()][v.getY()] = 0;
        targets.get(0).remove(villagers.indexOf(v));
        villagers.remove(v);
        for (int i = 0; i < villagers.size(); i++) {
            villagers.get(i).setIndex(i);
        }
    }

    public Villager getOwner(int[] coords){
        //Gets the owner of a plot of land (villagers own a work space)
        for (Villager v:villagers) {
            if(Arrays.equals(v.getHome(), coords) || Arrays.equals(v.getJob().getJobSite(), coords)) return v;
        }
        return null;
    }

    public Villager getHomeOwner(int[] coords){
        //Gets the owner of a plot of land (villagers own a work space)
        for (Villager v:villagers) {
            if(Arrays.equals(v.getHome(), coords)) return v;
        }
        return null;
    }

    public Villager getJobOwner(int[] coords){
        //Gets the owner of a plot of land (villagers own a work space)
        for (Villager v:villagers) {
            if(Arrays.equals(v.getJob().getJobSite(), coords) || Arrays.equals(v.getJob().getHomeBase(), coords)) return v;
        }
        return null;
    }

    public void refreshJobSites(){
        //For things like hunters or wanderers, the job sites can change. This refreshes them.
        for (Villager v:villagers) {
            v.getJob().setJobSite(v.getCoords());
        }
    }

    //Monster Section

    public void spawnMonster(ArrayList<int[]> targets){
        targets.add(centralCoords.clone());
        Monster m = new Monster(centralCoords.clone(), monsters.size(), this.list4);
        monsters.add(m);
        this.gameGrid[centralCoords[0]][centralCoords[1]] = 2;
    }

    public void spawnMonster(ArrayList<int[]> targets, int[] coords){
        targets.add(coords.clone());
        Monster m = new Monster(coords.clone(), monsters.size(), this.list4);
        monsters.add(m);
        this.gameGrid[coords[0]][coords[1]] = 2;
    }

    public Monster getMonster(int[] coords){
        for (Monster m:monsters){
            if(Arrays.equals(m.getCoords(), coords)) return m;
        }
        return null;
    }

    public ArrayList<Monster> monsterList(){
        return monsters;
    }

    //Animal Section
    public void spawnAnimal(ArrayList<int[]> targets){
        spawnAnimal(targets, this.centralCoords.clone());
    }

    public void spawnAnimal(ArrayList<int[]> targets, int[] coords){
        targets.add(coords.clone());
        Animal a = new Animal(coords.clone(), animals.size(), Controller.randInt(1, 2)==1,
                new boolean[]{Controller.randInt(1, 2)==1, Controller.randInt(1, 2)==1, Controller.randInt(1, 2)==1},
                this, list4);
        animals.add(a);
        this.gameGrid[coords[0]][coords[1]] = 3;
    }

    public void killAnimal(Animal a){
        this.gameGrid[a.getCoords()[0]][a.getCoords()[1]] = 0;
        this.targets.get(2).remove(animals.indexOf(a));
        animals.remove(a);
        for (int i = 0; i < animals.size(); i++) {
            animals.get(i).setIndex(i);
        }
    }

    public Animal getAnimalByCoords(int[] coords){
        for (Animal a:animals) {
            if(Arrays.equals(a.getCoords(), coords)) return a;
        }
        return null;
    }

    public ArrayList<Animal> animalList() {
        return animals;
    }
}
