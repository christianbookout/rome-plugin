package romeplugin.messageIntercepter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public final class SwearFilter {

    private static final String SWEAR_URL = "./src/main/resources/swears.csv";
    private static final ArrayList<String> swears = new ArrayList<>();

    private static void generateSwears() {
        try (BufferedReader br = new BufferedReader(new FileReader(SWEAR_URL))) {
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

    public static String replaceSwears(String message) {
        if (swears.isEmpty())
            generateSwears();

        String newMessage = message;
        for (String swear : swears) {
            newMessage = newMessage.replaceAll(swear, repeat('♥', swear.length()));
        }
        return newMessage;
    }
}
