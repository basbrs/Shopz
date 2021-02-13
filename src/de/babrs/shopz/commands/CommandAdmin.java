package de.babrs.shopz.commands;

import de.babrs.shopz.util.ShoppingUtil;
import de.babrs.shopz.ShopzPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandAdmin{
    static boolean run(CommandSender sender){
        if(!sender.hasPermission("shopz.adminshop")){
            sender.sendMessage(ChatColor.RED + ShopzPlugin.getLocalization().getString("no_permission"));
            return true;
        }

        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be executed as a player.");
            return true;
        }

        Player p = (Player) sender;
        ItemStack item = ShoppingUtil.createShopTokens(1, true);

        if(ShoppingUtil.hasInventorySpaceFor(p.getInventory(), item))
            p.getInventory().addItem(item);
        else
            p.sendMessage(ShopzPlugin.getPrefix() + ShopzPlugin.getLocalization().get("no_space_inv"));

        return true;
    }
}
