import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    public void saveMessage(String sender, String content) {
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "INSERT INTO messages (sender, content) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, sender);
                statement.setString(2, content);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "SELECT * FROM messages";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String sender = resultSet.getString("sender");
                        String content = resultSet.getString("content");
                        messages.add(sender + ": " + content);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }
}
