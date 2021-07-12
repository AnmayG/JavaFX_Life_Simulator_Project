package sample;

import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class Villager {
    private int x;
    private int y;
    private Job job;
    private final String name;
    private int[] home = new int[]{-1, -1};
    private final Town town;
    private final ListView<String> output;
    private final ArrayList<ArrayList<int[]>> targets;
    private int index;
    private int maxLives = 1;
    private int lives = 1;
    private int age = 0;

    public Villager(int x, int y, int n, String j, ArrayList<ArrayList<int[]>> target, Town t, ListView<String> o){
        this.x = x;
        this.y = y;
        this.name = "Villager " + n;
        this.index = n;
        this.targets = target;
        this.job = new Job(j, target);
        this.town = t;
        this.output = o;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int[] getCoords(){return new int[]{this.x, this.y};}
    public String getName() {
        return name;
    }
    public void setIndex(int ind){
        this.index = ind;
    }

    public Town getTown() {
        return town;
    }
    public Job getJob() {
        return job;
    }
    public void setJob(Job job) {
        if(job.getName().equals("Soldier")){
            lives += 2;
            maxLives += 2;
        }
        output.getItems().add(0, getName() + " is now a " + job.getName() + " working at " + Arrays.toString(job.getJobSite()));
        this.job = job;
    }
    public int[] getHome() {
        return home;
    }
    public void setHome(int[] home) {
        output.getItems().add(0, getName() + " now owns the house at " + Arrays.toString(home));
        this.home = home;
    }

    public void dijkstra(int[][] gameGrid, int[] target){
        if(attack(gameGrid)) return;
        //This is what I'm going to call poor man's Dijkstra
        //Because Dijkstra's algorithm is normally used to generate complete paths (I think), this procedurally generates a path
        //This means that even if things get in the way, it can dodge them and get out of the way
        int[] coords = this.getCoords().clone();
        if(Arrays.equals(coords, target)){
            return;
        }
        //if(gameGrid[target[0]][target[1]] == 0) gameGrid[target[0]][target[1]] = 2;
        //This gets a list of the available contiguous squares (I can increase the radius though)
        ArrayList<int[]> availableSquares = Town.getAvailableSquares(target, coords, gameGrid, 1);
        if(availableSquares == null) return;
        //This is the "every node has a default value of infinity" idea
        //Because we're doing poor man's Dijkstra, we don't need to physically assign those nodes a value
        //Instead we can just ignore them and get the closest available square.
        double dist = Double.POSITIVE_INFINITY;
        int index = -1;
        for (int i = 0; i < availableSquares.size(); i++) {
            int[] c = availableSquares.get(i);
            double temp = Math.sqrt(Math.pow(target[0] - c[0], 2) + Math.pow(target[1] - c[1], 2));
            if(temp < dist){
                dist = temp;
                index = i;
            }
        }
        gameGrid[coords[0]][coords[1]] = 0;
        this.x = availableSquares.get(index)[0];
        this.y = availableSquares.get(index)[1];
        this.targets.get(0).set(this.index, new int[]{this.x, this.y});
        gameGrid[this.x][this.y] = 1;
    }

    public void changeCoords(int[] newCoords, int[][] gameGrid){
        gameGrid[this.getCoords()[0]][this.getCoords()[1]] = 0;
        this.x = newCoords[0];
        this.y = newCoords[1];
        this.targets.get(0).set(this.index, new int[]{this.x, this.y});
        gameGrid[this.x][this.y] = 1;
    }

    public int getLives(){
        return lives;
    }

    public void increaseLives(){
        lives++;
        maxLives++;
    }

    public boolean tookDamage(){
        return lives < maxLives;
    }

    public void die(){
        lives--;
        if (lives == 0) {
            town.killVillager(this);
        }
    }

    public void ageUp(){
        this.age++;
        if(Controller.randInt(1, 100) < 3*(Math.pow(2, age-5))){
            System.out.println(3*(Math.pow(2, age-5)) + " " + age);
            output.getItems().add(0, job.getName().equals("None") ? "":job.getName() + " " + getName() + " has died due to old age.");
            town.killVillager(this);
        }
    }

    public boolean attack(int[][] gameGrid){
        if(this.job.getName().equals("Soldier") || this.job.getName().equals("Hunter")){
            ArrayList<Object> found = searchRadius(2, this.getCoords());
            for (Object o:found) {
                if(o.getClass().equals(Monster.class) && this.job.getName().equals("Soldier")){
                    Monster m = (Monster) o;
                    if(m.takeDamage(town.monsterList(), targets.get(1))){
                        output.getItems().add(0, getName() + " has killed " + m.getName() + "! What a victory!");
                        gameGrid[this.getCoords()[0]][this.getCoords()[1]] = 0;
                        this.x = m.getX();
                        this.y = m.getY();
                        this.targets.get(0).set(this.index, new int[]{this.x, this.y});
                        gameGrid[this.x][this.y] = 1;
                        return true;
                    }else{
                        output.getItems().add(0, getName() + " has attacked " + m.getName() + "!");
                    }
                }else if(o.getClass().equals(Animal.class) && this.job.getName().equals("Hunter")){
                    Animal a = (Animal) o;
                    a.die();
                    output.getItems().add(0, getName() + " successfully hunted " + a.getName() + "! What a victory!");
                    gameGrid[this.getCoords()[0]][this.getCoords()[1]] = 0;
                    this.x = a.getCoords()[0];
                    this.y = a.getCoords()[1];
                    this.targets.get(0).set(this.index, new int[]{this.x, this.y});
                    gameGrid[this.x][this.y] = 1;
                }
            }
        }
        return false;
    }

    public ArrayList<Object> searchRadius(int r, int[] coords){
        ArrayList<Object> foundThings = new ArrayList<>();
        for (int i = -1*r; i <= r; i++) {
            for (int j = -1*r; j < r; j++) {
                int[] newCoords = new int[]{coords[0]+i, coords[1]+j};
                Monster m = town.getMonster(newCoords);
                if(m != null) foundThings.add(m);
                Animal a = town.getAnimalByCoords(newCoords);
                if(a != null) foundThings.add(a);
            }
        }
        return foundThings;
    }
}
