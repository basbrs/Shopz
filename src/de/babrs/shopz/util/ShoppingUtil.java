package de.babrs.shopz.util;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.inventories.InventoriesSingleton;
import de.babrs.shopz.inventories.ShoppingInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.*;

public class ShoppingUtil{
    public static String blockToPath(Block b){
        StringBuilder sb = new StringBuilder();
        sb.append(b.getWorld().getName()).append(".");
        sb.append(b.getLocation().getBlockX()).append("|");
        sb.append(b.getLocation().getBlockY()).append("|");
        sb.append(b.getLocation().getBlockZ());
        return sb.toString();
    }

    public static void changeItemMeta(ItemStack is, String name, List<String> lore){
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        is.setItemMeta(meta);
    }

    public static Block getBlockWithAttachedItemFrame(ItemFrame e){
        return e.getLocation().add(e.getAttachedFace().getDirection()).getBlock();
    }

    public static void setUnderConstruction(ItemFrame frame){
        ItemStack underWork = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = underWork.getItemMeta();
        meta.setDisplayName(ShopzPlugin.getLocalization().getString("shop_settings"));
        underWork.setItemMeta(meta);
        frame.setItem(underWork);
    }

    public static void removeAndDropItemFrame(ItemFrame frame, boolean adminShop){
        frame.remove();
        if(!adminShop){
            ItemStack itemStack = createShopTokens(1, false);
            frame.getWorld().dropItem(frame.getLocation(), itemStack);
        }
    }

    public static ItemStack createShopTokens(int amount, boolean adminShop){
        ItemStack itemStack = new ItemStack(Material.ITEM_FRAME, amount);
        List<String> lore = Arrays.asList(getValidToken(adminShop));
        itemStack.setLore(lore);
        return itemStack;
    }

    public static String getValidToken(boolean adminShop){
        String admin = adminShop ? "Admin " : "";
        return ChatColor.GOLD + "" + ChatColor.ITALIC + admin + ShopzPlugin.getLocalization().get("frame_name_item");
    }

    public static String generateMaterialName(Material material){
        return WordUtils.capitalize(material.name().replace("_", " ").toLowerCase());
    }

    public static void removeShopFrame(ItemFrame entity){
        FileConfiguration shops = ShopzPlugin.getShops();
        FileConfiguration localization = ShopzPlugin.getLocalization();

        Block attachedTo = getBlockWithAttachedItemFrame(entity);
        String path = blockToPath(attachedTo);

        int frameCount = shops.getInt(path + ".frame_count");
        String ownerUUID = shops.getString(path + ".owner");
        Player owner = ownerUUID.equals("admin") ? null : Bukkit.getPlayer(UUID.fromString(ownerUUID));
        String prefix = ShopzPlugin.getPrefix();
        if(frameCount <= 1){
            if(owner != null && owner.isOnline()){
                String msg = prefix + localization.get("remove_last_frame");
                msg = msg.replace("@pos", "(" + attachedTo.getX() + ", " + attachedTo.getY() + ", " + attachedTo.getZ() + ")");
                owner.sendMessage(msg);
            }
            removeAndDropItemFrame(entity, shops.getBoolean(path + ".admin"));
            shops.set(path, null);
        }else{
            if(owner != null && owner.isOnline()){
                String msg = prefix + localization.get("remove_one_frame");
                msg = msg.replace("@pos", "(" + attachedTo.getX() + ", " + attachedTo.getY() + ", " + attachedTo.getZ() + ")");
                msg = msg.replace("@left", Integer.toString(frameCount - 1));
                owner.sendMessage(msg);
            }
            List<String> frames = (ArrayList<String>) ShopzPlugin.getShops().get(path + ".frames");
            frames.remove(entity.getUniqueId().toString());

            shops.set(path + ".frames", frames);
            shops.set(path + ".frame_count", frameCount - 1);
            removeAndDropItemFrame(entity, shops.getBoolean(path + ".admin"));
        }
        ShopzPlugin.saveShops();
    }

    public static void closeAllVisitorInventories(Block attachedTo){
        List<Player> shopVisitors = new ArrayList<>();
        List<ShoppingInventory> openShops = InventoriesSingleton.getOpenShopsAtBlock(attachedTo);

        if(openShops != null)
            for(ShoppingInventory inv : openShops)
                for(InventoryHolder holder : inv.getShopInventory().getViewers())
                    if(holder instanceof Player){
                        shopVisitors.add((Player) holder);
                    }

        for(Player visitor : shopVisitors){
            visitor.closeInventory();
            visitor.sendMessage(ShopzPlugin.getPrefix() + ShopzPlugin.getLocalization().get("shop_settings"));
        }
    }

    public static boolean hasInventorySpaceFor(Inventory inventory, ItemStack item){
        if(inventory.firstEmpty() != -1)
            return true;
        int left = item.getAmount();

        for(ItemStack invStack : inventory.getContents()){
            if(invStack != null && areSimilar(item, invStack)){
                left -= (item.getMaxStackSize() - invStack.getAmount());
                if(left <= 0)
                    return true;
            }
        }
        return false;
    }

    public static boolean areSimilar(ItemStack item1, ItemStack item2){
        if(item1.getType() != item2.getType())
            return false;

        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();
        boolean result = meta1.getEnchants().equals(meta2.getEnchants());

        if(result && meta1 instanceof Damageable)
            result = !((Damageable) meta1).hasDamage() && !((Damageable) meta2).hasDamage();
        if(result && meta1 instanceof EnchantmentStorageMeta)
            result = meta2 instanceof EnchantmentStorageMeta
                    && ((EnchantmentStorageMeta) meta1).getStoredEnchants().equals(((EnchantmentStorageMeta) meta2).getStoredEnchants());
        else if(result && meta1 instanceof PotionMeta)
            result = meta2 instanceof PotionMeta
                    && ((PotionMeta) meta1).getBasePotionData().equals(((PotionMeta) meta2).getBasePotionData());

        return result;
    }
}
