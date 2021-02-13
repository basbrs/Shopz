package de.babrs.shopz;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
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
    private final Economy econ;
    private final FileConfiguration localization;
    private final FileConfiguration shops;

    public ShoppingInventory(Inventory inventory, BlockInventoryHolder block, Material good, int amount, int buyPrice, int sellPrice){
        this.shopInventory = inventory;
        this.block = block;
        this.good = good;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.amount = amount;
        this.isAdminShop = ShopzPlugin.getShops().getBoolean(ShoppingUtil.blockToPath(block.getBlock()) + ".admin");
        this.econ = ShopzPlugin.getEconomy();
        this.localization = ShopzPlugin.getLocalization();
        this.shops = ShopzPlugin.getShops();
    }

    public void trade(Player p, boolean buy, int transactionCount){
        OfflinePlayer owner = null;
        String prefix = ShopzPlugin.getPrefix();

        if(!isAdminShop)
            owner = Bukkit.getOfflinePlayer(UUID.fromString(shops.getString(ShoppingUtil.blockToPath(getBlock()) + ".owner")));

        if(isAdminShop || econ.getBalance(p) >= buyPrice){
            boolean hasInvSpace = buy
                    ? ShoppingUtil.hasInventorySpaceFor(p.getInventory(), getStack(amount))
                    : isAdminShop || ShoppingUtil.hasInventorySpaceFor(getChestInventory(), getStack(amount));
            if(hasInvSpace){
                if(buy ? transferBuy() : transferSell()){
                    if(buy){
                        econ.withdrawPlayer(p, buyPrice);
                        if(!isAdminShop)
                            econ.depositPlayer(owner, buyPrice);
                    }else{
                        if(!isAdminShop)
                            econ.withdrawPlayer(owner, sellPrice);
                        econ.depositPlayer(p, sellPrice);
                    }

                    p.sendMessage(prefix + generateText(buy, true, isAdminShop ? localization.getString("admin_shop") : owner.getName()));

                    if(!isAdminShop && owner.isOnline())
                        ((Player) owner).sendMessage(prefix + generateText(!buy, false, p.getName()));
                }else{
                    String message = (buy
                    ? prefix + localization.getString("shop_insufficient_stock")
                            .replace("@pos", "(" + getBlock().getX() + ", " + getBlock().getY() + ", " + getBlock().getZ() + ")")
                    : prefix + localization.getString("player_insufficient_stock"))
                            .replace("@good", ShoppingUtil.generateMaterialName(good));

                    p.sendMessage(message);
                    if(buy && !isAdminShop && owner.isOnline())
                        ((Player) owner).sendMessage(message);
                }
            }else{
                String message = buy
                        ? prefix + localization.getString("no_space_inv")
                        : prefix + (localization.getString("no_space_shop"))
                            .replace("@pos", "(" + getBlock().getX() + ", " + getBlock().getY() + ", " + getBlock().getZ() + ")");
                p.sendMessage(message);
                if(!buy && owner.isOnline())
                    ((Player) owner).sendMessage(message);
            }
        }else{
            String message = buy
                    ? prefix + localization.getString("not_enough_money")
                    : prefix + (localization.getString("owner_not_enough_money")).replace("@owner", owner.getName());
            p.sendMessage(message);
        }
    }

    private String generateText(boolean buy, boolean forCustomer, String otherName){
        String currency = ShopzPlugin.getPluginConfig().getString("currency");
        String buyText = localization.getString("buy_success");
        String sellText = localization.getString("sell_success");

        if(buy) return buyText.replace("@amount", Integer.toString(amount))
                    .replace("@good", ShoppingUtil.generateMaterialName(good))
                    .replace("@buy_price", forCustomer ? Integer.toString(buyPrice) : Integer.toString(sellPrice))
                    .replace("@currency", currency)
                    .replace("@owner", otherName);
        else return sellText.replace("@amount", Integer.toString(amount))
                .replace("@good", ShoppingUtil.generateMaterialName(good))
                .replace("@buy_price", forCustomer ? Integer.toString(sellPrice) : Integer.toString(buyPrice))
                .replace("@currency", currency)
                .replace("@buyer", otherName);
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
