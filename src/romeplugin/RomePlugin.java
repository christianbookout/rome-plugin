/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import romeplugin.title.CheckTitleCommand;
import romeplugin.title.RomeTitles;
import romeplugin.title.SetTitleCommand;

import java.io.*;

/**
 *
 * @author chris
 */
public class RomePlugin extends JavaPlugin {
    private final RomeTitles titles = new RomeTitles();
    private final String titlesFilename = "rome_titles";
    //runs when the plugin is enabled on the server startup 
    @Override
    public void onEnable() {
        //registering the eventlistener
        try {
            titles.loadData(new DataInputStream(new FileInputStream(titlesFilename)));
        } catch (FileNotFoundException e) {
            getLogger().fine("could not find " + titlesFilename);
        }

        getCommand("checktitle").setExecutor(new CheckTitleCommand(titles));
        getCommand("settitle").setExecutor(new SetTitleCommand(titles));
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        try {
            titles.saveData(new DataOutputStream(new FileOutputStream(titlesFilename)));
        } catch (FileNotFoundException e) {
            getLogger().fine("could not find " + titlesFilename);
        }
    }

    //true/false if it worked or didnt work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }
}
