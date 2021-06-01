package blockchain;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.UUID;

public class BlockchainEventListener implements Listener {
    private final Plugin plugin;
    private final Ledger ledger;

    private static class MineTask implements Runnable {
        private static int nonce = 0;
        private final Ledger ledger;
        private final UUID target;
        private final int attempts;

        private MineTask(Ledger ledger, UUID target, int attempts) {
            this.ledger = ledger;
            this.attempts = attempts;
            this.target = target;
        }

        @Override
        public void run() {
            long timestamp = Instant.now().getEpochSecond();
            var block = ledger.getMineTarget(target);
            for (int i = 0; i < attempts; i++) {
                if (block.attemptMine(nonce)) {
                    nonce = 0;
                    ledger.submitValidBlock(block);
                    break;
                }
                nonce++;
            }
        }
    }

    public BlockchainEventListener(Plugin plugin, Ledger ledger) {
        this.plugin = plugin;
        this.ledger = ledger;
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                new MineTask(this.ledger, event.getPlayer().getUniqueId(), 1));
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        ledger.addPlayer(event.getPlayer().getUniqueId());
    }
}
