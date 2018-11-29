package com.ivan;

import java.util.Objects;

public class TimeInfo {

    enum Mode {
        TARGET_GROSS, CARRY_FORWARD
    };

    private Time target;
    private Time gross;
    private Time carryForward;
    private Mode mode;

    public TimeInfo() {
        target = new Time();
        gross = new Time();
        carryForward = new Time();
    }

    public TimeInfo(String target, String gross) {
        this.target = getTime(target);
        this.gross = getTime(gross);
        mode = Mode.TARGET_GROSS;
    }

    public TimeInfo(String carryForward) {
        this.carryForward = getTime(carryForward);
        mode = Mode.CARRY_FORWARD;
    }

    public Time getCarryForward() {
        return carryForward;
    }

    public Mode getMode() {
        return mode;
    }

    private Time getTime(String value) {
        String [] split = value.split("\\.");
        int hours = Integer.valueOf(split[0]);
        int minutes = Integer.valueOf(split[1]);
        if (hours == 0 && split[0].charAt(0) == '-') minutes *= -1;
        return new Time(hours, minutes);
    }

    public Time getTarget() {
        return target;
    }

    public Time getGross() {
        return gross;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeInfo timeInfo = (TimeInfo) o;
        return Objects.equals(target, timeInfo.target) &&
                Objects.equals(gross, timeInfo.gross);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, gross);
    }

    @Override
    public String toString() {
        return "TimeInfo{" +
                "target='" + target + '\'' +
                ", gross='" + gross + '\'' +
                '}';
    }
}
