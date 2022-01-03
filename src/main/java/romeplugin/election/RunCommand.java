package romeplugin.election;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.election.ElectionHandler.ElectionPhase;
import romeplugin.newtitle.Title;

public class RunCommand implements CommandExecutor {
    private final ElectionHandler electionHandler;
    public RunCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) return false;
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        //If there's no election or the election is not currently running then return
        if (!electionHandler.hasElection() || electionHandler.getElectionPhase() != ElectionPhase.RUNNING) {
            sender.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return true;
        }

        Title title = Title.getTitle(args[0]);

        if (title == null) {
            player.sendMessage(MessageConstants.CANT_FIND_TITLE + args[0]);
        }
        electionHandler.addCandidate(player.getUniqueId(), title);
        return false;
    }
    
}
