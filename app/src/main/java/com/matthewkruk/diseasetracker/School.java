package com.matthewkruk.diseasetracker;

public class School {

    public String schoolName;
    public long event;

    public School() {

    }

    public School(String schoolName, long event){
        this.schoolName = schoolName;
        this.event = event;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public long getEvent() {
        return event;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setEvent(long event) {
        this.event = event;
    }
}
