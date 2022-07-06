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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import romeplugin.database.SQLConn;
import romeplugin.election.*;
import romeplugin.empires.EmpireHandler;
import romeplugin.empires.role.*;
import romeplugin.messaging.*;
import romeplugin.messaging.SwearFilter.SwearLevel;
import romeplugin.misc.PeeController;
import romeplugin.misc.PlayerJoinListener;
import romeplugin.misc.SpawnCommand;
import romeplugin.title.BuilderCommand;
import romeplugin.zoning.*;
import romeplugin.zoning.claims.ClaimInfoCommand;
import romeplugin.zoning.claims.ClaimLandCommand;
import romeplugin.zoning.claims.GetClaimBlocksCommand;
import romeplugin.zoning.locks.LockManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Level;

public class RomePlugin extends JavaPlugin {
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
            if (claimMaterialStr != null) {
                claimMaterial = Material.valueOf(claimMaterialStr.toUpperCase().strip());
            } else {
                claimMaterial = LandEventListener.DEFAULT_MATERIAL;
            }
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

        PartyHandler partyHandler = new PartyHandler();
        var empireHandler = new EmpireHandler(partyHandler);

        var filterLevel = SwearLevel.CITY;
        var configFilterLevel = config.getString("messages.useSwearFilter");
        if (configFilterLevel != null) {
            filterLevel = SwearLevel.valueOf(configFilterLevel.toUpperCase());
        }
        SwearFilter filter = new SwearFilter(cityManager, filterLevel);
        var landEnterListener = new LandEnterListener(cityManager);
        var peeController = new PeeController(this);
        //var itemBank = new ItemBank(this);
        var notifications = new NotificationQueue();

        setExecutor("city", new CityCommand(cityManager));
        setExecutor("claim", new ClaimLandCommand(cityManager));
        setExecutor("claiminfo", new ClaimInfoCommand());
        setExecutor("removetitle", new RemoveRoleCommand(roleHandler));
        setExecutor("settitle", new SetRoleCommand(roleHandler, empireHandler));
        //setExecutor("bal", new BalanceCommand(ledger));
        setExecutor("builder", new BuilderCommand(roleHandler));
        setExecutor("shout", new ShoutCommand(partyHandler, roleHandler));
        setExecutor("pee", peeController);
        //setExecutor("makekey", new MakeKeyCommand(lockManager));
        setExecutor("getblocks", new GetClaimBlocksCommand(cityManager));
        setExecutor("elections", new ElectionCommand(new ElectionHandler(notifications, this, roleHandler), empireHandler, roleHandler));
        var elections = getCommand("elections");
        if (elections != null) {
            elections.setTabCompleter(new ElectionTabCompleter());
        }
        setExecutor("titles", new ListRolesCommand(empireHandler, roleHandler));
        setExecutor("parties", new PartyCommand(partyHandler));
        //setExecutor("itembank", itemBank);
        setExecutor("notification", new NotificationCommand(notifications));
        //getServer().getPluginManager().registerEvents(itemBank, this);
        setExecutor("spawn", new SpawnCommand(cityManager));
        setExecutor("banish", new BanishCommand(landEnterListener, roleHandler));
        getServer().getPluginManager().registerEvents(peeController, this);
        getServer().getPluginManager().registerEvents(new RoleEventListener(roleHandler, partyHandler), this);
        getServer().getPluginManager().registerEvents(
                new DistanceListener(config.getInt("messages.messageDistance"), filter, cityManager),
                this);
        //getServer().getPluginManager().registerEvents(new BlockchainEventListener(this, ledger), this);
        getServer().getPluginManager().registerEvents(landListener, this);
        getServer().getPluginManager().registerEvents(lockManager, this);
        getServer().getPluginManager().registerEvents(landEnterListener, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    private void setExecutor(String commandName, CommandExecutor executor) {
        var command = getCommand(commandName);
        if (command == null) {
            getLogger().severe("could not set executor of command '" + commandName + "' because it was null!");
            return;
        }
        command.setExecutor(executor);
    }

    // true/false if it worked or didnt work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }

}
