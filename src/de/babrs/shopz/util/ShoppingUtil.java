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

/**
 * Utility class containing lots of static methods for access all across the plugin.
 */
public class ShoppingUtil{
    /**
     * Returns the path to navigate to a shop, placed at Block b in the world of that Block.
     * @param b Block, usually the InventoryHolder of a Shop.
     * @return yaml-path looking like this: "world.xxx|yyy|zzz", does not include a "." at the end!
     */
    public static String blockToPath(Block b){
        StringBuilder sb = new StringBuilder();
        sb.append(b.getWorld().getName()).append(".");
        sb.append(b.getLocation().getBlockX()).append("|");
        sb.append(b.getLocation().getBlockY()).append("|");
        sb.append(b.getLocation().getBlockZ());
        return sb.toString();
    }

    /**
     * Changes the Lore and DisplayName of an ItemStack
     * @param is ItemStack to change
     * @param name desired DisplayName
     * @param lore List of Lore-Lines
     */
    public static void changeItemMeta(ItemStack is, String name, List<String> lore){
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        is.setItemMeta(meta);
    }

    /**
     * Returns the Block an ItemFrame is placed against, works in all directions.
     * @param e ItemFrame placed at the desired Block.
     * @return The Block with the attached Frame.
     */
    public static Block getBlockWithAttachedItemFrame(ItemFrame e){
        return e.getLocation().add(e.getAttachedFace().getDirection()).getBlock();
    }

    /**
     * Places a placeholder-ItemStack (GoldIngot with specific name) in the ItemFrame @frame to cancel editing or
     * destruction of said frame through other Players/Entities/Environment.
     * @param frame The Frame to lock.
     */
    public static void setUnderConstruction(ItemFrame frame){
        ItemStack underWork = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = underWork.getItemMeta();
        meta.setDisplayName(ShopzPlugin.getLocalization().getString("shop_settings"));
        underWork.setItemMeta(meta);
        frame.setItem(underWork);
    }

    /**
     * Removes an ItemFrame-Entity from the world and drops a ShopToken if it was no AdminShop.
     * @param frame ItemFrame-Entity to destroy.
     * @param adminShop true if the shop was an AdminShop (will not drop a Token), false otherwise.
     */
    public static void removeAndDropItemFrame(ItemFrame frame, boolean adminShop){
        frame.remove();
        if(!adminShop){
            ItemStack itemStack = createShopTokens(1, false);
            frame.getWorld().dropItem(frame.getLocation(), itemStack);
        }
    }

    /**
     * Creates ShopTokens, is called by the /shopz admin-command (to create AdminShopTokens), or by the @AnvilRenameListener.
     * @param amount Size of ItemStack to create.
     * @param adminShop Whether the result should be AdminShopTokens (true), or regular ShopTokens (false).
     * @return An ItemStack of @amount ShopTokens (or AdminShopTokens).
     */
    public static ItemStack createShopTokens(int amount, boolean adminShop){
        ItemStack itemStack = new ItemStack(Material.ITEM_FRAME, amount);
        List<String> lore = Arrays.asList(getValidToken(adminShop));
        itemStack.setLore(lore);
        return itemStack;
    }

    /**
     * Ensures validity of Lore to display a ShopToken, will add an Admin-Tag to an AdminShopToken if specified.
     * @param adminShop true if the result should be an identifier for the AdminShopTokens, false otherwise.
     * @return The String that will later be used to check an ItemFrame for validity of being a (Admin-) ShopToken.
     */
    public static String getValidToken(boolean adminShop){
        String admin = adminShop ? "Admin " : "";
        return ChatColor.GOLD + "" + ChatColor.ITALIC + admin + ShopzPlugin.getLocalization().get("frame_name_item");
    }

    /**
     * Turns the Enumeration-Name of Material material from "ALL_UPPERCASE_WITH_UNDERSCORES" into "All Uppercase With Spaces".
     */
    public static String generateMaterialName(Material material){
        return WordUtils.capitalize(material.name().replace("_", " ").toLowerCase());
    }

    /**
     * Remove a ShopFrame from the world, will check whether it was the only ShopFrame of a shop, if it was, that shop will
     * be deleted, otherwise it will just reduce the FrameCount of the shop by one. This method ensures, that no items will
     * drop from the frame, thus creating no opportunity for item-duplication.
     * Will save the shops.yml after deleting the frame.
     * @param entity The ItemFrame to remove from it's world.
     */
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

    /**
     * Closes all ShoppingInventories of players currently opening a shop, for example on editing the shop or on removing
     * the last ItemFrame.
     */
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

    /**
     * Will check, whether the ItemStack @item will fit into Inventory @inventory. Will check for blank spaces, as well
     * as already obtained stacks of the same Material and Lore as @item that are not fully stacked.
     * @param inventory The Inventory to deposit @item in.
     * @param item ItemStack to buy/sell and to deposit in @inventory.
     * @return true if there is enough space, false otherwise.
     */
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

    /**
     * Used to determine whether two ItemStacks are similar and thus stackable (if amounts and Material allow).
     * Will check for Material,
     * as well as ItemMeta, latter will ensure, that potions are not bought/sold unless being exactly the same (same effect,
     * same length and same activation), armor- and weapons/tools are not bought/sold unless on full durability and enchanted
     * books are not bought/sold, unless their enchantments, as well as their level match.
     * @param item1 First ItemStack to match @item2
     * @param item2 Second ItemStack to match @item1
     * @return true if both stacks are similar, false otherwise.
     */
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
