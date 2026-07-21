package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
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

                java.sql.Date sqlDate = rs.getDate("priorLoginDate");
                LocalDate priorLoginDate = (sqlDate == null) ? null : sqlDate.toLocalDate();

                trackedStudyCat = new TrackedCat(rs.getInt("id"), rs.getString("studyCat"), rs.getTimestamp("studyStart").toLocalDateTime(), rs.getInt("reviewTick"), rs.getTimestamp("lastReview").toLocalDateTime(), initialFinish, studyComplete, rs.getInt("dailyTickCount"), priorLoginDate);
                allTrackedCats.add(trackedStudyCat);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return allTrackedCats;
    }

    public static void addTrackedCat(String studyCat){
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("INSERT INTO CatTracker (studyCat, studyStart, reviewTick, lastReview, dailyTickCount, priorLoginDate) VALUES (?, NOW(), 0, NOW(), 0, NOW());");

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

                    System.out.println("congrats! your study count just went up.");
                    return;
                } 
            }
            System.out.println("that study cat doesn't exist in database.");
        }catch(SQLException e){
            e.printStackTrace();
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
        int dailyTick = currentCat.dailyTickCount - 1;
        int currentId = currentCat.id;
        //decrease reviewTick
        try(Connection conn = JDBC.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE CatTracker SET reviewTick = ?, initialFinish = null, dailyTickCount = ? WHERE id = ?;");
            ps.setInt(1, newTick);
            ps.setInt(2, dailyTick);
            ps.setInt(3, currentId);
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
        //set studyInitialFinish back to null (always)
        //decrease dailyTickCount (so mistaken tick increases don't take away from daily allotment
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
