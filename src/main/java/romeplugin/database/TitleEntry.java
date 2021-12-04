package romeplugin.database;

import romeplugin.newtitle.Title;

import java.util.UUID;

public class TitleEntry {
    public final Title t;
    public final UUID id;

    TitleEntry(Title t, UUID id) {
        this.t = t;
        this.id = id;
    }
}
