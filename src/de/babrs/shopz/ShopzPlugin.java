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

/**
 * {@link ShopzPlugin} by babrs
 *
 * This plugin enables you to deploy dynamic User- and AdminServers for trading {@link org.bukkit.inventory.ItemStack} of
 * up to 64 Items. You are able to open shops with renamed {@link org.bukkit.entity.ItemFrame} (just use an anvil and
 * rename a stack of {@link org.bukkit.entity.ItemFrame} to whatever is specified in config.yml/frame_name ($shop per default).
 * AdminShops can be created by using the /shopz admin command and using the resulting {@link org.bukkit.entity.ItemFrame}
 * as previously explained.
 */
public class ShopzPlugin extends JavaPlugin{
    private static final Logger logger = Logger.getLogger("Shopz");
    private static Economy econ = null;
    private static PluginDescriptionFile description;
    private static FileConfiguration config;
    private static FileConfiguration shops;
    private static FileConfiguration localization;
    private static File shopsFile;
    private static String prefix;
    private static ShopzPlugin instance;

    /**
     * onEnable(), inherited from {@link JavaPlugin}, loads configurations and shops, registers events and saves a singleton of
     * this class for later use.
     */
    @Override
    public void onEnable(){
        ShopzPlugin.description = super.getDescription();

        if(!loadEconomy()){
            logger.severe("Plugin disabled because vault-based economy could not be loaded.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        loadConfig();
        loadLocalization();
        loadShops();
        registerEvents();

        instance = this;
    }

    /**
     * Reload config.yml from hard drive, this will override all changes made since the last server restart.
     * Will also redefine the static config-object.
     */
    @Override
    public void reloadConfig(){
        super.reloadConfig();
        config = super.getConfig();
    }

    /**
     * Saves config.yml and shops.yml when shutting down the server or unloading the plugin.
     * Will also close all opened {@link ShoppingInventory} and {@link SetupInventory}.
     */
    @Override
    public void onDisable(){
        try{
            closeInventories();
            savePluginConfig();
            saveShops();
        }catch(NullPointerException e){
            //catch this exception when the plugin shuts down because of missing vault-dependencies
        }
    }

    /**
     * Register command /shopz
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args){
        String name = cmd.getName().toLowerCase();

        if(name.equals("shopz"))
            return CommandShopz.run(sender, cmd, lbl, args);
        return false;
    }

    /**
     * Register all events for {@link org.bukkit.event.Listener} from package {@link de.babrs.shopz.listerners}
     */
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

    /**
     * Load vault-economy using the RegisteredServiceProvider (https://dev.bukkit.org/projects/vault)
     */
    private boolean loadEconomy(){
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        ShopzPlugin.econ = rsp.getProvider();

        return econ != null;
    }

    /**
     * Create shops.yml if it does not exist, load it from hard drive otherwise
     */
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

    /**
     * Reload localization and config from hard drive, this will revert all changes made since last file save.
     * Will be executed using the "/shopz reload" command.
     */
    public static void reloadConfigurations(){
        instance.loadLocalization();
        instance.loadConfig();
    }

    /**
     * Load localization.yml from hard drive using the loadFile()-Method.
     */
    private void loadLocalization(){
        localization = loadFile("localization.yml");
    }

    /**
     * Loads config.yml from hardd rive using the loadFile()-Method, as well as saves the prefix in the static String.
     */
    private void loadConfig(){
        ShopzPlugin.config = loadFile("config.yml");
        ShopzPlugin.prefix = (String) config.get("chat_prefix");
    }

    /**
     * Create <path> if it does not exist, load it from hard drive otherwise. Files should be saved in UTF-8 for compatibility.
     *
     * @param path: Name and ending of file to load, e.g.: "filename.yml"
     */
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

    /**
     * Saves Plugin-Config on shutdown.
     */
    private void savePluginConfig(){
        try{
            config.save(new File(getDataFolder(), "config.yml"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Saves Shops on shutdown, will also be called when shops are created to make plugin more resistant to crashes.
     */
    public static void saveShops(){
        try{
            shops.save(shopsFile);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Close {@link ShoppingInventory} of all online {@link Player} on shutdown or Plugin-unloading. Used, because leaving
     * them open might cause weird behaviour when using /reload, has no use when regularly restarting the server.
     */
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

    //Getter-Methods

    /**
     * Getter for {@link PluginDescriptionFile} (from plugin.yml)
     * @return PluginDescriptionFile as specified from {@link Bukkit}
     */
    public static PluginDescriptionFile getPluginDescription(){
        return description;
    }

    /**
     * Getter for PluginConfig (from config.yml)
     * @return FileConfiguration as specified from {@link Bukkit}, including chat-prefix and costs/stepsizes
     */
    public static FileConfiguration getPluginConfig(){
        return config;
    }

    /**
     * Getter for FileConfiguration shops (from shops.yml)
     * @return FileConfiguration, including all shops
     */
    public static FileConfiguration getShops(){
        return shops;
    }

    /**
     * Getter for FileConfiguration localization (from localization.yml) for multi-language and custom messages.
     * @return FileConfiguration, including all shops
     */
    public static FileConfiguration getLocalization(){
        return localization;
    }

    /**
     * Getter for {@link ShopzPlugin} singleton
     * @return the instance of the plugin
     */
    public static ShopzPlugin getInstance(){
        return instance;
    }

    /**
     * Getter for {@link net.milkbowl.vault.Vault} Economy-Plugin
     * @return Economy
     */
    public static Economy getEconomy(){
        return econ;
    }

    /**
     * Getter for Chat-Prefix as specified in config.yml, this will display at the beginning of pretty much every message
     * the plugin sends to users (excluding console-outputs and some error-messages)
     * @return String prefix
     */
    public static String getPrefix(){
        return prefix;
    }
}
