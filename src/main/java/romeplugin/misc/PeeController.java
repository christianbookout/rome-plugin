package romeplugin.misc;

import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class PeeController implements CommandExecutor, Listener {
    private final Plugin plugin;

    //speed after pee for 30 seconds (times 20 ticks per second)
    private static final int PEE_TICKS = 30 * 20; //TODO get server ticks instead of using default of 20
    private static final int PEE_SELF_TIME = 120 * 20;

    //level of speed to give player after they pee
    private static final int PEE_AMPLIFIER = 0;

    private final Map<Player, BukkitTask> canPeePlayers = new HashMap<>();

    public PeeController(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (!(arg0 instanceof Player)) return false;
        Player player = (Player) arg0;

        //if the player is not in the canPeePlayers list then return
        var task = canPeePlayers.remove(player);
        if (task == null) {
            return false;
        }

        task.cancel();
        player.sendMessage(ChatColor.YELLOW + "ahhh... you feel relieved.");
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PEE_TICKS, PEE_AMPLIFIER));
        return true;
    }

    @EventHandler
    public void onWaterDrink(PlayerItemConsumeEvent e) {
        if (!(e.getItem().getItemMeta() instanceof PotionMeta))
            return;

        var potionMeta = (PotionMeta) e.getItem().getItemMeta();

        if (potionMeta.getBasePotionData().getType().equals(PotionType.WATER)) {
            var task = plugin.getServer().getScheduler().runTaskLater(plugin, new PissTask(e.getPlayer()), PEE_SELF_TIME);
            canPeePlayers.put(e.getPlayer(), task);
        }
    }

    private static class PissTask implements Runnable {
        private final Player who;

        private PissTask(Player who) {
            this.who = who;
        }

        @Override
        public void run() {
            // TODO: wet pants more?
            who.sendMessage(ChatColor.YELLOW + "you've peed yourself :(");
            who.playNote(who.getLocation(), Instrument.DIDGERIDOO, Note.flat(4, Note.Tone.C));
        }
    }
}
