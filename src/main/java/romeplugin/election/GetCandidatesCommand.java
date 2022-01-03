package romeplugin.election;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.election.ElectionHandler.ElectionPhase;

public class GetCandidatesCommand implements CommandExecutor{
    private final ElectionHandler electionHandler;

    public GetCandidatesCommand(ElectionHandler electionHandler) {
        this.electionHandler = electionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        //If there's no election or the election has ended then return
        if (!electionHandler.hasElection() || electionHandler.getElectionPhase() == ElectionPhase.ENDED) {
            sender.sendMessage(MessageConstants.NO_ELECTION_ERROR);
            return true;
        }

        var candidates = electionHandler.getCurrentElection().getCandidates();
        String toSend = ChatColor.GOLD + "<--CANDIDATES-->" + ChatColor.RESET;
        for (Candidate c: candidates) {
            String username = SQLConn.getUsername(c.getUniqueId());
            toSend += "\n" + username + ": " + c.getTitle().fancyName;
        }
        sender.sendMessage(toSend);
        return true;
    }
    
}
