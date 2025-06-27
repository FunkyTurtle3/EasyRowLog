package easyrow.database;

import easyrow.Boat;
import easyrow.data.BoatType;
import easyrow.data.Prio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoatDatabase {

    private static final String URL = "jdbc:sqlite:boats.db";

    public static void init() {
        try {
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();

            String athletesCreate = """
                    CREATE TABLE IF NOT EXISTS boats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        boatType TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        avgTeamWeight DOUBLE,
                        boatWeight DOUBLE
                    );
                    """;
            statement.execute(athletesCreate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveBoat(Boat boat) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO boats (name, boatType, priority, avgTeamWeight, boatWeight) VALUES (?, ?, ?, ?, ?)")) {

            preparedStatement.setString(1, boat.name());
            preparedStatement.setString(2, boat.boatType().toString());
            preparedStatement.setInt(3, boat.priority().getPriority());
            preparedStatement.setDouble(4, boat.avgTeamWeight());
            preparedStatement.setDouble(5, boat.boatWeight());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public static List<Boat> getBoats() {
        List<Boat> boats = new ArrayList<>();

        String sql = "SELECT name, boatType, priority, avgTeamWeight, boatWeight FROM boats";

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {

                String name = resultSet.getString(1);
                BoatType boatType = BoatType.valueOf(resultSet.getString(2));
                Prio priority = Prio.getPrioByInt(resultSet.getInt(3));
                double avgTeamWeight = resultSet.getDouble(4);
                double boatWeight = resultSet.getDouble(5);

                Boat athlete = new Boat(name, boatType, priority, avgTeamWeight, boatWeight);
                boats.add(athlete);
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
        }

        return boats;
    }

    public static Boat getBoatByName(String name) {
        for(Boat boat : getBoats()) {
            if (boat.name().equalsIgnoreCase(name)) {
                return boat;
            }
        }
        return null;
    }

}