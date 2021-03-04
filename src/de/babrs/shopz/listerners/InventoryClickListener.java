package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.inventories.InventoriesSingleton;
import de.babrs.shopz.inventories.SetupInventory;
import de.babrs.shopz.inventories.ShoppingInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class InventoryClickListener implements Listener{
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Inventory inv = event.getInventory();

        if(inv.getType() == InventoryType.HOPPER){
            if(InventoriesSingleton.getShopWithInventory(inv) != null
                    && event.getRawSlot() < 5 && event.getRawSlot() >= 0){
                event.setCancelled(true);

                int transactions = 0;
                if(event.getClick() == ClickType.LEFT) transactions = 1;
                else if(event.getClick() == ClickType.SHIFT_LEFT) transactions = 5;

                ShoppingInventory shop = InventoriesSingleton.getShopWithInventory(event.getInventory());
                for(int i = 0; i < transactions; i++){
                    Material item = event.getClickedInventory().getItem(event.getRawSlot()).getType();
                    if(item == Material.STRUCTURE_VOID)
                        shop.trade((Player) event.getWhoClicked(), true);
                    else if(item == Material.BARRIER)
                        shop.trade((Player) event.getWhoClicked(), false);
                }
            }else{
                SetupInventory setup = InventoriesSingleton.getSetupFrom((Player) event.getWhoClicked());
                if(setup != null && event.getRawSlot() < 5 && event.getRawSlot() >= 0){
                    if(event.getClick() == ClickType.LEFT){
                        int slot = event.getRawSlot();
                        if(slot == 0)
                            setup.changeSellPrice(-ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                        else if(slot == 1)
                            setup.changeSellPrice(ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                        else if(slot == 2)
                            setup.changeGood(event.getWhoClicked().getItemOnCursor());
                        else if(slot == 3)
                            setup.changeBuyPrice(-ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                        else if(slot == 4)
                            setup.changeBuyPrice(ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
}