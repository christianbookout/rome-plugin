import org.junit.Test;
import romeplugin.votgilconfig.VotgilB0jBag;
import romeplugin.votgilconfig.VotgilB0jKup;
import romeplugin.votgilconfig.VotgilB0jSic;
import romeplugin.votgilconfig.VotgilYwlNif;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class VotgilConfigTests {
    @Test
    public void wedHash() {
        assertEquals(6775106, VotgilYwlNif.wedHash(new byte[]{'B', 'a', 'g'}));
        assertEquals(VotgilYwlNif.KupKit, VotgilYwlNif.wedHash("Kup".getBytes()));
    }

    @Test
    public void emptyKup() throws IOException {
        assertEquals(0, new VotgilB0jKup(new ByteArrayInputStream("Dun".getBytes())).size());
    }

    @Test
    public void bagNullElement() throws IOException {
        assertNull(new VotgilB0jBag(new ByteArrayInputStream("Dun".getBytes())).getV0tPer("bar"));
    }

    @Test
    public void kup() throws IOException {
        assertEquals(2, new VotgilB0jKup(new ByteArrayInputStream("Sic\"foo\"Sic\"bar\"Dun".getBytes())).size());
    }

    @Test
    public void string() throws IOException {
        assertEquals("hello", new VotgilB0jSic(new ByteArrayInputStream("\"hello\"".getBytes())).toString());
    }
}
