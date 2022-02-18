package romeplugin.empires;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import romeplugin.database.SQLConn;
import romeplugin.election.PartyHandler;
import romeplugin.election.PartyHandler.Party;
import romeplugin.zoning.claims.City;

public class EmpireHandler {

    private final PartyHandler partyHandler;

    public EmpireHandler(PartyHandler partyHandler) {
        this.partyHandler = partyHandler;
        this.initializedb();
    }

    /**
     * set up tables for parties, and for members
     */
    private void initializedb() {
        try (Connection conn = SQLConn.getConnection()) {

            // list of empires
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empires (" +
                    "empireName VARCHAR(50) NOT NULL UNIQUE," +
                    "ownerUUID CHAR(36) NOT NULL," +
                    "empireId INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," + 
                    "isPublic BOOLEAN);").execute();
            
            // list of cities under an empire
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireCities (" +
                    "cityId INT NOT NULL," +
                    "empireId INT PRIMARY KEY NOT NULL);").execute();

            // list of members in an empire 
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS empireMembers (" +
                    "uuid CHAR(36) PRIMARY KEY NOT NULL," +
                    "empireName VARCHAR(50) NOT NULL);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Collection<Empire> getEmpires() {
        var empires = new ArrayList<Empire>();
        try (Connection conn = SQLConn.getConnection()) {
            var results = conn.prepareStatement("SELECT * FROM empires;").executeQuery();
            while (results.next()) {
                String name = results.getString("empireName");
                boolean isPublic = results.getBoolean("isPublic");
                String owner = results.getString("ownerUUID");
                Party party = partyHandler.getParty(UUID.fromString(owner));
                empires.add(new Empire(name, isPublic, owner, party));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empires;
    }

    /**
     * get a list of all uuids of members in a party
     */
    /*public Collection<UUID> getMembers(String name) {
        var members = new ArrayList<UUID>();
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT uuid FROM empireMembers WHERE empireName=?;");
            stmt.setString(1, acronym.str);
            var results = stmt.executeQuery();
            while (results.next()) {
                members.add(UUID.fromString(results.getString(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }*/

 
    public Collection<String> getEmpireMembers(String name) {
        var members = new ArrayList<String>();
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT usernames.username FROM empireMembers INNER JOIN usernames ON empireMembers.uuid = usernames.uuid WHERE empireName=?;");
            stmt.setString(1, name);
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
            var stmt = conn.prepareStatement("SELECT * FROM parties WHERE ownerUUID=?;");
            stmt.setString(1, uuid.toString());
            var results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createEmpire(UUID owner, String name, City... cities) {
        try (Connection conn = SQLConn.getConnection()) {
            /*var stmt = conn.prepareStatement("INSERT INTO parties VALUES (?, ?, ?, ?, ?, ?);");
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
            return stmt.executeUpdate() > 0;*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean leaveEmpire(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM empireMembers WHERE uuid=?;");
            stmt.setString(1, player.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disbandEmpire(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            //TODO stuff here

            /*var party = this.getParty(player);
            var stmt = conn.prepareStatement("DELETE FROM parties WHERE ownerUUID=?;");
            stmt.setString(1, player.toString());
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement("DELETE FROM partyMembers WHERE acronym=?;");
            stmt.setString(1, party.acronym.str);
            stmt.executeUpdate();*/
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean joinEmpire(UUID player, String name) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("REPLACE INTO empireMembers VALUES (?, ?);");
            stmt.setString(1, player.toString());
            stmt.setString(2, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getName(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT uuid FROM empireMembers WHERE empireName=?;");
            stmt.setString(1, player.toString());
            var results = stmt.executeQuery();
            if (results.next()) {
                return SQLConn.getUsername(UUID.fromString(results.getString("uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Empire getEmpire(UUID player) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM empires WHERE empireName=(SELECT empireName FROM empireMembers WHERE uuid=?);");
            stmt.setString(1, player.toString());
            var results = stmt.executeQuery();
            if (results.next()) {
                String name = results.getString("empireName");
                boolean isPublic = results.getBoolean("isPublic");
                String owner = results.getString("ownerUUID");
                Party party = partyHandler.getParty(UUID.fromString(owner));
                return new Empire(name, isPublic, owner, party);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean empireExists(String name) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM empires WHERE acronym=?;");
            stmt.setString(1, name);
            var results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean kickMember(UUID owner, UUID toKick) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM empireMembers WHERE uuid=? AND empireName = (SELECT empireName FROM empires WHERE ownerUUID=?);");
            stmt.setString(1, toKick.toString());
            stmt.setString(2, owner.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<Boolean> isPartyPublic(String name) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT isPublic FROM empires WHERE empireName=?");
            stmt.setString(1, name);
            var res = stmt.executeQuery();
            if (res.next()) {
                return Optional.of(res.getBoolean("isPublic"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * rename an empire
     * @param uuid owner uuid
     * @param newName the new name
     * @return if it worked or not 
     */
    public boolean rename(UUID uuid, String oldName, String newName) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE empires SET empireName=? WHERE ownerUUID=?;");
            var stmt2 = conn.prepareStatement("UPDATE empireMembers SET empireName=? WHERE empireName=?;");
            stmt.setString(1, newName);
            stmt.setString(2, uuid.toString());
            stmt2.setString(1, newName);
            stmt2.setString(2, oldName);
            stmt2.execute();
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setOwner(UUID oldUUID, UUID newUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE parties SET ownerUUID=? WHERE ownerUUID=?;");
            stmt.setString(1, newUUID.toString());
            stmt.setString(2, oldUUID.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static class Empire {
        public final String name, owner;
        public final Party party;
        public final boolean isPublic;

        public Empire(String name, boolean isPublic, String owner, Party party) {
            this.name = name;
            this.isPublic = isPublic;
            this.owner = owner;
            this.party = party;
        }
    }
}
