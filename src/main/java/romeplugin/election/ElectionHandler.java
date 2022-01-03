package romeplugin.election;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.newtitle.Title;
import romeplugin.newtitle.TitleHandler;

public class ElectionHandler {
    //don't make one of these greater than 12 characters :D 
    public enum ElectionPhase {
        RUNNING,
        VOTING,
        ENDED
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
        this.currentPhase = ElectionPhase.ENDED;
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
        try(Connection conn = SQLConn.getConnection()) {
            
            conn.prepareStatement("DELETE FROM election WHERE uuid = '" + uuid.toString() + "';").execute();

        } catch (SQLException e) {}
    }

    private void storeCandidate(UUID uuid, Title title) {
        
        try(Connection conn = SQLConn.getConnection()) {
            
            var preparedStatement = conn.prepareStatement("REPLACE INTO election VALUES (?, ?, ?, ?);");

            //set 1: uuid, 2: username, 3: title, 4: votes
            preparedStatement.setString(1, uuid.toString()); 
            preparedStatement.setString(2, SQLConn.getUsername(uuid));
            preparedStatement.setString(3, title.toString());
            preparedStatement.setInt(4, 0);
            preparedStatement.execute();

        } catch (SQLException e) {}
    }
    
    public void startElection() {
        this.currentElection = new Election();
        this.currentPhase = ElectionPhase.RUNNING;
        this.updateElectionState();
    }

    /**
     * begin the voting phase in the election 
     */
    public void startVoting() {
        this.currentPhase = ElectionPhase.VOTING;
        this.updateElectionState();
    }

    /**
     * ends the election, adding all titles to the winners and updating the election state/results
     */
    public void endElection() {
        this.results = this.currentElection.endElection();

        //Apply all titles to each winner
        this.results.forEach(winner -> titleHandler.setTitle(winner.getUniqueId(), winner.getTitle()));

        this.currentElection = null;
        this.currentPhase = ElectionPhase.ENDED;
        this.electionNum++;

        this.updateElectionState();
        this.storeElectionResults();

        plugin.getServer().broadcastMessage(MessageConstants.ELECTION_ENDED);
    }

    private boolean alreadyVoted(UUID player, Title title) {
        try(Connection conn = SQLConn.getConnection()) {
            var statement = conn.prepareStatement("SELECT uuid, titleVotedFor FROM playerVotes WHERE uuid = '" + player.toString() + "' AND titleVotedFor = '" + title.toString() + "';");
            var results = statement.executeQuery();
            return results.next();
        } catch (SQLException e) {}
        return false;
    }

    /**
     * initializes the current election state, number and candidates/votes if applicable
     */
    public void initialize() {
        try(Connection conn = SQLConn.getConnection()) {
            
            this.readElectionState();
            
            if (this.currentPhase == ElectionPhase.ENDED) return;

            this.currentElection = new Election();

            var statement = conn.prepareStatement("SELECT uuid, username, title, votes FROM election;");
            var results = statement.executeQuery();
            
            while (results.next()) {

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
        try(Connection conn = SQLConn.getConnection()) {
            var currInfo = conn.prepareStatement("SELECT electionNumber, electionPhase FROM electionState").executeQuery();
            if (currInfo.next()) {
                this.electionNum = currInfo.getInt("electionNumber");
                this.currentPhase = ElectionPhase.valueOf(currInfo.getString("electionPhase"));
            } 
        } catch (SQLException e) {}
    }
    /**
     * updates the electionState table to represent current election number and phase
     */
    public void updateElectionState() {
        try(Connection conn = SQLConn.getConnection()) {
            var statement = conn.prepareStatement("REPLACE INTO electionState (?, ?)");
            statement.setInt(1, this.electionNum);
            statement.setString(2, this.currentPhase.toString());
        } catch (SQLException e) {}
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

            try(Connection conn = SQLConn.getConnection()) {

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
        try(Connection conn = SQLConn.getConnection()) {
            for (Candidate winner: results) {   
                var statement = conn.prepareStatement("REPLACE INTO electionResults VALUES (?, ?, ?, ?);");
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
