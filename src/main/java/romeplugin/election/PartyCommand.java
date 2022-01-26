package romeplugin.election;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.election.PartyHandler.PartyAcronym;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyCommand implements CommandExecutor, TabCompleter {
    private final PartyHandler partyHandler;
    private final HashMap<UUID, PartyAcronym> invitations = new HashMap<>();

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
        switch (args[0]) {
            case "help":
                help(sender);
                return true;
            case "leave":
                leave(player);
                return true;
            case "create":
                if (args.length < 4)
                    return false;
                create(player, args[1], getName(args, 3));
                return true;
            case "join":
                if (args.length < 2)
                    return false;
                join(player, args[1]);
                return true;
            case "delete":
                delete(player);
                return true;
            case "rename":
                if (args.length < 4)
                    return false;
                rename(player, args[1], getName(args, 3));
                return true;
            case "invite":
                if (args.length < 3)
                    return false;
                invite(player, args[1]);
                return true;
            case "accept":
                accept(player);
                return true;
            case "deny":
                deny(player);
                return true;
            case "info":
                info(player, args[1]);
                return true;
            case "list":
                list(player);
                return true;
            case "public":
                setPublic(player, args[1]);
                return true;
            default:
                return false;
        }
    }

    private void setPublic(Player player, String arg) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        arg = arg.toLowerCase();
        partyHandler.setPublic(player.getUniqueId(), arg.equals("true") || arg.equals("yes") || arg.equals("yeah"));
    }

    private void list(Player player) {
        player.sendMessage("parties:\n" + String.join("\n", partyHandler.getParties()));
    }

    private void info(Player player, String acronym) {
        var acronym_canon = PartyAcronym.make(acronym);
        Collection<String> members = partyHandler.getMembersUsernames(acronym_canon);
        String name = partyHandler.getName(acronym_canon);
        String description = partyHandler.getDescription(acronym_canon);
        if (name == null || description == null || members.isEmpty()) {
            player.sendMessage(MessageConstants.CANT_FIND_PARTY);
            return;
        }
        String membersStr = members.stream().limit(10).collect(Collectors.joining(", "));
        player.sendMessage(
                ChatColor.YELLOW + "<-- " + ChatColor.RESET + name + ChatColor.YELLOW + " -->\n" + ChatColor.GOLD +
                        "Description: " + ChatColor.RESET + description + "\n" + ChatColor.GOLD +
                        "Members: " + ChatColor.RESET + membersStr);
    }

    private void accept(Player player) {
        var invite = invitations.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(MessageConstants.NO_INVITE_ERROR);
            return;
        }
        if (partyHandler.getParty(player.getUniqueId()) != null) {
            player.sendMessage(MessageConstants.ALREADY_IN_PARTY_ERROR);
        }
        if (partyHandler.joinParty(player.getUniqueId(), invite)) {
            player.sendMessage(MessageConstants.SUCCESSFUL_INVITE_ACCEPT);
        }
    }

    private void deny(Player player) {
        if (invitations.remove(player.getUniqueId()) == null) {
            player.sendMessage(MessageConstants.NO_INVITE_ERROR);
            return;
        }
        player.sendMessage(MessageConstants.SUCCESSFUL_INVITE_DENY);
    }

    private void invite(Player player, String invitedStr) {
        UUID invitedPlayer = SQLConn.getUUIDFromUsername(invitedStr);
        if (invitedPlayer == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return;
        }
        var partyName = partyHandler.getParty(player.getUniqueId());
        if (partyName == null) {
            player.sendMessage(MessageConstants.NOT_IN_PARTY);
            return;
        }
        invitations.put(invitedPlayer, partyName);
        player.sendMessage(MessageConstants.SUCCESSFUL_INVITE_SEND);
    }

    private void rename(Player player, String acronym, String name) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
        }
        MessageConstants.sendOnSuccess(
                partyHandler.rename(player.getUniqueId(), PartyAcronym.make(acronym), name),
                player,
                MessageConstants.SUCCESSFUL_PARTY_RENAME
        );
    }

    private void delete(Player player) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
        }
        MessageConstants.sendOnSuccess(
                partyHandler.disbandParty(player.getUniqueId()),
                player,
                MessageConstants.SUCCESSFUL_PARTY_DISBAND
        );
    }

    private void join(Player player, String partyAcronym) {
        var acronym = PartyAcronym.make(partyAcronym);
        partyHandler.isPartyPublic(acronym).ifPresentOrElse(
                is_public -> {
                    if (!is_public) {
                        player.sendMessage(MessageConstants.PARTY_PRIVATE_ERROR);
                        return;
                    }
                    partyHandler.joinParty(player.getUniqueId(), acronym);
                },
                // FIXME: check if this player has an invite to a party before failing
                () -> player.sendMessage(MessageConstants.CANT_FIND_PARTY)
        );
    }

    private void create(Player player, String acronym, String name) {
        if (partyHandler.getParty(player.getUniqueId()) != null) {
            player.sendMessage(MessageConstants.ALREADY_IN_PARTY_ERROR);
            return;
        }
        MessageConstants.sendOnSuccess(
                partyHandler.createParty(player.getUniqueId(), PartyAcronym.make(acronym), name),
                player,
                MessageConstants.SUCCESSFUL_PARTY_CREATE
        );
    }

    private void leave(Player player) {
        if (partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.OWNER_OF_PARTY_ERROR);
            return;
        }
        partyHandler.leaveParty(player.getUniqueId());
    }

    private void help(CommandSender player) {
        player.sendMessage(MessageConstants.PARTIES_HELP_COMMAND);
    }

    /**
     * get title from array of args past certain index
     *
     * @param args      passed into command
     * @param pastIndex index where title starts
     * @return combined title
     */
    private String getName(String[] args, int pastIndex) {
        String str = args[pastIndex];
        for (int i = pastIndex + 1; i < args.length - 1; i++) {
            str += " " + args[i];
        }
        return str;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // TODO: implement me!
        return null;
    }

}
