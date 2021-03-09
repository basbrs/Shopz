package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.inventories.SetupInventory;
import de.babrs.shopz.util.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlaceItemFrameListener implements Listener{

    @EventHandler(priority = EventPriority.LOW)
    public void onItemFramePlacement(HangingPlaceEvent event){
        if(event.getEntity() instanceof ItemFrame && event.getBlock().getState() instanceof BlockInventoryHolder && !event.isCancelled()){
            Player p = event.getPlayer();
            ItemStack main = p.getInventory().getItemInMainHand();
            ItemStack off = p.getInventory().getItemInOffHand();
            String frameName = ShoppingUtil.getValidToken(false);
            String adminFrameName = ShoppingUtil.getValidToken(true);

            ItemStack frame = null;

            if(main.getType() == Material.ITEM_FRAME)
                frame = main;
            else if(off.getType() == Material.ITEM_FRAME)
                frame = off;

            Boolean isAdminShopToken = null;

            if(frame != null && frame.getLore() != null && frame.getLore().size() > 0){
                if(frame.getLore().get(0).equals(frameName))
                    isAdminShopToken = false;
                else if(frame.getLore().get(0).equals(adminFrameName)){
                    if(!p.hasPermission("shopz.admin")){
                        p.getInventory().remove(frame);
                        event.setCancelled(true);
                        return;
                    }
                    isAdminShopToken = true;
                }
            }

            if(isAdminShopToken != null){
                FileConfiguration shops = ShopzPlugin.getShops();
                String coordinate = ShoppingUtil.blockToPath(event.getBlock());

                if(shops.get(coordinate) != null){
                    FileConfiguration localization = ShopzPlugin.getLocalization();

                    String ownerUUID = shops.getString(coordinate + ".owner");
                    String prefix = ShopzPlugin.getPrefix();

                    if(ownerUUID.equals("admin")){
                        if(!p.hasPermission("shopz.admin") || !isAdminShopToken){
                            p.sendMessage(prefix + localization.getString("error_not_your_shop_admin"));
                            event.setCancelled(true);
                            return;
                        }
                    }else if(!ownerUUID.equals(p.getUniqueId().toString())){
                        p.sendMessage(prefix + localization.getString("error_not_your_shop")
                                .replace("@owner", Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName()));
                        event.setCancelled(true);
                        return;
                    }else if(isAdminShopToken){
                        p.sendMessage(prefix + localization.getString("error_your_shop_no_admin"));
                        event.setCancelled(true);
                        return;
                    }

                    int frameCount = shops.getInt(coordinate + ".frame_count");
                    shops.set(coordinate + ".frame_count", frameCount + 1);
                    ItemStack display = ((ItemFrame) Bukkit.getEntity(UUID.fromString(((List<String>) shops.get(coordinate + ".frames")).get(0)))).getItem();
                    ((ItemFrame) event.getEntity()).setItem(display);

                    ArrayList<String> frames = (ArrayList<String>) shops.get(coordinate + ".frames");
                    frames.add(event.getEntity().getUniqueId().toString());
                    shops.set(coordinate + ".frames", frames);
                    ShopzPlugin.saveShops();

                    String pos = "(" + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ() + ")";
                    p.sendMessage(prefix + localization.getString("added_shop_frame")
                            .replace("@pos", pos).replace("@amount", Integer.toString(frameCount + 1)));
                }else{
                    if(p.hasPermission("shopz.create")){
                        new SetupInventory(p, (ItemFrame) event.getEntity(), isAdminShopToken).openSetupDialogue();

                        shops.set(coordinate + ".owner", isAdminShopToken ? "admin" : p.getUniqueId().toString());
                        ArrayList<String> frames = new ArrayList<>();
                        frames.add(event.getEntity().getUniqueId().toString());
                        shops.set(coordinate + ".frames", frames);
                        shops.set(coordinate + ".frame_count", 1);
                        shops.set(coordinate + ".admin", isAdminShopToken);
                    }else{
                        p.sendMessage(ShopzPlugin.getLocalization().getString("no_permission"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
