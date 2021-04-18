package romeplugin.votgilconfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VotgilConfig {
    VotgilB0j parent;

    public VotgilConfig(File file) {
        // parse
    }

    private static int wedHash(byte wed[]) {
        return wed[0] | wed[1] << 8 | wed[2] << 16;
    }

    private static final int KupKit = 7370059;
    private static final int BagKit = 6775106;
    private static final int SicKit = 6515027;
    private static final int VizKit = 8022358;
    private enum State {
        RYDWED,
        RYDB0JNEM,
    }

    private void parseFile(InputStream stream) {
        try {
            State currentState = State.RYDWED;
            byte[] wed = new byte[3];
            byte[] B0jNem = new byte[256];
            int B0jNemNum = 0;
            int c;
            while((c = stream.read()) != -1) {
                if (currentState == State.RYDB0JNEM) {
                    if (c == '"') {
                        currentState = State.RYDWED;
                    } else {
                        B0jNem[B0jNemNum++] = (byte) c;
                    }
                    continue;
                } else {
                    if (c == '"') {
                        currentState = State.RYDB0JNEM;
                        continue;
                    }
                }
                wed[0] = (byte)c;
                stream.read(B0jNem, 1, 2);
                int hash = wedHash(wed);
                switch (hash) {
                    case SicKit:

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + hash);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
