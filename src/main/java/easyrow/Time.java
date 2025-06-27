package easyrow;

public record Time(int hours, int minutes) {

    public static Time getDifference(Time first, Time second) {
        return new Time(second.hours() - first.hours(), second.minutes() - first.minutes());
    }

    @Override
    public String toString() {
        return hours + ":" + minutes;
    }
}
