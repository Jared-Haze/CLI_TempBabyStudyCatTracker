package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrackedCat {
    int id;
    String studyCat;
    LocalDateTime studyStart;
    int reviewTick;
    LocalDateTime lastReview;
    LocalDateTime initialFinish;
    LocalDateTime studyComplete;
    int dailyTickCount;
    LocalDate priorLoginDate;

    TrackedCat(int id, String studyCat, LocalDateTime studyStart, int reviewTick, LocalDateTime lastReview, LocalDateTime initialFinish, LocalDateTime studyComplete, int dailyTickCount, LocalDate priorLoginDate){
        this.id = id;
        this.studyCat = studyCat;
        this.studyStart = studyStart;
        this.reviewTick = reviewTick;
        this.lastReview = lastReview;
        this.initialFinish = initialFinish;
        this.studyComplete = studyComplete;
        this.dailyTickCount = dailyTickCount;
        this.priorLoginDate = priorLoginDate;
    }

    public String getTrackedCat() {
        String reviewStatus;
        if(studyComplete != null){
            reviewStatus = "[Completed]";
        } else if(initialFinish != null){
            reviewStatus = "[Initial Finish]";
        } else {
            reviewStatus = String.valueOf(reviewTick) + " [Active]";
        }
        String trackedCatString = studyCat + " | total studied: " + reviewStatus + " | times studied today: " + dailyTickCount + " | last reviewed: " + lastReview; //HashMap methods | 1 review/initial finish/completed | last reviewed: 7/13/26
        return trackedCatString;
    }
}
