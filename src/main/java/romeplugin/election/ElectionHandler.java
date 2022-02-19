package romeplugin.election;

import org.bukkit.plugin.Plugin;
import romeplugin.MessageConstants;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;
import romeplugin.empires.EmpireHandler;
import romeplugin.messaging.NotificationQueue;
import romeplugin.title.Title;
import romeplugin.title.TitleHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class ElectionHandler {
    // list of every required title
    public static final Title[] RUNNABLE_TITLES = {
            Title.TRIBUNE,
            Title.AEDILE,
            Title.PRAETOR,
            Title.CONSUL
    };

    //don't make one of these greater than 12 characters :D
    public enum ElectionPhase {
        RUNNING,
        VOTING
    }

    private final NotificationQueue notifications;
    private final TitleHandler titleHandler;
    private final Plugin plugin;

    public ElectionHandler(NotificationQueue notifications, Plugin plugin, TitleHandler titleHandler) {
        this.notifications = notifications;
        this.titleHandler = titleHandler;
        this.plugin = plugin;
        this.initialize();
    }

    public void initialize() {
        try (Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS candidates (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "title " + RomePlugin.TITLE_ENUM + " NOT NULL," +
                    "empireUUID CHAR(36) NOT NULL," + 
                    "votes INT NOT NULL DEFAULT 0);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionNumber (" + 
                    "number INT DEFAULT 1 NOT NULL PRIMARY KEY,"+
                    "empireUUID CHAR(36) NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionPhase (" +
                    "phase ENUM('RUNNING', 'VOTING', 'NULL') DEFAULT 'NULL' PRIMARY KEY," +
                    "empireUUID CHAR(36) NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionResults (" +
                    "number INT NOT NULL DEFAULT 0 PRIMARY KEY," +
                    "title " + RomePlugin.TITLE_ENUM + " NOT NULL," +
                    "uuid CHAR(36) NOT NULL," +
                    "empireUUID CHAR(36) NOT NULL," +
                    "votes INT NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS playerVotes (" +
                    "uuid CHAR(36) NOT NULL," +
                    "titleVotedFor " + RomePlugin.TITLE_ENUM + " NOT NULL," +
                    "empireUUID CHAR(36) NOT NULL);").execute();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    public boolean hasElection(UUID empireUUID) {
        return this.getElectionPhase(empireUUID) != null;
    }

    public ElectionPhase getElectionPhase(UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT phase FROM electionPhase WHERE empireUUID = ?;");
            stmt.setString(1, empireUUID.toString());
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

    public int getElectionNumber(UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT number FROM electionNumber WHERE empireUUID = ?;");
            stmt.setString(1, empireUUID.toString());
            var currInfo = stmt.executeQuery();
            if (currInfo.next()) {
                return currInfo.getInt("number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void incrementElectionNumber(UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("UPDATE electionNumber SET number = number + 1 WHERE empireUUID = ?;");
            stmt.setString(1, empireUUID.toString());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Collection<Candidate> getCandidates(UUID empireUUID) {
        var candidates = new ArrayList<Candidate>();
        try (Connection conn = SQLConn.getConnection()) {
            var currInfo = conn.prepareStatement("SELECT * FROM candidates WHERE empireUUID = ?;");
            currInfo.setString(1, empireUUID.toString());
            var results = currInfo.executeQuery();
            while (results.next()) {
                var uuid = UUID.fromString(results.getString("uuid"));
                var title = Title.getTitle(results.getString("title"));
                var candidate = new Candidate(uuid, title);
                candidate.setVotes(results.getInt("votes"));
                candidates.add(candidate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return candidates;
    }

    public boolean vote(UUID voter, UUID candidate, UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var title = conn.prepareStatement("SELECT title FROM candidates WHERE uuid = ? AND empireUUID = ?;");
            title.setString(1, candidate.toString());
            title.setString(2, empireUUID.toString());
            var results = title.executeQuery();
            if (!results.next()) return false;
            var preparedStatement = conn.prepareStatement("REPLACE INTO playerVotes VALUES (?, ?, ?);");
            preparedStatement.setString(1, voter.toString());
            preparedStatement.setString(2, results.getString("title"));
            preparedStatement.setString(3, empireUUID.toString());
            preparedStatement.execute();

            var stmt = conn.prepareStatement("UPDATE candidates SET votes = votes + 1 WHERE uuid = ? AND empireUUID = ?;");
            stmt.setString(1, candidate.toString());
            stmt.setString(2, empireUUID.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeCandidate(UUID uuid, UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM candidates WHERE uuid = ? AND empireUUID = ?;");
            stmt.setString(1, uuid.toString());
            stmt.setString(2, empireUUID.toString());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCandidate(UUID uuid, Title title, UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var preparedStatement = conn.prepareStatement("REPLACE INTO candidates VALUES (?, ?, ?, ?);");

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, title.toString());
            preparedStatement.setString(3, empireUUID.toString());
            preparedStatement.setInt(4, 0);
            preparedStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setElectionPhase(ElectionPhase phase, UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            String phaseStr = phase == null ? "NULL" : phase.toString();
            var stmt = conn.prepareStatement("UPDATE electionPhase SET phase = ? where empireUUID = ?;");
            stmt.setString(1, phaseStr);
            stmt.setString(2, empireUUID.toString());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startElection(UUID empireUUID) {
        this.setElectionPhase(ElectionPhase.RUNNING, empireUUID);
        notifications.broadcastNotification(MessageConstants.SUCCESSFUL_ELECTION_START);
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_ELECTION_START);
    }

    /**
     * begin the voting phase in the election
     */
    public void startVoting(UUID empireUUID) {
        this.setElectionPhase(ElectionPhase.VOTING, empireUUID);
        notifications.broadcastNotification(MessageConstants.SUCCESSFUL_VOTING_START);
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_VOTING_START);
    }

    private Collection<Candidate> getWinners(Collection<Candidate> candidates) {
        Collection<Candidate> winners = new ArrayList<>();

        for (Title title : ElectionHandler.RUNNABLE_TITLES) {
            candidates.stream()
                    .filter(c -> c.getTitle() == title)
                    .max(Candidate::compareTo)
                    .ifPresent(winners::add);
        }

        return winners;
    }

    /**
     * ends the election, adding all titles to the winners and updating the election state/results
     */
    public void endElection(UUID empireUUID) {
        var results = this.getWinners(this.getCandidates(empireUUID));

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

        //Apply all titles to each winner
        results.forEach(winner -> titleHandler.setTitle(winner.getUniqueId(), winner.getTitle()));

        this.storeElectionResults(results, empireUUID);

        this.incrementElectionNumber(empireUUID);
        this.setElectionPhase(null, empireUUID);
        this.clearTempTables(empireUUID);

        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_ENDED);
    }

    public void cancelElection(UUID empireUUID) {
        this.setElectionPhase(null, empireUUID);
        this.clearTempTables(empireUUID);
        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_CANCELLED);
    }

    public void clearTempTables(UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            var stmt1 = conn.prepareStatement("DELETE FROM candidates WHERE empireUUID=?;");
            stmt1.setString(1, empireUUID.toString());
            stmt1.execute();
            var stmt2 = conn.prepareStatement("DELETE FROM playerVotes WHERE empireUUID=?;");
            stmt2.setString(1, empireUUID.toString());
            stmt2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * checks if you've voted for a specific title yet
     *
     * @param player
     * @param title
     * @return
     */
    public boolean alreadyVoted(UUID player, Title title, UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            return conn.prepareStatement("SELECT * FROM playerVotes WHERE uuid = '" + player.toString() + "' AND titleVotedFor = '" + title.toString() + "';").executeQuery().next();
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
    public Collection<Candidate> getElectionResults(int number, UUID empireUUID) {
        Collection<Candidate> readResults = new ArrayList<>();

        try (Connection conn = SQLConn.getConnection()) {
            var results = conn.prepareStatement("SELECT * FROM electionResults WHERE number = " + number + ";").executeQuery();

            //add all winners of the previous election to readResults
            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                Title title = Title.getTitle(results.getString("title"));
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
    public void storeElectionResults(Collection<Candidate> results, UUID empireUUID) {
        try (Connection conn = SQLConn.getConnection()) {
            for (Candidate winner : results) {
                var statement = conn.prepareStatement("INSERT INTO electionResults VALUES (?, ?, ?, ?, ?);");
                statement.setInt(1, this.getElectionNumber(empireUUID));
                statement.setString(2, winner.getTitle().toString());
                statement.setString(3, winner.getUniqueId().toString());
                statement.setString(4, empireUUID.toString());
                statement.setInt(5, winner.getVotes());
                statement.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "unable to store election results in the database! uh oh");
        }
    }

    public Optional<Candidate> getCandidate(UUID toVote, UUID empireUUID) {
        return this.getCandidates(empireUUID)
                .stream()
                .filter(c -> c.getUniqueId().equals(toVote))
                .findFirst();
    }

    public boolean hasCandidate(UUID candidate, UUID empireUUID) {
        return this.getCandidate(candidate, empireUUID).isPresent();
    }
}
