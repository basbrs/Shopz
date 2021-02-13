package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.util.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandClear{
    static boolean run(CommandSender sender){
        if(!sender.hasPermission("shopz.clear")){
            sender.sendMessage(ChatColor.RED + ShopzPlugin.getLocalization().getString("no_permission"));
            return true;
        }

        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "This command can only be executed as a player.");
            return true;
        }
        Player p = (Player) sender;

        String prefix = ShopzPlugin.getPrefix();
        FileConfiguration localization = ShopzPlugin.getLocalization();

        Block target = p.getTargetBlock(5);
        if(target != null && target.getType() != Material.AIR){
            FileConfiguration shops = ShopzPlugin.getShops();
            String path = ShoppingUtil.blockToPath(target);
            if(shops.get(path) != null){
                List<String> frames = (List<String>) shops.get(path + ".frames");
                List<HangingBreakEvent> removes = new ArrayList<>();
                int i = 0;
                for(String uuid : frames){
                    ItemFrame frame = (ItemFrame) Bukkit.getEntity(UUID.fromString(uuid));
                    if(frame != null){
                        removes.add(new HangingBreakEvent(frame, HangingBreakEvent.RemoveCause.DEFAULT));
                        i++;
                    }
                }
                for(HangingBreakEvent remove : removes)
                    Bukkit.getServer().getPluginManager().callEvent(remove);
                shops.set(path, null);
                ShopzPlugin.saveShops();
                p.sendMessage(prefix + ((String) localization.get("cleared_frames"))
                        .replace("@amount", Integer.toString(i))
                        .replace("@pos", "(" + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")"));
            }else
                p.sendMessage(prefix + localization.get("no_registered_shops"));
        }else
            p.sendMessage(prefix + localization.get("no_block_sight"));
        return true;
    }
}
