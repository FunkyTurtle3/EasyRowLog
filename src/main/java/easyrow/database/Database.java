package easyrow.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Database {

    protected final String URL = "jdbc:sqlite:" + getName() + ".db";

    protected Database() {
        init();
    }

    public abstract void init();

    public String getURL() {
        return URL;
    }

    public abstract String getName();

    public void clearDatabase() {
        try {
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();

            statement.execute("DELETE FROM " + getName() + "; VACUUM; DELETE FROM sqlite_sequence WHERE name='" + getName() + "';");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
