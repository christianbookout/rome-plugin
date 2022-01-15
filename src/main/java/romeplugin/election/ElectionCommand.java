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

import java.util.UUID;

public class ElectionCommand implements CommandExecutor {

    private final ElectionHandler electionHandler;

    public ElectionCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) return false;

        if (sender instanceof Player) {
            Player player = (Player) sender;
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
                    run(player, title);
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
                default:
                    if (!player.isOp() && playerTitle != Title.CENSOR) {
                        player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
                        return true;
                    }

                    if (arg.equals("start")) {
                        startElection(player);
                    } else if (arg.equals("voting")) {
                        startVoting(player);
                    } else if (arg.equals("end")) {
                        endElection(player);
                    } else if (arg.equals("cancel")) {
                        cancel(player);
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * cancel the election
     */
    private void cancel(Player player) {
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
    private void endElection(Player player) {
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
        String toSend = ChatColor.GOLD + "<--CANDIDATES-->" + ChatColor.RESET;
        for (Candidate c : candidates) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend += "\n" + username + ": " + c.getTitle().fancyName;
        }
        player.sendMessage(toSend);
    }

    private void startElection(Player player) {
        if (electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.ALREADY_ELECTION_ERROR);
        } else {
            electionHandler.startElection();
        }
    }

    private void startVoting(Player player) {
        if (!electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
        } else if (electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.ALREADY_VOTING_ERROR);
        } else {
            electionHandler.startVoting();
        }
    }

    private void vote(Player player, UUID toVote) {
        if (!this.electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
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
        if (target == Title.TRIBUNE && current == null) {
            return true;
        }
        if ((target == Title.AEDILE || target == Title.PRAETOR) && current == Title.QUAESTOR) {
            return true;
        }
        return target == Title.CONSUL && (current == Title.QUAESTOR || current == Title.PRAETOR || current == Title.AEDILE);
    }

    //title may be null! 
    private void run(Player player, Title targetTitle) {
        if (targetTitle == null) {
            player.sendMessage(MessageConstants.CANT_FIND_TITLE);
            return;
        }

        var currentTitle = SQLConn.getTitle(player.getUniqueId());
        if (!isEligibleFor(currentTitle, targetTitle)) {
            player.sendMessage("you can't run for this position!");
            return;
        }

        //If there's no election or the election is not currently running then return
        if (!electionHandler.hasElection() || electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }
        electionHandler.addCandidate(player.getUniqueId(), targetTitle);
    }
}
