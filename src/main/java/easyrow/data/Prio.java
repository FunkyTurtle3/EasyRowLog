package easyrow.data;

public enum Prio {
    MASTERS(0),
    JUNIORS(1),
    KIDS(2);

    private final int priority;

    Prio(int priority) {

        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static Prio getPrioByInt(int i) {
        return switch (i) {
            case 1 -> JUNIORS;
            case 2 -> KIDS;
            default -> MASTERS;
        };
    }
}
