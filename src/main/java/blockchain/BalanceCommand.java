package blockchain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {
    private final Ledger ledger;

    public BalanceCommand(Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player sender = (Player)commandSender;
        if (sender == null) {
            return false;
        }
        commandSender.sendMessage("balance: " + ledger.getBalance(sender.getUniqueId()) + " coins");
        return true;
    }
}
