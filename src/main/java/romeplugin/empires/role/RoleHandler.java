package romeplugin.empires.role;

import org.bukkit.ChatColor;
import romeplugin.database.SQLConn;

import java.sql.SQLException;
import java.util.HashSet;

public class RoleHandler {
    public RoleHandler() {
        makeDB();
    }

    private void makeDB() {
        try (var conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS roles (" +
                    "name VARCHAR(24) PRIMARY KEY NOT NULL," +
                    "roleID INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "obtainMethod ENUM('DEFAULT', 'ELECTED', 'APPOINTED') NOT NULL," +
                    "color CHAR(1));").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS rolePermissions (" +
                    "roleID INT NOT NULL," +
                    "permission VARCHAR(24) NOT NULL);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Role getRole(String roleName) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM roles WHERE name = ?;");
            stmt.setString(1, roleName);
            var res = stmt.executeQuery();
            if (!res.next()) {
                return null;
            }
            int id = res.getInt("roleID");
            var obtainMethod = ObtainMethod.valueOf(res.getString("obtainMethod"));
            var color = ChatColor.getByChar(res.getString("color"));
            res.close();
            stmt.close();
            var perms = getPermissions(id);
            if (perms == null) {
                return null;
            }
            return new Role(perms, color, roleName, id, obtainMethod);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private HashSet<Permission> getPermissions(int roleID) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT permission FROM rolePermissions WHERE " +
                    "roleID = ?;");
            stmt.setInt(1, roleID);
            var res = stmt.executeQuery();
            var perms = new HashSet<Permission>();
            while (res.next()) {
                perms.add(Permission.valueOf(res.getString(1)));
            }
            return perms;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addPermission(int roleId, Permission perm) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO rolePermissions (roleId, permission) VALUES (?, ?);");
            stmt.setInt(1, roleId);
            stmt.setString(2, perm.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
