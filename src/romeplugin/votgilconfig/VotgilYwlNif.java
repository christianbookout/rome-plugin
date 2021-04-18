package romeplugin.votgilconfig;

import java.io.IOException;
import java.io.InputStream;

public class VotgilYwlNif {
    public static final int KupKit = 7370059;
    public static final int BagKit = 6775106;
    public static final int SicKit = 6515027;
    public static final int DunKit = 7238980;

    public static int wedHash(byte[] wed) {
        return (int)wed[0] | (int)wed[1] << 8 | (int)wed[2] << 16;
    }

    public static VotgilB0j rydB0j(int wedHash, InputStream stream) throws IOException {
        switch (wedHash) {
            case VotgilYwlNif.SicKit:
                return new VotgilB0jSic(stream);
            case VotgilYwlNif.BagKit:
                return new VotgilB0jBag(stream);
            case VotgilYwlNif.KupKit:
                return new VotgilB0jKup(stream);
        }
        throw new IOException("How");
    }
}
