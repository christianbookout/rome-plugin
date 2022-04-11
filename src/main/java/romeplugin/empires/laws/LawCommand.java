package romeplugin.empires.laws;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import romeplugin.MessageConstants;
import romeplugin.empires.EmpireHandler;

import java.util.List;
import java.util.stream.Collectors;

public class LawCommand implements CommandExecutor, TabCompleter {
    private static final int MAX_LAWS_TO_SHOW = 10;
    private final EmpireHandler empireHandler;
    private final LawHandler lawHandler;

    public LawCommand(EmpireHandler empireHandler, LawHandler lawHandler) {
        this.empireHandler = empireHandler;
        this.lawHandler = lawHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length <= 0) {
            help(player);
            return true;
        }
        var maybeEmpire = empireHandler.getPlayerEmpireId(player.getUniqueId());
        if (maybeEmpire.isEmpty()) {
            sender.sendMessage("not in empire");
            return true;
        }
        var empireId = maybeEmpire.getAsInt();
        switch (args[0]) {
            case "help":
                help(player);
                return true;
            case "propose":
                if (args.length <= 2) return false;
                return true;
            case "delete":
                if (args.length <= 1) return false;
                return true;
            case "list":
                listLaws(sender, empireId);
                return true;
        }
        return false;
    }

    private void listLaws(CommandSender sender, int empireId) {
        var laws = lawHandler.getLawsList(empireId, MAX_LAWS_TO_SHOW);
        if (laws == null) {
            sender.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
            return;
        }
        var toShow = laws.stream()
                .map(law -> law.getLawType().typePrefix + "-" + law.getNumber() + " " + law.getDescription())
                .collect(Collectors.joining());
        sender.sendMessage(toShow);
    }

    private void help(Player player) {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
