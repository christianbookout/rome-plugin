package romeplugin.messageIntercepter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.entity.Player;
import romeplugin.zoning.LandControl;

public final class SwearFilter {

    private static final InputStream SWEAR_STREAM = SwearFilter.class.getResourceAsStream("/swears.csv");
    private static final ArrayList<String> swears = new ArrayList<>();
    private final LandControl controller;
    
    //0 for off, 1 for only city/government, 2 for everywhere
    private final int swearLevel;

    public SwearFilter(LandControl controller, int swearLevel) {
        this.controller = controller;
        this.swearLevel = swearLevel;
    }

    private static void generateSwears() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(SWEAR_STREAM))) {
            br.lines().forEach(line -> {
                String[] values = line.split(", ");
                swears.addAll(Arrays.asList(values));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String repeat(char c, int amount) {
        char[] toReturn = new char[amount];
        Arrays.fill(toReturn, c);
        return new String(toReturn);
    }

    //Return whether or not the current player should be swear-filtered 
    public boolean doFilter(Player p) {
        return swearLevel == 2 || (controller.inCity(p.getLocation()) && swearLevel == 1);
    }

    public String replaceSwears(String message) {
        if (swears.isEmpty())
            generateSwears();

        String newMessage = message;
        for (String swear : swears) {
            newMessage = newMessage.replaceAll("\\b(?i)" + swear + "\\b", repeat('♥', swear.length()));
        }
        return newMessage;
    }
}
