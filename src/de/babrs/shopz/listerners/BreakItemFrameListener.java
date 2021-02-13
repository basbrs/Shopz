package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.List;
import java.util.UUID;

public class BreakItemFrameListener implements Listener{
    @EventHandler(priority = EventPriority.LOW)
    public void onItemFrameBreaking(HangingBreakEvent event){
        if(event.getEntity() instanceof ItemFrame && !event.isCancelled()){
            Block attachedTo = ShoppingUtil.getBlockWithAttachedItemFrame((ItemFrame) event.getEntity());
            FileConfiguration shops = ShopzPlugin.getShops();
            String path = ShoppingUtil.blockToPath(attachedTo);
            if(shops.get(path) != null){
                List<String> frames = (List<String>) shops.get(path + ".frames");
                if(frames.contains(event.getEntity().getUniqueId().toString())){
                    ShoppingUtil.removeShopFrame((ItemFrame) event.getEntity());
                    ShoppingUtil.closeAllVisitorInventories(attachedTo);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event){
        String path = ShoppingUtil.blockToPath(event.getBlock());
        if(ShopzPlugin.getShops().get(path) != null && !event.isCancelled()){
            List<String> frames = (List<String>) ShopzPlugin.getShops().get(path + ".frames");
            for(String uuid : frames){
                ItemFrame frame = (ItemFrame) Bukkit.getEntity(UUID.fromString(uuid));
                HangingBreakEvent be = new HangingBreakEvent(frame, HangingBreakEvent.RemoveCause.DEFAULT);
                Bukkit.getServer().getPluginManager().callEvent(be);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPistonDestroyShulker(BlockPistonExtendEvent event){
        for(Block b : event.getBlocks()){
            if(b.getType().name().contains("SHULKER_BOX")){
                FileConfiguration shops = ShopzPlugin.getShops();
                if(shops.get(ShoppingUtil.blockToPath(b)) != null){
                    List<String> frames = (List<String>) shops.get(ShoppingUtil.blockToPath(b) + ".frames");
                    for(String uuid : frames){
                        ItemFrame frame = (ItemFrame) Bukkit.getEntity(UUID.fromString(uuid));
                        HangingBreakEvent be = new HangingBreakEvent(frame, HangingBreakEvent.RemoveCause.DEFAULT);
                        Bukkit.getServer().getPluginManager().callEvent(be);
                    }
                }
            }
        }
    }
}
