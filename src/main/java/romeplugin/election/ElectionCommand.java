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
import java.util.Optional;
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
                case "quit":
                    quit(player);
                case "help":
                    help(player);
                    break;
                case "results":
                    if (args.length >= 2) {
                        try {
                            int number = Integer.valueOf(args[1]);
                            getResults(player, Optional.of(number));
                        } catch (NumberFormatException e) {
                            help(player);
                        }
                    }
                    else getResults(player, Optional.empty());
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
                default:
                    return false;    
            }
            return true;
        }
        return true;
    }

    private void quit(Player player) {
        if(!electionHandler.hasCandidate(player.getUniqueId())) {
            player.sendMessage(MessageConstants.SELF_NOT_RUNNING_ERROR);
            return;
        }
        electionHandler.removeCandidate(player.getUniqueId());
        player.sendMessage(MessageConstants.NO_LONGER_RUNNING);
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
        electionHandler.cancelElection();
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
    private void getResults(Player player, Optional<Integer> number) {
        int num = number.orElse(electionHandler.getElectionNumber()-1);
        var results = electionHandler.getElectionResults(num);
        if (results.isEmpty()) {
            player.sendMessage(MessageConstants.NO_PAST_ELECTION_RESULTS);
            return;
        }
        String toSend = ChatColor.YELLOW + "\n<-- " + ChatColor.WHITE + "Results" + ChatColor.YELLOW + " -->" + ChatColor.RESET;
        for (Candidate c : results) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend += "\n" + c.getTitle().color + c.getTitle().fancyName  + ChatColor.RESET + ": " + username + " with " + c.getVotes() + " votes";
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

        var candidates = electionHandler.getCandidates();
        String toSend = ChatColor.YELLOW + "\n<-- " + ChatColor.WHITE + "Candidates" + ChatColor.YELLOW + " -->" + ChatColor.RESET;
        
        //concatinate all candidates under their title
        for (Title t: ElectionHandler.RUNNABLE_TITLES) {
            toSend += "\n" + t.color + t.fancyName + ChatColor.RESET + ": ";
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
        } else if (electionHandler.getElectionPhase() == ElectionPhase.VOTING) {
            player.sendMessage(MessageConstants.ALREADY_VOTING_ERROR);
            return;
        } else if (electionHandler.getCandidates().isEmpty()) {
            player.sendMessage(MessageConstants.NO_CANDIDATES);
            return;
        }
        
        Set<Title> filledTitles = new HashSet<>();
        for (var candidate : electionHandler.getCandidates()) {
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
        
        Optional<Candidate> candidate = electionHandler.getCandidate(toVote);
        if (candidate.isPresent()) {
            Title title = candidate.get().getTitle();
            if (electionHandler.alreadyVoted(player.getUniqueId(), title)) {
                player.sendMessage(MessageConstants.ALREADY_VOTED_ERROR);
                return;
            }

            if (this.electionHandler.vote(player.getUniqueId(), toVote)) {
                player.sendMessage(MessageConstants.SUCCESSFUL_VOTE + title.color + title.fancyName);
                return;
            }
        }
        player.sendMessage(MessageConstants.NO_VOTING);
            
        
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
        electionHandler.removeCandidate(player.getUniqueId());
        electionHandler.addCandidate(player.getUniqueId(), targetTitle);
        player.sendMessage(MessageConstants.SUCCESSFUL_RUN);
        
    }
}
