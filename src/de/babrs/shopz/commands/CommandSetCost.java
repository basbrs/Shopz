package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandSetCost{
    static boolean run(CommandSender sender, String arg){
        if(!sender.hasPermission("shopz.setcost")){
            sender.sendMessage(ChatColor.RED + "" + ShopzPlugin.getLocalization().get("no_permission"));
            return true;
        }

        return false;
    }
}
