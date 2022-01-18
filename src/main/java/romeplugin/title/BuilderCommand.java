package romeplugin.title;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import romeplugin.MessageConstants;
import romeplugin.database.SQLConn;

import java.sql.SQLException;

public class BuilderCommand implements CommandExecutor {
    private final TitleHandler titles;

    public BuilderCommand(TitleHandler titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (args.length == 1 && args[0].equals("list")) {
            var builders = SQLConn.getBuilders();
            if (builders == null) {
                sender.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
                return true;
            }
            sender.sendMessage("builders: ");
            sender.sendMessage(String.join(", ", builders));
            return true;
        } else if (args.length < 2) {
            return false;
        }
        var target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("player not found");
            return false;
        }
        switch (args[0]) {
            case "assign":
                try {
                    if (SQLConn.isBuilder(target.getUniqueId())) {
                        sender.sendMessage("they are already a builder, silly!");
                        return true;
                    }
                    SQLConn.setBuilder(target.getUniqueId());
                    sender.sendMessage("made " + target.getName() + " a builder");
                } catch (SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
                }
                return true;
            case "revoke":
                try {
                    if (SQLConn.removeBuilder(target.getUniqueId())) {
                        sender.sendMessage("made " + target.getName() + " not a builder");
                    } else {
                        sender.sendMessage("this person wasn't even a builder!");
                    }
                } catch (SQLException e) {
                    sender.sendMessage(MessageConstants.UWU_DATABASE_ERROR);
                    e.printStackTrace();
                }
                return true;
        }
        return false;
    }
}
