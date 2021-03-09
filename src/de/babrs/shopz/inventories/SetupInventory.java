package de.babrs.shopz.inventories;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.util.ShoppingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SetupInventory{
    private final FileConfiguration localization = ShopzPlugin.getLocalization();
    private final Player owner;
    private final Inventory inventory;
    private final ItemFrame frame;
    private final boolean isAdminShop;
    private boolean definedGood = false;
    private boolean loreAdded = false;
    private int buyPrice = 0;
    private int sellPrice = 0;

    public SetupInventory(Player owner, ItemFrame frame, boolean isAdminShop){
        this.owner = owner;
        this.frame = frame;
        this.isAdminShop = isAdminShop;
        this.inventory = Bukkit.createInventory(null, InventoryType.HOPPER, isAdminShop
                ? localization.getString("admin_shop_settings")
                : localization.getString("shop_settings"));

        InventoriesSingleton.addSetup(this);
    }

    public void openSetupDialogue(){
        ShoppingUtil.setUnderConstruction(frame);

        FileConfiguration config = ShopzPlugin.getPluginConfig();
        int stepSize = config.getInt("price_step_size");
        String currency = config.getString("currency");

        String increase = localization.getString("increase");
        String decrease = localization.getString("decrease");
        String buyPrice = localization.getString("buy_price");
        String sellPrice = localization.getString("sell_price");
        String dropHere = localization.getString("empty_settings_slot");
        String current = localization.getString("current");
        String currentSell = current + " " + sellPrice + ": 0" + currency;
        String currentBuy = current + " " + buyPrice + ": 0" + currency;

        ItemStack increaseSell = new ItemStack(Material.STRUCTURE_VOID);
        ItemStack decreaseSell = new ItemStack(Material.STRUCTURE_VOID);
        ItemStack increaseBuy = new ItemStack(Material.BARRIER);
        ItemStack decreaseBuy = new ItemStack(Material.BARRIER);
        ItemStack good = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        List<String> loreSell = isAdminShop
                ? Arrays.asList(currentSell, localization.getString("admin_shop_warning").replace("@price", sellPrice))
                : Collections.singletonList(currentSell);

        List<String> loreBuy = isAdminShop
                ? Arrays.asList(currentBuy, localization.getString("admin_shop_warning").replace("@price", buyPrice))
                : Collections.singletonList(currentBuy);

        ShoppingUtil.changeItemMeta(increaseSell, increase.replace("@price", sellPrice) + " " + stepSize + currency, loreSell);
        ShoppingUtil.changeItemMeta(decreaseSell, decrease.replace("@price", sellPrice) + " " + stepSize + currency, loreSell);
        ShoppingUtil.changeItemMeta(increaseBuy, increase.replace("@price", buyPrice) + " " + stepSize + currency, loreBuy);
        ShoppingUtil.changeItemMeta(decreaseBuy, decrease.replace("@price", buyPrice) + " " + stepSize + currency, loreBuy);

        ItemMeta meta = good.getItemMeta();
        meta.setDisplayName(dropHere);
        meta.setLore(Arrays.asList(currentSell, currentBuy));
        good.setItemMeta(meta);

        inventory.setItem(0, decreaseSell);
        inventory.setItem(1, increaseSell);
        inventory.setItem(2, good);
        inventory.setItem(3, decreaseBuy);
        inventory.setItem(4, increaseBuy);

        owner.openInventory(inventory);
    }

    public void publish(){
        String prefix = ShopzPlugin.getPrefix();
        boolean success = true;

        if(buyPrice >= sellPrice && !isAdminShop){
            String bP = localization.getString("buy_price");
            String sP = localization.getString("sell_price");

            owner.sendMessage(prefix + ChatColor.RED + localization.getString("error_buy_ge_sell").replace("@buy_price", bP).replace("@sell_price", sP));
            success = false;
        }else if(isAdminShop && buyPrice > sellPrice){
            owner.sendMessage(prefix + ChatColor.RED + localization.getString("error_admin_sell_gr_buy"));
            success = false;
        }

        if(!definedGood){
            owner.sendMessage(prefix + ChatColor.RED + localization.getString("error_empty_good"));
            success = false;
        }

        FileConfiguration shops = ShopzPlugin.getShops();
        Block block = ShoppingUtil.getBlockWithAttachedItemFrame(frame);
        String path = ShoppingUtil.blockToPath(block);
        if(success){
            if(frame.isDead()){
                shops.set(path, null);
                owner.sendMessage(prefix + ChatColor.RED + localization.getString("error"));
            }else{
                ItemStack good = inventory.getItem(2);

                shops.set(path + ".amount", good.getAmount());
                shops.set(path + ".buy_price", buyPrice);
                shops.set(path + ".sell_price", sellPrice);

                ShopzPlugin.saveShops();

                List<String> lore = good.getLore();
                good.setLore(lore);
                frame.setItem(good);

                String pos = "(" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")";
                if(isAdminShop){
                    for(Player admin : Bukkit.getOnlinePlayers())
                        if(admin.hasPermission("shopz.admin"))
                            admin.sendMessage(prefix + localization.getString("placed_admin_shop").replace("@pos", pos));
                }else
                    owner.sendMessage(prefix + localization.getString("placed_shop_frame").replace("@pos", pos));
            }
        }else{
            List<String> frames = (List<String>) shops.get(path + ".frames");
            for(String f : frames){
                ShoppingUtil.removeAndDropItemFrame((ItemFrame) Bukkit.getEntity(UUID.fromString(f)), isAdminShop);
            }
            shops.set(path, null);
        }
    }

    public Player getOwner(){
        return owner;
    }

    public Inventory getInventory(){
        return inventory;
    }

    public ItemFrame getFrame(){
        return frame;
    }

    public void changeSellPrice(int value){
        sellPrice = Math.max(0, sellPrice + value);
        generateLore(false, inventory.getItem(0), inventory.getItem(1));
    }

    public void changeBuyPrice(int value){
        buyPrice = Math.max(0, buyPrice + value);
        generateLore(true, inventory.getItem(3), inventory.getItem(4));
    }

    public void changeGood(ItemStack newGood){
        if(newGood.getType() != Material.AIR){
            int amt = newGood.getAmount();
            Material mat = newGood.getType();
            ItemMeta meta = newGood.getItemMeta();
            ItemStack toTrade = new ItemStack(mat, amt);
            String goodName = ShoppingUtil.generateMaterialName(mat);

            meta.setDisplayName(ChatColor.BOLD + "" + amt + "x " + goodName);
            toTrade.setItemMeta(meta);

            loreAdded = false;
            updateGoodLore(toTrade);
            getInventory().setItem(2, toTrade);
            definedGood = true;
        }
    }

    private void generateLore(boolean buy, ItemStack decrease, ItemStack increase){
        loreAdded = true;
        FileConfiguration localization = ShopzPlugin.getLocalization();

        String currency = ShopzPlugin.getPluginConfig().getString("currency");
        String current = localization.getString("current");
        String price = buy ? localization.getString("buy_price") : localization.getString("sell_price");

        String curPrice = current + " " + price + ": " + (buy ? buyPrice : sellPrice) + currency;
        List<String> lore = isAdminShop
                ? Arrays.asList(curPrice, localization.getString("admin_shop_warning").replace("@price", price))
                : Collections.singletonList(curPrice);

        ShoppingUtil.changeItemMeta(decrease, decrease.getItemMeta().getDisplayName(), lore);
        ShoppingUtil.changeItemMeta(increase, increase.getItemMeta().getDisplayName(), lore);

        updateGoodLore(getInventory().getItem(2));
    }

    private void updateGoodLore(ItemStack toTrade){
        ItemMeta meta = toTrade.getItemMeta();
        List<String> lore = meta.hasLore() && meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        if(loreAdded){
            int size = lore.size() - 1;
            lore.set(size - 1, inventory.getItem(0).getLore().get(0));
            lore.set(size, inventory.getItem(3).getLore().get(0));
        }else{
            lore.add(inventory.getItem(0).getLore().get(0));
            lore.add(inventory.getItem(3).getLore().get(0));
            loreAdded = true;
        }
        meta.setLore(lore);
        toTrade.setItemMeta(meta);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        SetupInventory that = (SetupInventory) o;
        return Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode(){
        return Objects.hash(owner);
    }
}
