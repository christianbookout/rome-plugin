package romeplugin.newtitle;

import org.bukkit.ChatColor;

public enum Title {
    TRIBUNE("Tribune of the Plebs", ChatColor.DARK_RED, "romeplugin.tribune"),
    QUAESTOR("Quaestor", ChatColor.GREEN, "romeplugin.quaestor"),
    AEDILE("Aedile", ChatColor.YELLOW, "romeplugin.aedile"),
    PRAETOR("Praetor", ChatColor.GOLD, "romeplugin.praetor"),
    CONSUL("Consul", ChatColor.BLUE, "romeplugin.consul"),
    CENSOR("Censor", ChatColor.AQUA, "romeplugin.censor"),
    POPE("Pontifex Maximus", ChatColor.DARK_PURPLE, "romeplugin.pope"),
    BUILDER("Builder", ChatColor.RESET, "romeplugin.builder"),
    CITIZEN("ok and?", ChatColor.RESET, "romeplugin.okand?");

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
        try {
            return Title.valueOf(title.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
