package de.babrs.shopz.listerners;

import de.babrs.shopz.*;
import de.babrs.shopz.inventories.InventoriesSingleton;
import de.babrs.shopz.inventories.SetupInventory;
import de.babrs.shopz.inventories.ShoppingInventory;
import de.babrs.shopz.util.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InteractItemFrameListener implements Listener{

    @EventHandler
    public void onItemFrameInteraction(PlayerInteractEntityEvent event){
        if(event.getRightClicked() instanceof ItemFrame){
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            FileConfiguration shops = ShopzPlugin.getShops();
            Block attachedTo = ShoppingUtil.getBlockWithAttachedItemFrame(frame);
            String path = ShoppingUtil.blockToPath(attachedTo);

            if(shops.get(path) != null){
                FileConfiguration localization = ShopzPlugin.getLocalization();
                FileConfiguration config = ShopzPlugin.getPluginConfig();

                Player p = event.getPlayer();
                String prefix = ShopzPlugin.getPrefix();
                List<String> frames = (List<String>) shops.get(path + ".frames");
                String underConstruction = localization.getString("shop_settings");

                if(frames == null){
                    p.sendMessage(prefix + underConstruction);
                    event.setCancelled(true);
                }else{
                    if(frames.contains(frame.getUniqueId().toString())){
                        //Frame ist ein registierter Shop-Frame
                        String ownerUUID = shops.getString(path + ".owner");
                        event.setCancelled(true);
                        if(p.getUniqueId().toString().equals(ownerUUID)){
                            ShoppingUtil.closeAllVisitorInventories(attachedTo);
                            new SetupInventory(p, frame, false).openSetupDialogue();
                            for(String uuid : frames)
                                ShoppingUtil.setUnderConstruction((ItemFrame) Objects.requireNonNull(Bukkit.getEntity(UUID.fromString(uuid))));
                        }else if(p.hasPermission("shopz.trade")){
                            //Wird der Shop in diesem Moment erneut durch den Besitzer editiert?
                            String frameName = frame.getItem().getItemMeta().getDisplayName();
                            Material frameDisplay = frame.getItem().getType();

                            if(frameName.equals(underConstruction) && frameDisplay == Material.GOLD_INGOT){
                                p.sendMessage(prefix + underConstruction);
                                event.setCancelled(true);
                                return;
                            }

                            boolean isAdminShop = shops.getBoolean(path + ".admin");
                            String title = isAdminShop ? localization.getString("admin_shop") : localization.getString("shop_title");
                            String buyText = localization.getString("buy_text");
                            String sellText = localization.getString("sell_text");
                            String currency = config.getString("currency");

                            int amount = shops.getInt(path + ".amount");

                            //buy_price and sell_price from the perspective of the shop-owner
                            int buyPrice = shops.getInt(path + ".sell_price"); //Ankaufspreis
                            int sellPrice = shops.getInt(path + ".buy_price"); //Verkaufspreis
                            String sellPriceText = (String) localization.get("sell_price");
                            String buyPriceText = (String) localization.get("buy_price");

                            String owner = isAdminShop ? "" : Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();

                            Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, title.replace("@user", owner));
                            ArrayList<String> lore = new ArrayList<>();

                            ItemStack buy = new ItemStack(Material.STRUCTURE_VOID);
                            ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                            ItemStack good = frame.getItem();
                            good.setAmount(amount);

                            String goodName = ShoppingUtil.generateMaterialName(good.getType());

                            ItemMeta buyMeta = buy.getItemMeta();
                            buyMeta.setDisplayName(
                                    buyText.replace("@amount", amount + "")
                                            .replace("@good", goodName)
                                            .replace("@buy_price", buyPrice + "")
                                            .replace("@currency", currency));
                            buy.setItemMeta(buyMeta);

                            ItemMeta fillerMeta = filler.getItemMeta();
                            if(isAdminShop) fillerMeta.setDisplayName(ChatColor.MAGIC + "O " + ChatColor.DARK_PURPLE + title + ChatColor.RESET + ChatColor.MAGIC + " O");
                            else{
                                lore.add(owner);
                                fillerMeta.setDisplayName(title.replace("@user", ""));
                            }
                            fillerMeta.setLore(lore);
                            filler.setItemMeta(fillerMeta);

                            ItemMeta goodMeta = good.getItemMeta();
                            lore.clear();
                            lore.add(buyPriceText + ": " + buyPrice + currency); //Ankaufspreis: 40$

                            if(sellPrice > 0){
                                ItemStack sell = new ItemStack(Material.BARRIER);
                                ItemMeta sellMeta = sell.getItemMeta();
                                sellMeta.setDisplayName(
                                        sellText.replace("@amount", amount + "")
                                                .replace("@good", goodName)
                                                .replace("@sell_price", sellPrice + "")
                                                .replace("@currency", currency));
                                sell.setItemMeta(sellMeta);
                                lore.add(sellPriceText + ": " + sellPrice + currency); //Verkaufspreis: 0$

                                inv.setItem(3, sell);
                            }

                            goodMeta.setLore(lore);
                            good.setItemMeta(goodMeta);

                            inv.setItem(0, filler);
                            inv.setItem(1, buy);
                            inv.setItem(2, good);
                            inv.setItem(4, filler);

                            if(sellPrice <= 0)
                                inv.setItem(3, buy);

                            InventoriesSingleton.addShop(new ShoppingInventory(inv, (BlockInventoryHolder) attachedTo.getState(), good.getType(), amount, buyPrice, sellPrice));

                            p.openInventory(inv);
                        }else{
                            p.sendMessage(ShopzPlugin.getLocalization().getString("no_permission"));
                        }
                    }
                }
            }
        }
    }
}
