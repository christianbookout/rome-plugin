package romeplugin.messaging;

import org.bukkit.entity.Player;
import romeplugin.zoning.ZoneType;
import romeplugin.zoning.claims.LandControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public final class SwearFilter {
    private static final InputStream SWEAR_STREAM = SwearFilter.class.getResourceAsStream("/swears.csv");
    private static final char SWEAR_REPLACE = '♥';
    private final ArrayList<String> swears = new ArrayList<>();
    private final LandControl controller;

    public enum SwearLevel {
        NEVER, 
        SUBURBS, 
        CITY, 
        GOVERNMENT, 
        ALWAYS
    }
    private final SwearLevel swearLevel;

    public SwearFilter(LandControl controller, SwearLevel swearLevel) {
        this.controller = controller;
        this.swearLevel = swearLevel;
        generateSwears();
    }

    private void generateSwears() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(SWEAR_STREAM))) {
            br.lines().forEach(line -> {
                String[] values = line.split(", ");
                swears.addAll(Arrays.asList(values));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String repeat(int amount) {
        char[] toReturn = new char[amount];
        Arrays.fill(toReturn, SWEAR_REPLACE);
        return new String(toReturn);
    }

    //Return whether or not the current player should be swear-filtered 
    public boolean doFilter(Player p) {
        if (swearLevel == SwearLevel.NEVER) return false;
        else if (swearLevel == SwearLevel.ALWAYS) return true;

        ZoneType zone = controller.getArea(p.getLocation()).getType();
        return  (zone == ZoneType.SUBURB && swearLevel == SwearLevel.SUBURBS) ||
                (zone == ZoneType.CITY && swearLevel == SwearLevel.CITY) ||
                (zone == ZoneType.GOVERNMENT && swearLevel == SwearLevel.GOVERNMENT);
    }

    public String replaceSwears(String message) {
        String newMessage = message;
        for (String swear : swears) {
            newMessage = newMessage.replaceAll("\\b(?i)" + swear + "\\b", repeat(swear.length()));
        }
        return newMessage;
    }
}
