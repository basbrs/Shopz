package de.babrs.shopz.listerners;

import de.babrs.shopz.InventoriesSingleton;
import de.babrs.shopz.SetupInventory;
import de.babrs.shopz.ShoppingUtil;
import de.babrs.shopz.ShopzPlugin;
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

            Boolean isAdminShop = null;

            if(frame != null && frame.getLore() != null && frame.getLore().size() > 0){
                if(frame.getLore().get(0).equals(frameName))
                    isAdminShop = false;
                else if(frame.getLore().get(0).equals(adminFrameName)){
                    if(!p.hasPermission("shopz.admin")){
                        frame.setAmount(0);
                        event.setCancelled(true);
                        return;
                    }
                    isAdminShop = true;
                }
            }

            if(isAdminShop != null){
                FileConfiguration shops = ShopzPlugin.getShops();
                String coordinate = ShoppingUtil.blockToPath(event.getBlock());

                if(shops.get(coordinate) != null){
                    FileConfiguration localization = ShopzPlugin.getLocalization();

                    String ownerUUID = shops.getString(coordinate + ".owner");
                    String prefix = ShopzPlugin.getPrefix();

                    if(!ownerUUID.equals(p.getUniqueId().toString()) && !(isAdminShop && p.hasPermission("shopz.admin"))){
                        if(isAdminShop)
                            p.sendMessage(prefix + localization.getString("error_not_your_shop_admin"));
                        else
                            p.sendMessage(prefix + localization.getString("error_not_your_shop")
                                    .replace("@owner", Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName()));
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
                    new SetupInventory(p, (ItemFrame) event.getEntity(), isAdminShop).openSetupDialogue();

                    shops.set(coordinate + ".owner", isAdminShop ? "admin" : p.getUniqueId().toString());
                    ArrayList<String> frames = new ArrayList<>();
                    frames.add(event.getEntity().getUniqueId().toString());
                    shops.set(coordinate + ".frames", frames);
                    shops.set(coordinate + ".frame_count", 1);
                    shops.set(coordinate + ".admin", isAdminShop);
                }
            }
        }
    }
}
