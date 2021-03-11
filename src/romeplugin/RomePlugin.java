/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author chris
 */
public class RomePlugin extends JavaPlugin {
    //runs when the plugin is enabled on the server startup 
    @Override
    public void onEnable() {
        //registering the eventlistener 
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        
    }
    
   //true/false if it worked or didnt work
   @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        return false;
    }
}
