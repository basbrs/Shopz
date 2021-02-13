package de.babrs.shopz.inventories;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoriesSingleton{
    private static ArrayList<ShoppingInventory> shops;
    private static ArrayList<SetupInventory> setups;

    private InventoriesSingleton(){}

    public static void addShop(ShoppingInventory i){
        if(InventoriesSingleton.shops == null)
            InventoriesSingleton.shops = new ArrayList<>();
        InventoriesSingleton.shops.add(i);
    }

    public static ShoppingInventory getShopWithInventory(Inventory inv){
        if(InventoriesSingleton.shops == null)
            return null;
        for(ShoppingInventory si : shops)
            if(si.getShopInventory().equals(inv))
                return si;
        return null;
    }

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

    public static void removeShopWithInventory(Inventory inv){
        if(InventoriesSingleton.shops == null)
            return;
        shops.removeIf(si -> si.getShopInventory() == inv);
    }

    public static List<ShoppingInventory> getOpenShops(){
        if(InventoriesSingleton.shops == null)
            return Collections.EMPTY_LIST;

        return InventoriesSingleton.shops;
    }

    //------------------------------- SetupInventories -------------------------------//

    public static void addSetup(SetupInventory i){
        if(InventoriesSingleton.setups == null)
            InventoriesSingleton.setups = new ArrayList<>();
        InventoriesSingleton.setups.add(i);
    }

    public static SetupInventory getSetupFrom(Player player){
        if(InventoriesSingleton.setups == null)
            return null;
        for(SetupInventory su : setups)
            if(su.getOwner() == player)
                return su;
        return null;
    }

    public static void removeSetupFrom(Player player){
        if(InventoriesSingleton.setups == null)
            return;
        setups.removeIf(su -> su.getOwner() == player);
    }

    public static List<SetupInventory> getOpenSetups(){
        if(InventoriesSingleton.setups == null)
            return Collections.EMPTY_LIST;

        return InventoriesSingleton.setups;
    }
}
