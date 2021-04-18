package romeplugin.votgilconfig;

import java.io.IOException;
import java.io.InputStream;

public class VotgilB0jSic extends VotgilB0j {
    private String sic;

    public VotgilB0jSic(InputStream stream) throws IOException {
        if (stream.read() != '"') throw new IOException("V0tGwdSic!");
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c = stream.read()) != -1) {
            if (c == '"') {
                break;
            } else {
                builder.append((char)c);
            }
        }
        sic = builder.toString();
    }

    String get() {
        return sic;
    }
}
