package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.UUID;

public class RemoveItemFromItemFrameListener implements Listener{

    @EventHandler(priority = EventPriority.LOW)
    public void onItemFrameItemRemove(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof ItemFrame && !event.isCancelled()){
            String path = ShoppingUtil.blockToPath(ShoppingUtil.getBlockWithAttachedItemFrame((ItemFrame) event.getEntity()));
            if(ShopzPlugin.getShops().get(path) != null){
                Entity damager = event.getDamager();
                Player source = null;
                if(damager instanceof Player){
                    source = (Player) damager;
                }else if(damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player){
                    source = (Player)((Projectile) damager).getShooter();
                }
                if(source != null){
                    String ownerUUID = (String) ShopzPlugin.getShops().get(path + ".owner");
                    if(ownerUUID.equals(source.getUniqueId().toString()) || (ownerUUID.equals("admin") && source.hasPermission("shopz.admin")) || source.hasPermission("shopz.clear")){
                        //destroy Frame, because owner or admin wants to destroy it
                        HangingBreakEvent be = new HangingBreakEvent((Hanging) event.getEntity(), HangingBreakEvent.RemoveCause.DEFAULT);
                        Bukkit.getServer().getPluginManager().callEvent(be);
                    }else{
                        FileConfiguration localization = ShopzPlugin.getLocalization();
                        String message = ShopzPlugin.getPrefix();
                        message += ownerUUID.equals("admin")
                                ? localization.getString("error_not_your_shop_admin")
                                : localization.getString("error_not_your_shop").replace("@owner", Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName());
                        source.sendMessage(message);
                        event.setCancelled(true);
                    }
                }else
                    event.setCancelled(true);
            }
        }
    }
}
