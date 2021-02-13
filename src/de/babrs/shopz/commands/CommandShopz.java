package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandShopz{
    public static boolean run(CommandSender sender, Command cmd, String lbl, String[] args){
        String prefix = ShopzPlugin.getPrefix();
        if(args.length == 0){
            PluginDescriptionFile desc = ShopzPlugin.getPluginDescription();
            String message = "";
            if(sender instanceof Player)
                message = prefix;
            message = message + "This is the " + desc.getName() + "-Plugin by " + desc.getAuthors().get(0)
                    + " in version " + desc .getVersion() + ".";
            sender.sendMessage(message);
            return true;
        }else if(args.length == 1){
            if(args[0].equalsIgnoreCase("clear")){
                return CommandClear.run(sender);
            }else if(args[0].equalsIgnoreCase("admin")){
                return CommandAdmin.run(sender);
            }else if(args[0].equalsIgnoreCase("reload")){
                return CommandReload.run(sender);
            }
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("setCost")){
                return CommandSetCost.run(sender, args[1]);
            }else if(args[0].equalsIgnoreCase("setStep")){
                return CommandSetStep.run(sender, args[1]);
            }
        }
        return false;
    }
}
