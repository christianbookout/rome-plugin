package romeplugin.election;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

public class PartyCommand implements CommandExecutor, TabCompleter {
    private PartyHandler partyHandler;

    public PartyCommand(PartyHandler partyHandler) {
        this.partyHandler = partyHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            help(sender);
            return true;
        }
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        switch (args[1]) {
            case "help":
                help(sender);
                return true;
            case "leave":
                leave(player);
                return true;
            case "create":
                if (args.length < 4)
                    return false;
                create(player, args[2], getName(args, 3));
                return true;
            case "join":
                if (args.length < 3)
                    return false;
                join(player, args[2]);
                return true;
            case "delete":
                delete(player);
                return true;
            case "rename":
                if (args.length < 4)
                    return false;
                rename(player, args[2], getName(args, 3));
                return true;
            case "invite":
                if (args.length < 3)
                    return false;
                invite(player, args[2]);
                return true;
            case "accept":
                accept(player);
                return true;
            case "info":
                info(player, args[2]);
                return true;
            case "list":
                list(player);
                return true;
            case "public":
                setPublic(player, args[2]);
                return true;
            default:
                return false;
        }
    }

    private void setPublic(Player player, String string) {
        if (partyHandler.getParty(player.getUniqueId()) == null) {
            player.sendMessage(MessageConstants.NOT_IN_PARTY);
            return;
        }
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        partyHandler.setPublic(player.getUniqueId(), string.toLowerCase().equals("true") ? true : false);
    }

    private void list(Player player) {
        return;
    }

    private void info(Player player, String acronym) {
        Collection<String> members = partyHandler.getMembers(acronym);
        String title = partyHandler.getFullTitle(acronym);
        String description = partyHandler.getDescription(acronym);
        if (title == null || description == null || members.isEmpty()) {
            player.sendMessage(MessageConstants.CANT_FIND_PARTY);
            return;
        }
        Collection<String> shortenedMembers = members.stream().limit(10).collect(Collectors.toList());
        String membersStr = String.join(", ", shortenedMembers);
        player.sendMessage(
                ChatColor.YELLOW + "<-- " + ChatColor.RESET + title + ChatColor.YELLOW + " -->\n" + ChatColor.GOLD +
                        "Description: " + ChatColor.RESET + description + "\n" + ChatColor.GOLD +
                        "Members: " + ChatColor.RESET + membersStr);
    }

    private void accept(Player player) {
        if (partyHandler.getParty(player.getUniqueId()) != null) {
            player.sendMessage(MessageConstants.ALREADY_IN_PARTY_ERROR);
        }
        if (!partyHandler.accept(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_INVITE_ERROR);
        }
    }

    private void invite(Player player, String invitedStr) {
        // if player is not in a party then
        UUID invitedPlayer = SQLConn.getUUIDFromUsername(invitedStr);
        if (invitedPlayer == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return;
        }
        String partyName = partyHandler.getParty(player.getUniqueId());
        if (partyName == null) {
            player.sendMessage(MessageConstants.NOT_IN_PARTY);
            return;
        }
        partyHandler.invite(invitedPlayer, partyName);
    }

    private void rename(Player player, String acronym, String name) {
    }

    private void delete(Player player) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }

    }

    private void join(Player player, String party) {
    }

    private void create(Player player, String acronym, String name) {
    }

    private void leave(Player player) {
    }

    private void help(CommandSender player) {
        player.sendMessage(MessageConstants.PARTIES_HELP_COMMAND);
    }

    /**
     * get title from array of args past certain index
     * @param args passed into command
     * @param pastIndex index where title starts 
     * @return combined title
     */
    private String getName(String[] args, int pastIndex) {
        String str = args[pastIndex];
        for (int i = pastIndex+1; i < args.length-1; i++) {
            str += " " + args[i];
        }
        return str;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

}
