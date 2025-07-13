package easyrow;

import easyrow.data.Club;
import easyrow.data.Prio;

import java.time.LocalDate;

public class Athlete {

    private final String firstName;
    private final String lastName;
    private final Club club;
    private final int prio;
    private final LocalDate dateOfBirth;
    private final long licenseNumber;

    public Athlete(String firstName, String lastName, Club club, Prio prio, LocalDate dateOfBirth, long licenseNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.club = club;
        this.prio = prio.getPriority();
        this.dateOfBirth = dateOfBirth;
        this.licenseNumber = licenseNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getShownName() {
        return lastName + ", " + firstName;
    }

    public Club getClub() {
        return club;
    }

    public long getLicenseNumber() {
        return licenseNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public int getPrio() {
        return prio;
    }
}
