package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import de.babrs.shopz.ShoppingUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AnvilRenameListener implements Listener{
    @EventHandler
    public void onAnvilRename(InventoryClickEvent event){
        Inventory inv = event.getInventory();

        if(inv.getType() == InventoryType.ANVIL //when Player is renaming an ItemFrame
                && event.getSlotType() == InventoryType.SlotType.RESULT
                && event.getCurrentItem() != null
                && event.getCurrentItem().getType() != Material.AIR
                && event.getWhoClicked().getItemOnCursor().getType() == Material.AIR
                && event.getCurrentItem().getType() == Material.ITEM_FRAME
                && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase((String) ShopzPlugin.getPluginConfig().get("frame_name"))){
            FileConfiguration config = ShopzPlugin.getPluginConfig();
            FileConfiguration localization = ShopzPlugin.getLocalization();
            Economy econ = ShopzPlugin.getEconomy();
            Player player = (Player) event.getWhoClicked();
            String prefix = ShopzPlugin.getPrefix();
            int amount = event.getCurrentItem().getAmount();
            int moneyCost = config.getInt("frame_cost_money");

            if(econ.getBalance(player) < moneyCost * amount){
                player.sendMessage(prefix + ((String) localization.get("not_enough_money_to_create_shop"))
                        .replace("@amount", Integer.toString(amount))
                        .replace("@required", Integer.toString(moneyCost * amount))
                        .replace("@currency", (String) config.get("currency")));
            }else{
                econ.withdrawPlayer(player, moneyCost * amount);
                player.setItemOnCursor(ShoppingUtil.createShopTokens(amount, false));
                event.getInventory().setItem(0, new ItemStack(Material.AIR));
            }
            event.setCancelled(true);
        }
    }
}
