package romeplugin.messaging;

import org.bukkit.entity.Player;
import romeplugin.zoning.CityManager;
import romeplugin.zoning.ZoneType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class SwearFilter {
    private static final InputStream SWEAR_STREAM = SwearFilter.class.getResourceAsStream("/swears.csv");
    private static final char SWEAR_REPLACE = '♥';
    private final CityManager controller;
    private static final Pattern swear_pattern = generateSwears();

    public enum SwearLevel {
        NEVER,
        SUBURBS,
        CITY,
        GOVERNMENT,
        ALWAYS
    }

    private final SwearLevel swearLevel;

    public SwearFilter(CityManager controller, SwearLevel swearLevel) {
        this.controller = controller;
        this.swearLevel = swearLevel;
    }

    private static Pattern generateSwears() {
        if (SWEAR_STREAM == null) {
            return null;
        }
        var regex = new StringBuilder("\\b(");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(SWEAR_STREAM))) {
            br.lines().forEach(line -> regex.append(line.replace(", ", "|")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        regex.append(")\\b");
        try {
            return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException exception) {
            System.err.println("could not compile swear regex!");
            System.err.println(exception.getMessage());
            return null;
        }
    }

    private static String repeat(int amount) {
        char[] toReturn = new char[amount];
        Arrays.fill(toReturn, SWEAR_REPLACE);
        return new String(toReturn);
    }

    //Return whether the current player should be swear-filtered
    public boolean doFilter(Player p) {
        if (swearLevel == SwearLevel.NEVER) return false;
        else if (swearLevel == SwearLevel.ALWAYS) return true;

        // TODO: maybe allow cities to choose swearing levels?
        ZoneType zone = controller.getArea(p.getLocation()).getType();
        return (zone == ZoneType.SUBURB && swearLevel == SwearLevel.SUBURBS) ||
                (zone == ZoneType.CITY && swearLevel == SwearLevel.CITY) ||
                (zone == ZoneType.GOVERNMENT && swearLevel == SwearLevel.GOVERNMENT);
    }

    public String replaceSwears(String originalMessage) {
        if (swear_pattern == null) {
            return originalMessage;
        }
        var matcher = swear_pattern.matcher(originalMessage);
        return matcher.replaceAll(match -> repeat(match.end() - match.start()));
    }
}
