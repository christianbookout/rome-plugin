package romeplugin.election;

import org.bukkit.plugin.Plugin;
import romeplugin.MessageConstants;
import romeplugin.RomePlugin;
import romeplugin.database.SQLConn;
import romeplugin.title.Title;
import romeplugin.title.TitleHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
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

    private TitleHandler titleHandler;
    private final Plugin plugin;

    public ElectionHandler(Plugin plugin, TitleHandler titleHandler) {
        this.titleHandler = titleHandler;
        this.plugin = plugin;
    }

    public boolean hasElection() {
        return this.getElectionPhase() != null;
    }

    //Initialize all of the tables
    private void initCandidatesTable(Connection conn) throws SQLException {
        conn.prepareStatement("CREATE TABLE IF NOT EXISTS candidates (" +
                        "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                        "title " + RomePlugin.TITLE_ENUM + " NOT NULL," +
                        "votes INT NOT NULL DEFAULT 0);").execute();
    }

    private void initNumberTable(Connection conn) throws SQLException {
        conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionNumber (number INT DEFAULT 1 NOT NULL PRIMARY KEY);").execute();
        if (!conn.prepareStatement("SELECT * FROM electionNumber;").executeQuery().next())
            conn.prepareStatement("INSERT INTO electionNumber VALUES ();");
    }

    private void initPhaseTable(Connection conn) throws SQLException{
        conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionPhase (phase ENUM('RUNNING', 'VOTING', 'NULL') DEFAULT 'NULL' PRIMARY KEY);").execute();
        if (!conn.prepareStatement("SELECT * FROM electionPhase;").executeQuery().next())
            conn.prepareStatement("INSERT INTO electionPhase VALUES ();");
    }

    private void initResultsTable(Connection conn) throws SQLException{
        conn.prepareStatement("CREATE TABLE IF NOT EXISTS electionResults (" +
                        "number INT NOT NULL DEFAULT 0 PRIMARY KEY," +
                        "title " + RomePlugin.TITLE_ENUM + " NOT NULL," +
                        "uuid CHAR(36) NOT NULL," +
                        "votes INT NOT NULL);").execute();
    }

    private void initVotesTable(Connection conn) throws SQLException{
        conn.prepareStatement("CREATE TABLE IF NOT EXISTS playerVotes (" +
                        "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                        "titleVotedFor " + RomePlugin.TITLE_ENUM + " NOT NULL);");
    }

    public ElectionPhase getElectionPhase() {
        try (Connection conn = SQLConn.getConnection()) {
            this.initPhaseTable(conn);
            var currInfo = conn.prepareStatement("SELECT phase FROM electionPhase;").executeQuery();
            if (currInfo.next()) {
                String phase = currInfo.getString("phase");
                return phase == null || phase.equals("NULL") ? null : ElectionPhase.valueOf(phase);
            }
        } catch (SQLException e) {}
        return null;
    }

    public int getElectionNumber() {
        try (Connection conn = SQLConn.getConnection()) {
            this.initNumberTable(conn);
            var currInfo = conn.prepareStatement("SELECT number FROM electionNumber;").executeQuery();
            if (currInfo.next()) {
                return currInfo.getInt("number");
            }
        } catch (SQLException e) {}
        return 1;
    }

    public void incrementElectionNumber() {
        try (Connection conn = SQLConn.getConnection()) {
            this.initNumberTable(conn);
            conn.prepareStatement("UPDATE electionNumber SET number = number + 1;").execute();
        } catch (SQLException e) {}
    }


    public Collection<Candidate> getCandidates() {
        var candidates = new ArrayList<Candidate>();
        try (Connection conn = SQLConn.getConnection()) {
            this.initCandidatesTable(conn);

            var currInfo = conn.prepareStatement("SELECT * FROM candidates;").executeQuery();
            while (currInfo.next()) {
                var uuid = UUID.fromString(currInfo.getString("uuid"));
                var title = Title.getTitle(currInfo.getString("title"));
                var candidate = new Candidate(uuid, title);
                candidate.setVotes(currInfo.getInt("votes"));
                candidates.add(candidate);
            }
        } catch (SQLException e) {}
        return candidates;
    }

    public boolean vote(UUID voter, UUID candidate) {
        try (Connection conn = SQLConn.getConnection()) {
            this.initCandidatesTable(conn);

            var currInfo = conn.prepareStatement("UPDATE candidates SET votes = votes + 1 WHERE uuid = '" + candidate.toString() + "';").executeQuery();
            return currInfo.next();
        } catch (SQLException e) {}
        return false;
    }

    public void removeCandidate(UUID uuid) {
        try (Connection conn = SQLConn.getConnection()) {
            this.initCandidatesTable(conn);
            conn.prepareStatement("DELETE FROM candidates WHERE uuid = '" + uuid.toString() + "';").execute();
        } catch (SQLException e) {}
    }

    public void addCandidate(UUID uuid, Title title) {

        try (Connection conn = SQLConn.getConnection()) {
            this.initCandidatesTable(conn);

            var preparedStatement = conn.prepareStatement("REPLACE INTO candidates VALUES (?, ?, ?);");

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, title.toString());
            preparedStatement.setInt(3, 0);
            preparedStatement.execute();

        } catch (SQLException e) {}
    }

    public void setElectionPhase(ElectionPhase phase) {
        try (Connection conn = SQLConn.getConnection()) {
            this.initPhaseTable(conn);
            String phaseStr = phase == null ? "NULL" : phase.toString();
            conn.prepareStatement("UPDATE electionPhase SET phase = '" + phaseStr + "';").execute();
        } catch (SQLException e) {}
    }

    public void startElection() {
        try (Connection conn = SQLConn.getConnection()) {
            this.initCandidatesTable(conn);
            this.setElectionPhase(ElectionPhase.RUNNING);
        } catch (SQLException e) {}
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_ELECTION_START);
    }

    /**
     * begin the voting phase in the election
     */
    public void startVoting() {
        try (Connection conn = SQLConn.getConnection()) {
            this.initVotesTable(conn);
            this.setElectionPhase(ElectionPhase.VOTING);
        } catch (SQLException e) {}
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_VOTING_START);
    }

    private Collection<Candidate> getWinners(Collection<Candidate> candidates) {
        HashMap<Title, Collection<Candidate>> splitCandidates = new HashMap<>(); 
        
        for (Title title: ElectionHandler.RUNNABLE_TITLES)
            splitCandidates.put(title, new ArrayList<>());
        
        candidates.forEach(c -> 
            splitCandidates.get(c.getTitle()).add(c)
        );

        Collection<Candidate> winners = new ArrayList<>();

        var values = splitCandidates.values();
        values.forEach(collection -> {
            if (!collection.isEmpty())
                winners.add(Collections.max(collection));
        });
        
        return winners;
    }

    /**
     * ends the election, adding all titles to the winners and updating the election state/results
     */
    public void endElection() { //TODO truncate electionResults 
        var results = this.getWinners(this.getCandidates());

        //Apply all titles to each winner
        results.forEach(winner -> titleHandler.setTitle(winner.getUniqueId(), winner.getTitle()));

        this.storeElectionResults(results);
        
        this.incrementElectionNumber();
        this.setElectionPhase(null);
        this.clearTempTables();

        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_ENDED);
    }

    public void cancelElection() {
        this.setElectionPhase(null);
        this.clearTempTables();
        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_CANCELLED);
    }

    public void clearTempTables() {
        try (Connection conn = SQLConn.getConnection()) {
            this.initCandidatesTable(conn);
            this.initVotesTable(conn);
            conn.prepareStatement("TRUNCATE TABLE candidates;").execute();
            conn.prepareStatement("TRUNCATE TABLE playerVotes;").execute();
        } catch (SQLException e) {}
    }

    /**
     * checks if you've voted for a specific title yet
     * @param player
     * @param title
     * @return
     */
    public boolean alreadyVoted(UUID player, Title title) {
        try (Connection conn = SQLConn.getConnection()) {
            this.initVotesTable(conn);
            var statement = conn.prepareStatement("SELECT * FROM playerVotes WHERE uuid = '" + player.toString() + "' AND titleVotedFor = '" + title.toString() + "';");
            var results = statement.executeQuery();
            return results.next();
        } catch (SQLException e) {}
        return false;
    }

    /**
     * gets election results from the database
     *
     * @return a collection of the previous election's winners
     */
    public Collection<Candidate> getElectionResults(int number) {

        Collection<Candidate> readResults = new ArrayList<>();

        try (Connection conn = SQLConn.getConnection()) {
            this.initResultsTable(conn);
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
    public void storeElectionResults(Collection<Candidate> results) {
        try (Connection conn = SQLConn.getConnection()) {
            for (Candidate winner : results) {
                this.initResultsTable(conn);
                var statement = conn.prepareStatement("INSERT INTO electionResults VALUES (?, ?, ?, ?);");
                statement.setInt(1, this.getElectionNumber());
                statement.setString(2, winner.getTitle().toString());
                statement.setString(3, winner.getUniqueId().toString());
                statement.setInt(4, winner.getVotes());
                statement.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "unable to store election results in the database! uh oh");
        }
    }

    public Optional<Candidate> getCandidate(UUID toVote) {
        return this.getCandidates()
                   .stream()
                   .filter(c -> c.getUniqueId().equals(toVote))
                   .findFirst();
    }

    public boolean hasCandidate(UUID candidate) {
        return this.getCandidate(candidate).isPresent();
    }
}
