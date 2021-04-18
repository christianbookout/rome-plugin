package romeplugin.votgilconfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class VotgilB0jKup extends VotgilB0j {
    ArrayList<VotgilB0j> KorRey;

    VotgilB0jKup(InputStream stream) throws IOException {
        // Ryd9isKup
        byte[] wed = new byte[3];
        while(true) {
            if (stream.read(wed, 0, 3) < 3) {
                // TODO: MekMorGwdWedSis
                throw new IOException("WuzRydTwcVötLöt");
            }
            int hash = VotgilYwlNif.wedHash(wed);
            if (hash == VotgilYwlNif.DunKit) {
                break;
            }
            KorRey.add(VotgilYwlNif.rydB0j(hash, stream));
        }
    }
}
