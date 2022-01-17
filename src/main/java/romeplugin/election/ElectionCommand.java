package romeplugin.election;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.election.ElectionHandler.ElectionPhase;
import romeplugin.title.Title;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ElectionCommand implements CommandExecutor {

    private final ElectionHandler electionHandler;

    public ElectionCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                help(player);
                return true;
            }
            var playerTitle = SQLConn.getTitle(player);
            String arg = args[0].toLowerCase();
            Title title = null;
            UUID targetedPlayer = null;
            // like /elections vote (user) or /elections run (title)
            if (args.length >= 2) {
                targetedPlayer = SQLConn.getUUIDFromUsername(args[1]);
                if (targetedPlayer != null) {
                    title = SQLConn.getTitle(targetedPlayer);
                } else {
                    title = Title.getTitle(args[1]);
                }
            }
            switch (arg) {
                case "run":
                    run(player, title, playerTitle);
                    break;
                case "help":
                    help(player);
                    break;
                case "results":
                    getResults(player);
                    break;
                case "candidates":
                    getCandidates(player);
                    break;
                case "vote":
                    vote(player, targetedPlayer);
                    break;
                case "seevotes":
                    seeVotes(player);
                    break;
                case "phase":
                    getPhase(player);
                    break;
                case "start":
                    startElection(player, playerTitle);
                    break;
                case "voting":
                    startVoting(player, playerTitle);
                    break;
                case "end":
                    endElection(player, playerTitle);
                    break;
                case "cancel":
                    cancel(player, playerTitle);
                    break;    
            }
            help(player);
            return true;
        }
        return true;
    }

    /**
     * show the player the current election phase
     * @param player
     */
    private void getPhase(Player player) {
        var phase = electionHandler.getElectionPhase();
        if (phase != null)
            player.sendMessage(phase.toString());
        else player.sendMessage("no election bruh moment");
    }

    /**
     * see all of the current votes by the player
     * @param player
     */
    private void seeVotes(Player player) {
    }

    /**
     * cancel the election
     */
    private void cancel(Player player, Title playerTitle) {
        if (!player.isOp() && playerTitle != Title.CENSOR && playerTitle != Title.CONSUL) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        electionHandler.clearElectionTable();
        player.sendMessage(MessageConstants.ELECTION_CANCELLED);
    }

    /**
     * print help command
     */
    private void help(Player player) {
        player.sendMessage(MessageConstants.ELECTIONS_HELP_COMMAND);
    }

    /**
     * end the election
     */
    private void endElection(Player player, Title playerTitle) {
        if (!player.isOp() && playerTitle != Title.CENSOR && playerTitle != Title.CONSUL) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (electionHandler.getElectionPhase() != ElectionPhase.VOTING) {
            player.sendMessage(MessageConstants.NOT_VOTING);
            return;
        } 
        electionHandler.endElection();
    }

    /**
     * get the results of the last election
     */
    private void getResults(Player player) {
        var results = electionHandler.getElectionResults();
        if (results == null) {
            player.sendMessage(MessageConstants.NO_PAST_ELECTION_RESULTS);
            return;
        }
        String toSend = ChatColor.GOLD + "<--RESULTS-->" + ChatColor.RESET;
        for (Candidate c : results) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend += "\n" + username + ": " + c.getTitle().fancyName + ChatColor.RESET + " with " + c.getVotes() + " votes";
        }
        player.sendMessage(toSend);
    }

    /**
     * get the current candidates for the election
     */
    private void getCandidates(Player player) {
        //If there's no election or the election has ended then return
        if (!electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }

        var candidates = electionHandler.getCurrentElection().getCandidates();
        String toSend = ChatColor.YELLOW + "<-- " + ChatColor.WHITE + "Candidates" + ChatColor.YELLOW + " -->" + ChatColor.RESET;
        
        //concatinate all candidates under their title
        for (Title t: ElectionHandler.RUNNABLE_TITLES) {
            toSend += "\n" + t.color + t.fancyName + ChatColor.RESET + ":";
            var printCandidates = new ArrayList<String>();

            for (Candidate c : candidates) {
                if (c.getTitle() == t) {
                    String username = SQLConn.getUsername(c.getUniqueId());
                    printCandidates.add(username);
                }
            }
            toSend += String.join(", ", printCandidates);
        }
        player.sendMessage(toSend);
    }

    private void startElection(Player player, Title playerTitle) {
        if (!player.isOp() && playerTitle != Title.CENSOR && playerTitle != Title.CONSUL) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.ALREADY_ELECTION_ERROR);
        } else {
            electionHandler.startElection();
        }
    }

    private void startVoting(Player player, Title playerTitle) {
        if (!player.isOp() && playerTitle != Title.CENSOR && playerTitle != Title.CONSUL) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (!electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        } else if (electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.ALREADY_VOTING_ERROR);
            return;
        } else if (electionHandler.getCurrentElection().getCandidates().isEmpty()) {
            player.sendMessage(MessageConstants.NO_CANDIDATES);
            return;
        }
        
        Set<Title> filledTitles = new HashSet<>();
        for (var candidate : electionHandler.getCurrentElection().getCandidates()) {
            filledTitles.add(candidate.getTitle());
        }
        for (var title : ElectionHandler.RUNNABLE_TITLES) {
            if (!filledTitles.contains(title)) {
                player.sendMessage(MessageConstants.TITLES_NOT_FILLED);
                break;
            }
        }
        electionHandler.startVoting();
    }

    private void vote(Player player, UUID toVote) {
        if (!this.electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }
        if (this.electionHandler.getElectionPhase() != ElectionPhase.VOTING) {
            player.sendMessage(MessageConstants.NOT_VOTING);
            return;
        }
        if (toVote == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return;
        }
        if (!electionHandler.vote(player.getUniqueId(), toVote)) {
            player.sendMessage(MessageConstants.NO_VOTING);
        } else {
            player.sendMessage(MessageConstants.SUCCESSFUL_VOTE + electionHandler.getCurrentElection().getCandidate(toVote).getTitle().fancyName);
        }
    }

    private boolean isEligibleFor(Title current, Title target) {
        if (target == Title.TRIBUNE) {
            return true;
        }
        if ((target == Title.AEDILE || target == Title.PRAETOR) && (current == Title.QUAESTOR || current == Title.CENSOR || current == Title.AEDILE)) {
            return true;
        }
        return target == Title.CONSUL && (current == Title.QUAESTOR || current == Title.PRAETOR || current == Title.AEDILE || current == Title.CENSOR);
    }

    //title may be null! 
    private void run(Player player, Title targetTitle, Title currentTitle) {
        if (targetTitle == null) {
            player.sendMessage(MessageConstants.CANT_FIND_TITLE);
            return;
        }
        if (!isEligibleFor(currentTitle, targetTitle)) {
            player.sendMessage("you can't run for this position!");
            return;
        }

        //If there's no election or the election is not currently running then return
        if (!electionHandler.hasElection() || electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }
        electionHandler.removeCandidate(player.getUniqueId()); // just in case the player is currently a candidate in the election and runs for something else
        if (electionHandler.addCandidate(player.getUniqueId(), targetTitle)) {
            player.sendMessage(MessageConstants.SUCCESSFUL_RUN);
        }
    }
}
