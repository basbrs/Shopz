package de.babrs.shopz.handlers;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.InventoriesSingleton;
import de.babrs.shopz.ShoppingInventory;
import de.babrs.shopz.ShoppingUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopInteractionHandler{
    public static void handle(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getClickedInventory().getItem(event.getRawSlot());

        if(cursor != null){
            FileConfiguration localization = ShopzPlugin.getLocalization();
            FileConfiguration shops = ShopzPlugin.getShops();
            Economy econ = ShopzPlugin.getEconomy();
            ShoppingInventory inventory = InventoriesSingleton.getShopWithInventory(event.getInventory());
            Block attachedTo = inventory.getBlock();

            Material goodMaterial = inventory.getGood();
            int amount = inventory.getAmount();

            int buyPrice = inventory.getBuyPrice(); //from the perspective of the shop-owner
            int sellPrice = inventory.getSellPrice(); //from the perspective of the shop-owner
            String buyText = localization.getString("buy_success");
            String sellText = localization.getString("sell_success");
            String prefix = ShopzPlugin.getPrefix();
            String currency = ShopzPlugin.getPluginConfig().getString("currency");

            String ownerUUID = shops.getString(ShoppingUtil.blockToPath(inventory.getBlock()) + ".owner");
            OfflinePlayer owner = null;
            boolean isAdminShop = ownerUUID.equals("admin");
            if(!isAdminShop)
                owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));

            if(cursor.getType() == Material.STRUCTURE_VOID){ //Player kauft
                if(econ.getBalance(player) >= buyPrice){
                    if(ShoppingUtil.hasInventorySpaceFor(player.getInventory(), inventory.getStack(amount))){
                        if(inventory.transferBuy()){
                            econ.withdrawPlayer(player, buyPrice);
                            if(!isAdminShop)
                                econ.depositPlayer(owner, buyPrice);

                            player.sendMessage(prefix + buyText.replace("@amount", amount + "")
                                    .replace("@good", ShoppingUtil.generateMaterialName(goodMaterial))
                                    .replace("@buy_price", buyPrice + "")
                                    .replace("@currency", currency)
                                    .replace("@owner", isAdminShop ? localization.getString("admin_shop") : owner.getName()));

                            if(!isAdminShop && owner.isOnline())
                                ((Player) owner).sendMessage(prefix + sellText.replace("@amount", amount + "")
                                        .replace("@good", ShoppingUtil.generateMaterialName(goodMaterial))
                                        .replace("@buy_price", buyPrice + "")
                                        .replace("@currency", currency)
                                        .replace("@buyer", player.getName()));
                        }else{
                            String noStock = prefix + localization.getString("shop_insufficient_stock")
                                    .replace("@pos", "(" + attachedTo.getX() + ", " + attachedTo.getY() + ", " + attachedTo.getZ() + ")")
                                    .replace("@good", ShoppingUtil.generateMaterialName(goodMaterial));
                            player.sendMessage(noStock);
                            if(!isAdminShop && owner.isOnline())
                                ((Player) owner).sendMessage(noStock);
                        }
                    }else{
                        player.sendMessage(prefix + localization.getString("no_space_inv"));
                    }
                }else{
                    player.sendMessage(prefix + localization.getString("not_enough_money"));
                }
            }else if(cursor.getType() == Material.BARRIER){ //Player verkauft
                if(isAdminShop || econ.getBalance(owner) >= sellPrice){
                    if(isAdminShop || ShoppingUtil.hasInventorySpaceFor(inventory.getChestInventory(), inventory.getStack(amount))){
                        if(inventory.transferSell()){
                            if(!isAdminShop)
                                econ.withdrawPlayer(owner, sellPrice);
                            econ.depositPlayer(player, sellPrice);

                            player.sendMessage(prefix + sellText.replace("@amount", amount + "")
                                    .replace("@good", ShoppingUtil.generateMaterialName(goodMaterial))
                                    .replace("@buy_price", sellPrice + "")
                                    .replace("@currency", currency)
                                    .replace("@buyer", isAdminShop ? localization.getString("admin_shop") : owner.getName()));
                            if(!isAdminShop && owner.isOnline())
                                ((Player) owner).sendMessage(prefix + buyText.replace("@amount", amount + "")
                                        .replace("@good", ShoppingUtil.generateMaterialName(goodMaterial))
                                        .replace("@buy_price", sellPrice + "")
                                        .replace("@currency", currency)
                                        .replace("@owner", player.getName()));
                        }else{
                            player.sendMessage((prefix + localization.getString("player_insufficient_stock")) .replace("@good", ShoppingUtil.generateMaterialName(goodMaterial)));
                        }
                    }else{
                        String message = prefix + (localization.getString("no_space_shop")).replace("@pos", "(" + attachedTo.getX() + ", " + attachedTo.getY() + ", " + attachedTo.getZ() + ")");
                        player.sendMessage(message);
                        if(owner.isOnline())
                            ((Player) owner).sendMessage(message);
                    }
                }else{
                    player.sendMessage(prefix + (localization.getString("owner_not_enough_money")).replace("@owner", owner.getName()));
                }
            }
            event.setCancelled(true);
        }
    }
}
