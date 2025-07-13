package easyrow.database;

import easyrow.Athlete;
import easyrow.data.Club;
import easyrow.data.Prio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class AthleteDatabase extends Database{

    public AthleteDatabase() {
        super();
    }
    
    public void init() {
        try {
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();

            String athletesCreate = """
                    CREATE TABLE IF NOT EXISTS athletes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        firstname TEXT NOT NULL,
                        lastname TEXT NOT NULL,
                        club TEXT,
                        prio INT,
                        birthdate STRING,
                        licenseNumber LONG
                    );
                    """;
            statement.execute(athletesCreate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getURL() {
        return URL;
    }

    @Override
    public String getName() {
        String NAME = "athletes";
        return NAME;
    }

    public void saveAthlete(Athlete athlete) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO athletes (firstname, lastname, club, prio, birthdate, licenseNumber) VALUES (?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setString(1, athlete.getFirstName());
            preparedStatement.setString(2, athlete.getLastName());
            preparedStatement.setString(3, athlete.getClub().name());
            preparedStatement.setInt(4, athlete.getPrio());
            preparedStatement.setString(5, athlete.getDateOfBirth().toString());
            preparedStatement.setLong(6, athlete.getLicenseNumber());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public List<Athlete> getAthletes() {
        List<Athlete> athletes = new ArrayList<>();

        String sql = "SELECT firstname, lastname, club, prio, birthdate, licenseNumber FROM athletes";

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                String clubString = resultSet.getString("club");
                int prioInt = resultSet.getInt("prio");
                LocalDate localDate =  LocalDate.parse(resultSet.getString("birthdate"));
                long licenseNumber = resultSet.getLong("licenseNumber");

                Club club = Club.valueOf(clubString);

                Athlete athlete = new Athlete(firstName, lastName, club, Prio.MASTERS, localDate, licenseNumber);
                athletes.add(athlete);
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
        }

        return athletes;
    }

    public Athlete getAthleteByName(String name) {
        for(Athlete athlete : getAthletes()) {
            if (athlete.getFirstName().equalsIgnoreCase(name.split(" ")[0]) && athlete.getLastName().equalsIgnoreCase(name.split(" ")[1])) {
                return athlete;
            }
        }
        return null;
    }
}