package romeplugin.messageIntercepter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public final class SwearFilter{

    private static final String SWEAR_URL = "./src/main/resources/swears.csv";
    private static String[] swears;

    private static final void generateSwears() {
        ArrayList<String> swears = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SWEAR_URL))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(", ");
                Arrays.asList(values).forEach(swears::add);
            }
        } catch (NullPointerException | IOException  e) {
            e.printStackTrace();
        }
        SwearFilter.swears = swears.toArray(new String[0]);
    }

    private static final String repeat(char c, int amount) {
        char[] toReturn = new char[amount];
        Arrays.fill(toReturn, c);
        return new String(toReturn);
    }

    public static String replaceSwears(String message) {
        if (SwearFilter.swears == null) 
            generateSwears();
            
        String newMessage = message;
        for (String swear: swears) {
            newMessage = newMessage.replaceAll(swear, repeat('♥', swear.length()));
        }
        return newMessage;
    }
}
