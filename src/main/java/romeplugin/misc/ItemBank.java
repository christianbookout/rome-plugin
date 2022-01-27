package romeplugin.misc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import romeplugin.database.SQLConn;

public class ItemBank implements CommandExecutor, Listener {

    //TODO pages

    private static final String UNOBTAINABLE_STR = "unobtainable";
    private static final String BANK_TITLE =  ChatColor.BOLD + "Bank of Rome";
    private final HashMap<UUID, Inventory> openBanks = new HashMap<>();

    public ItemBank() {
        this.initialize();
    }
    private void initialize () {
        try(Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS itemBank (" +
                                  "item VARCHAR(100) NOT NULL PRIMARY KEY," + 
                                  "count INT NOT NULL);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        openBank(player);
        return true;
    }
    
    private void openBank(Player player) {
        ArrayList<ItemStack> items = new ArrayList<>();
        try(Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM itemBank;");
            var results = stmt.executeQuery();
            while (results.next()) {
                items.add(new ItemStack(Material.valueOf(results.getString("item")), results.getInt("amount")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Inventory inventory = Bukkit.createInventory(player, 6*9, BANK_TITLE);
        var filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.RESET + ""); // have to make it .reset to make it not have a name
        for (int i = 2; i <= 8; i++) {
            inventory.setItem(6 * 9 - i, filler);
        }
        var next = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Next Page"); //TODO maybe make a "Last Page" and "First Page"
        var prev = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Previous Page");

        inventory.setItem(53, next);
        inventory.setItem(45, prev);
        openBanks.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);
    }

    private ItemStack makeItem(Material type, String name) {
        var item = new ItemStack(type);
        ItemMeta itemMeta= item.getItemMeta();
        itemMeta.setDisplayName(name);
        var lore = new ArrayList<String>();
        lore.add(UNOBTAINABLE_STR);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    @EventHandler
    public void onItemTaken(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(BANK_TITLE)) return;

        ItemMeta meta = e.getCurrentItem().getItemMeta();
        if (meta.hasLore() && meta.getLore().contains(UNOBTAINABLE_STR)) {
            e.setCancelled(true);
        }

        //TODO check if any modifications were made and pass them to the store/remove items methods
    } 

    @EventHandler
    public void onItemDragged(InventoryDragEvent e) {
        if (!e.getView().getTitle().equals(BANK_TITLE)) return;

        //TODO remove all of the items from e.getNewItems() if they're not in the top inventory (you can drag from the bottom inventory up into the top inventory)

        storeItems(e.getNewItems().values().toArray(ItemStack[]::new));
    }

    private void storeItems(ItemStack[] items) {
        try (Connection conn = SQLConn.getConnection()){
            StringBuilder statement = new StringBuilder("INSERT INTO itemBank VALUES");
            for(ItemStack i: items) {
                statement.append("('" + i.getType().toString() + "'," + i.getAmount()+"),");
            }
            statement.append(";");
            conn.prepareStatement(statement.toString()).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void removeItems(ItemStack[] items) {
        try (Connection conn = SQLConn.getConnection()){
            for(ItemStack i: items) {
                var stmt = conn.prepareStatement("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        openBanks.remove(e.getPlayer().getUniqueId());
    } 
}
