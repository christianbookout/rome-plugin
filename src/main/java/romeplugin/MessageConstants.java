package romeplugin;

import org.bukkit.ChatColor;

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
    public static final String ELECTIONS_HELP_COMMAND = "/elections <vote> <user> | " + ChatColor.ITALIC + ChatColor.GRAY + "vote for a user. you may only vote for each position once.\n" + 
                                                        "/elections <run> <title> | " + ChatColor.ITALIC + ChatColor.GRAY + "run for a position\n (permissions: quaestor+)" +
                                                        "/elections <candidates> | " + ChatColor.ITALIC + ChatColor.GRAY + "show all running candidates\n" +
                                                        "/elections <results> | " + ChatColor.ITALIC + ChatColor.GRAY + "show the results for the previous election\n" +
                                                        "/elections <start> | " + ChatColor.ITALIC + ChatColor.GRAY + "start an election (permissions: consul/censor)\n" +
                                                        "/elections <voting> | " + ChatColor.ITALIC + ChatColor.GRAY + "start the voting phase (permissions: consul/censor)\n" +
                                                        "/elections <end> | " + ChatColor.ITALIC + ChatColor.GRAY + "end the current election, granting the winners their roles (permissions: consul/censor)\n" +
                                                        "/elections <cancel> | " + ChatColor.ITALIC + ChatColor.GRAY + "cancel the current election (permissions: consul/censor)\n";
    public static final String NO_PERMISSION_ERROR = "you do not have permission to do that";
    public static final String ELECTION_CANCELLED = "the election has been cancelled";
}
