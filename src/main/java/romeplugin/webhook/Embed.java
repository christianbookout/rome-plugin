package romeplugin.webhook;

import java.util.ArrayList;

import org.json.JSONObject;

public class Embed {
    private String name;
    private String color;
    private ArrayList<EmbedField> embedFields = new ArrayList<>();
    public Embed(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public JSONObject toJSON() {


        return new JSONObject();
    }

}
