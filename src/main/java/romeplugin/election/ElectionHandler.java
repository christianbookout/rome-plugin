package romeplugin.election;

import org.bukkit.plugin.Plugin;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.title.Title;
import romeplugin.title.TitleHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.logging.Level;

public class ElectionHandler {
    // list of every required title
    private static final Title[] RUNNABLE_TITLES = {
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

    private Election currentElection;
    private TitleHandler titleHandler;
    private ElectionPhase currentPhase;
    private final Plugin plugin;

    //The number of elections that have occurred (represents the current election, eg if 2 elections have occurred and one more is happening then it's the 3rd election);
    private int electionNum;

    private Collection<Candidate> results;

    public ElectionHandler(Plugin plugin, TitleHandler titleHandler) {
        this.titleHandler = titleHandler;
        this.plugin = plugin;
        this.currentPhase = null;
        this.currentElection = null;
        this.results = null;
        this.electionNum = 0;
    }

    public boolean hasElection() {
        return this.currentElection != null;
    }

    public ElectionPhase getElectionPhase() {
        return this.currentPhase;
    }

    public Election getCurrentElection() {
        return currentElection;
    }

    public boolean vote(UUID voter, UUID candidate) {

        Title title = currentElection.getCandidate(candidate).getTitle();

        if (alreadyVoted(voter, title)) return false;

        if (this.currentPhase != ElectionPhase.VOTING) return false;

        return currentElection.vote(candidate);
    }

    public boolean removeCandidate(UUID uuid) {
        if (this.currentPhase != ElectionPhase.RUNNING) return false;
        Candidate candidate = currentElection
                .getCandidates()
                .stream()
                .filter(c -> c.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);

        if (candidate == null) return false;
        unstoreCandidate(uuid);
        return currentElection.removeCandidate(candidate);
    }

    public boolean addCandidate(UUID uuid, Title title) {
        if (this.currentPhase != ElectionPhase.RUNNING) return false;

        Candidate toAdd = new Candidate(uuid, title);

        this.storeCandidate(uuid, title);

        this.currentElection.addCandidate(toAdd);
        return true;
    }

    private void unstoreCandidate(UUID uuid) {
        try (Connection conn = SQLConn.getConnection()) {

            conn.prepareStatement("DELETE FROM election WHERE uuid = '" + uuid.toString() + "';").execute();

        } catch (SQLException e) {
        }
    }

    private void storeCandidate(UUID uuid, Title title) {

        try (Connection conn = SQLConn.getConnection()) {

            var preparedStatement = conn.prepareStatement("REPLACE INTO election VALUES (?, ?, ?, ?);");

            //set 1: uuid, 2: username, 3: title, 4: votes
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, SQLConn.getUsername(uuid));
            preparedStatement.setString(3, title.toString());
            preparedStatement.setInt(4, 0);
            preparedStatement.execute();

        } catch (SQLException e) {
        }
    }

