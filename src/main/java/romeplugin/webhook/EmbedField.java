package romeplugin.webhook;

public class EmbedField {
    public String name;
    public String value;
    public boolean inline = false;

    public String toJson() {
        return "{\"name\":\"" + name + "\",\"value\":\"" + value + "\",\"inline\":" + inline + "}";
    }
}
