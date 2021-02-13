package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.HashMap;
import java.util.UUID;

public class RemoveItemFromItemFrameListener implements Listener{
    HashMap<Player, String> removeRequests = new HashMap<>();

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
                    FileConfiguration localization = ShopzPlugin.getLocalization();
                    String ownerUUID = ShopzPlugin.getShops().getString(path + ".owner");

                    if(ownerUUID.equals(source.getUniqueId().toString())){
                        //source is the owner of the shop
                        HangingBreakEvent be = new HangingBreakEvent((Hanging) event.getEntity(), HangingBreakEvent.RemoveCause.DEFAULT);
                        Bukkit.getServer().getPluginManager().callEvent(be);

                    }else if(ownerUUID.equals("admin") && source.hasPermission("shopz.admin")){
                        //shop is an adminshop and source has permission to destroy those
                        confirmBreaking(event, source, path, ownerUUID, localization, true);

                    }else if(source.hasPermission("shopz.clear") && !ownerUUID.equals("admin")){
                        //source is not the owner, but has permission shopz.clear
                        confirmBreaking(event, source, path, ownerUUID, localization, false);

                    }else{
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

    private void confirmBreaking(EntityDamageByEntityEvent event, Player source, String path, String ownerUUID, FileConfiguration localization, boolean adminShop){
        Block attachedTo = ShoppingUtil.getBlockWithAttachedItemFrame((ItemFrame) event.getEntity());
        if(removeRequests.get(source) == null || !removeRequests.get(source).equals(path)){
            removeRequests.put(source, path);
            source.sendMessage(modifyString(ShopzPlugin.getLocalization().getString("destroy_shop_confirm"), ownerUUID, localization, adminShop, attachedTo));

            Player finalSource = source;    //Player has 7 Seconds to confirm destruction of the frame
            Bukkit.getScheduler().scheduleSyncDelayedTask(ShopzPlugin.getInstance(), () -> removeRequests.put(finalSource, null), 7*20L);

            event.setCancelled(true);
        }else{
            source.sendMessage(modifyString(ShopzPlugin.getLocalization().getString("destroyed_shop"), ownerUUID, localization, adminShop, attachedTo));

            HangingBreakEvent be = new HangingBreakEvent((Hanging) event.getEntity(), HangingBreakEvent.RemoveCause.DEFAULT);
            Bukkit.getServer().getPluginManager().callEvent(be);
        }
    }

    private String modifyString(String msg, String ownerUUID, FileConfiguration localization, boolean adminShop, Block attachedTo){
        return ShopzPlugin.getPrefix() + msg
                .replace("@type", adminShop
                    ? localization.getString("admin_shop")
                    : localization.getString("shop_title").replace("@user", Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName()))
                .replace("@pos", "(" + attachedTo.getX() + ", " + attachedTo.getY() + ", " + attachedTo.getZ() + ")");
    }
}
