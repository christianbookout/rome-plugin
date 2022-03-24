package romeplugin.election;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.election.ElectionHandler.ElectionPhase;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.role.Permission;
import romeplugin.empires.role.Role;
import romeplugin.empires.role.RoleHandler;

import java.util.*;

public class ElectionCommand implements CommandExecutor {

    private final ElectionHandler electionHandler;
    private final EmpireHandler empireHandler;
    private final RoleHandler roleHandler;

    public ElectionCommand(ElectionHandler electionHandler, EmpireHandler empireHandler, RoleHandler roleHandler) {
        this.electionHandler = electionHandler;
        this.empireHandler = empireHandler;
        this.roleHandler = roleHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                help(player);
                return true;
            }
            var maybeEmpire = empireHandler.getPlayerEmpireId(player.getUniqueId());
            if (maybeEmpire.isEmpty()) {
                // TODO: add some message
                return true;
            }
            var empireId = maybeEmpire.getAsInt();
            var playerRole = roleHandler.getPlayerRole(player);
            String arg = args[0].toLowerCase();
            Role role = null;
            UUID targetedPlayer = null;
            // like /elections vote (user) or /elections run (title)
            if (args.length >= 2) {
                targetedPlayer = SQLConn.getUUIDFromUsername(args[1]);
                if (targetedPlayer != null) {
                    role = roleHandler.getPlayerRole(targetedPlayer);
                } else {
                    role = roleHandler.getRoleByName(empireId, args[1]);
                }
            }
            switch (arg) {
                case "run":
                    run(player, role, playerRole, empireId);
                    break;
                case "quit":
                    quit(player, empireId);
                    break;
                case "help":
                    help(player);
                    break;
                case "results":
                    if (args.length >= 2) {
                        try {
                            int number = Integer.parseInt(args[1]);
                            getResults(player, Optional.of(number), empireId);
                        } catch (NumberFormatException e) {
                            help(player);
                        }
                    } else getResults(player, Optional.empty(), empireId);
                    break;
                case "candidates":
                    getCandidates(player, empireId);
                    break;
                case "vote":
                    vote(player, targetedPlayer, empireId);
                    break;
                case "seevotes":
                    seeVotes(player);
                    break;
                case "phase":
                    getPhase(player, empireId);
                    break;
                case "start":
                    startElection(player, playerRole, empireId);
                    break;
                case "voting":
                    startVoting(player, playerRole, empireId);
                    break;
                case "end":
                    endElection(player, playerRole, empireId);
                    break;
                case "cancel":
                    cancel(player, playerRole, empireId);
                    break;
                default:
                    return false;
            }
            return true;
        }
        return true;
    }

    private void quit(Player player, int empireId) {
        if (!electionHandler.hasCandidate(player.getUniqueId(), empireId)) {
            player.sendMessage(MessageConstants.SELF_NOT_RUNNING_ERROR);
            return;
        }
        electionHandler.removeCandidate(player.getUniqueId(), empireId);
        player.sendMessage(MessageConstants.NO_LONGER_RUNNING);
    }

    /**
     * show the player the current election phase
     *
     * @param player
     * @param empireId
     */
    private void getPhase(Player player, int empireId) {
        var phase = electionHandler.getElectionPhase(empireId);
        if (phase != null)
            player.sendMessage(phase.toString());
        else player.sendMessage("no election bruh moment");
    }

    /**
     * see all of the current votes by the player
     *
     * @param player
     */
    private void seeVotes(Player player) {
    }

    private boolean hasPermissions(Player player, Role role) {
        return player.isOp() || role.hasPerm(Permission.ManageElections);
    }

    /**
     * cancel the election
     */
    private void cancel(Player player, Role role, int empireId) {
        if (!hasPermissions(player, role)) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        electionHandler.cancelElection(empireId);
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
    private void endElection(Player player, Role role, int empireId) {
        if (!hasPermissions(player, role)) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (electionHandler.getElectionPhase(empireId) != ElectionPhase.VOTING) {
            player.sendMessage(MessageConstants.NOT_VOTING);
            return;
        }
        electionHandler.endElection(empireId);
    }

    /**
     * get the results of the last election
     */
    private void getResults(Player player, Optional<Integer> number, int empireId) {
        int num = number.orElse(electionHandler.getElectionNumber(empireId) - 1);
        var results = electionHandler.getElectionResults(num, empireId);
        if (results.isEmpty()) {
            player.sendMessage(MessageConstants.NO_PAST_ELECTION_RESULTS);
            return;
        }
        StringBuilder toSend = new StringBuilder(ChatColor.YELLOW + "\n<-- " + ChatColor.WHITE + "Results" + ChatColor.YELLOW + " -->" + ChatColor.RESET);
        for (Candidate c : results) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend.append("\n").append(c.getRole().color).append(c.getRole().name).append(ChatColor.RESET).append(": ").append(username).append(" with ").append(c.getVotes()).append(" votes");
        }
        player.sendMessage(toSend.toString());
    }

    /**
     * get the current candidates for the election
     */
    private void getCandidates(Player player, int empireId) {
        //If there's no election or the election has ended then return
        if (!electionHandler.hasElection(empireId)) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }

        var candidates = electionHandler.getCandidates(empireId);
        StringBuilder toSend = new StringBuilder(ChatColor.YELLOW + "\n<-- " + ChatColor.WHITE + "Candidates" + ChatColor.YELLOW + " -->" + ChatColor.RESET);

        //concatinate all candidates under their title
        for (var role : roleHandler.getEmpireRoles(empireId)) {
            if (!role.hasPerm(Permission.RunInElections)) {
                continue;
            }
            toSend.append("\n").append(role.color).append(role.name).append(ChatColor.RESET).append(": ");
            var printCandidates = new ArrayList<String>();

            for (Candidate c : candidates) {
                if (c.getRole() == role) {
                    String username = SQLConn.getUsername(c.getUniqueId());
                    printCandidates.add(username);
                }
            }
            toSend.append(String.join(", ", printCandidates));
        }
        player.sendMessage(toSend.toString());
    }

    private void startElection(Player player, Role role, int empireId) {
        if (!hasPermissions(player, role)) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (electionHandler.hasElection(empireId)) {
            player.sendMessage(MessageConstants.ALREADY_ELECTION_ERROR);
        } else {
            electionHandler.startElection(empireId);
        }
    }

    private void startVoting(Player player, Role role, int empireId) {
        if (!hasPermissions(player, role)) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (!electionHandler.hasElection(empireId)) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        } else if (electionHandler.getElectionPhase(empireId) == ElectionPhase.VOTING) {
            player.sendMessage(MessageConstants.ALREADY_VOTING_ERROR);
            return;
        } else if (electionHandler.getCandidates(empireId).isEmpty()) {
            player.sendMessage(MessageConstants.NO_CANDIDATES);
            return;
        }

        Set<Role> filledTitles = new HashSet<>();
        for (var candidate : electionHandler.getCandidates(empireId)) {
            filledTitles.add(candidate.getRole());
        }
        for (var empireRole : roleHandler.getEmpireRoles(empireId)) {
            if (empireRole.hasPerm(Permission.RunInElections) && !filledTitles.contains(empireRole)) {
                player.sendMessage(MessageConstants.TITLES_NOT_FILLED);
                break;
            }
        }
        electionHandler.startVoting(empireId);
    }

    private void vote(Player player, UUID toVote, int empireId) {
        if (!this.electionHandler.hasElection(empireId)) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }
        if (this.electionHandler.getElectionPhase(empireId) != ElectionPhase.VOTING) {
            player.sendMessage(MessageConstants.NOT_VOTING);
            return;
        }
        if (toVote == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return;
        }

        Optional<Candidate> candidate = electionHandler.getCandidate(toVote, empireId);
        if (candidate.isPresent()) {
            Role role = candidate.get().getRole();
            if (electionHandler.alreadyVoted(player.getUniqueId(), role, empireId)) {
                player.sendMessage(MessageConstants.ALREADY_VOTED_ERROR);
                return;
            }

            if (this.electionHandler.vote(player.getUniqueId(), toVote, empireId)) {
                player.sendMessage(MessageConstants.SUCCESSFUL_VOTE + role.color + role.name);
                return;
            }
        }
        player.sendMessage(MessageConstants.NO_VOTING);
    }

    //title may be null! 
    private void run(Player player, Role targetRole, Role currentRole, int empireId) {
        if (targetRole == null) {
            player.sendMessage(MessageConstants.CANT_FIND_TITLE);
            return;
        }
        /* TODO: re-implement some form of custom eligibility
        if (!isEligibleFor(currentTitle, targetTitle)) {
            player.sendMessage("you can't run for this position!");
            return;
        }
        */

        //If there's no election or the election is not currently running then return
        if (!electionHandler.hasElection(empireId) || electionHandler.getElectionPhase(empireId) != ElectionPhase.RUNNING) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return;
        }
        electionHandler.removeCandidate(player.getUniqueId(), empireId);
        electionHandler.addCandidate(player.getUniqueId(), targetRole, empireId);
        player.sendMessage(MessageConstants.SUCCESSFUL_RUN);
    }
}
