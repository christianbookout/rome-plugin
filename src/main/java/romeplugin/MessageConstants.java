package romeplugin;

import org.bukkit.ChatColor;

import romeplugin.title.Title;

public class MessageConstants { //TODO transfer all message strings to this file

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
    //public static final String
    //public static final String
    //public static final String
    //public static final String
    public static final String NOT_RUNNING_ERROR = ChatColor.RED + "that player is not running for the election";
    public static final String NO_VOTING = ChatColor.RED + "you may not vote for this player";
    public static final String SUCCESSFUL_VOTE = "you successfully voted for ";
    //<vote|run|candidates|results|start|voting|end>
    public static final String ELECTIONS_HELP_COMMAND = "/elections vote <user>" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "vote for a user. you may only vote for each position once.\n" + ChatColor.RESET +
                                                        "/elections run <title>" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "run for a position (permissions: "+ Title.QUAESTOR.fancyName + ChatColor.ITALIC + ChatColor.GRAY +"+)\n" + ChatColor.RESET +
                                                        "/elections candidates" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "show all running candidates\n" + ChatColor.RESET +
                                                        "/elections results" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "show the results for the previous election\n" + ChatColor.RESET +
                                                        "/elections start" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "start an election (permissions: "+ Title.CONSUL.fancyName + ChatColor.ITALIC + ChatColor.GRAY + "/"+ Title.CENSOR.fancyName + ChatColor.ITALIC + ChatColor.GRAY + ")\n" + ChatColor.RESET +
                                                        "/elections voting" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "start the voting phase (permissions: "+ Title.CONSUL.fancyName + ChatColor.ITALIC + ChatColor.GRAY + "/"+ Title.CENSOR.fancyName + ChatColor.ITALIC + ChatColor.GRAY + ")\n" + ChatColor.RESET +
                                                        "/elections end" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "end the current election, granting the winners their roles (permissions: "+ Title.CONSUL.fancyName + ChatColor.ITALIC + ChatColor.GRAY + "/"+ Title.CENSOR.fancyName + ChatColor.ITALIC + ChatColor.GRAY + ")\n" + ChatColor.RESET +
                                                        "/elections cancel" + ChatColor.BLACK + " | " + ChatColor.ITALIC + ChatColor.GRAY + "cancel the current election (permissions: "+ Title.CONSUL.fancyName + ChatColor.ITALIC + ChatColor.GRAY + "/"+ Title.CENSOR.fancyName + ChatColor.ITALIC + ChatColor.GRAY + ")\n";
    public static final String NO_PERMISSION_ERROR = ChatColor.RED + "you do not have permission to do that";
    public static final String ELECTION_CANCELLED = "the election has been cancelled";
    public static final String SUCCESSFUL_RUN = "you are sucessfully running! use /elections candidates to see your competitors";
    public static final String NOT_VOTING = ChatColor.RED + "the election is not in a voting phase!";
    public static final String NO_CANDIDATES = ChatColor.RED + "nobody is currently running! use /elections cancel to cancel the election";
}
