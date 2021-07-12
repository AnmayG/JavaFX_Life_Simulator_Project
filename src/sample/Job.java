package sample;

import java.util.ArrayList;

public class Job {
    private int[] jobSite = new int[]{-1, -1};
    private int[] homeBase = new int[]{-1, -1};
    private int targetIndex = -1;
    private int homeIndex = -1;
    private final String name;
    private final ArrayList<ArrayList<int[]>> targets;

    public Job(String n, ArrayList<ArrayList<int[]>> target){
        this.name = n;
        this.targets = target;
    }

    public Job(String n, int[] jCoords, int targetI, ArrayList<ArrayList<int[]>> t){
        this.name = n;
        this.jobSite = jCoords.clone();
        this.homeIndex = targetI;
        this.homeBase = jCoords.clone();
        this.targets = t;
        this.targetIndex = targetI;
    }

    public int[] getJobSite() {
        return jobSite;
    }

    public int[] getHomeBase(){
        return homeBase;
    }

    public String getName() {
        return name;
    }

    public void targetJobSite(int newIndex){
        this.targetIndex = newIndex;
    }

    public void setJobSite(int[] currentCoords) {
        //This sets the job site based on the job. Unemployed people go to the targets, and farmers have their own land
        switch(this.name){
            case "None" -> {
                //If we don't have a job find a new one
                if(targets.get(3).size() == 0){
                    this.jobSite = currentCoords;
                    return;
                }
                if(targetIndex == -1 || targetIndex >= targets.get(3).size()) {
                    if(!targets.get(3).contains(jobSite)){
                        targetIndex = Controller.randInt(0, targets.get(3).size() - 1);
                        this.homeIndex = targetIndex;
                    }
                }
                this.jobSite = targets.get(3).get(targetIndex);
                this.homeBase = targets.get(3).get(homeIndex);
            }
            case "Farmer" -> {
                //This is just here to keep track of the name, the job site should already be decided.
                if(targetIndex == -1 || targetIndex >= targets.get(5).size()) {
                    if(!targets.get(5).contains(jobSite)){
                        targetIndex = Controller.randInt(0, targets.get(5).size() - 1);
                        this.homeIndex = targetIndex;
                    }
                }
                this.jobSite = targets.get(5).get(targetIndex);
                this.homeBase = targets.get(5).get(homeIndex);
            }
            case "Hunter" -> {
                //I haven't implemented monsters or livestock yet, so this technically doesn't exist
                if(targets.get(2).size() <= 2){
                    this.jobSite = targets.get(6).get(homeIndex);
                    targetIndex = -1;
                    return;
                }
                if(targetIndex == -1 || targetIndex >= targets.get(2).size()) {
                    targetIndex = Controller.randInt(0, targets.get(2).size() - 1);
                }
                this.jobSite = targets.get(2).get(targetIndex);
                this.homeBase = targets.get(6).get(homeIndex);
            }
            case "Soldier" -> {
                //These are going to be the people that fight monsters
                if(targets.get(1).size() == 0){
                    this.jobSite = targets.get(7).get(homeIndex);
                    targetIndex = -1;
                    return;
                }
                if(targetIndex == -1 || targetIndex >= targets.get(1).size()) {
                    targetIndex = Controller.randInt(0, targets.get(1).size() - 1);
                }
                this.jobSite = targets.get(1).get(targetIndex);
                this.homeBase = targets.get(7).get(homeIndex);
            }
        }
    }
}
