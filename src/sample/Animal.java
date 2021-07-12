package sample;

import javafx.scene.control.ListView;

import java.util.ArrayList;

public class Animal {
    private int x;
    private int y;
    private final String name;
    private int index;
    private int age = 0;
    //Male is true and female is false just cause it makes things a tiny bit easier
    private final boolean gender;
    private final boolean[] genes;
    private int cooldown = 5;
    private final Town town;
    private final ListView<String> list4;

    //Because this project needs to have some kind of variety beyond my stupid village ideas, I'm implementing genes and animals.
    //This way I can do something kind of cool where the animals try to evade the hunters and evolve
    //Right now, there are 3 traits that I have planned:
    //Nicer Fur, Detection, and Nocturnal
    //Resilience is what it sounds like, detection allows the animal to move when it detects something within 1 meter, and nocturnal is what it sounds like
    //Every time someone reproduces, there's a 25% chance of a gene being switched from the current inheritance.

    public Animal(int[] coords, int n, boolean gender, boolean[] gene, Town t, ListView<String> output){
        this.x = coords[0];
        this.y = coords[1];
        this.index = n;
        this.name = "Animal " + n;
        this.gender = gender;
        this.genes = gene;
        this.town = t;
        this.list4 = output;
    }

    public void die(){
        town.killAnimal(this);
        list4.getItems().add(0, getName() + " has died due to old age. ");
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Animal reproduce(int[][] gameGrid, ArrayList<int[]> targets){
        if(cooldown < 5) return null;
        if(this.gender) return null;
        if(Controller.randInt(1, 100) < (-4*age*(age - 7))) return null;
        if(age == 0) return null;
        ArrayList<int[]> foundPeople = new ArrayList<>();
        int r = 2;
        for (int i = -1*r; i <= r; i++) {
            for (int j = -1*r; j <= r; j++) {
                if(i==0&&j==0)j++;
                foundPeople.add(new int[]{this.x+i, this.y+j});
            }
        }
        Animal dad = null;
        if(foundPeople.size() > 0){
            for (int[] person:foundPeople) {
                dad = town.getAnimalByCoords(person);
                //They need to be accessible, a male, and awake at the same time (involuntary reproduction isn't the best thing in the world)
                if(dad != null && dad.isGender() && dad.getGenes()[2] == genes[2]) break;
            }
            if(dad == null) return null;
            boolean[] kidGenes = new boolean[genes.length];
            for (int i = 0; i < dad.getGenes().length; i++) {
                int r2 = Controller.randInt(0, 99);
                if(r2 < 25){
                    kidGenes[i] = Controller.randInt(1, 2) == 1;
                }else if(r2 < 63){
                    kidGenes[i] = genes[i];
                }else{
                    kidGenes[i] = dad.getGenes()[i];
                }
            }
            ArrayList<int[]> availableSquares = Town.getAvailableSquares(this.getCoords(), gameGrid, 2);
            if(availableSquares == null) availableSquares = Town.getAvailableSquares(this.getCoords(), gameGrid, 2);
            if(availableSquares == null) return null;
            cooldown = 0;
            list4.getItems().add(0, this.getName() + " and " + dad.getName() + " gave birth!");
            return new Animal(availableSquares.get(Controller.randInt(0, availableSquares.size() - 1)), targets.size(), Controller.randInt(1, 2)==1, kidGenes, town, list4);
        }
        return null;
    }

    public void move(int[][] gameGrid, ArrayList<int[]> targets, ArrayList<int[]> stayAway){
        ArrayList<int[]> availableSquares = Town.getAvailableSquares(this.getCoords(), gameGrid, 1);
        if (availableSquares == null || availableSquares.size() == 0) return;
        cooldown++;
        if(!genes[1]) {
            int[] newCoords = availableSquares.get(Controller.randInt(0, availableSquares.size() - 1));
            setCoords(newCoords[0], newCoords[1], targets, gameGrid);
        }else{
            //Get closest villager
            double closest = Double.POSITIVE_INFINITY;
            int[] avoid = null;
            for (int[] c:stayAway) {
                if(closest > Math.sqrt(Math.pow(c[0] - this.x, 2) + Math.pow(c[1] - this.y, 2))) avoid = c.clone();
            }
            if(avoid == null) return;
            //Get farthest possible square
            double maxDist = Math.sqrt(Math.pow(availableSquares.get(0)[0] - avoid[0], 2) + Math.pow(availableSquares.get(0)[1] - avoid[1], 2));
            int[] bestCoords = availableSquares.get(0).clone();
            for (int[] coords:availableSquares) {
                if(maxDist < Math.sqrt(Math.pow(coords[0] - avoid[0], 2) + Math.pow(coords[1] - avoid[1], 2))) bestCoords = coords.clone();
            }
            setCoords(bestCoords[0], bestCoords[1], targets, gameGrid);
        }
    }

    public void ageUp(){
        this.age++;
        if(Controller.randInt(1, 100) < (Math.pow(2, age) - 1)){
            this.die();
        }
    }

    public int[] getCoords(){
        return new int[]{this.x, this.y};
    }

    public void setCoords(int newx, int newy, ArrayList<int[]> targets, int[][] gameGrid){
        gameGrid[x][y] = 0;
        gameGrid[newx][newy] = 2;
        x = newx;
        y = newy;
        targets.set(this.index<targets.size()?this.index:targets.size() - 1, new int[]{x, y});
    }

    public boolean[] getGenes() {
        return genes;
    }

    public boolean isGender() {
        return gender;
    }

    public String getName() {
        return name;
    }
}
