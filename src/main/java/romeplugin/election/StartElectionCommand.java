package romeplugin.election;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import romeplugin.MessageConstants;

public class StartElectionCommand implements CommandExecutor{

    private final ElectionHandler electionHandler;

    public StartElectionCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (electionHandler.hasElection()) {
            sender.sendMessage(MessageConstants.ALREADY_ELECTION_ERROR);
        }
        else {
            electionHandler.startElection();
        }
        return true;
    }
    
}
