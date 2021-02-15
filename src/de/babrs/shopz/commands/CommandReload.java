package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReload{
    static boolean run(CommandSender sender){
        if(!sender.hasPermission("shopz.reload")){
            sender.sendMessage(ShopzPlugin.getLocalization().getString("no_permission"));
            return true;
        }

        ShopzPlugin.reloadConfigurations();
        String prefix = sender instanceof Player ? ShopzPlugin.getPrefix() : "";
        sender.sendMessage(prefix + ShopzPlugin.getLocalization().getString("successful_reload"));
        return true;
    }
}
