package romeplugin.zoning.claims;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;
import romeplugin.zoning.CityManager;

import java.sql.SQLException;
import java.util.*;

public class ClaimLandCommand implements CommandExecutor, TabCompleter {
    private final CityManager city;
    private final Plugin plugin;

    public ClaimLandCommand(CityManager city, Plugin plugin) {
        this.city = city;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            help(sender);
            return true;
        }
        if (args[0].equals("removeall") && sender.isOp() && args.length >= 2) return removeAllClaims(sender, args[1]);
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args[0].equals("radius")) {
            var r = Integer.parseInt(args[1]);
            var loc = player.getLocation();
            var x0 = loc.getBlockX() - r;
            var y0 = loc.getBlockZ() + r;
            var x1 = loc.getBlockX() + r;
            var y1 = loc.getBlockZ() - r;
            return city.tryClaimLand(player, x0, y0, x1, y1);
        } else if (args[0].equals("share") && args.length >= 2) {
            return shareClaim(player, args[1]);
        } else if (args[0].equals("remove")) {
            return removeClaim(player);
        } else if (args[0].equals("removeall")) {
            return removeAllClaims(player, player.getUniqueId(), "you");
        } else if (args[0].equals("transfer") && args.length >= 2) {
            return transferClaim(player, args[1]);
        } else if (args[0].equals("unshare") && args.length >= 2) {
            return unshareClaim(player, args[1]);
        } else if (args[0].equals("help")) {
            help(player);
            return true;
        } else if (args[0].equals("list")) {
            listClaims(player);
            return true;
        } else if (args[0].equals("info")) {
            claimInfo(player);
            return true;
        }
        if (args.length < 4) {
            return false;
        }
        try {
            var xa = Integer.parseInt(args[0]);
            var ya = Integer.parseInt(args[1]);
            var xb = Integer.parseInt(args[2]);
            var yb = Integer.parseInt(args[3]);
            return city.tryClaimLand(player, xa, ya, xb, yb);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static void claimInfo(Player player) {
        var loc = player.getLocation();
        var claim = SQLConn.getClaim(loc.getBlockX(), loc.getBlockZ());
        if (claim == null) {
            player.sendMessage("no claim here!");
            return;
        }
        var owner_username = SQLConn.getUsername(claim.owner);
        if (owner_username == null) {
            owner_username = claim.owner.toString();
        }
        var msg = "claim owner: " + owner_username +
                "\nsize: " + claim.getLength() + "x" + claim.getHeight() + " (" + claim.getArea() + " blocks)" +
                "\nfrom (" + claim.x0 + ", " + claim.y0 + ") to (" + claim.x1 + ", " + claim.y1 + ")";

        var shared = SQLConn.claimSharedWithUsernames(claim);
        if (shared != null && !shared.isEmpty()) {
            msg += "\nshared with: " + String.join(", ", shared);
        }

        player.sendMessage(msg);
    }

    private void listClaims(Player player) {
        var claims = SQLConn.getClaimsBy(player.getUniqueId());
        if (claims == null) {
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            return;
        } else if (claims.isEmpty()) {
            player.sendMessage("you don't have any claims!");
            return;
        }

        var sb = new StringBuilder();
        for (var claim : claims) {
            sb.append("claim from (")
                    .append(claim.x0)
                    .append(", ")
                    .append(claim.y0)
                    .append(") to (")
                    .append(claim.x1)
                    .append(", ")
                    .append(claim.y1)
                    .append(")\n");
            var sharedWith = SQLConn.claimSharedWithUsernames(claim);
            if (sharedWith == null) {
                player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
                return;
            }
            if (!sharedWith.isEmpty()) {
                sb.append("shared with: ").append(String.join(", ", sharedWith)).append('\n');
            }
        }
        player.sendMessage(sb.toString());
    }

    private void help(CommandSender sender) {
        sender.sendMessage(MessageConstants.CLAIMS_HELP_COMMAND);
    }

    /**
     * Force remove all claims for player with name arg
     *
     * @param sender         sender trying to remove claims
     * @param targetUsername username of owner of the claims to remove
     * @return if it worked
     */
    private boolean removeAllClaims(CommandSender sender, String targetUsername) {
        var target = SQLConn.getUUIDFromUsername(targetUsername);
        if (target == null) {
            sender.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return true;
        }
        String targetName = SQLConn.getUsername(target);
        return removeAllClaims(sender, target, targetName);
    }

    // actually do the removing
    private boolean removeAllClaims(CommandSender sender, UUID target, String targetName) {
        try (var conn = SQLConn.getConnection()) {
            SQLConn.removeAllShared(target);
            var stmt2 = conn.prepareStatement("DELETE FROM cityClaims WHERE owner_uuid = ?;");
            stmt2.setString(1, target.toString());
            stmt2.execute();
            stmt2.close();
            sender.sendMessage("Successfully deleted all of " + targetName + "'s claims");
        } catch (SQLException e) {
            sender.sendMessage("oopsies! we're vewwy sowwy!! o(╥﹏╥)o something went wrong...");
            e.printStackTrace();
        }
        return true;
    }

    private boolean transferClaim(Player player, String targetUsername) {
        var loc = player.getLocation();
        var targetUUID = SQLConn.getUUIDFromUsername(targetUsername);
        if (targetUUID == null) {
            player.sendMessage("invalid username");
            return false;
        }
        var claim = SQLConn.getClaim(loc);
        if (claim == null) {
            player.sendMessage("no claim here");
            return false;
        }
        if (!claim.owner.equals(player.getUniqueId())) {
            player.sendMessage("not your claim");
            return false;
        }
        if (!SQLConn.updateClaimOwner(claim, targetUUID)) {
            player.sendMessage("database error!");
            return false;
        }
        player.sendMessage("transferred claim to " + targetUsername);
        return true;
    }

    private boolean removeClaim(Player player) {
        var loc = player.getLocation();
        var claim = SQLConn.getClaim(loc.getBlockX(), loc.getBlockZ());
        if (claim == null) {
            player.sendMessage("no claim here");
            return true;
        }
        if (!player.isOp() && !claim.owner.equals(player.getUniqueId())) {
            player.sendMessage("insufficient permissions");
            return true;
        }
        if (!SQLConn.removeClaim(claim)) {
            player.sendMessage("database error!");
            return true;
        }
        if (!SQLConn.unshareClaim(claim)) {
            player.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            return true;
        }
        player.sendMessage("successfully removed claim");
        return true;
    }

    //TODO make a method that makes it so we arent copy-pasting shareClaim into unshareClaim
    private boolean shareClaim(Player player, String targetUsername) {
        var loc = player.getLocation();
        var targetUUID = SQLConn.getUUIDFromUsername(targetUsername);
        if (targetUUID == null) {
            player.sendMessage("invalid username");
            return false;
        }
        var claim = SQLConn.getClaim(loc);
        if (claim == null) {
            player.sendMessage("no claim here");
            return false;
        }
        if (!claim.owner.equals(player.getUniqueId())) {
            player.sendMessage("not your claim");
            return false;
        }
        if (!SQLConn.shareClaim(claim, targetUUID)) {
            player.sendMessage("already shared with this player");
            return true;
        }
        player.sendMessage("added " + targetUsername + " to your claim");
        return true;
    }

    private boolean unshareClaim(Player player, String targetUsername) {
        var loc = player.getLocation();
        var targetUUID = SQLConn.getUUIDFromUsername(targetUsername);
        if (targetUUID == null) {
            player.sendMessage("invalid username");
            return false;
        }
        var claim = SQLConn.getClaim(loc);
        if (claim == null) {
            player.sendMessage("no claim here");
            return false;
        }
        if (!claim.owner.equals(player.getUniqueId())) {
            player.sendMessage("not your claim");
            return false;
        }
        if (!SQLConn.unshareClaim(claim, targetUUID)) {
            player.sendMessage("database error!");
            return false;
        }
        player.sendMessage("removed " + targetUsername + " from your claim");
        return true;
    }

    private final List<String> subcommands = Arrays.asList(
            "radius",
            "share",
            "remove",
            "transfer",
            "unshare",
            "help",
            "info",
            "list"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return subcommands;
        } else if (args.length == 1) {
            var started = new ArrayList<>(subcommands);
            if (sender.isOp()) {
                started.add("removeall");
            }
            started.removeIf(str -> !str.startsWith(args[0]));
            return started;
        } else if (args.length == 2) {
            switch (args[1]) {
                case "transfer":
                case "share":
                case "unshare":
                    return null;
                case "removeall":
                    if (sender.isOp()) return null;
                    break;
            }
        }
        return Collections.emptyList();
    }
}
