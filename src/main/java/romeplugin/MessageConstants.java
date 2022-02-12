package romeplugin;

import org.bukkit.ChatColor;

import org.bukkit.command.CommandSender;
import romeplugin.title.Title;

public class MessageConstants {

    private static final ChatColor ERROR_COLOR = ChatColor.RED;
    private static final ChatColor SUCCESS_COLOR = ChatColor.GREEN;
    private static final ChatColor POLITICS_COLOR = ChatColor.LIGHT_PURPLE;


    /****************************************** ELECTIONS ******************************************/

    public static final String ALREADY_ELECTION_ERROR = ERROR_COLOR + "election is already running";
    public static final String ALREADY_VOTING_ERROR = ERROR_COLOR + "the election is already in the voting phase";
    public static final String NO_ELECTION_ERROR = ERROR_COLOR + "there is no election at the moment";
    public static final String SUCCESSFUL_VOTING_START = POLITICS_COLOR + "voting period started";
    public static final String SUCCESSFUL_ELECTION_START = POLITICS_COLOR + "election period started. use /elections run to run for a position";
    public static final String CANT_FIND_PLAYER = ERROR_COLOR + "can't find player";
    public static final String CANT_FIND_TITLE = ERROR_COLOR + "can't find title";
    public static final String ELECTION_ENDED = POLITICS_COLOR + "election ended! use /elections results to view the results";
    public static final String ALREADY_VOTED_ERROR = ERROR_COLOR + "you've already voted for this title!";
    public static final String NO_PAST_ELECTION_RESULTS = ERROR_COLOR + "past election results are unavailable";
    public static final String NOT_RUNNING_ERROR = ERROR_COLOR + "that player is not running for the election";
    public static final String SELF_NOT_RUNNING_ERROR = ERROR_COLOR + "you are not running for the election";
    public static final String NO_VOTING = ERROR_COLOR + "you may not vote for this player";
    public static final String SUCCESSFUL_VOTE = POLITICS_COLOR + "you successfully voted for ";
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
                                                        "/claim unshare <user>: " + ChatColor.RESET + "unshare your claim with user\n" + ChatColor.GOLD +
                                                        "/claim list: " + ChatColor.RESET + "list claims you own\n" + ChatColor.GOLD +
                                                        "/claim info: " + ChatColor.RESET + "get info about the claim you are in";
    public static final String NO_LONGER_RUNNING = POLITICS_COLOR + "you are no longer running for the election";
    public static final String NO_PERMISSION_ERROR = ERROR_COLOR + "you do not have permission to do that";
    public static final String ELECTION_CANCELLED = POLITICS_COLOR  + "the election has been cancelled";
    public static final String SUCCESSFUL_RUN = SUCCESS_COLOR + "you are sucessfully running! use /elections candidates to see your competitors";
    public static final String NOT_VOTING = ERROR_COLOR + "the election is not in a voting phase!";
    public static final String NO_CANDIDATES = ERROR_COLOR + "nobody is currently running! use /elections cancel to cancel the election";
    public static final String TITLES_NOT_FILLED = ERROR_COLOR + "the titles aren't filled! starting voting anyway";
    public static final String UWU_DATABASE_ERROR = ERROR_COLOR + "somewwing went reawwy wrong!! uwu pwease tell uws devs!!";
    public static final String PARTIES_HELP_COMMAND = ChatColor.YELLOW + "\n<-------- " + ChatColor.RESET + "Parties Help" + ChatColor.YELLOW + " ----------->\n" + ChatColor.RESET + ChatColor.GOLD +
                                                        "/parties create <acronym> <name>: " + ChatColor.RESET + "create a political party\n" + ChatColor.GOLD + 
                                                        "/parties disband: " + ChatColor.RESET + "disband your party\n" + ChatColor.GOLD + 
                                                        "/parties setowner <player>: " + ChatColor.RESET + "transfer ownership of your party \n" + ChatColor.GOLD +
                                                        "/parties invite <player>: " + ChatColor.RESET + "invite a player to your party\n" + ChatColor.GOLD + 
                                                        "/parties accept: " + ChatColor.RESET + "accept a party invite\n" + ChatColor.GOLD + 
                                                        "/parties deny: " + ChatColor.RESET + "deny a party invite\n" + ChatColor.GOLD + 
                                                        "/parties leave: " + ChatColor.RESET + "leave your current party\n" + ChatColor.GOLD + 
                                                        "/parties join: " + ChatColor.RESET + "join a public party\n" + ChatColor.GOLD + 
                                                        "/parties description <description>: " + ChatColor.RESET + "set your party description\n" + ChatColor.GOLD + 
                                                        "/parties color <color>: " + ChatColor.RESET + "set your party color\n" + ChatColor.GOLD + 
                                                        "/parties colors: " + ChatColor.RESET + "get a list of colors\n" + ChatColor.GOLD + 
                                                        "/parties list: " + ChatColor.RESET + "get a list of parties\n" + ChatColor.GOLD + 
                                                        "/parties rename <acronym> <name>: " + ChatColor.RESET + "change your party's name and acronym\n" + ChatColor.GOLD +
                                                        "/parties shareclaim: " + ChatColor.RESET + "share a claim with your whole party";
    public static final String NOT_IN_PARTY = ERROR_COLOR + "you aren't currently in a party";
    public static final String CANT_FIND_PARTY = ERROR_COLOR + "can't find that party";
    public static final String NO_INVITE_ERROR = ERROR_COLOR + "you have no pending invites";
    public static final String ALREADY_IN_PARTY_ERROR = ERROR_COLOR + "you're already in a party";
    public static final String SUCCESSFUL_INVITE_ACCEPT = SUCCESS_COLOR + "invite accepted";
    public static final String SUCCESSFUL_INVITE_DENY = SUCCESS_COLOR + "invite denied";
    public static final String SUCCESSFUL_INVITE_SEND = SUCCESS_COLOR + "invite sent";
    public static final String SUCCESSFUL_PARTY_DISBAND = SUCCESS_COLOR + "party is no more";
    public static final String SUCCESSFUL_PARTY_RENAME = SUCCESS_COLOR + "party successfully renamed. well done! you did so great <3.";
    public static final String SUCCESSFUL_PARTY_CREATE = SUCCESS_COLOR + "you made a party";
    public static final String SUCCESSFUL_PARTY_JOIN = SUCCESS_COLOR + "you've successfully joined a party";
    public static final String SUCCESSFUL_PARTY_KICK = SUCCESS_COLOR + "you've really kicked that guy! nice <3";
    public static final String SUCCESSFUL_PARTY_SETOWNER = SUCCESS_COLOR + "you changed the owner of the party! good work <3";
    public static final String PARTY_PRIVATE_ERROR = ERROR_COLOR + "that party is private";
    public static final String OWNER_OF_PARTY_ERROR = ERROR_COLOR + "use /party disband or /parties setowner <player> first";
    public static final String SUCCESSFUL_DESCRIPTION_SET = SUCCESS_COLOR + "you have set the description :thumbsup:";
    public static final String DESCRIPTION_SET_ERROR = ERROR_COLOR + "didnt work";
    public static final String SUCCESSFUL_PUBLIC_SET = SUCCESS_COLOR + "you did it!";
    public static final String PUBLIC_SET_ERROR = ERROR_COLOR + "better luck next time";
    public static final String ALREADY_INVITE_SENT = ERROR_COLOR + "that player already has a pending invite";
    public static final String COLORS_HELP = ChatColor.BLACK + "&0" + ChatColor.DARK_BLUE + "&1" + ChatColor.DARK_GREEN + "&2" + ChatColor.DARK_AQUA + "&3" +
                                             ChatColor.DARK_RED + "&4" + ChatColor.DARK_PURPLE + "&5" + ChatColor.GOLD + "&6" + ChatColor.GRAY + "&7" +
                                             ChatColor.DARK_GRAY + "&8" + ChatColor.BLUE + "&9" + ChatColor.GREEN + "&a" + ChatColor.AQUA + "&b" +
                                             ChatColor.RED + "&c" + ChatColor.LIGHT_PURPLE + "&d" + ChatColor.YELLOW + "&e" + ChatColor.WHITE + "&f";
    public static final String SUCCESSFUL_COLOR_SET = SUCCESS_COLOR + "you have changed the color.";
    public static final String COLOR_SET_ERROR = ERROR_COLOR + "didnt work";
    public static final String PARTY_NAME_COLLISION_ERROR = "that name already exists";
    public static final String TARGET_NOT_IN_PARTY = ERROR_COLOR + "that person isn't in your party!";

    public static final String NOTIFICATION_RECEIVED = "you've got mail!";
    public static final String NOTIFICATION_INDEX_OUT_OF_BOUNDS = ERROR_COLOR + "you don't have that much mail";
    public static final String SUCCESSFUL_NOTIFICATION_CLEAR = SUCCESS_COLOR + "you cleared all your notifications! <3";
    public static final String NO_CLAIM_ERROR = ERROR_COLOR + "there is no claim here";
    public static final String NOT_CLAIM_OWNER = ERROR_COLOR + "you do not own this claim";
    public static final String PARTY_CLAIM_SHARE = SUCCESS_COLOR + "you've shared this claim with all of your party";

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
