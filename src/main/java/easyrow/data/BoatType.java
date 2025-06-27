package easyrow.data;

public enum BoatType {
    SINGLE_SCULLS(1, false),
    DOUBLE_SCULLS(2, false),
    TRIPLE_SCULLS(3, false),
    QUADRUPLE_SCULLS(4, false),
    QUADRUPLE_SCULLS_WC(4, true),
    OCTUPLE_SCULLS(8, true),
    DOUBLE_SWEEP(2, false),
    DOUBLE_SWEEP_WC(2, true),
    QUADRUPLE_SWEEP(4, false),
    QUADRUPLE_SWEEP_WC(4, true),
    OCTUPLE_SWEEP_WC(8, true);

    private final int athleteCount;
    private final boolean hasCox;

    BoatType(int athleteCount, boolean hasCox) {
        this.athleteCount = athleteCount;
        this.hasCox = hasCox;
    }

    public int getAthleteCount() {
        return athleteCount;
    }

    public int getFullAthleteCount() {
        return athleteCount + (hasCox ? 1 : 0);
    }

    public boolean hasCox() {
        return hasCox;
    }
}
