package easyrow;

import easyrow.data.BoatType;
import easyrow.data.Club;
import easyrow.data.Prio;
import easyrow.database.AthleteDatabase;
import easyrow.database.BoatDatabase;

import java.time.LocalDate;

public class EasyRowLog {

    private static final String URL = "jdbc:sqlite:athletes.db";

    public static void main(String[] args) {
        new EasyRowLog();
    }

    public EasyRowLog() {
        new GUI();
        BoatDatabase.init();
        AthleteDatabase.init();
    }

    public void addBoatToLib(Boat boat) {
        BoatDatabase.saveBoat(boat);
    }

    public void addBoatToLib(String name, BoatType boatType, Prio priority, double avgTeamWeight, double boatWeight) {
        BoatDatabase.saveBoat(new Boat(name, boatType, priority, avgTeamWeight, boatWeight));
    }

    public void addAthleteToLib(Athlete athlete) {
        AthleteDatabase.saveAthlete(athlete);
    }

    public void addAthleteToLib(String firstName, String lastName, Club club, Prio prio, LocalDate dateOfBirth, int licenseNumber) {
        AthleteDatabase.saveAthlete(new Athlete(firstName, lastName, club, prio, dateOfBirth, licenseNumber));
    }
}