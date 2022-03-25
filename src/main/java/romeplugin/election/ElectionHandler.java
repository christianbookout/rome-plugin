package romeplugin.election;

import org.bukkit.plugin.Plugin;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.empires.role.Role;
import romeplugin.empires.role.RoleHandler;
import romeplugin.messaging.NotificationQueue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ElectionHandler {
    //don't make one of these greater than 12 characters :D
    public enum ElectionPhase {
        RUNNING,
        VOTING
    }

    private final NotificationQueue notifications;
    private final RoleHandler roleHandler;
    private final Plugin plugin;

    public ElectionHandler(NotificationQueue notifications, Plugin plugin, RoleHandler roleHandler) {
        this.notifications = notifications;
        this.plugin = plugin;
        this.roleHandler = roleHandler;
        this.initialize();
    }

    public void initialize() {
        try (Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS candidates (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "roleId INT UNSIGNED NOT NULL," +
                    "empireId INT UNSIGNED NOT NULL," +
                    "votes INT NOT NULL DEFAULT 0);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionNumber (" +
                    "number INT DEFAULT 1 NOT NULL PRIMARY KEY," +
                    "empireId INT UNSIGNED NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionPhase (" +
                    "phase ENUM('RUNNING', 'VOTING', 'NULL') DEFAULT 'NULL' PRIMARY KEY," +
                    "empireId INT UNSIGNED NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionResults (" +
                    "number INT NOT NULL DEFAULT 0 PRIMARY KEY," +
                    "roleId INT UNSIGNED NOT NULL," +
                    "uuid CHAR(36) NOT NULL," +
                    "empireId INT UNSIGNED NOT NULL," +
                    "votes INT NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS playerVotes (" +
                    "uuid CHAR(36) NOT NULL," +
                    "roleVotedFor INT UNSIGNED NOT NULL," +
                    "empireId INT UNSIGNED NOT NULL);").execute();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    public boolean hasElection(int empireId) {
        return this.getElectionPhase(empireId) != null;
    }

    public ElectionPhase getElectionPhase(int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT phase FROM electionPhase WHERE empireId = ?;");
            stmt.setInt(1, empireId);
            var currInfo = stmt.executeQuery();
            if (currInfo.next()) {
                String phase = currInfo.getString("phase");
                return phase == null || phase.equals("NULL") ? null : ElectionPhase.valueOf(phase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getElectionNumber(int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT number FROM electionNumber WHERE empireId = ?;");
            stmt.setInt(1, empireId);
            var currInfo = stmt.executeQuery();
            if (currInfo.next()) {
                return currInfo.getInt("number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void incrementElectionNumber(int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE electionNumber SET number = number + 1 WHERE empireId = ?;");
            stmt.setInt(1, empireId);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Collection<Candidate> getCandidates(int empireId) {
        var candidates = new ArrayList<Candidate>();
        try (Connection conn = SQLConn.getConnection()) {
            var currInfo = conn.prepareStatement("SELECT * FROM candidates WHERE empireId = ?;");
            currInfo.setInt(1, empireId);
            var results = currInfo.executeQuery();
            while (results.next()) {
                var uuid = UUID.fromString(results.getString("uuid"));
                var title = roleHandler.getRoleById(results.getInt("roleId"));
                var candidate = new Candidate(uuid, title);
                candidate.setVotes(results.getInt("votes"));
                candidates.add(candidate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return candidates;
    }

    public boolean vote(UUID voter, UUID candidate, int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var title = conn.prepareStatement("SELECT title FROM candidates WHERE uuid = ? AND empireId = ?;");
            title.setString(1, candidate.toString());
            title.setInt(2, empireId);
            var results = title.executeQuery();
            if (!results.next()) return false;
            var preparedStatement = conn.prepareStatement("REPLACE INTO playerVotes VALUES (?, ?, ?);");
            preparedStatement.setString(1, voter.toString());
            preparedStatement.setString(2, results.getString("title"));
            preparedStatement.setInt(3, empireId);
            preparedStatement.execute();

            var stmt = conn.prepareStatement("UPDATE candidates SET votes = votes + 1 WHERE uuid = ? AND empireId = ?;");
            stmt.setString(1, candidate.toString());
            stmt.setInt(2, empireId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeCandidate(UUID uuid, int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM candidates WHERE uuid = ? AND empireId = ?;");
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, empireId);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCandidate(UUID uuid, Role role, int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var preparedStatement = conn.prepareStatement("REPLACE INTO candidates VALUES (?, ?, ?, ?);");

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, role.id);
            preparedStatement.setInt(3, empireId);
            preparedStatement.setInt(4, 0);
            preparedStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setElectionPhase(ElectionPhase phase, int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            String phaseStr = phase == null ? "NULL" : phase.toString();
            var stmt = conn.prepareStatement("UPDATE electionPhase SET phase = ? where empireId = ?;");
            stmt.setString(1, phaseStr);
            stmt.setInt(2, empireId);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startElection(int empireId) {
        this.setElectionPhase(ElectionPhase.RUNNING, empireId);
        notifications.broadcastNotification(MessageConstants.SUCCESSFUL_ELECTION_START);
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_ELECTION_START);
    }

    /**
     * begin the voting phase in the election
     */
    public void startVoting(int empireId) {
        this.setElectionPhase(ElectionPhase.VOTING, empireId);
        notifications.broadcastNotification(MessageConstants.SUCCESSFUL_VOTING_START);
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_VOTING_START);
    }

    private Collection<Candidate> getWinners(Collection<Candidate> candidates) {
        var winning_candidates = new HashMap<Role, Candidate>();

        for (Candidate candidate : candidates) {
            int winning_votes = -1;
            var current_winner = winning_candidates.get(candidate.getRole());
            if (current_winner != null) {
                winning_votes = current_winner.getVotes();
            }
            if (candidate.getVotes() > winning_votes) {
                winning_candidates.put(candidate.getRole(), candidate);
            }
        }

        return winning_candidates.values();
    }

    /**
     * ends the election, adding all titles to the winners and updating the election state/results
     */
    public void endElection(int empireId) {
        var results = this.getWinners(this.getCandidates(empireId));

        /* TODO: somehow reimplement this?
        for (var uuid : SQLConn.getUUIDsWithTitle(Title.CONSUL)) {
            titleHandler.setTitle(uuid, Title.CENSOR);
        }

        for (var uuid : SQLConn.getUUIDsWithTitle(Title.CENSOR)) {
            if (results.stream().noneMatch(winner -> winner.getUniqueId().equals(uuid))) {
                titleHandler.setTitle(uuid, Title.QUAESTOR);
            }
        }

        for (var uuid : SQLConn.getUUIDsWithTitle(Title.TRIBUNE)) {
            titleHandler.setTitle(uuid, Title.QUAESTOR);
        }

        for (var uuid : SQLConn.getUUIDsWithTitle(Title.PRAETOR)) {
            titleHandler.setTitle(uuid, Title.QUAESTOR);
        }

        for (var uuid : SQLConn.getUUIDsWithTitle(Title.AEDILE)) {
            titleHandler.setTitle(uuid, Title.QUAESTOR);
        }
        */

        //Apply all titles to each winner
        results.forEach(winner -> roleHandler.setPlayerRole(winner.getUniqueId(), winner.getRole()));

        this.storeElectionResults(results, empireId);

        this.incrementElectionNumber(empireId);
        this.setElectionPhase(null, empireId);
        this.clearTempTables(empireId);

        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_ENDED);
    }

    public void cancelElection(int empireId) {
        this.setElectionPhase(null, empireId);
        this.clearTempTables(empireId);
        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_CANCELLED);
    }

    public void clearTempTables(int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt1 = conn.prepareStatement("DELETE FROM candidates WHERE empireId=?;");
            stmt1.setInt(1, empireId);
            stmt1.execute();
            var stmt2 = conn.prepareStatement("DELETE FROM playerVotes WHERE empireId=?;");
            stmt2.setInt(1, empireId);
            stmt2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * checks if you've voted for a specific title yet
     *
     * @param player
     * @param role
     * @return
     */
    public boolean alreadyVoted(UUID player, Role role, int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM playerVotes WHERE uuid = ? AND titleVotedFor = ? AND empireId = ?;");
            stmt.setString(1, player.toString());
            stmt.setInt(2, role.id);
            stmt.setInt(3, empireId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * gets election results from the database
     *
     * @return a collection of the previous election's winners
     */
    public Collection<Candidate> getElectionResults(int number, int empireId) {
        Collection<Candidate> readResults = new ArrayList<>();

        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM electionResults WHERE number = ? AND empireId = ?;");
            stmt.setInt(1, number);
            stmt.setInt(2, empireId);
            var results = stmt.executeQuery();
            //add all winners of the previous election to readResults
            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                Role title = roleHandler.getRoleById(results.getInt("title"));
                Candidate candidate = new Candidate(uuid, title);
                candidate.setVotes(results.getInt("votes"));
                readResults.add(candidate);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "unable to read election results from the database");
        }
        return readResults;
    }


    /**
     * store the current election results into the database
     */
    public void storeElectionResults(Collection<Candidate> results, int empireId) {
        try (Connection conn = SQLConn.getConnection()) {
            for (Candidate winner : results) {
                var statement = conn.prepareStatement("INSERT INTO electionResults VALUES (?, ?, ?, ?, ?);");
                statement.setInt(1, this.getElectionNumber(empireId));
                statement.setInt(2, winner.getRole().id);
                statement.setString(3, winner.getUniqueId().toString());
                statement.setInt(4, empireId);
                statement.setInt(5, winner.getVotes());
                statement.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "unable to store election results in the database! uh oh");
        }
    }

    public Optional<Candidate> getCandidate(UUID toVote, int empireId) {
        return this.getCandidates(empireId)
                .stream()
                .filter(c -> c.getUniqueId().equals(toVote))
                .findFirst();
    }

    public boolean hasCandidate(UUID candidate, int empireId) {
        return this.getCandidate(candidate, empireId).isPresent();
    }
}
