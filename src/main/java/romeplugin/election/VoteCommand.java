package romeplugin.election;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

public class VoteCommand implements CommandExecutor {
    private final ElectionHandler electionHandler;

    public VoteCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) return false;

        if (!(sender instanceof Player)) return false;
        
        Player player = (Player) sender;

        if (!this.electionHandler.hasElection()) {
            player.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        UUID toVote;
        //If the target isn't online then go into the database to fetch their UUID
        if (target == null) {
            UUID uuid = SQLConn.getUUIDFromUsername(args[0]);
            //Can't find player in database
            if (uuid == null) {
                player.sendMessage(MessageConstants.CANT_FIND_PLAYER + args[0]);
                return true;
            }
            toVote = uuid;
        } else {
            toVote = target.getUniqueId();
        }
        if (!electionHandler.vote(player.getUniqueId(), toVote)) {
            player.sendMessage(MessageConstants.NO_VOTING);
        } else {
            player.sendMessage(MessageConstants.SUCCESSFUL_VOTE + electionHandler.getCurrentElection().getCandidate(toVote).getTitle().fancyName + ChatColor.RESET + ": " + args[0]);
        }
        return true;
    }
    
}
