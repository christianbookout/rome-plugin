package romeplugin.election;

import romeplugin.database.SQLConn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class PartyHandler {
    //DB: table 1 - players, their respective party name
    //    table 2 - party name
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

    /**
     * helper class that holds the invariant that the contained string must be uppercase
     */
    public static class PartyAcronym {
        public final String str;

        private PartyAcronym(String input) {
            str = input;
        }

        public static PartyAcronym make(String input) {
            return new PartyAcronym(input.toUpperCase());
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
     * get a list of all uuids of members in a party
     */
    public Collection<UUID> getMembers(PartyAcronym acronym) {
        var members = new ArrayList<UUID>();
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT uuid FROM partyMembers WHERE acronym=?;");
            stmt.setString(1, acronym.str);
            var results = stmt.executeQuery();
            while (results.next()) {
                members.add(UUID.fromString(results.getString(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * @param acronym acronym of the party
     * @return list of usernames of members in the party
     */
    public Collection<String> getMembersUsernames(PartyAcronym acronym) {
        var members = new ArrayList<String>();
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT usernames.username FROM partyMembers INNER JOIN usernames ON partyMembers.uuid = usernames.uuid WHERE acronym=?;");
            stmt.setString(1, acronym.str);
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
            var stmt = conn.prepareStatement("SELECT * FROM parties WHERE owner_uuid=?;");
            stmt.setString(1, uuid.toString());
            var results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createParty(UUID owner, PartyAcronym acronym, String name) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("INSERT INTO parties VALUES (?, ?, ?, ?, ?);");
            stmt.setString(1, name);
            stmt.setString(2, acronym.str);
            stmt.setString(3, owner.toString());
            stmt.setBoolean(4, false);
            stmt.setString(5, "This is a political party.");
            if (stmt.executeUpdate() < 1) {
                return false;
            }
            stmt.close();
            stmt = conn.prepareStatement("REPLACE INTO partyMembers VALUES (?, ?);");
            stmt.setString(1, owner.toString());
            stmt.setString(2, acronym.str);
            return stmt.executeUpdate() > 0;
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
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement("DELETE FROM partyMembers WHERE acronym=?;");
            stmt.setString(1, party.str);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean joinParty(UUID player, PartyAcronym acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("REPLACE INTO partyMembers VALUES (?, ?);");
            stmt.setString(1, player.toString());
            stmt.setString(2, acronym.str);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getName(PartyAcronym acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT name FROM parties WHERE acronym=?;");
            stmt.setString(1, acronym.str);
            var results = stmt.executeQuery();
            if (results.next()) {
                return results.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDescription(PartyAcronym acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT description FROM parties WHERE uuid=?;");
            stmt.setString(1, acronym.str);
            var results = stmt.executeQuery();
            if (results.next()) {
                return results.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PartyAcronym getParty(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT title FROM partyMembers WHERE uuid=?;");
            stmt.setString(1, player.toString());
            var results = stmt.executeQuery();
            if (results.next()) {
                return new PartyAcronym(results.getString("acronym"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean partyExists(PartyAcronym acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM partyMembers WHERE acronym=?;");
            stmt.setString(1, acronym.str);
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

    public Optional<Boolean> isPartyPublic(PartyAcronym acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT is_public FROM parties WHERE acronym=?");
            stmt.setString(1, acronym.str);
            var res = stmt.executeQuery();
            if (res.next()) {
                return Optional.of(res.getBoolean("is_public"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean rename(UUID uuid, PartyAcronym newAcronym, String newName) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE parties SET acronym=?, name=? WHERE owner_uuid=?;");
            stmt.setString(1, newAcronym.str);
            stmt.setString(2, newName);
            stmt.setString(3, uuid.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
