package romeplugin.title;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import romeplugin.database.SQLConn;

public class BuilderCommand implements CommandExecutor {
    private final TitleHandler titles;

    public BuilderCommand(TitleHandler titles) {
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 2) {
            return false;
        }
        var target = sender.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("player not found");
            return false;
        }
        switch (args[0]) {
            case "assign":
                if (SQLConn.getTitle(target.getUniqueId()) != null) {
                    sender.sendMessage("they already have a job silly!");
                    return false;
                }
                titles.setTitle(target, Title.BUILDER);
                sender.sendMessage("made " + target.getName() + " a builder");
                return true;
            case "revoke":
                var title = SQLConn.getTitle(target.getUniqueId());
                if (title != null && title.t == Title.BUILDER) {
                    titles.removeTitle(target);
                }
                sender.sendMessage("made " + target.getName() + " not a builder");
                return true;
        }
        return false;
    }
}
