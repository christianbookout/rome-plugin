package romeplugin.webhook;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Embed {
    private String title;
    private String description;
    private int color;
    private final ArrayList<EmbedField> embedFields = new ArrayList<>();

    public Embed(String title, String description, int color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    public String toJSON() {
        var json = "{";
        if (title != null) {
            json += "\"title\":\"" + title + "\",";
        }
        if (description != null) {
            json += "\"description\":\"" + description + "\",";
        }
        if (!embedFields.isEmpty()) {
            json += "\"fields\":[";
            json += embedFields.stream().map(EmbedField::toJson).collect(Collectors.joining(","));
            json += "],";
        }
        json += "\"color\":" + color;
        return json + "}";
    }

}
