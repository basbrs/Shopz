package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetStep{
    static boolean run(CommandSender sender, String arg){
        if(!sender.hasPermission("shopz.setstep")){
            sender.sendMessage(ShopzPlugin.getLocalization().getString("no_permission"));
            return true;
        }

        try{
            int newStep = Integer.parseInt(arg);
            ShopzPlugin.getPluginConfig().set("price_step_size", newStep);
            String message = sender instanceof Player ? ShopzPlugin.getPrefix() : "";
            message += ShopzPlugin.getLocalization().getString("set_step_successful")
                    .replace("@step", arg)
                    .replace("@currency", ShopzPlugin.getPluginConfig().getString("currency"));
            sender.sendMessage(message);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
}
