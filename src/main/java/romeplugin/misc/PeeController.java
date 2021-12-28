package romeplugin.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PeeController implements CommandExecutor, Listener{

    //speed after pee for 30 seconds (times 20 ticks per second)
    private static final int PEE_TICKS = 30 * 20;

    //level of speed to give player after they pee
    private static final int PEE_AMPLIFIER = 1;

    private static final List<Player> canPeePlayers = new ArrayList<Player>();

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (!(arg0 instanceof Player)) return false;
        Player player = (Player) arg0;

        player.sendMessage(ChatColor.YELLOW + "ahhh... you feel relieved.");
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PEE_TICKS, PEE_AMPLIFIER));

        canPeePlayers.remove(player);
        return true;
    }

    //TODO make it so you wet your pants if you don't pee 
    @EventHandler
    public void onWaterDrink(PlayerItemConsumeEvent e) {
        if (!(e.getItem().getItemMeta() instanceof PotionMeta)) 
            return;

        var potionMeta = (PotionMeta) e.getItem().getItemMeta();

        if (potionMeta.getBasePotionData().getType().equals(PotionType.WATER))
            canPeePlayers.add(e.getPlayer());
    }
    
}
