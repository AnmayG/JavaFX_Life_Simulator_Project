package sample;

import javafx.scene.control.ListView;

import java.util.ArrayList;

public class Monster {
    private int x;
    private int y;
    private final String name;
    private int index;
    private final ListView<String> output;
    private int lives = 1;

    public Monster(int[] coords, int n, ListView<String> o){
        this.x = coords[0];
        this.y = coords[1];
        this.index = n;
        this.name = "Monster " + n;
        this.output = o;
    }

    public void move(int[][] gameGrid, ArrayList<int[]> targets, Town town){
        if(currentlyTargeting == null) {
            if (attack(gameGrid, targets, town)) {
                ArrayList<int[]> availableSquares = Town.getAvailableSquares(this.getCoords(), gameGrid, 1);
                if (availableSquares == null || availableSquares.size() == 0) return;
                int[] newCoords = availableSquares.get(Controller.randInt(0, availableSquares.size() - 1));
                setCoords(newCoords[0], newCoords[1], targets, gameGrid);
            }
        }else{
            screwThatGuy(currentlyTargeting, gameGrid, targets);
        }
    }

    public boolean attack(int[][] gameGrid, ArrayList<int[]> targets, Town town){
        int searchRadius = 2;
        for (int i = -1*searchRadius; i <= searchRadius ; i++) {
            for (int j = -1*searchRadius; j <= searchRadius; j++) {
                if((x + i < gameGrid.length && x + i >= 0) && (y + j < gameGrid[0].length && y + j >= 0)) {
                    if (gameGrid[x + i][y + j] == 1) {
                        Villager v = town.getVillagerByCoords(new int[]{x + i, y + j});
                        v.die();
                        output.getItems().add(0, getName() + " has attacked " + v.getName() + "!");
                        if(v.getLives() <= 0) {
                            if (v == currentlyTargeting) currentlyTargeting = null;
                            setCoords(x + i, y + j, targets, gameGrid);
                            output.getItems().add(0, getName() + " has killed " + v.getName() + "! What a tragedy!");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void strengthen(){
        this.lives++;
    }

    private Villager currentlyTargeting = null;
    public void screwThatGuy(Villager v, int[][] gameGrid, ArrayList<int[]> targets){
        System.out.println("here");
        currentlyTargeting = v;
        int[] target = v.getCoords();
        ArrayList<int[]> availableSquares = Town.getAvailableSquares(v.getCoords(), this.getCoords(), gameGrid, 1);
        if(availableSquares == null) return;
        double minDistance = Double.POSITIVE_INFINITY;
        int tempx = -1;
        int tempy = -1;
        for (int[] square:availableSquares) {
            double tempDistance = Math.sqrt(Math.pow((target[0] - square[0]), 2) + Math.pow((target[0]-square[0]), 2));
            if(tempDistance < minDistance){
                minDistance = tempDistance;
                tempx = square[0];
                tempy = square[1];
            }
        }
        if(tempx == -1 || tempy == -1) return;
        setCoords(tempx, tempy, targets, gameGrid);
        attack(gameGrid, targets, v.getTown());
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int[] getCoords(){
        return new int[]{x, y};
    }
    public String getName() {
        return name;
    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean takeDamage(ArrayList<Monster> monsters, ArrayList<int[]> targets){
        lives--;
        if(lives <= 0){
            die(monsters, targets);
            return true;
        }
        return false;
    }

    public void die(ArrayList<Monster> monsters, ArrayList<int[]> targets){
        this.lives = 0;
        monsters.remove(this);
        targets.remove(index);
        for (int i = 0; i < monsters.size(); i++) {
            monsters.get(i).setIndex(i);
        }
    }

    public void setCoords(int tempx, int tempy, ArrayList<int[]> targets, int[][] gameGrid){
        gameGrid[x][y] = 0;
        gameGrid[tempx][tempy] = 2;
        x = tempx;
        y = tempy;
        targets.set(this.index<targets.size()?this.index:targets.size() - 1, new int[]{x, y});
    }
}
