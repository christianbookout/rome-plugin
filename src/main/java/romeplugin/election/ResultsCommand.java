package romeplugin.election;

import java.util.StringJoiner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

public class ResultsCommand implements CommandExecutor {
    private ElectionHandler electionHandler;
    public ResultsCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        var results = electionHandler.getElectionResults();
        if (results == null) {
            sender.sendMessage(MessageConstants.NO_PAST_ELECTION_RESULTS);
            return true;
        }
        String toSend = ChatColor.GOLD + "<--RESULTS-->" + ChatColor.RESET;
        for (Candidate c: results) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend += "\n" + username + ": " + c.getTitle().fancyName + ChatColor.RESET + " with " + c.getVotes() + " votes";
        }
        sender.sendMessage(toSend);
        return true;
    }
    
}
