package romeplugin.zoning;

import org.bukkit.ChatColor;
import romeplugin.newtitle.Title;
import static romeplugin.newtitle.Title.*;

import java.util.Arrays;

public enum ZoneType {
    GOVERNMENT(ChatColor.LIGHT_PURPLE, CONSUL, AEDILE, BUILDER, CENSOR),
    CITY(ChatColor.GOLD, AEDILE, BUILDER, PRAETOR),
    SUBURB(ChatColor.DARK_AQUA),
    WILDERNESS(ChatColor.GREEN);

    private final Title[] titles;
    public final ChatColor color;
    ZoneType(ChatColor color, Title... titles) {
        this.color = color;
        this.titles = titles;
    }

    public Title[] getTitles() {
        return titles;
    }

    public boolean canBuild(Title title) {
        return Arrays.asList(this.titles).contains(title);
    }
}
