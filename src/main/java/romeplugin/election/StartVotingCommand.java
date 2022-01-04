package romeplugin.election;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import romeplugin.MessageConstants;
import romeplugin.election.ElectionHandler.ElectionPhase;

public class StartVotingCommand implements CommandExecutor {

    private final ElectionHandler electionHandler;

    public StartVotingCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!electionHandler.hasElection()) {
            sender.sendMessage(MessageConstants.NO_ELECTION_ERROR);
        }
        else if (electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            sender.sendMessage(MessageConstants.ALREADY_VOTING_ERROR);
        }
        else {
            electionHandler.startElection();
        }
        return true;
    }
    
}
