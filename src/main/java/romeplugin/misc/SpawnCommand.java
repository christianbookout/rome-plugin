package romeplugin.misc;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.zoning.claims.LandControl;

public class SpawnCommand implements CommandExecutor {
    private final LandControl landControl;

    public SpawnCommand(LandControl landControl) {
        this.landControl = landControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        // TODO: some kind timer for teleporting
        var player = (Player) sender;
        var pos = player.getWorld().getHighestBlockAt(landControl.getCenterX(), landControl.getCenterY());
        // have to add go up one otherwise player spawns in the ground
        var newLoc = pos.getLocation().add(0, 1, 0);
        newLoc.setYaw(player.getLocation().getYaw());
        newLoc.setPitch(player.getLocation().getPitch());

        player.teleport(newLoc);

        player.sendMessage(ChatColor.BLUE.toString() + "splash!");
        player.spawnParticle(Particle.WATER_SPLASH, newLoc.add(0, 1, 0), 200);
        player.playSound(newLoc, Sound.AMBIENT_UNDERWATER_ENTER, 100, 100);
        return true;
    }
    
}
