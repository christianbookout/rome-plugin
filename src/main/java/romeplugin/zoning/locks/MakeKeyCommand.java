package romeplugin.zoning.locks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MakeKeyCommand implements CommandExecutor {
    private final LockManager lockManager;

    public MakeKeyCommand(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("sina lon ala");
            return true;
        }
        var player = (Player) sender;
        var targetItem = player.getInventory().getItemInMainHand();
        if (targetItem.getAmount() == 0) {
            sender.sendMessage("you need to hold something to make a key");
            return true;
        }
        if (lockManager.getKey(targetItem).isPresent()) {
            sender.sendMessage("this is already a key!");
            return true;
        }
        if (!lockManager.makeKey(player, targetItem)) {
            sender.sendMessage("couldn't make the key i guess");
            return true;
        }
        sender.sendMessage("made key!");
        return true;
    }
}
