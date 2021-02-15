package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetCost{
    static boolean run(CommandSender sender, String arg){
        if(!sender.hasPermission("shopz.setcost")){
            sender.sendMessage(ShopzPlugin.getLocalization().getString("no_permission"));
            return true;
        }

        try{
            int newCost = Integer.parseInt(arg);
            ShopzPlugin.getPluginConfig().set("frame_cost_money", newCost);
            String message = sender instanceof Player ? ShopzPlugin.getPrefix() : "";
            message += ShopzPlugin.getLocalization().getString("set_cost_successful")
                    .replace("@cost", arg)
                    .replace("@currency", ShopzPlugin.getPluginConfig().getString("currency"));
            sender.sendMessage(message);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
}
