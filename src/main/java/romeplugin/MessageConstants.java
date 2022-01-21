package romeplugin;

import org.bukkit.ChatColor;

import org.bukkit.command.CommandSender;
import romeplugin.title.Title;

public class MessageConstants { 

    /****************************************** ELECTIONS ******************************************/

    public static final String ALREADY_ELECTION_ERROR = ChatColor.RED + "election is already running";
    public static final String ALREADY_VOTING_ERROR = ChatColor.RED + "the election is already in the voting phase";
    public static final String NO_ELECTION_ERROR = ChatColor.RED + "there is no election at the moment";
    public static final String SUCCESSFUL_VOTING_START = "voting period started";
    public static final String SUCCESSFUL_ELECTION_START = "election period started. use /elections run to run for a position";
    public static final String CANT_FIND_PLAYER = ChatColor.RED + "can't find player";
    public static final String CANT_FIND_TITLE = ChatColor.RED + "can't find title";
    public static final String ELECTION_ENDED = "election ended! use /elections results to view the results";
    public static final String ALREADY_VOTED_ERROR = ChatColor.RED + "you've already voted for this title!";
    public static final String NO_PAST_ELECTION_RESULTS = ChatColor.RED + "past election results are unavailable";
    public static final String NOT_RUNNING_ERROR = ChatColor.RED + "that player is not running for the election";
    public static final String SELF_NOT_RUNNING_ERROR = ChatColor.RED + "you are not running for the election";
    public static final String NO_VOTING = ChatColor.RED + "you may not vote for this player";
    public static final String SUCCESSFUL_VOTE = "you successfully voted for ";
    public static final String ELECTIONS_HELP_COMMAND = ChatColor.YELLOW + "\n<-------- " + ChatColor.RESET + "Elections Help" + ChatColor.YELLOW + " ----------->\n" + ChatColor.RESET + ChatColor.GOLD +
                                                        "/elections vote <user>: " + ChatColor.RESET + "vote for a user\n" + ChatColor.GOLD +
                                                        "/elections candidates: " + ChatColor.RESET + "show all running candidates\n" +  ChatColor.GOLD +
                                                        "/elections results <number>: " + ChatColor.RESET + "show past election results\n" + ChatColor.GOLD +
                                                        ChatColor.UNDERLINE + Title.QUAESTOR.color + Title.QUAESTOR.fancyName + "\n" +  ChatColor.GOLD +
                                                        "/elections run <title>: " + ChatColor.RESET + "run for a position\n" + ChatColor.GOLD +
                                                        "/elections quit: " + ChatColor.RESET + "stop running\n" + ChatColor.GOLD +
                                                        ChatColor.UNDERLINE + Title.CONSUL.color + Title.CONSUL.fancyName + ChatColor.GOLD + "/" + 
                                                        Title.CENSOR.color + Title.CENSOR.fancyName + ChatColor.RESET + "\n" +  ChatColor.GOLD +
                                                        "/elections start: " + ChatColor.RESET + "start an election\n" +  ChatColor.GOLD +
                                                        "/elections voting: " + ChatColor.RESET + "start the voting phase\n" + ChatColor.GOLD +
                                                        "/elections end: " + ChatColor.RESET + "end the current election\n" + ChatColor.GOLD +
                                                        "/elections cancel: " + ChatColor.RESET + "cancel the current election\n";
    public static final String CLAIMS_HELP_COMMAND = ChatColor.YELLOW + "\n<-------- " + ChatColor.RESET + "Claim Help" + ChatColor.YELLOW + " ----------->\n" + ChatColor.RESET + ChatColor.GOLD +
                                                        "/claim <radius>: " + ChatColor.RESET + "claim in a radius around you\n" + ChatColor.GOLD +
                                                        "/claim x0 y0 x1 y1: " + ChatColor.RESET + "claim area from x0 to x1 and y0 to y1\n" + ChatColor.GOLD +
                                                        "/claim remove: " + ChatColor.RESET + "remove the claim you're standing on\n" +ChatColor.GOLD +
                                                        "/claim removeall <op only: user>: " + ChatColor.RESET + "remove all claims\n" +ChatColor.GOLD +
                                                        "/claim transfer: " + ChatColor.RESET +  "transfer your claim to another user\n" +ChatColor.GOLD +
                                                        "/claim share <user>: " + ChatColor.RESET + "add another user to your claim\n" + ChatColor.GOLD + 
                                                        "/claim unshare <user>: " + ChatColor.RESET + "unshare your claim with user\n";
    public static final String NO_LONGER_RUNNING = "you are no longer running for the election";
    public static final String NO_PERMISSION_ERROR = ChatColor.RED + "you do not have permission to do that";
    public static final String ELECTION_CANCELLED = "the election has been cancelled";
    public static final String SUCCESSFUL_RUN = "you are sucessfully running! use /elections candidates to see your competitors";
    public static final String NOT_VOTING = ChatColor.RED + "the election is not in a voting phase!";
    public static final String NO_CANDIDATES = ChatColor.RED + "nobody is currently running! use /elections cancel to cancel the election";
    public static final String TITLES_NOT_FILLED = ChatColor.RED + "the titles aren't filled! starting voting anyway";
    public static final String UWU_DATABASE_ERROR = ChatColor.RED + "somewwing went reawwy wrong!! uwu pwease tell uws devs!!";

    /**
     * @param cond sends successMessage on true, database error on false
     * @param sender who to send the message to
     * @param successMessage the message to send when cond is true
     */
    public static void sendOnSuccess(boolean cond, CommandSender sender, String successMessage) {
        if (cond) {
            sender.sendMessage(successMessage);
        } else {
            sender.sendMessage(UWU_DATABASE_ERROR);
        }
    }
}
