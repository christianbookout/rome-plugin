/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import blockchain.Ledger;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import romeplugin.database.SQLConn;
import romeplugin.election.*;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.role.RoleHandler;
import romeplugin.messaging.*;
import romeplugin.messaging.SwearFilter.SwearLevel;
import romeplugin.misc.PeeController;
import romeplugin.misc.SpawnCommand;
import romeplugin.title.*;
import romeplugin.zoning.*;
import romeplugin.zoning.claims.City;
import romeplugin.zoning.claims.ClaimInfoCommand;
import romeplugin.zoning.claims.ClaimLandCommand;
import romeplugin.zoning.claims.GetClaimBlocksCommand;
import romeplugin.zoning.locks.LockManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

public class RomePlugin extends JavaPlugin {
    public static final HashMap<Player, Title> onlinePlayerTitles = new HashMap<>();
    // TODO: make the ledger persistent
    private final Ledger ledger = new Ledger();

    public static final String TITLE_ENUM = "ENUM('TRIBUNE', 'QUAESTOR', 'AEDILE', 'PRAETOR', 'CONSUL', 'CENSOR', 'POPE', 'BUILDER', 'CITIZEN')";

    // runs when the plugin is enabled on the server startup
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        // we set our credentials
        dataSource.setServerName(config.getString("database.host"));
        dataSource.setPortNumber(config.getInt("database.port"));
        dataSource.setDatabaseName(config.getString("database.database"));
        dataSource.setUser(config.getString("database.username"));
        dataSource.setPassword(config.getString("database.password"));

        Material claimMaterial;
        String claimMaterialStr = config.getString("claims.claimMaterial");
        var protectedMaterialStrings = config.getStringList("claims.autoLockedBlocks");
        try {
            claimMaterial = Material.valueOf(claimMaterialStr.toUpperCase().strip());
        } catch (IllegalArgumentException e) {
            this.getLogger().log(Level.WARNING,
                    "error getting minecraft material from " + claimMaterialStr);
            claimMaterial = LandEventListener.DEFAULT_MATERIAL;
        }
        var protectedMaterials = new HashSet<Material>();
        protectedMaterialStrings.forEach(matStr -> protectedMaterials.add(Material.valueOf(matStr)));

        var lockManager = new LockManager(this);

        SQLConn.setSource(dataSource);
        try (Connection conn = SQLConn.getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS titles (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "title " + TITLE_ENUM + " NOT NULL);")
                    .execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS builders (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY);")
                    .execute();
            // (x0, y0) must be the top-left point and (x1, y1) must be the bottom-right
            // point
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityClaims (" +
                    "x0 INT NOT NULL," +
                    "y0 INT NOT NULL," +
                    "x1 INT NOT NULL," +
                    "y1 INT NOT NULL," +
                    "owner_uuid CHAR(36) NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS strawberry (" +
                    "x0 INT NOT NULL," +
                    "y0 INT NOT NULL," +
                    "x1 INT NOT NULL," +
                    "y1 INT NOT NULL," +
                    "added_player_uuid CHAR(36) NOT NULL);").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityInfo (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "size INT NOT NULL," +
                    "x INT NOT NULL," +
                    "y INT NOT NULL," +
                    "name VARCHAR(20) NOT NULL," +
                    "founder_uuid CHAR(36) NOT NULL," +
                    "found_date DATE NOT NULL);").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS cityMembers (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "cityId INT UNSIGNED NOT NULL);").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS usernames (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "username CHAR(32) NOT NULL);").execute();

            // all players extra claim blocks
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS extraClaimBlocks (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY," +
                    "blocks INT NOT NULL DEFAULT 0);").execute();

            // fun lock stuff
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS lockedBlocks (" +
                    "x INT NOT NULL," +
                    "y INT NOT NULL," +
                    "z INT NOT NULL," +
                    "keyId INT NOT NULL);").execute();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS lockKeys (" +
                    "keyId INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "creator_uuid CHAR(36) NOT NULL);").execute();

            conn.prepareStatement("CREATE TABLE IF NOT EXISTS banished (" +
                    "uuid CHAR(36) NOT NULL PRIMARY KEY);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        var roleHandler = new RoleHandler();

        var cityManager = new CityManager(
                config.getInt("land.initialCitySize"),
                config.getInt("land.cityMultiplier"),
                config.getInt("land.suburbsMultiplier"),
                config.getInt("claims.defaultClaimBlocks"), roleHandler);

        LandEventListener landListener = new LandEventListener(
                cityManager,
                lockManager,
                claimMaterial,
                protectedMaterials,
                config.getLong("claims.claimTimeoutMS"));

        // TODO: remove this
        City mainCity = new City(0,
                0,
                0,
                "rome", config.getInt("land.cityMultiplier"),
                config.getInt("land.suburbsMultiplier"),
                config.getInt("claims.defaultClaimBlocks"), roleHandler);

        var titles = new TitleHandler(this);
        PartyHandler partyHandler = new PartyHandler();
        var empireHandler = new EmpireHandler(partyHandler);

        SwearFilter filter = new SwearFilter(cityManager, SwearLevel.valueOf(config.getString("messages.useSwearFilter").toUpperCase()));
        var landEnterListener = new LandEnterListener(cityManager);
        var peeController = new PeeController(this);
        //var itemBank = new ItemBank(this);
        var notifications = new NotificationQueue();

        getCommand("city").setExecutor(new LandCommand(cityManager));
        getCommand("claim").setExecutor(new ClaimLandCommand(cityManager, this));
        getCommand("claiminfo").setExecutor(new ClaimInfoCommand());
        getCommand("removetitle").setExecutor(new RemoveTitleCommand(titles));
        getCommand("settitle").setExecutor(new SetTitleCommand(titles));
        //getCommand("bal").setExecutor(new BalanceCommand(ledger));
        getServer().getPluginManager().registerEvents(new RemovePopeListener(), this);
        getCommand("builder").setExecutor(new BuilderCommand());
        getCommand("shout").setExecutor(new ShoutCommand(partyHandler));
        getCommand("pee").setExecutor(peeController);
        //getCommand("makekey").setExecutor(new MakeKeyCommand(lockManager));
        getCommand("getblocks").setExecutor(new GetClaimBlocksCommand(mainCity));
        getCommand("elections").setExecutor(new ElectionCommand(new ElectionHandler(notifications, this, titles), empireHandler));
        getCommand("elections").setTabCompleter(new ElectionTabCompleter());
        getCommand("titles").setExecutor(new TitlesCommand());
        getCommand("parties").setExecutor(new PartyCommand(partyHandler, this));
        //getCommand("itembank").setExecutor(itemBank);
        getCommand("notification").setExecutor(new NotificationCommand(notifications));
        //getServer().getPluginManager().registerEvents(itemBank, this);
        getCommand("spawn").setExecutor(new SpawnCommand(mainCity));
        getCommand("banish").setExecutor(new BanishCommand(landEnterListener));
        getServer().getPluginManager().registerEvents(peeController, this);
        getServer().getPluginManager().registerEvents(new TitleEventListener(titles, partyHandler), this);
        getServer().getPluginManager().registerEvents(
                new DistanceListener(config.getInt("messages.messageDistance"), filter, cityManager),
                this);
        //getServer().getPluginManager().registerEvents(new BlockchainEventListener(this, ledger), this);
        getServer().getPluginManager().registerEvents(landListener, this);
        getServer().getPluginManager().registerEvents(lockManager, this);
        getServer().getPluginManager().registerEvents(landEnterListener, this);
    }

    // true/false if it worked or didnt work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }

}
