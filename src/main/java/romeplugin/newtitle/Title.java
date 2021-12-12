package romeplugin.newtitle;

import org.bukkit.ChatColor;

public enum Title {
    TRIBUNE("Tribune of the Plebs", ChatColor.DARK_RED),
    SENATOR("Quaestor", ChatColor.GREEN),
    MAYOR("Aedile", ChatColor.YELLOW),
    JUDGE("Praetor", ChatColor.GOLD),
    CONSOLE("Consul", ChatColor.BLUE),
    SENSOR("Censor", ChatColor.AQUA),
    POPE("Pontifex Maximus", ChatColor.DARK_PURPLE),
    BUILDER("Builder", ChatColor.RESET),
    CITIZEN("ok and?", ChatColor.RESET);

    public final String fancyName;
    public final ChatColor color;

    Title(String fancyName, ChatColor color) {
        this.fancyName = fancyName;
        this.color = color;
    }

    public static Title getTitle(String title) {
        return Title.valueOf(title.toUpperCase());
    }
}
