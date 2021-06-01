package blockchain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
    private final Ledger currentLedger;

    public PayCommand(Ledger currentLedger) {
        this.currentLedger = currentLedger;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player sender = (Player)commandSender;
        if (sender == null) {
            return false;
        }
        Player receiver = commandSender.getServer().getPlayer(args[0]);
        if (receiver == null) {
            return false;
        }
        if (!currentLedger.enqueueTransaction(new Transaction(sender.getUniqueId(), receiver.getUniqueId(), Float.parseFloat(args[1])))) {
            commandSender.sendMessage("you don't have enough money!");
            return false;
        }
        commandSender.sendMessage("payment enqueued");
        return true;
    }
}
