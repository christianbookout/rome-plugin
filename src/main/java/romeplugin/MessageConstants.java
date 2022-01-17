package romeplugin;

import org.bukkit.ChatColor;

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
    public static final String ALREADY_VOTED_ERROR = ChatColor.RED + "you've already voted!";
    public static final String NO_PAST_ELECTION_RESULTS = ChatColor.RED + "past election results are unavailable";
    public static final String NOT_RUNNING_ERROR = ChatColor.RED + "that player is not running for the election";
    public static final String NO_VOTING = ChatColor.RED + "you may not vote for this player";
    public static final String SUCCESSFUL_VOTE = "you successfully voted for ";
    public static final String ELECTIONS_HELP_COMMAND = ChatColor.YELLOW + "\n<-- " + ChatColor.RESET + "Elections Help" + ChatColor.YELLOW + " -->\n" + ChatColor.RESET + ChatColor.GOLD +
                                                        "/elections vote <user>: " + ChatColor.RESET + "vote for a user\n" + ChatColor.GOLD +
                                                        "/elections candidates: " + ChatColor.RESET + "show all running candidates\n" +  ChatColor.GOLD +
                                                        "/elections results: " + ChatColor.RESET + "show the results for the previous election\n" + ChatColor.GOLD +
                                                        ChatColor.UNDERLINE + Title.QUAESTOR.color + Title.QUAESTOR.fancyName + "\n" +  ChatColor.GOLD +
                                                        "/elections run <title>: " + ChatColor.RESET + "run for a position\n" + ChatColor.GOLD +
                                                        ChatColor.UNDERLINE + Title.CONSUL.color + Title.CONSUL.fancyName + ChatColor.GOLD + "/" + 
                                                        Title.CENSOR.color + Title.CENSOR.fancyName + ChatColor.RESET + "\n" +  ChatColor.GOLD +
                                                        "/elections start: " + ChatColor.RESET + "start an election\n" +  ChatColor.GOLD +
                                                        "/elections voting: " + ChatColor.RESET + "start the voting phase\n" + ChatColor.GOLD +
                                                        "/elections end: " + ChatColor.RESET + "end the current election\n" + ChatColor.GOLD +
                                                        "/elections cancel: " + ChatColor.RESET + "cancel the current election\n";
    public static final String NO_PERMISSION_ERROR = ChatColor.RED + "you do not have permission to do that";
    public static final String ELECTION_CANCELLED = "the election has been cancelled";
    public static final String SUCCESSFUL_RUN = "you are sucessfully running! use /elections candidates to see your competitors";
    public static final String NOT_VOTING = ChatColor.RED + "the election is not in a voting phase!";
    public static final String NO_CANDIDATES = ChatColor.RED + "nobody is currently running! use /elections cancel to cancel the election";
    public static final String TITLES_NOT_FILLED = ChatColor.RED + "the titles aren't filled! starting voting anyway";
}
