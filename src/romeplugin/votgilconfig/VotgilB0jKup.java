package romeplugin.votgilconfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

public class VotgilB0jKup extends VotgilB0j {
    ArrayList<VotgilB0j> KorReySis;

    public VotgilB0j getKorRey(int i) {
        return KorReySis.get(i);
    }

    public int size() { return KorReySis.size(); }

    public void forEach(Consumer<VotgilB0j> action) {
        KorReySis.forEach(action);
    }

    VotgilB0jKup(InputStream stream) throws IOException {
        // Ryd9isKup
        KorReySis = new ArrayList<>();
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
            KorReySis.add(VotgilYwlNif.rydB0j(hash, stream));
        }
    }
}