    public void startElection() {
        this.currentElection = new Election();
        this.currentPhase = ElectionPhase.RUNNING;
        this.updateElectionState();
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_ELECTION_START);
    }

    /**
     * begin the voting phase in the election
     */
    public void startVoting() {
        // verify there is a candidate for every position
        Set<Title> filledTitles = new HashSet<>();
        for (var candidate : currentElection.getCandidates()) {
            filledTitles.add(candidate.getTitle());
        }
        for (var title : RUNNABLE_TITLES) {
            if (!filledTitles.contains(title)) {
                // TODO: tell the user not all positions are filled.
                return;
            }
        }

        this.currentPhase = ElectionPhase.VOTING;
        this.updateElectionState();
        plugin.getServer().broadcastMessage(MessageConstants.SUCCESSFUL_VOTING_START);
    }

    /**
     * ends the election, adding all titles to the winners and updating the election state/results
     */
    public void endElection() {
        this.results = this.currentElection.endElection();

        //Apply all titles to each winner
        this.results.forEach(winner -> titleHandler.setTitle(winner.getUniqueId(), winner.getTitle()));

        this.currentElection = null;
        this.currentPhase = null;
        this.electionNum++;

        this.updateElectionState();
        this.storeElectionResults();

        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_ENDED);
    }

    //TODO: implement electionNum in a good way
    public void clearElectionTable() {
        try (Connection conn = SQLConn.getConnection()) {
            var results = conn.prepareStatement("SELECT number FROM election;").executeQuery();
            if (results.next())
                this.electionNum = results.getInt("number");
            conn.prepareStatement("TRUNCATE TABLE election;");
        } catch (SQLException e) {

        }
    }

    /**
     * checks if you've voted for a specific title yet
     * @param player
     * @param title
     * @return
     */
    private boolean alreadyVoted(UUID player, Title title) {
        try (Connection conn = SQLConn.getConnection()) {
            var statement = conn.prepareStatement("SELECT uuid, titleVotedFor FROM playerVotes WHERE uuid = '" + player.toString() + "' AND titleVotedFor = '" + title.toString() + "';");
            var results = statement.executeQuery();
            return results.next();
        } catch (SQLException e) {
        }
        return false;
    }

    /**
     * initializes the current election state, number and candidates/votes if applicable
     */
    public void initialize() {
        try (Connection conn = SQLConn.getConnection()) {

            this.readElectionState();

            if (this.currentPhase != null) return;

            this.currentElection = new Election();

            var statement = conn.prepareStatement("SELECT uuid, username, title, votes, number, phase FROM election;");
            var results = statement.executeQuery();

            while (results.next()) {
                String phase = results.getString("phase");
                this.currentPhase = phase != null ? ElectionPhase.valueOf(phase) : null;
                this.electionNum = results.getInt("number");

                UUID uuid = UUID.fromString(results.getString("uuid"));
                Title title = Title.getTitle(results.getString("title"));

                Candidate candidate = new Candidate(uuid, title);
                candidate.setVotes(results.getInt("votes"));
                this.currentElection.addCandidate(candidate);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "can't retrieve election info from database");
        }

    }

    /**
     * reads the current election phase and number from the database
     */
    public void readElectionState() {
        try (Connection conn = SQLConn.getConnection()) {
            var currInfo = conn.prepareStatement("SELECT number, phase FROM election").executeQuery();
            if (currInfo.next()) {
                this.electionNum = currInfo.getInt("number");
                String phase = currInfo.getString("phase");
                this.currentPhase = phase != null ? ElectionPhase.valueOf(phase) : null;
            }
        } catch (SQLException e) {
        }
    }

    /**
     * updates the electionState table to represent current election number and phase
     */
    public void updateElectionState() {
        try (Connection conn = SQLConn.getConnection()) {
            var statement = conn.prepareStatement("REPLACE INTO election (?, ?)");
            statement.setInt(1, this.electionNum);
            if (this.currentPhase == null) {
                statement.setNull(2, Types.OTHER);
            } else {
                statement.setString(2, this.currentPhase.toString());
            }
            statement.execute();
        } catch (SQLException e) {
        }
    }

    /**
     * returns or lazily creates the results from the previous election from the database
     *
     * @return a collection of the previous election's winners
     */
    public Collection<Candidate> getElectionResults() {
        if (this.results != null) {
            return results;
        } else {
            Collection<Candidate> readResults = new ArrayList<>();

            try (Connection conn = SQLConn.getConnection()) {

                var statement = conn.prepareStatement("SELECT number, title, uuid, votes FROM electionResults WHERE number = " + (electionNum - 1) + ";");
                var results = statement.executeQuery();

                //add all winners of the previous election to readResults
                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    Title title = Title.getTitle(results.getString("title"));
                    Candidate candidate = new Candidate(uuid, title);
                    candidate.setVotes(results.getInt("votes"));
                    readResults.add(candidate);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "unable to read election results from the database... that's probably okay");
            }
            if (readResults.isEmpty()) return null;
            else return this.results = readResults;
        }
    }

    /**
     * store the current election results into the database
     */
    public void storeElectionResults() {
        try (Connection conn = SQLConn.getConnection()) {
            for (Candidate winner : results) {
                var statement = conn.prepareStatement("INSERT INTO electionResults VALUES (?, ?, ?, ?);");
                statement.setInt(1, this.electionNum);
                statement.setString(2, winner.getTitle().toString());
                statement.setString(3, winner.getUniqueId().toString());
                statement.setInt(4, winner.getVotes());
                statement.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "unable to store election results in the database! uh oh");
        }
    }
}
