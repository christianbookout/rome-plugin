package romeplugin.election;

import org.bukkit.ChatColor;
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
                    "owner_uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "is_public BOOLEAN," +
                    "color CHAR(1)," +
                    "description VARCHAR(250));").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS partyMembers (" +
                    "uuid CHAR(36) PRIMARY KEY NOT NULL," +
                    "acronym CHAR(4) NOT NULL);").execute();
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

        @Override
        public String toString() {
            return this.str;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PartyAcronym) {
                return ((PartyAcronym) other).str.equals(this.str);
            }
            if (other instanceof String) {
                return (other).equals(str);
            }
            return false;
        }
    }

    public Collection<Party> getParties() {
        var parties = new ArrayList<Party>();
        try (Connection conn = SQLConn.getConnection()) {
            var results = conn.prepareStatement("SELECT * FROM parties;").executeQuery();
            while (results.next()) {
                String name = results.getString("name");
                String acronym = results.getString("acronym");
                String description = results.getString("description");
                boolean isPublic = results.getBoolean("is_public");
                String owner = results.getString("owner_uuid");
                char color = results.getString("color").toCharArray()[0];
                parties.add(new Party(name, PartyAcronym.make(acronym), description, isPublic, owner, color));
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
            var stmt = conn.prepareStatement("INSERT INTO parties VALUES (?, ?, ?, ?, ?, ?);");
            stmt.setString(1, name);
            stmt.setString(2, acronym.str);
            stmt.setString(3, owner.toString());
            stmt.setBoolean(4, false);
            stmt.setString(5, "7"); //set default color to gray i guess
            stmt.setString(6, "This is a political party.");
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
            stmt.setString(1, party.acronym.str);
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
                return results.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setDescription(PartyAcronym acronym, String description) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE parties SET description=? WHERE acronym=?;");
            stmt.setString(1, description);
            stmt.setString(2, acronym.str);
            var results = stmt.executeUpdate();
            return results > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getDescription(PartyAcronym acronym) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT description FROM parties WHERE acronym=?;");
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

    public Party getParty(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM parties WHERE acronym=(SELECT acronym FROM partyMembers WHERE uuid=?);");
            stmt.setString(1, player.toString());
            var results = stmt.executeQuery();
            if (results.next()) {
                String name = results.getString("name");
                String acronym = results.getString("acronym");
                String description = results.getString("description");
                boolean isPublic = results.getBoolean("is_public");
                String owner = results.getString("owner_uuid");
                char color = results.getString("color").toCharArray()[0];
                return new Party(name, PartyAcronym.make(acronym), description, isPublic, owner, color);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setColor(PartyAcronym acronym, char color) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE parties SET color=? WHERE acronym=?;");
            stmt.setString(1, String.valueOf(color));
            stmt.setString(2, acronym.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public boolean kickMember(UUID owner, UUID toKick) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM partyMembers WHERE uuid=? AND acronym = (SELECT acronym FROM parties WHERE owner_uuid=?);");
            stmt.setString(1, toKick.toString());
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

    public boolean rename(UUID uuid, PartyAcronym oldAcronym, PartyAcronym newAcronym, String newName) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE parties SET acronym=?, name=? WHERE owner_uuid=?;");
            var stmt2 = conn.prepareStatement("UPDATE partyMembers SET acronym=? WHERE acronym=?;");
            stmt.setString(1, newAcronym.str);
            stmt.setString(2, newName);
            stmt.setString(3, uuid.toString());
            stmt2.setString(1, newAcronym.toString());
            stmt2.setString(2, oldAcronym.toString());
            stmt2.execute();
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public class Party {
        public final String name, description, owner;
        public final PartyAcronym acronym;
        public final boolean isPublic;
        public final ChatColor color;
        private static final int cutoffInt = 50;

        public Party(String name, PartyAcronym acronym, String description, boolean isPublic, String owner, char color) {
            this.name = name;
            this.acronym = acronym;
            this.description = description;
            this.isPublic = isPublic;
            this.owner = owner;
            this.color = ChatColor.getByChar(color);
        }

        @Override
        public String toString() {
            String cutoff = description.length() > cutoffInt ? description.substring(0, cutoffInt).strip() + "..." : description;
            return this.color + name + ChatColor.RESET + " (" + this.color + acronym + ChatColor.RESET + "): " + ChatColor.RESET + cutoff;
        }
    }
}
