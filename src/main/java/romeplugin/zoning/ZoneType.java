package romeplugin.zoning;

import org.bukkit.ChatColor;

public enum ZoneType {
    GOVERNMENT(ChatColor.LIGHT_PURPLE),
    CITY(ChatColor.GOLD),
    SUBURB(ChatColor.DARK_AQUA),
    WILDERNESS(ChatColor.GREEN);

    public final ChatColor color;

    ZoneType(ChatColor color) {
        this.color = color;
    }
}
