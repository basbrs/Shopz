package de.babrs.shopz.listerners;

import de.babrs.shopz.inventories.SetupInventory;
import de.babrs.shopz.inventories.InventoriesSingleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class InventoryCloseListener implements Listener{

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inv = event.getInventory();
        if(InventoriesSingleton.getShopWithInventory(inv) != null){
            InventoriesSingleton.removeShopWithInventory(inv);
        }else{
            SetupInventory setup = InventoriesSingleton.getSetupFrom((Player) event.getPlayer());
            if(setup != null){
                InventoriesSingleton.removeSetupFrom((Player) event.getPlayer());
                setup.publish();
            }
        }
    }
}
