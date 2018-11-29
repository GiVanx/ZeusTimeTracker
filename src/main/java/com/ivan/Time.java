package com.ivan;

public class Time {

    int hours;
    int minutes;

    public Time() {
        hours = minutes = 0;
    }

    public Time(int hours, int minute) {
        this.hours = hours;
        this.minutes = minute;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return "Time{" +
                "hours=" + hours +
                ", minutes=" + minutes +
                '}';
    }
}
