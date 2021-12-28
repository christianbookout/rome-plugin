package romeplugin.newtitle;

import org.bukkit.ChatColor;

public enum Title {
    TRIBUNE("Tribune of the Plebs", ChatColor.DARK_RED, null),
    SENATOR("Quaestor", ChatColor.GREEN, null),
    MAYOR("Aedile", ChatColor.YELLOW, "romeplugin.aedile"),
    JUDGE("Praetor", ChatColor.GOLD, null),
    CONSOLE("Consul", ChatColor.BLUE, null),
    SENSOR("Censor", ChatColor.AQUA, null),
    POPE("Pontifex Maximus", ChatColor.DARK_PURPLE, null),
    BUILDER("Builder", ChatColor.RESET, null),
    CITIZEN("ok and?", ChatColor.RESET, null);

    public final String fancyName;
    public final ChatColor color;
    public final String perms;

    Title(String fancyName, ChatColor color, String perms) {
        this.fancyName = fancyName;
        this.color = color;
        this.perms = perms;
    }

    public static Title getTitle(String title) {
        for (var t : Title.values()) {
            if (t.fancyName.toLowerCase().equals(title)) {
                return t;
            }
        }
        return Title.valueOf(title.toUpperCase());
    }
}
