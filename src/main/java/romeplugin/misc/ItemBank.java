package romeplugin.misc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import romeplugin.database.SQLConn;

public class ItemBank implements CommandExecutor, Listener {

    //TODO pages

    private static final String UNOBTAINABLE_STR = "unobtainable";
    private static final String BANK_TITLE =  ChatColor.BOLD + "Bank of Rome";
    private Inventory openBank = null;
    private int openPage = 1;
    private static final int INVENTORY_SIZE = 6*9;
    public ItemBank() {
        this.initialize();
    }
    private void initialize () {
        try(Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS itemBank (" +
                                  "item VARCHAR(100) NOT NULL," + 
                                  "count INT NOT NULL," +
                                  "slot INT NOT NULL," +
                                  "page INT NOT NULL,"+
                                  "itemNumber INT NOT NULL PRIMARY KEY);").execute(); // the unique item number of slot * page
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
    
    private final ItemStack selectedPage = this.makeItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GOLD + "Selected Page");

    private Inventory getBank() {
        if (this.openBank == null) return this.openBank = Bukkit.createInventory(null, 6*9, BANK_TITLE);
        else return this.openBank;
    }

    private void openBank(Player player) {
        ArrayList<ItemStack> items = new ArrayList<>();
        try(Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM itemBank;");
            var results = stmt.executeQuery();
            while (results.next()) {
                items.add(new ItemStack(Material.valueOf(results.getString("item")), results.getInt("count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Inventory inventory = this.getBank();
        showItems(inventory, this.openPage);
        player.openInventory(inventory);
    }

    private void initInventory(Inventory inventory) {
        for (int i = 1; i <= 9; i++) {
            var filler = makeFiller(i); // have to make it .reset to make it not have a name
            inventory.setItem(pageToSlot(i), filler); // Fill the bottom row with stained glass
            inventory.setItem(pageToSlot(this.openPage), selectedPage);
        }
    }

    private ItemStack makeFiller(int page) {
        return makeItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + "Page " + page);
    }

    //Gets the absolute page number depending on current slot number
    private int getPage(int i) {
        return (i%9)+1;
    }

    private int pageToSlot(int page) {
        return page + INVENTORY_SIZE - 10;
    }

    private int getItemNumber(int slot, int page) {
        return (page-1) * INVENTORY_SIZE + slot;
    }

    private boolean insideBank(int i) {
        return i < INVENTORY_SIZE;
    }

    private ItemStack makeItem(Material type, String name) {
        var item = new ItemStack(type);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        var lore = new ArrayList<String>();
        lore.add(UNOBTAINABLE_STR);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }


    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(BANK_TITLE)) return;
        if (e.getCurrentItem() != null) {
            ItemMeta meta = e.getCurrentItem().getItemMeta();
            //make it so you cant take the glass pane page selectors
            if (meta.hasLore() && meta.getLore().contains(UNOBTAINABLE_STR)) {
                this.setPage(getPage(e.getRawSlot())); // change the page
                e.setCancelled(true);
                return;
            }
        }
        //TODO check if the shit changed
    } 

    private void setPage(int pageNumber) {
        this.openBank.setItem(this.pageToSlot(this.openPage), this.makeFiller(this.openPage));
        this.openPage = pageNumber;
        this.openBank.setItem(this.pageToSlot(pageNumber), selectedPage);
        this.showItems(this.getBank(), pageNumber);
    }

    @EventHandler
    public void onItemDragged(InventoryDragEvent e) { // this works
        if (!e.getView().getTitle().equals(BANK_TITLE)) return;

        Map<Integer, ItemStack> items = new HashMap<>();
        e.getNewItems().entrySet().forEach(entry -> items.put(entry.getKey(), entry.getValue()));

        items.keySet()
             .removeIf(entry -> !this.insideBank(entry)); // don't store items dragged in the player's inventory

        items.entrySet().forEach(i -> storeItem(i.getValue(), i.getKey(), openPage));
    }

    private void storeItem(ItemStack item, int rawSlot, int page) {
        try (Connection conn = SQLConn.getConnection()){
            var statement = conn.prepareStatement("REPLACE INTO itemBank VALUES (?, ?, ?, ?, ?);");
            statement.setString(1, item.getType().toString());
            statement.setInt(2, item.getAmount());
            statement.setInt(3, rawSlot);
            statement.setInt(4, page);
            statement.setInt(5, getItemNumber(rawSlot, page));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeItem(int rawSlot, int page) {
        try (Connection conn = SQLConn.getConnection()){
            var stmt = conn.prepareStatement("DELETE FROM itemBank WHERE itemNumber=?");
            stmt.setInt(1, getItemNumber(rawSlot, page));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showItems(Inventory inventory, int page) {
        inventory.clear();
        this.initInventory(inventory);
        try (Connection conn = SQLConn.getConnection()) {
            var stmt = conn.prepareStatement("SELECT * FROM itemBank WHERE page=?");
            stmt.setInt(1, page);
            var results = stmt.executeQuery();
            while (results.next()) {
                Material material = Material.getMaterial(results.getString("item"));
                ItemStack item = new ItemStack(material, results.getInt("count"));
                inventory.setItem(results.getInt("slot"), item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
