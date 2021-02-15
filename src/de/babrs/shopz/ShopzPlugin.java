package de.babrs.shopz;

import de.babrs.shopz.commands.CommandShopz;
import de.babrs.shopz.inventories.InventoriesSingleton;
import de.babrs.shopz.inventories.SetupInventory;
import de.babrs.shopz.inventories.ShoppingInventory;
import de.babrs.shopz.listerners.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;

public class ShopzPlugin extends JavaPlugin{
    private static final Logger logger = Logger.getLogger(ShopzPlugin.class.getName());
    private static Economy econ = null;
    private static PluginDescriptionFile description;
    private static FileConfiguration config;
    private static FileConfiguration shops;
    private static FileConfiguration localization;
    private static File shopsFile;
    private static String prefix;
    private static ShopzPlugin instance;

    @Override
    public void onEnable(){
        //TODO: Permissions (auch für Shoperstellung mit Frame)
        //TODO: Commands
        //TODO: Doku?
        //TODO: /help command
        ShopzPlugin.description = super.getDescription();

        loadEconomy();
        loadConfig();
        loadLocalization();
        loadShops();
        registerEvents();

        instance = this;
    }

    @Override
    public void reloadConfig(){
        super.reloadConfig();
        config = super.getConfig();
    }

    @Override
    public void onDisable(){
        closeInventories();
        saveConfig();
        saveShops();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args){
        String name = cmd.getName().toLowerCase();

        if(name.equals("shopz")){
            return CommandShopz.run(sender, cmd, lbl, args);
        }
        return false;
    }

    public static PluginDescriptionFile getPluginDescription(){
        return description;
    }

    public static FileConfiguration getPluginConfig(){
        return config;
    }

    public static FileConfiguration getShops(){
        return shops;
    }

    public static FileConfiguration getLocalization(){
        return localization;
    }

    public static ShopzPlugin getInstance(){
        return instance;
    }

    public static Economy getEconomy(){
        return econ;
    }

    public static String getPrefix(){
        return prefix;
    }

    private void registerEvents(){
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new InventoryCloseListener(), this);
        manager.registerEvents(new InventoryClickListener(), this);
        manager.registerEvents(new InteractItemFrameListener(), this);
        manager.registerEvents(new PlaceItemFrameListener(), this);
        manager.registerEvents(new BreakItemFrameListener(), this);
        manager.registerEvents(new RemoveItemFromItemFrameListener(), this);
        manager.registerEvents(new AnvilRenameListener(), this);
        manager.registerEvents(new TabCompleteListener(), this);
    }

    private void loadEconomy(){
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
    }

    private void loadShops(){
        shopsFile = new File(getDataFolder(), "shops.yml");

        try{
            if(shopsFile.getParentFile().mkdirs() | shopsFile.createNewFile()){
                //Datei existierte noch nicht
                logger.info("Created empty shops.yml");
            }
            shops = YamlConfiguration.loadConfiguration(shopsFile);
            logger.info("Loaded shops.yml successfully.");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void reloadConfigurations(){
        instance.loadLocalization();
        instance.loadConfig();
    }

    private void loadLocalization(){
        localization = loadFile("localization.yml");
    }

    private void loadConfig(){
        ShopzPlugin.config = loadFile("config.yml");
        ShopzPlugin.prefix = (String) config.get("chat_prefix");
    }

    private YamlConfiguration loadFile(String path){
        File file = new File(getDataFolder(), path);

        try{
            if(file.getParentFile().mkdirs() | file.createNewFile()){
                InputStreamReader inputStreamReader = new InputStreamReader(getResource(path));
                InputStream inputStream = IOUtils.toInputStream(IOUtils.toString(inputStreamReader), "UTF-8");
                FileUtils.copyInputStreamToFile(inputStream, file);
                inputStreamReader.close();
                inputStream.close();

                logger.info("Copied " + path + " to plugins/" + getPluginDescription().getName() + "/.");
            }
            logger.info("Loaded " + path + " successfully.");
            return YamlConfiguration.loadConfiguration(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void saveShops(){
        try{
            shops.save(shopsFile);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void closeInventories(){
        for(Player p : Bukkit.getOnlinePlayers()){
            SetupInventory setup = InventoriesSingleton.getSetupFrom(p);
            if(setup != null){
                p.closeInventory();
                setup.getFrame().remove();
                setup.publish();
            }
        }

        for(ShoppingInventory inv : InventoriesSingleton.getOpenShops())
            for(HumanEntity e : inv.getShopInventory().getViewers())
                e.closeInventory();
    }
}
