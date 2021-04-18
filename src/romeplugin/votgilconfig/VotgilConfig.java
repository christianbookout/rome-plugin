package romeplugin.votgilconfig;

import java.io.*;

public class VotgilConfig {
    private final VotgilB0jBag Per;

    public VotgilConfig(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public VotgilConfig(InputStream stream) throws IOException {
        Per = new VotgilB0jBag(stream);
    }
}