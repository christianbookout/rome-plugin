package romeplugin.zoning.claims;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

public class ClaimLandCommand implements CommandExecutor, TabCompleter {
    private final LandControl landControl;

    public ClaimLandCommand(LandControl landControl) {
        this.landControl = landControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            help(sender);
            return true;
        }
        if (args[0].equals("removeall") && sender.isOp()) return removeAllClaims(sender, args[1]);
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args[0].equals("radius")) {
            var r = Integer.parseInt(args[1]);
            var loc = player.getLocation();
            var x0 = loc.getBlockX() - r;
            var y0 = loc.getBlockZ() + r;
            var x1 = loc.getBlockX() + r;
            var y1 = loc.getBlockZ() - r;
            return landControl.tryClaimLand(player, x0, y0, x1, y1);
        } else if (args[0].equals("share") && args.length >= 2) {
            return shareClaim(player, args[1]);
        } else if (args[0].equals("remove")) {
            return removeClaim(player);
        } else if (args[0].equals("transfer") && args.length >= 2) {
            return transferClaim(player, args[1]);
        } else if (args[0].equals("unshare") && args.length >= 2) {
            return unshareClaim(player, args[1]);
        } else if (args[0].equals("help")) {
            help(player);
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
            return landControl.tryClaimLand(player, xa, ya, xb, yb);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void help(CommandSender sender) {
        sender.sendMessage(MessageConstants.CLAIMS_HELP_COMMAND);
    }
    /**
     * Force remove all claims for player with name arg
     * @param sender
     * @param arg
     * @return if it worked
     */
    private boolean removeAllClaims(CommandSender sender, String arg) {
        var target = SQLConn.getUUIDFromUsername(arg);
        String targetName = SQLConn.getUsername(target);
        if (target == null) {
            sender.sendMessage(MessageConstants.CANT_FIND_PLAYER);
            return true;
        }
        return removeAllClaims(sender, target, targetName);
    }
    //test if it is possible for the player
    private boolean removeAllClaims(Player player, String arg) {
        var target = SQLConn.getUUIDFromUsername(arg);
        String targetName = SQLConn.getUsername(target);
        if (!player.isOp() && !player.getUniqueId().equals(target)) {
            player.sendMessage(MessageConstants.NO_PERMISSION_ERROR);
            return true;
        }
        return removeAllClaims(player, target, targetName);
    }

    // actually do the removing
    private boolean removeAllClaims(CommandSender sender, UUID target, String targetName) {
        try (var conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM cityClaims WHERE owner_uuid = ?;");
            stmt.setString(1, target.toString());
            stmt.execute();
            stmt.close();
            sender.sendMessage("Successfully deleted all of " + targetName + "'s claims");
        } catch (SQLException e) {
            sender.sendMessage("oopsies! we're vewwy sowwy!! o(╥﹏╥)o something went wrong...");
            e.printStackTrace();
        }
        return true;
    }

    private boolean transferClaim(Player player, String arg) {
        var loc = player.getLocation();
        var target = arg;
        var targetUUID = SQLConn.getUUIDFromUsername(target);
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
        player.sendMessage("transferred claim to " + target);
        return true;
    }

    private boolean removeClaim(Player player) {
        var loc = player.getLocation();
        var claim = SQLConn.getClaim(loc.getBlockX(), loc.getBlockZ());
        if (claim == null) {
            player.sendMessage("no claim here");
            return false;
        }
        if (!player.isOp() && !claim.owner.equals(player.getUniqueId())) {
            player.sendMessage("insufficient permissions");
            return false;
        }
        if (!SQLConn.removeClaim(claim)) {
            player.sendMessage("database error!");
            return false;
        }
        player.sendMessage("successfully removed claim");
        return true;
    }

    //TODO make a method that makes it so we arent copy-pasting shareClaim into unshareClaim
    private boolean shareClaim(Player player, String arg) {
        var loc = player.getLocation();
        var target = arg;
        var targetUUID = SQLConn.getUUIDFromUsername(target);
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
        player.sendMessage("added " + target + " to your claim");
        return true;
    }

    private boolean unshareClaim(Player player, String arg) {
        var loc = player.getLocation();
        var target = arg;
        var targetUUID = SQLConn.getUUIDFromUsername(target);
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
        player.sendMessage("removed " + target + " from your claim");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Arrays.asList("owned");
    }
}
