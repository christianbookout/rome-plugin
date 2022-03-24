package romeplugin.election;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.election.PartyHandler.Party;
import romeplugin.election.PartyHandler.PartyAcronym;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            case "shareclaim":
                shareClaim(player);
                return true;
            case "help":
                help(sender);
                return true;
            case "setowner":
                setOwner(player, args[1]);
                return false;
            case "kick":
                kick(player, args[1]);
                return true;
            case "color":
                if (args.length < 2) return false;
                color(player, args[1]);
                return true;
            case "colors":
                colors(player);
                return true;
            case "description":
                if (args.length < 2) return false;
                description(player, getName(args, 1));
                return true;
            case "leave":
                leave(player);
                return true;
            case "create":
                if (args.length < 3)
                    return false;
                create(player, args[1], getName(args, 2));
                return true;
            case "join":
                if (args.length < 2)
                    return false;
                join(player, args[1]);
                return true;
            case "disband":
                disband(player);
                return true;
            case "rename":
                if (args.length < 3)
                    return false;
                rename(player, args[1], getName(args, 2));
                return true;
            case "invite":
                if (args.length < 2)
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
                if (args.length < 2) {
                    infoPlayerParty(player);
                    return true;
                }
                info(player, PartyAcronym.make(args[1]));
                return true;
            case "list":
                list(player);
                return true;
            case "public":
                if (args.length < 2)
                    return false;
                setPublic(player, args[1]);
                return true;
            default:
                return false;
        }
    }

    private void shareClaim(Player player) {
        Party party = partyHandler.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(MessageConstants.NOT_IN_PARTY);
            return;
        }
        var claim = SQLConn.getClaim(player.getLocation());
        if (claim == null) {
            player.sendMessage(MessageConstants.NO_CLAIM_ERROR);
            return;
        }
        if (!claim.owner.equals(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NOT_CLAIM_OWNER);
        }
        player.sendMessage(MessageConstants.PARTY_CLAIM_SHARE);
        partyHandler.getMembers(party.acronym).forEach(m -> {
            if (!player.getUniqueId().equals(m)) SQLConn.shareClaim(claim, m);
        });
    }

    private void setOwner(Player player, String arg) {
        var target = SQLConn.getUUIDFromUsername(arg);
        if (target == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return;
        }
        if (target.equals(player.getUniqueId())) {
            player.sendMessage("you silly goose, that's yourself!");
            return;
        }
        var ownerParty = partyHandler.getOwnerAcronym(player.getUniqueId());
        if (ownerParty == null) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        var targetPartyOptional = partyHandler.getMemberAcronym(target);
        if (targetPartyOptional.isEmpty()) {
            player.sendMessage(MessageConstants.TARGET_NOT_IN_PARTY);
            return;
        }
        var targetParty = targetPartyOptional.get();
        if (!targetParty.str.equals(ownerParty.str)) {
            player.sendMessage(MessageConstants.TARGET_NOT_IN_PARTY);
            return;
        }

        MessageConstants.sendOnSuccess(
                partyHandler.setOwner(player.getUniqueId(), target),
                player,
                MessageConstants.SUCCESSFUL_PARTY_SETOWNER
        );
    }

    private void colors(Player player) {
        player.sendMessage(MessageConstants.COLORS_HELP);
    }

    private void color(Player player, String col) {
        Party party = partyHandler.getParty(player.getUniqueId());
        char color;
        try {
            color = ChatColor.valueOf(col).getChar();
        } catch (IllegalArgumentException e) {
            color = col.charAt(col.length() - 1);
        }

        if (!partyHandler.isOwner(player.getUniqueId()) || party == null) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (partyHandler.setColor(party.acronym, color) && ChatColor.getByChar(color) != null) {
            player.sendMessage(MessageConstants.SUCCESSFUL_COLOR_SET);
            return;
        }
        player.sendMessage(MessageConstants.COLOR_SET_ERROR);
    }

    private void kick(Player player, String toKick) {
        var toKickUUID = SQLConn.getUUIDFromUsername(toKick);
        if (toKickUUID == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return;
        }
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (partyHandler.kickMember(player.getUniqueId(), toKickUUID)) {
            player.sendMessage(MessageConstants.SUCCESSFUL_PARTY_KICK);
            return;
        }
        player.sendMessage(MessageConstants.TARGET_NOT_IN_PARTY);
    }

    private void description(Player player, String desc) {
        Party party = partyHandler.getParty(player.getUniqueId());
        if (!partyHandler.isOwner(player.getUniqueId()) || party == null) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        if (partyHandler.setDescription(party.acronym, desc)) {
            player.sendMessage(MessageConstants.SUCCESSFUL_DESCRIPTION_SET);
            return;
        }
        player.sendMessage(MessageConstants.DESCRIPTION_SET_ERROR);
    }

    private void setPublic(Player player, String arg) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        arg = arg.toLowerCase();
        if (partyHandler.setPublic(player.getUniqueId(), Arrays.asList(TRUE_STRINGS).contains(arg))) {
            player.sendMessage(MessageConstants.SUCCESSFUL_PUBLIC_SET);
        }
        player.sendMessage(MessageConstants.PUBLIC_SET_ERROR);
    }

    private void list(Player player) {
        List<String> players = partyHandler.getParties().stream().map(Party::toString).collect(Collectors.toList());
        player.sendMessage(ChatColor.YELLOW + "<-- " + ChatColor.RESET + "Parties" + ChatColor.YELLOW + " -->\n" + String.join(ChatColor.GOLD + "\n" + ChatColor.RESET, players));
    }

    private void info(Player player, PartyAcronym acronym) {
        Collection<String> members = partyHandler.getMembersUsernames(acronym);
        String name = partyHandler.getName(acronym);
        String description = partyHandler.getDescription(acronym);
        if (name == null || description == null || members.isEmpty()) {
            player.sendMessage(MessageConstants.CANT_FIND_PARTY);
            return;
        }
        String membersStr = members.stream().limit(10).collect(Collectors.joining(", "));
        player.sendMessage(
                ChatColor.YELLOW + "<-- " + ChatColor.RESET + "Party: " + name + " (" + acronym.str + ")" + ChatColor.YELLOW + " -->\n" + ChatColor.GOLD +
                        "Description: " + ChatColor.RESET + description + "\n" + ChatColor.GOLD +
                        "Members: " + ChatColor.RESET + membersStr);
    }

    private void infoPlayerParty(Player player) {
        partyHandler.getMemberAcronym(player.getUniqueId())
                .ifPresentOrElse(
                        acronym -> info(player, acronym),
                        () -> player.sendMessage(MessageConstants.CANT_FIND_PARTY)
                );
    }

    private void accept(Player player) {
        var invite = invitations.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(MessageConstants.NO_INVITE_ERROR);
            return;
        }
        if (partyHandler.getName(invite) == null) {
            player.sendMessage(MessageConstants.CANT_FIND_PARTY);
            return;
        }
        if (partyHandler.getParty(player.getUniqueId()) != null) {
            player.sendMessage(MessageConstants.ALREADY_IN_PARTY_ERROR);
            return;
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
        var party = partyHandler.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(MessageConstants.NOT_IN_PARTY);
            return;
        }
        if (invitations.containsKey(invitedPlayer)) {
            player.sendMessage(MessageConstants.ALREADY_INVITE_SENT);
            return;
        }
        invitations.put(invitedPlayer, party.acronym);

        player.sendMessage(MessageConstants.SUCCESSFUL_INVITE_SEND);
    }

    private void rename(Player player, String acronym, String name) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }

        MessageConstants.sendOnSuccess(
                partyHandler.rename(player.getUniqueId(), partyHandler.getParty(player.getUniqueId()).acronym, PartyAcronym.make(acronym), name),
                player,
                MessageConstants.SUCCESSFUL_PARTY_RENAME
        );
    }

    private void disband(Player player) {
        if (!partyHandler.isOwner(player.getUniqueId())) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return;
        }
        var acronymOptional = partyHandler.getMemberAcronym(player.getUniqueId());
        if (acronymOptional.isEmpty()) {
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            return;
        }
        var partyAcronym = acronymOptional.get();
        var disbandedParty = partyHandler.disbandParty(player.getUniqueId());
        MessageConstants.sendOnSuccess(
                disbandedParty,
                player,
                MessageConstants.SUCCESSFUL_PARTY_DISBAND
        );
        if (disbandedParty) {
            invitations.values().removeIf(acronym -> acronym.str.equals(partyAcronym.str));
        }
    }

    private void join(Player player, String partyAcronym) {
        if (partyHandler.getParty(player.getUniqueId()) != null) {
            player.sendMessage(MessageConstants.ALREADY_IN_PARTY_ERROR);
            return;
        }
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
        if (!partyHandler.createParty(player.getUniqueId(), PartyAcronym.make(acronym), name)) {
            player.sendMessage(MessageConstants.PARTY_NAME_COLLISION_ERROR);
            return;
        }
        player.sendMessage(MessageConstants.SUCCESSFUL_PARTY_CREATE);
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
        StringBuilder str = new StringBuilder(args[pastIndex]);
        for (int i = pastIndex + 1; i < args.length; i++) {
            str.append(" ").append(args[i]);
        }
        return str.toString();
    }

    private static final String[] SUBCOMMANDS = new String[]{
            "help",
            "setowner",
            "kick",
            "color",
            "colors",
            "description",
            "leave",
            "create",
            "join",
            "disband",
            "rename",
            "invite",
            "accept",
            "deny",
            "info",
            "list",
            "public",
            "shareclaim"
    };

    private static final String[] TRUE_STRINGS = {
            "true",
            "yes",
            "yeah",
            "uh-huh",
            "yep",
            "sure",
            "oui",
            "y",
            "yea",
            "ye",
            "yeehaw",
            "tru",
            "t",
            "ya",
            "uhhuh",
            "okay",
            "ok",
            "k",
            "o.k."
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Stream<String> subCommands = Arrays.stream(SUBCOMMANDS); // you can only call .collect on a stream once so we have to make a new stream every time 
        if (args.length == 0) {
            return subCommands.collect(Collectors.toList());
        } else if (args.length == 1) {
            return subCommands.filter(cmd -> cmd.startsWith(args[0])).collect(Collectors.toList());
        } else if (args.length == 2) {
            switch (args[0]) {
                case "invite":
                case "kick":
                case "setowner":
                    return null;
                case "public":
                    return Arrays.stream(TRUE_STRINGS)
                            .filter(str -> str.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "color":
                    return Arrays.stream(ChatColor.values())
                            .filter(ChatColor::isColor)
                            .map(ChatColor::toString)
                            .filter(str -> str.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "info":
                case "join":
                    return partyHandler.getParties().stream().map(p -> p.acronym.toString()).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

}
