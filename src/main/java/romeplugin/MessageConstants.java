package romeplugin;

import org.bukkit.ChatColor;

public class MessageConstants { //TODO transfer all message strings to this file

    /****************************************** ELECTIONS ******************************************/

    public static final String ALREADY_ELECTION_ERROR = ChatColor.RED + "election is already running";
    public static final String ALREADY_VOTING_ERROR = ChatColor.RED + "the election is already in the voting phase";
    public static final String NO_ELECTION_ERROR = ChatColor.RED + "there is no election at the moment";
    public static final String SUCCESSFUL_VOTING_START = "voting period started";
    public static final String SUCCESSFUL_ELECTION_START = "election period started. use /run to run for a position";
    public static final String CANT_FIND_PLAYER = ChatColor.RED + "can't find player named " + ChatColor.ITALIC;
    public static final String CANT_FIND_TITLE = ChatColor.RED + "can't find title named " + ChatColor.ITALIC;
    public static final String ELECTION_ENDED = "election ended! use /results to view the results";
    public static final String ALREADY_VOTED_ERROR = ChatColor.RED + "you've already voted!";
    public static final String NO_PAST_ELECTION_RESULTS = ChatColor.RED + "past election results are unavailable";
    //public static final String
    //public static final String
    //public static final String
    //public static final String
    public static final String NOT_RUNNING_ERROR = ChatColor.RED + "that player is not running for the election";
    public static final String NO_VOTING = ChatColor.RED + "you may not vote for this player";
    public static final String SUCCESSFUL_VOTE = "you voted for ";
}
