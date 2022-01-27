package romeplugin.webhook;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class Webhooks {
    private String url;
    private String name;
    private String avatar_url = null;

    public Webhooks(String url, String name, String avatar_url) {
        this.url = url;
        this.name = name;
        this.avatar_url = avatar_url;
    }

    public Webhooks(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public void sendMessage(String message) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.addRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("User-Agent", "romeplugin");
            OutputStream output = conn.getOutputStream();
            output.write(("{\"content\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEmbeds(Collection<Embed> embeds) {
        if (embeds.size() > 10) {
            System.err.println("too many embeds!!");
            return;
        }
        StringBuilder embedStr = new StringBuilder("[");
        for (var embed : embeds) {
            embedStr.append(embed.toJSON());
        }
        embedStr.append("]");
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.addRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("User-Agent", "romeplugin");
            OutputStream output = conn.getOutputStream();
            output.write(("{\"embeds\":\"" + embedStr + "\"}").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
