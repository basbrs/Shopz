package de.babrs.shopz;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShoppingInventory{
    private final Inventory shopInventory;
    private final BlockInventoryHolder block;
    private final int buyPrice;
    private final int sellPrice;
    private final int amount;
    private final Material good;
    private final boolean isAdminShop;

    public ShoppingInventory(Inventory inventory, BlockInventoryHolder block, Material good, int amount, int buyPrice, int sellPrice){
        this.shopInventory = inventory;
        this.block = block;
        this.good = good;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.amount = amount;
        this.isAdminShop = ShopzPlugin.getShops().getBoolean(ShoppingUtil.blockToPath(block.getBlock()) + ".admin");
    }

    public Inventory getShopInventory(){
        return shopInventory;
    }

    public Inventory getChestInventory(){
        return block.getInventory();
    }

    public Block getBlock(){
        return block.getBlock();
    }

    public int getBuyPrice(){
        return buyPrice;
    }

    public int getSellPrice(){
        return sellPrice;
    }

    public int getAmount(){
        return amount;
    }

    public Material getGood(){
        return good;
    }

    public List<ItemFrame> getFrames(){
        FileConfiguration shops = ShopzPlugin.getShops();
        List<String> uuids = (List<String>) shops.get(ShoppingUtil.blockToPath(block.getBlock()) + ".frames");
        ArrayList<ItemFrame> frames = new ArrayList<>();
        for(String uuid : uuids)
            frames.add((ItemFrame) Bukkit.getEntity(UUID.fromString(uuid)));
        return frames;
    }

    private boolean hasSufficientStock(Inventory inv){
        int left = amount;
        ItemStack toMatch = getFrames().get(0).getItem();

        for(ItemStack is : inv.getContents()){
            if(is != null && ShoppingUtil.areSimilar(toMatch, is)){
                left -= is.getAmount();
                if(left <= 0)
                    return true;
            }
        }
        return false;
    }

    public boolean transferSell(){
        return transfer(shopInventory.getViewers().get(0).getInventory(), block.getInventory());
    }

    public boolean transferBuy(){
        return transfer(block.getInventory(), shopInventory.getViewers().get(0).getInventory());
    }

    private boolean transfer(Inventory from, Inventory to){
        if(isAdminShop && block.getInventory().equals(from)){
            to.addItem(getStack(amount));
            return true;
        }

        if(!hasSufficientStock(from)){
            return false;
        }
        ItemStack items = getStack(0);

        for(ItemStack is : from.getContents()){
            if(is != null && ShoppingUtil.areSimilar(items, is)){
                int toTake = Math.min(is.getAmount(), amount - items.getAmount());
                is.subtract(toTake);
                items.add(toTake);
            }
        }

        if(!(isAdminShop && block.getInventory().equals(to)))
            to.addItem(items);
        return true;
    }

    public ItemStack getStack(int amount){
        ItemStack stack = new ItemStack(good, amount);
        ItemStack frameItem = getFrames().get(0).getItem();

        ItemMeta meta = frameItem.getItemMeta();
        meta.setDisplayName(stack.getItemMeta().getDisplayName());
        meta.setLore(stack.getItemMeta().getLore());
        stack.setItemMeta(meta);
        return stack;
    }
}
