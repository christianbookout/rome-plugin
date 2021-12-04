/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import blockchain.BalanceCommand;
import blockchain.BlockchainEventListener;
import blockchain.Ledger;
import blockchain.PayCommand;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
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
import romeplugin.zoning.LandControl;
import romeplugin.zoning.LandEventListener;

import java.util.HashMap;

/**
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
    private final LandControl landControl = new LandControl(0, 0, 0, 5, 10);

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

        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        // we set our credentials
        dataSource.setServerName(config.getString("database.host"));
        dataSource.setPortNumber(config.getInt("database.port"));
        dataSource.setDatabaseName(config.getString("database.database"));
        dataSource.setUser(config.getString("database.username"));
        dataSource.setPassword(config.getString("database.password"));

        SQLConn.setSource(dataSource);

        getCommand("checktitle").setExecutor(new CheckTitleCommand(titles));
        getCommand("settitle").setExecutor(new SetTitleCommand(titles));
        getCommand("listtitles").setExecutor(new ListTitlesCommand(titles));
        getCommand("pay").setExecutor(new PayCommand(ledger));
        getCommand("bal").setExecutor(new BalanceCommand(ledger));
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new BlockchainEventListener(this, ledger), this);
        getServer().getPluginManager().registerEvents(new LandEventListener(landControl), this);
    }

    @Override
    public void onDisable() {
        titles.saveData();
    }

    //true/false if it worked or didnt work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }
}
