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

/**
 *
 * @author chris
 */
public class RomePlugin extends JavaPlugin {
    private final RomeTitles titles = new RomeTitles();
    //runs when the plugin is enabled on the server startup 
    @Override
    public void onEnable() {
        //registering the eventlistener
        getCommand("checktitle").setExecutor(new CheckTitleCommand(titles));
        getCommand("settitle").setExecutor(new SetTitleCommand(titles));
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        
    }
    
   //true/false if it worked or didnt work
   @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }
}
