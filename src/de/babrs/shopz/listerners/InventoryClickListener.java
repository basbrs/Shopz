package de.babrs.shopz.listerners;

import de.babrs.shopz.InventoriesSingleton;
import de.babrs.shopz.SetupInventory;
import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.handlers.ShopInteractionHandler;
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
                    && event.getRawSlot() < 5 && event.getRawSlot() >= 0)
                if(event.getClick() == ClickType.LEFT)
                    ShopInteractionHandler.handle(event);
                else if(event.getClick() == ClickType.SHIFT_LEFT)
                    for(int i = 0; i < 5; i++)
                        ShopInteractionHandler.handle(event);
                else event.setCancelled(true);
            else{
                SetupInventory setup = InventoriesSingleton.getSetupFrom((Player) event.getWhoClicked());
                if(setup != null && event.getRawSlot() < 5 && event.getRawSlot() >= 0){
                    if(event.getClick() == ClickType.LEFT)
                        switch(event.getRawSlot()){
                            case 0 -> setup.changeSellPrice(-ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                            case 1 -> setup.changeSellPrice(ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                            case 2 -> setup.changeGood(event.getWhoClicked().getItemOnCursor());
                            case 3 -> setup.changeBuyPrice(-ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                            case 4 -> setup.changeBuyPrice(ShopzPlugin.getPluginConfig().getInt("price_step_size"));
                        }
                    event.setCancelled(true);
                }
            }
        }
    }
}
