package de.babrs.shopz.inventories;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton to handle all open Inventories, this is crucial for detecting interactions with the inventories, be it closing,
 * opening or closing them (and thus calling events)
 */
public class InventoriesSingleton{
    //List of all ShoppingInventories, these are shops that players currently have open to buy or sell
    private static ArrayList<ShoppingInventory> shops;

    //List of all SetupInventories, these are the inventories player use to setup or edit their shops
    private static ArrayList<SetupInventory> setups;

    /**
     * Empty constructor, private to prevent instances of this singleton to be created from anywhere but this class
     */
    private InventoriesSingleton(){}

    /**
     * Adds a {@link ShoppingInventory} @i to the Singleton.
     * Creates the Singleton-Object if there was none before.
     * @param i The {@link ShoppingInventory} to add.
     */
    public static void addShop(ShoppingInventory i){
        if(InventoriesSingleton.shops == null)
            InventoriesSingleton.shops = new ArrayList<>();
        InventoriesSingleton.shops.add(i);
    }

    /**
     * Checks whether there is a {@link ShoppingInventory} with the key @inv and returns it, so it exists.
     * @param inv The {@link Inventory} to look for (i.e. the inventory clicked or closed).
     * @return null if the Singleton is null or if there is no {@link ShoppingInventory} with the {@link Inventory} @inv.
     */
    public static ShoppingInventory getShopWithInventory(Inventory inv){
        if(InventoriesSingleton.shops == null)
            return null;
        for(ShoppingInventory si : shops)
            if(si.getShopInventory().equals(inv))
                return si;
        return null;
    }

    /**
     * Checks whether there are {@link Player} currently shopping at a shop at the specified {@link Block} @b. Returns a
     * list of {@link ShoppingInventory} if there is at least one person trading, null otherwise.
     * @param b {@link Block} identifying the {@link ShoppingInventory}.
     * @return List of all currently open {@link ShoppingInventory} of {@link Player} shopping at {@link Block} @b.
     */
    public static List<ShoppingInventory> getOpenShopsAtBlock(Block b){
        if(InventoriesSingleton.shops == null)
            return null;

        List<ShoppingInventory> openShops = new ArrayList<>();
        for(ShoppingInventory si : shops){
            Block siBlock = si.getBlock();
            if(siBlock.getWorld() == b.getWorld()
                    && siBlock.getX() == b.getX()
                    && siBlock.getY() == b.getY()
                    && siBlock.getZ() == b.getZ())
                openShops.add(si);
        }
        return openShops;
    }

    /**
     * Uses {@link Inventory} @inv as a key to check in the {@link ShoppingInventory}-List and deletes the corresponding
     * entry.
     * @param inv Key for {@link ShoppingInventory} to delete.
     */
    public static void removeShopWithInventory(Inventory inv){
        if(InventoriesSingleton.shops == null)
            return;
        shops.removeIf(si -> si.getShopInventory() == inv);
    }

    /**
     * Returns a list of all currently opened {@link ShoppingInventory} on the server.
     * @return Empty List if noone is shopping at the moment, a List of all open {@link ShoppingInventory} otherwise.
     */
    public static List<ShoppingInventory> getOpenShops(){
        if(InventoriesSingleton.shops == null)
            return Collections.EMPTY_LIST;

        return InventoriesSingleton.shops;
    }

    //------------------------------- SetupInventories -------------------------------//

    /**
     * Adds a {@link SetupInventory} @i to the Singleton.
     * Creates the Singleton-Object if there was none before.
     * @param i The {@link SetupInventory} to add.
     */
    public static void addSetup(SetupInventory i){
        if(InventoriesSingleton.setups == null)
            InventoriesSingleton.setups = new ArrayList<>();
        InventoriesSingleton.setups.add(i);
    }

    /**
     * Checks whether there is a {@link SetupInventory}, opened by the {@link Player} @player and returns it, so it exists.
     * @param player The {@link Player} setting up a shop.
     * @return null if the Singleton is null or if there is no {@link SetupInventory} for {@link Player} @player.
     */
    public static SetupInventory getSetupFrom(Player player){
        if(InventoriesSingleton.setups == null)
            return null;
        for(SetupInventory su : setups)
            if(su.getOwner() == player)
                return su;
        return null;
    }

    /**
     * Uses {@link Player} @player as a key to check in the {@link SetupInventory}-List and deletes the corresponding
     * entry.
     * @param player Key for {@link SetupInventory} to delete.
     */
    public static void removeSetupFrom(Player player){
        if(InventoriesSingleton.setups == null)
            return;
        setups.removeIf(su -> su.getOwner() == player);
    }
}
