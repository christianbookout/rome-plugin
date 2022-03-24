package romeplugin.title;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import romeplugin.empires.role.Permission;
import romeplugin.empires.role.RoleHandler;

public class RemovePopeListener implements Listener {
    private final RoleHandler roleHandler;

    public RemovePopeListener(RoleHandler roleHandler) {
        this.roleHandler = roleHandler;
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent e) {
        var role = roleHandler.getPlayerRole(e.getEntity());
        if (role != null && role.hasPerm(Permission.RemoveOnDeath)) {
            roleHandler.removePlayerRole(e.getEntity());
        }
    }
}
