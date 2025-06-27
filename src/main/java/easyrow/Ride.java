package easyrow;

import easyrow.data.BoatType;

public class Ride {

    private final BoatType boatType;
    private final Time startTime;
    private Time endTime;

    public Ride(BoatType boatType, Time startTime) {
        this.boatType = boatType;
        this.startTime = startTime;
    }

    public Time getLenght() {
        return Time.getDifference(startTime, endTime);
    }

    public Time getEnd() {
        return endTime;
    }

    public void setEnd(Time end) {
        this.endTime = end;
    }

    public Time getStartTime() {
        return startTime;
    }

    public BoatType getBoatType() {
        return boatType;
    }
}