package romeplugin.title;

import org.bukkit.ChatColor;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class RomeTitles {
    private final HashMap<UUID, Title> playerTitles;
    private final HashMap<String, Title> knownTitles;

    public RomeTitles() {
        playerTitles = new HashMap<>();
        knownTitles = new HashMap<>();
    }

    public boolean isTitle(String title) {
        return knownTitles.containsKey(title);
    }

    public void addTitle(String title) {
        addTitle(title, ChatColor.WHITE);
    }

    public void addTitle(String title, ChatColor color) {
        knownTitles.put(title, new Title(title, color));
    }

    public Title getKnownTitle(String title) {
        return knownTitles.get(title);
    }
    public Collection<Title> getAllKnownTitles() { return knownTitles.values(); }

    public Title getPlayerTitle(UUID uuid) {
        return playerTitles.get(uuid);
    }

    public void setPlayerTitle(UUID uuid, String title) {
        playerTitles.put(uuid, getKnownTitle(title));
    }

    public void saveData(DataOutputStream stream) {
        playerTitles.forEach((id,title)-> {
            try {
                stream.writeLong(id.getMostSignificantBits());
                stream.writeLong(id.getLeastSignificantBits());
                stream.writeUTF(title.name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadData(DataInputStream stream) {
        try {
            while (stream.available() > 0) {
                UUID id = new UUID(stream.readLong(), stream.readLong());
                String titleName = stream.readUTF();
                playerTitles.put(id, getKnownTitle(titleName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
