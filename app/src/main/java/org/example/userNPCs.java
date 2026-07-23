package org.example;

public class userNPCs {
    int id;
    String name;
    int pwrLvl;

    userNPCs(int id, String name, int pwrLvl){
        this.id = id;
        this.name = name;
        this.pwrLvl = pwrLvl;
    }

    public String getUNPC(){
        String userStats = name + " | powerLVL : " + pwrLvl + " | title : ";
        return userStats;
    }
}
