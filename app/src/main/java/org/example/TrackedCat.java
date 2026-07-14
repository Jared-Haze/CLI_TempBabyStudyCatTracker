package org.example;

import java.time.LocalDateTime;

public class TrackedCat {
    int id;
    String studyCat;
    LocalDateTime studyStart;
    int reviewTick;
    LocalDateTime lastReview;
    LocalDateTime initialFinish;
    LocalDateTime studyComplete;

    TrackedCat(int id, String studyCat, LocalDateTime studyStart, int reviewTick, LocalDateTime lastReview, LocalDateTime initialFinish, LocalDateTime studyComplete){
        this.id = id;
        this.studyCat = studyCat;
        this.studyStart = studyStart;
        this.reviewTick = reviewTick;
        this.lastReview = lastReview;
        this.initialFinish = initialFinish;
        this.studyComplete = studyComplete;
    }

    public String getTrackedCat() {
        String reviewStatus;
        if(studyComplete != null){
            reviewStatus = "completed";
        } else if(initialFinish != null){
            reviewStatus = "initial finish";
        } else {
            reviewStatus = String.valueOf(reviewTick);
        }
        String trackedCatString = studyCat + " | " + reviewStatus + " | last reviewed: " + lastReview; //HashMap methods | 1 review/initial finish/completed | last reviewed: 7/13/26
        return trackedCatString;
    }
}
