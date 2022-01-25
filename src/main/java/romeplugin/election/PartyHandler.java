package romeplugin.election;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import romeplugin.database.SQLConn;

public class PartyHandler {
    //DB: table 1 - players, their respective party name
    //    table 2 - party name
    private HashMap<UUID, String> invitations = new HashMap<>();
    public PartyHandler() {
        this.initializedb();
    }
    /**
     * set up tables for parties, and for members
     */
    private void initializedb() {
        try (Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS parties (" +
                                  "name VARCHAR(50) NOT NULL UNIQUE," +
                                  "acronym CHAR(4) NOT NULL UNIQUE," +
                                  "owner_uuid CHAR(36) NOT NULL UNIQUE PRIMARY KEY," + 
                                  "is_public BOOLEAN," +
                                  "description VARCHAR(250));" +
                                  "CREATE TABLE IF NOT EXISTS partyMembers (" +
                                  "uuid CHAR(36) UNIQUE PRIMARY KEY NOT NULL," +
                                  "acronym CHAR(4) NOT NULL);").executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Collection<String> getParties() {
        var parties = new ArrayList<String>();
        try (Connection conn = SQLConn.getConnection()) {
            var results = conn.prepareStatement("SELECT name, acronym FROM parties;").executeQuery();
            while (results.next()) {
                parties.add(results.getString("name") + " (" + results.getString("acronym") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parties;
    }
    /**
     * get a list of all usernames of members in a party
     */
    public Collection<String> getMembers(String acronym) {
        var members = new ArrayList<String>();
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT uuid FROM partyMembers WHERE LOWER(acronym)=?");
            stmt.setString(1, acronym.toLowerCase());
            var results = stmt.executeQuery();
            while (results.next()) {
                members.add(results.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public boolean isOwner(UUID uuid) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM parties WHERE owner_uuid=?");
            stmt.setString(1, uuid.toString());
            var results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createParty(UUID owner, String party, String acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO parties VALUES (?, ?, ?, ?, ?);");
            stmt.setString(1, party);
            stmt.setString(2, acronym);
            stmt.setString(3, owner.toString());
            stmt.setBoolean(4, false);
            stmt.setString(5, "This is a political party.");
            var results = stmt.executeQuery();
            if (results.next()) {
                this.joinParty(owner, party);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean leaveParty(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM partyMembers WHERE uuid=?;");
            stmt.setString(1, player.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disbandParty(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var party = this.getParty(player);
            var stmt = conn.prepareStatement("DELETE FROM parties WHERE owner_uuid=?;");
            stmt.setString(1, player.toString());
            if (stmt.executeUpdate() > 0) {
                var players = this.getMembers(party);
                players.forEach(p -> this.leaveParty(player));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean joinParty(UUID player, String acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("REPLACE INTO partyMembers VALUES (?, ?);");
            stmt.setString(1, player.toString());
            stmt.setString(2, acronym);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getName(String acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT name FROM parties WHERE acronym=?;");
            stmt.setString(1, acronym);
            var results = stmt.executeQuery();
            if (results.next()) {
                return results.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDescription(String acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT description FROM parties WHERE uuid=?;");
            stmt.setString(1, acronym);
            var results = stmt.executeQuery();
            if (results.next()) {
                return results.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getParty(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT title FROM partyMembers WHERE uuid=?;");
            stmt.setString(1, player.toString());
            var results = stmt.executeQuery();
            if (results.next()) {
                return results.getString("acronym");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean partyExists(String acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM partyMembers WHERE LOWER(acronym)=?;");
            stmt.setString(1, acronym.toLowerCase());
            var results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setPublic(UUID owner, boolean isPublic) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE parties SET is_public=? WHERE owner_uuid=?;");
            stmt.setBoolean(1, isPublic);
            stmt.setString(2, owner.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void invite(UUID player, String party) {
        this.invitations.put(player, party);
    }

    public boolean deny(UUID player) {
        return invitations.remove(player) != null;
    }

    public boolean accept (UUID player) {
        return this.joinParty(player, invitations.get(player));
    }
}
