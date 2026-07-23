package org.example;

import java.util.ArrayList;

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
        String title;
        if(pwrLvl >= 450){
            title = "supreme learning being (make the next study program)";
        }else if(pwrLvl >= 375){
            title = "study God";
        }else if(pwrLvl >= 300){
            title = "study master";
        }else if(pwrLvl >= 225){
            title = "study warrior";
        }else if(pwrLvl >= 150){
            title = "study trainee";
        }else{
            title = "study baby";
        }
        String userStats = name + " | powerLVL : " + pwrLvl + " | title : " + title;
        return userStats;
    }
}
