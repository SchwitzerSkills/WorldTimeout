package eu.acewolf.spigot.worldtimeout.mysql;

import eu.acewolf.spigot.worldtimeout.WorldTimeout;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerTimeoutMySQL {

    private final MySQL mySQL = WorldTimeout.getInstance().getMySQL();

    public void createTable(){
        String sql = "CREATE TABLE IF NOT EXISTS playertimeout (id INT AUTO_INCREMENT PRIMARY KEY," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "world VARCHAR(10) NOT NULL," +
                "activity LONG NOT NULL)";

        try (Statement statement = mySQL.getConnection().createStatement()){
            statement.executeUpdate(sql);
        } catch (SQLException e){
        }
    }

    public void addPlayerTimeout(String playerUUID, String world, long activity){
        String sql = "INSERT INTO playertimeout (player_uuid, world, activity) VALUES (?, ?, ?)";

        try(PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(sql)){
            preparedStatement.setString(1, playerUUID);
            preparedStatement.setString(2, world);
            preparedStatement.setLong(3, activity);

            preparedStatement.executeUpdate();
        } catch (SQLException e){
        }
    }

    public void removePlayerTimeout(String playerUUID, String world){
        String sql = "DELETE FROM playertimeout WHERE player_uuid=? AND world=?";

        try(PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(sql)){
            preparedStatement.setString(1, playerUUID);
            preparedStatement.setString(2, world);

            preparedStatement.executeUpdate();
        } catch (SQLException e){
        }
    }

    public void updatePlayerTimeout(String playerUUID, String world, long activity){
        String sql = "UPDATE playertimeout SET activity=? WHERE player_uuid=? AND world=?";

        try(PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(sql)){
            preparedStatement.setLong(1, activity);
            preparedStatement.setString(2, playerUUID);
            preparedStatement.setString(3, world);

            preparedStatement.executeUpdate();
        } catch (SQLException e){
        }
    }

    public boolean hasPlayerTimeoutInWorld(String playerUUID, String world){
        String sql = "SELECT COUNT(activity) AS count FROM playertimeout WHERE player_uuid=? AND world=?";

        try(PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(sql)){
            preparedStatement.setString(1, playerUUID);
            preparedStatement.setString(2, world);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return resultSet.getBoolean("count");
            }

            resultSet.close();
        } catch (SQLException e){
        }
        return false;
    }

    public long getPlayerTimeout(String playerUUID, String world){
        String sql = "SELECT activity FROM playertimeout WHERE player_uuid=? AND world=?";

        try(PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(sql)){
            preparedStatement.setString(1, playerUUID);
            preparedStatement.setString(2, world);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return resultSet.getLong("activity");
            }

            resultSet.close();
        } catch (SQLException e){
        }

        return 0;
    }
}
