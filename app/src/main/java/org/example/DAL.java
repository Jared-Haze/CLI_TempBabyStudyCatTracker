package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class DAL {

    public static ArrayList<userNPCs> getUserNPCs(){
        ArrayList<userNPCs> rivals = new ArrayList<>();

        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM userNPCs ORDER BY id;");
            ResultSet rs = ps.executeQuery();

            userNPCs userNPC;
            while(rs.next()){
                userNPC = new userNPCs(rs.getInt("id"), rs.getString("playerName"), rs.getInt("powerLVL"));
                rivals.add(userNPC);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }


        return rivals;
    }
    
    public static ArrayList<TrackedCat> getTrackedCats(){
        ArrayList<TrackedCat> allTrackedCats = new ArrayList<>();

        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM CatTracker ORDER BY id;");

            ResultSet rs = ps.executeQuery();
            TrackedCat trackedStudyCat;
            while(rs.next()){
                Timestamp ts = rs.getTimestamp("initialFinish");
                LocalDateTime initialFinish = (ts == null) ? null : ts.toLocalDateTime();

                Timestamp ts2 = rs.getTimestamp("studyComplete");
                LocalDateTime studyComplete = (ts2 == null) ? null : ts2.toLocalDateTime();

                java.sql.Date sqlDate = rs.getDate("priorLoginDate");
                LocalDate priorLoginDate = (sqlDate == null) ? null : sqlDate.toLocalDate();

                trackedStudyCat = new TrackedCat(rs.getInt("id"), rs.getString("studyCat"), rs.getTimestamp("studyStart").toLocalDateTime(), rs.getInt("reviewTick"), rs.getTimestamp("lastReview").toLocalDateTime(), initialFinish, studyComplete, rs.getInt("dailyTickCount"), priorLoginDate, rs.getBoolean("tempQuit"));
                allTrackedCats.add(trackedStudyCat);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return allTrackedCats;
    }

    public static void addTrackedCat(String studyCat){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("INSERT INTO CatTracker (studyCat, studyStart, reviewTick, lastReview, dailyTickCount, priorLoginDate, tempQuit) VALUES (?, NOW(), 0, NOW(), 0, NOW(), false);");

            ps.setString(1, studyCat);
            ps.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void removeTrackedCat(String removedCat, Scanner scanner){
        try(Connection conn = JDBC.getConnection()){
            ArrayList<TrackedCat> allTrackedCats = getTrackedCats();

            for(TrackedCat studyCat : allTrackedCats){
                if(removedCat.equalsIgnoreCase(studyCat.studyCat)){
                    System.out.print("enter 'remove' to confirm: ");
                    String input = scanner.nextLine();
                    if(input.equals("remove")){
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM CatTracker WHERE studyCat = ?;");
                        ps.setString(1, removedCat);
                        ps.executeUpdate();
                        System.out.println("It has been removed from the list");
                        return;
                    }else{
                        System.out.println("seems you've changed your mind");
                    }
                    break;
                }
            }
            System.out.println("that study cat doesn't exist in database.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void reviewedTrackedCat(String reviewedCat, Scanner scanner){
        try(Connection conn = JDBC.getConnection()){
            ArrayList<TrackedCat> allTrackedCats = getTrackedCats();
            
            
            for(TrackedCat studyCat : allTrackedCats){
                if(reviewedCat.equalsIgnoreCase(studyCat.studyCat)){
                    int newTick = studyCat.reviewTick + 1;
                    int dailyTick = studyCat.dailyTickCount + 1;
                    int currentId = studyCat.id;

                    //prevent increase in initial finished study cats, until 2 days after initially finishing
                    if(studyCat.initialFinish != null){
                        //if today is not 2 days after initial finish, print : you have to wait until forced spaced repetition is over
                        LocalDate today = LocalDate.now();
                        if(today.isBefore(studyCat.initialFinish.toLocalDate().plusDays(2))){
                            System.out.println("You have to wait until forced spaced repetition is over to review this study cat");
                            return;
                        }
                    }

                    if(studyCat.dailyTickCount >= 3){
                        System.out.println("You can't increase the review count for this study cat anymore today [you've hit 3 review MAX limit]");
                        return;
                    }

                    if(studyCat.reviewTick >= 13){
                        System.out.println("You can't increase max reviewed study cat");
                        return;
                    } else if(studyCat.reviewTick == 11){
                        PreparedStatement psIF = conn.prepareStatement("UPDATE CatTracker SET initialFinish = NOW() WHERE id = ?;");
                        psIF.setInt(1, currentId);
                        psIF.executeUpdate();
                    } else if (studyCat.reviewTick ==12){
                        PreparedStatement psC = conn.prepareStatement("UPDATE CatTracker SET studyComplete = NOW() WHERE id = ?;");
                        psC.setInt(1, currentId);
                        psC.executeUpdate();
                    }

                    PreparedStatement ps4 = conn.prepareStatement("UPDATE CatTracker SET dailyTickCount = ? WHERE id = ?;");
                    ps4.setInt(1, dailyTick);
                    ps4.setInt(2, currentId);
                    ps4.executeUpdate();

                    PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET reviewTick = ? WHERE id = ?;");
                    
                    ps.setInt(1, newTick);
                    ps.setInt(2, currentId);
                    ps.executeUpdate();

                    PreparedStatement ps2 = conn.prepareStatement("UPDATE CatTracker SET lastReview = NOW() WHERE id = ?;");
                    ps2.setInt(1, currentId);
                    ps2.executeUpdate();

                    //update user power level with every study tick
                    ArrayList<userNPCs> users = getUserNPCs();
                    userNPCs me = users.getFirst();
                    int myPwrLvl = me.pwrLvl + 1;
                    PreparedStatement psidk = conn.prepareStatement("UPDATE userNPCs SET powerLVL = ? WHERE id = 1;");
                    psidk.setInt(1, myPwrLvl);
                    psidk.executeUpdate();

                    System.out.println("congrats! your study count just went up.");
                    return;
                } 
            }
            System.out.println("that study cat doesn't exist in database.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void VBLvlUp(){
        ArrayList<userNPCs> users = getUserNPCs();
        userNPCs VegetaBot = users.getLast();
        userNPCs me = users.getFirst();
        Random random = new Random();
        

        String status = "filler";
        int increase;
        
        //check VegetaBot's power level
        if(me.pwrLvl - 50 >= VegetaBot.pwrLvl){
            status = "wayWeaker";
        }else if(VegetaBot.pwrLvl < me.pwrLvl){
            status = "weaker";
        }else if(VegetaBot.pwrLvl == me.pwrLvl){
            status = "equal";
        }else if(VegetaBot.pwrLvl > me.pwrLvl){
            status = "stronger";
        }else if(me.pwrLvl + 75 <= VegetaBot.pwrLvl){
            status = "wayStronger";
            System.out.println("VegetaBot has overcome you too much, he has lost the urge to train or fight until you get stronger.");
            return;
        }

        //randomization logic for VegetBot's pwr increase
        int[] weakIncrease = {6, 7, 8, 9}; //40% chance
        int[] midIncrease = {10, 12, 14, 16, 18}; //40% chance
        int[] highIncrease = {25, 30}; //20% chance "huge evolution"

        int weakChoice = weakIncrease[random.nextInt(4)];
        int midChoice = midIncrease[random.nextInt(5)];
        int highChoice = highIncrease[random.nextInt(2)];

        int[] gamble = {1,2,3,4,5,6,7,8,9,10};
        int computerChoice = gamble[random.nextInt(10)];
        switch(computerChoice){
            case 1,2,3,4 -> increase = weakChoice;
            case 5,6,7,8 -> increase = midChoice;
            case 9,10 -> {increase = highChoice; System.out.println("VegetaBot had a huge power lvl increase!!!");}
            default -> increase = 777; //shouldn't ever happen if my programming is correct
        }
        //increase VegetaBot's power lvl
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE userNPCs SET powerLVL = ? WHERE id = 2;");
            int VegPwrLvl = VegetaBot.pwrLvl + increase;
            ps.setInt(1, VegPwrLvl);
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }

        //determine status for random fight scenario
        if(random.nextDouble() <= 0.73){
            System.out.println("Random fight scenario!!! VegetaBot wants to fight head to head!");
                switch(status){
                case "wayWeaker" -> VBZenkai(me); //brutal I kick his ass, then he gets a zenkai boost
                case "weaker" -> VBFight(.01, me); //99% chance I win
                case "equal" -> VBFight(.50, me); //50% chance I lose
                case "stronger" -> VBFight(.25, me); //75% chance I lose
                default -> System.out.println("shit, VegetaBot's power is unreadable (error)\n");
            }
            return;
        }
        VBZenkai(me);
    }

    public static void VBZenkai(userNPCs me){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE userNPCs SET powerLVL = ? WHERE id = 2;");
            int VegPwrLvl = me.pwrLvl - 10;
            ps.setInt(1, VegPwrLvl);
            ps.executeUpdate();
            System.out.println("VegetaBot had a sudden power boost! His pwrLvl is now only 10 behind yours");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void VBFight(Double chance, userNPCs me){
        Random random = new Random();
        if(random.nextDouble() <= chance){
            System.out.println("Yay!!! You won.");
        }else{
            System.out.println("VegetaBot has defeated you. you lose");
            Random random2 = new Random();
            if(random2.nextBoolean()){
                System.out.println("VegetaBot bellitles your work ethic and tells you to study harder. He lets you off easy this time.");
            }else{
                System.out.println("VegetaBot is sickened with you. He strikes you so hard you lose some of your power");
                try(Connection conn = JDBC.getConnection()){
                    PreparedStatement ps = conn.prepareStatement("UPDATE userNPCs SET powerLVL = ? WHERE id = 1;");
                    int newLvl = me.pwrLvl - random.nextInt(7, 16);
                    ps.setInt(1, newLvl);
                    ps.executeUpdate();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void resetDailyTicks(){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET dailyTickCount = 0;");
            ps.executeUpdate();
            
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void resetLoginDate(){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET priorLoginDate = NOW();");
            ps.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void minusReview(TrackedCat currentCat){

        int newTick = currentCat.reviewTick - 1;
        int dailyTick = (currentCat.dailyTickCount >= 0) ? 0 : currentCat.dailyTickCount - 1;
        int currentId = currentCat.id;
        //decrease reviewTick
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET reviewTick = ?, initialFinish = null, dailyTickCount = ? WHERE id = ?;");
            ps.setInt(1, newTick);
            ps.setInt(2, dailyTick);
            ps.setInt(3, currentId);
            ps.executeUpdate();

            //update user power level with every study tick
            ArrayList<userNPCs> users = getUserNPCs();
            userNPCs me = users.getFirst();
            int myPwrLvl = me.pwrLvl - 1;
            PreparedStatement psidk = conn.prepareStatement("UPDATE userNPCs SET powerLVL = ? WHERE id = 1;");
            psidk.setInt(1, myPwrLvl);
            psidk.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void tempQuitBool(TrackedCat currentCat){
        int currentId = currentCat.id;
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET tempQuit = true WHERE id = ?;");
            ps.setInt(1, currentId);
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void tempQuitReset(){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET tempQuit = false;");
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void resetCatTracking(TrackedCat currentCat){

        //delete row of current cat
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("DELETE FROM CatTracker WHERE id = ?;");
            ps.setInt(1, currentCat.id);
            ps.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
        //enter new cat using current cat's name
        addTrackedCat(currentCat.studyCat);
    }
}
