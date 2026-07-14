package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class DAL {
    
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

                trackedStudyCat = new TrackedCat(rs.getInt("id"), rs.getString("studyCat"), rs.getTimestamp("studyStart").toLocalDateTime(), rs.getInt("reviewTick"), rs.getTimestamp("lastReview").toLocalDateTime(), initialFinish, studyComplete);
                allTrackedCats.add(trackedStudyCat);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return allTrackedCats;
    }

    public static void addTrackedCat(String studyCat){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("INSERT INTO CatTracker (studyCat, studyStart, reviewTick, lastReview) VALUES (?, NOW(), 0, NOW());");

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
            System.out.println("study cat doesn't exist in database.");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
