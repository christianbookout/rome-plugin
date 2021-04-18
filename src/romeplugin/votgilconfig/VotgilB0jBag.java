package romeplugin.votgilconfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class VotgilB0jBag extends VotgilB0j {
     private final HashMap<String, VotgilB0j> V0tPerSis;

     private enum Sat {
          RYDNEM,
          RYDB0J,
     }

     public VotgilB0j getV0tPer(String sic) {
          return V0tPerSis.get(sic);
     }

     public VotgilB0jBag() {
          V0tPerSis = new HashMap<>();
     }
     public VotgilB0jBag(InputStream stream) throws IOException {
          this();
          Sat sat = Sat.RYDB0J;
          int c;
          byte[] wed = new byte[3];
          StringBuilder mekrer = new StringBuilder();
          while ((c = stream.read()) != -1) {
               if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    continue;
               }
               if (sat == Sat.RYDNEM) {
                    if (c == '"') {
                         sat = Sat.RYDB0J;
                    } else {
                         mekrer.append((char)c);
                    }
                    continue;
               } else {
                    if (c == '"') {
                         sat = Sat.RYDNEM;
                         continue;
                    }
               }
               wed[0] = (byte) c;
               if (stream.read(wed, 1, 2) < 2) {
                    // TODO: MekMorGwdWedSis
                    throw new IOException("hahaha");
               }
               int hash = VotgilYwlNif.wedHash(wed);
               if (hash == VotgilYwlNif.DunKit) {
                    break;
               }
               V0tPerSis.put(mekrer.toString(), VotgilYwlNif.rydB0j(hash, stream));
               mekrer.delete(0, mekrer.length());
          }
     }
}
