package romeplugin.title;

import org.bukkit.Server;

import java.util.HashMap;
import java.util.UUID;

public class RomeTitles {
    private final HashMap<UUID, Title> playerTitles;
    private final HashMap<String, Title> knownTitles;

    public RomeTitles() {
        playerTitles = new HashMap<>();
        knownTitles = new HashMap<>();
        addTitle("Praetor");
        addTitle("Tribune of the Plebs");
    }

    public boolean isTitle(String title) {
        return knownTitles.containsKey(title);
    }

    public void addTitle(String title) {
        knownTitles.put(title, new Title(title));
    }

    public Title getKnownTitle(String title) {
        return knownTitles.get(title);
    }

    public Title getPlayerTitle(UUID uuid) {
        return playerTitles.get(uuid);
    }

    public void setPlayerTitle(UUID uuid, String title) {
        playerTitles.put(uuid, getKnownTitle(title));
    }
}
