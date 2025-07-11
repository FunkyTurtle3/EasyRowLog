package easyrow;

import easyrow.config.ConfigManager;
import easyrow.data.BoatType;
import easyrow.data.Club;
import easyrow.data.Prio;
import easyrow.database.AthleteDatabase;
import easyrow.database.BoatDatabase;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class EasyRowLog {

    private final BoatDatabase boatDatabase;
    private final AthleteDatabase athleteDatabase;

    public static void main(String[] args) {
        EasyRowLog log =  new EasyRowLog();
        log.clearDatabase();
        log.saveAthlete(new Athlete("Lasse", "Stark", Club.RGW, Prio.JUNIORS, LocalDate.of(2008, 12, 2), 023472034));
        Athlete lasse = log.getAthletes().get(0);
        System.out.println(lasse.getFullName() + " " + lasse.getDateOfBirth() + " " + lasse.getClub());
        ConfigManager.init();
    }

    public EasyRowLog() {
        boatDatabase = new BoatDatabase();
        athleteDatabase = new AthleteDatabase();
    }

    public void addBoatToLib(Boat boat) {
        boatDatabase.saveBoat(boat);
    }

    public void addBoatToLib(String name, BoatType boatType, Prio priority, double avgTeamWeight, double boatWeight) {
        boatDatabase.saveBoat(new Boat(name, boatType, priority, avgTeamWeight, boatWeight));
    }

    public void addAthleteToLib(Athlete athlete) {
        athleteDatabase.saveAthlete(athlete);
    }

    public void addAthleteToLib(String firstName, String lastName, Club club, Prio prio, LocalDate dateOfBirth, int licenseNumber) {
        athleteDatabase.saveAthlete(new Athlete(firstName, lastName, club, prio, dateOfBirth, licenseNumber));
    }

    public List<Athlete> getAthletes() {
        return athleteDatabase.getAthletes();
    }

    public List<Boat> getBoats() {
        return boatDatabase.getBoats();
    }

    public List<Athlete> getAthletesSimOrder(String input) {
        List<Athlete> athletes = athleteDatabase.getAthletes();
        LevenshteinDistance ld = new LevenshteinDistance();
        athletes.sort(Comparator.comparingInt(athlete -> ld.apply(athlete.getFullName().toLowerCase(), input.toLowerCase())));
        return athletes;
    }

    public List<Boat> getBoatsSimOrder(String input) {
        List<Boat> boats = boatDatabase.getBoats();
        LevenshteinDistance ld = new LevenshteinDistance();
        boats.sort(Comparator.comparingInt(boat -> ld.apply(boat.name().toLowerCase(), input.toLowerCase())));
        return boats;
    }

    public void saveBoat(Boat boat) {
        boatDatabase.saveBoat(boat);
    }

    public void saveAthlete(Athlete athlete) {
        athleteDatabase.saveAthlete(athlete);
    }

    public void clearDatabase() {
        athleteDatabase.clearDatabase();
    }
}