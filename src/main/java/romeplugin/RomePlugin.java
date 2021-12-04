/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import blockchain.BalanceCommand;
import blockchain.BlockchainEventListener;
import blockchain.PayCommand;
import blockchain.Ledger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import romeplugin.database.SQLConn;
import romeplugin.newtitle.Title;
import romeplugin.title.CheckTitleCommand;
import romeplugin.title.ListTitlesCommand;
import romeplugin.title.RomeTitles;
import romeplugin.title.SetTitleCommand;
import romeplugin.votgilconfig.*;
import romeplugin.zoning.*;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

/**
 *
 * @author chris
 */
public class RomePlugin extends JavaPlugin {
    private final RomeTitles titles = new RomeTitles();
    public static final HashMap<Player, Title> onlinePlayers = new HashMap<>();
    //Hashmap of players who joined the server and don't exist in the database
    //TODO: store these players when the server closes (and/or over a timed interval)
    public static final HashMap<Player, Title> toStore = new HashMap<>();
    //private final String titlesFilename = "rome_titles";
    // TODO: make the ledger persistent
    private final Ledger ledger = new Ledger();
    //private SQLConn connection;
    //runs when the plugin is enabled on the server startup 
    @Override
    public void onEnable() {
        //registering the eventlistener
        //try {
            //titles.loadData(new DataInputStream(new FileInputStream(titlesFilename)));
        //} catch (FileNotFoundException e) {
            //getLogger().fine("could not find " + titlesFilename);
        //}
        
        FileConfiguration config = this.getConfig();
        config.createSection("database");
        config.addDefault("sql_username", "username");
        config.addDefault("sql_password", "password");

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource(); //TODO make all connections come from datasource

        
        //Database database = config.getDatabase();
        // we set our credentials
        dataSource.setServerName(database.getHost());
        dataSource.setPortNumber(database.getPort());
        dataSource.setDatabaseName(database.getDatabase());
        dataSource.setUser(database.getUser());
        dataSource.setPassword(database.getPassword());

        SQLConn.setStuff(url, username, password);

        File f = new File("rome_config.vot");
        if (f.canRead()) {
            try {
                VotgilConfig config = new VotgilConfig(f);
                VotgilB0jKup names = (VotgilB0jKup) config.getPer().getV0tPer("FacNem");
                getLogger().info("loading " + names.size() + " titles...");
                names.forEach((votgilB0j -> {
                    VotgilB0jBag bag = (VotgilB0jBag) votgilB0j;
                    VotgilB0jSic name = (VotgilB0jSic) bag.getV0tPer("NemVunNem");
                    VotgilB0j color = bag.getV0tPer("Kul");
                    if (color == null) {
                        titles.addTitle(name.toString());
                    } else {
                        titles.addTitle(name.toString(), ChatColor.valueOf(color.toString()));
                    }
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getCommand("checktitle").setExecutor(new CheckTitleCommand(titles));
        getCommand("settitle").setExecutor(new SetTitleCommand(titles));
        getCommand("listtitles").setExecutor(new ListTitlesCommand(titles));
        getCommand("pay").setExecutor(new PayCommand(ledger));
        getCommand("bal").setExecutor(new BalanceCommand(ledger));
        getServer().getPluginManager().registerEvents(new EventListener(titles), this);
        getServer().getPluginManager().registerEvents(new BlockchainEventListener(this, ledger), this);
        getServer().getPluginManager().registerEvents(new LandEventListener(new LandControl()), this);
    }

    @Override
    public void onDisable() {
        try {
            titles.saveData(connection);
        } catch (SQLException e) {
            getLogger().fine("could not save to database");
        }
    }

    //true/false if it worked or didnt work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }
}
