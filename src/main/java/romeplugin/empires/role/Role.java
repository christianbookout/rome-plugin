package romeplugin.empires.role;

import org.bukkit.ChatColor;

import java.util.Set;

public class Role {
    private final Set<Permission> perms;
    public final ChatColor color;
    public final String name;
    public final int id;
    public final ObtainMethod obtainMethod;

    Role(Set<Permission> perms, ChatColor color, String name, int id, ObtainMethod obtainMethod) {
        this.perms = perms;
        this.color = color;
        this.name = name;
        this.id = id;
        this.obtainMethod = obtainMethod;
    }

    public boolean hasPerm(Permission perm) {
        return perms.contains(perm);
    }

    public Set<Permission> getPerms() {
        return perms;
    }
}
