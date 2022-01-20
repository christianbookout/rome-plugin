package romeplugin.election;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import romeplugin.title.Title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ElectionTabCompleter implements TabCompleter {
    private final String[] subCommands = {
            "run",
            "quit",
            "help",
            "results",
            "candidates",
            "vote",
            "seevotes",
            "phase",
            "start",
            "voting",
            "end",
            "cancel"
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            return Arrays.asList(subCommands);
        } else if (args.length == 1) {
            var complete = new ArrayList<>(Arrays.asList(subCommands));
            complete.removeIf(str -> !str.startsWith(args[0]));
            return complete;
        } else if (args.length == 2) {
            switch (args[0]) {
                case "run":
                    return Arrays.stream(Title.values())
                            .map(Enum::toString)
                            .filter(title -> title.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "vote":
                    // return default list of online players
                    return null;
            }
        }
        return Collections.emptyList();
    }
}
