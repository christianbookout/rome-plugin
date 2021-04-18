/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package romeplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import romeplugin.title.CheckTitleCommand;
import romeplugin.title.RomeTitles;
import romeplugin.title.SetTitleCommand;
import romeplugin.votgilconfig.VotgilB0jBag;
import romeplugin.votgilconfig.VotgilB0jKup;
import romeplugin.votgilconfig.VotgilB0jSic;
import romeplugin.votgilconfig.VotgilConfig;

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

        File f = new File("rome_config.vot");
        if (f.canRead()) {
            try {
                VotgilConfig config = new VotgilConfig(f);
                VotgilB0jKup names = (VotgilB0jKup) config.getPer().getV0tPer("FacNem");
                getLogger().info("loading " + names.size() + " titles...");
                names.forEach((votgilB0j -> {
                    VotgilB0jBag bag = (VotgilB0jBag) votgilB0j;
                    VotgilB0jSic name = (VotgilB0jSic) bag.getV0tPer("NemVunNem");
                    VotgilB0jSic color = (VotgilB0jSic) bag.getV0tPer("Kul");
                    titles.addTitle(name.toString(), ChatColor.valueOf(color.toString()));
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getCommand("checktitle").setExecutor(new CheckTitleCommand(titles));
        getCommand("settitle").setExecutor(new SetTitleCommand(titles));
        getServer().getPluginManager().registerEvents(new EventListener(titles), this);
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
