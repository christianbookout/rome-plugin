package romeplugin.election;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.database.TitleEntry;
import romeplugin.election.ElectionHandler.ElectionPhase;
import romeplugin.title.Title;

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
            String arg = args[0].toLowerCase();
            //If the player has perms to start/end elections
            boolean electionPerms = false;
            //If player has perms to run for office;
            boolean canRun = false;
            Title title = null;
            UUID targetedPlayer = null;
            if (args.length >= 1) {
                targetedPlayer = SQLConn.getUUIDFromUsername(args[1]);
                if (targetedPlayer != null) {
                    title = SQLConn.getTitle(targetedPlayer).t;
                } else {
                    title = Title.getTitle(args[1]);
                }
            }
            if (arg.equals("run") && canRun && args.length > 1) {
                run(player, title);
            }else if (arg.equals("help")) {
                help(player);
            } else if (arg.equals("results")) {
                getResults(player);
            } else if (arg.equals("candidates")) {
                getCandidates(player);
            } else if (arg.equals("vote")) {
                vote(player, targetedPlayer);
            } else {
                if (!electionPerms) {
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
        for (Candidate c: results) {
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
        for (Candidate c: candidates) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend += "\n" + username + ": " + c.getTitle().fancyName;
        }
        player.sendMessage(toSend);
    }

    private void startElection(Player player) {
        if (electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.ALREADY_ELECTION_ERROR);
        }
        else {
            electionHandler.startElection();
        }
    }

    private void startVoting(Player player) {
        if (!electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
        }
        else if (electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.ALREADY_VOTING_ERROR);
        }
        else {
            electionHandler.startElection();
        }
    }

    private void vote (Player player, UUID other) {
        if (!this.electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }

        Player target = player.getServer().getPlayer(other);
        UUID toVote;
        //If the target isn't online then go into the database to fetch their UUID
        if (target == null) {
            //Can't find player in database
            if (other == null) {
                player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
                return;
            }
            toVote = other;
        } else {
            toVote = target.getUniqueId();
        }
        if (!electionHandler.vote(player.getUniqueId(), toVote)) {
            player.sendMessage(MessageConstants.NO_VOTING);
        } else {
            player.sendMessage(MessageConstants.SUCCESSFUL_VOTE + electionHandler.getCurrentElection().getCandidate(toVote).getTitle().fancyName);
        }
    }

    //title may be null! 
    private void run(Player player, Title title) {

        //If there's no election or the election is not currently running then return
        if (!electionHandler.hasElection() || electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }

        if (title == null) {
            player.sendMessage(MessageConstants.CANT_FIND_TITLE);
            return;
        }
        electionHandler.addCandidate(player.getUniqueId(), title);
    }
}
