package de.babrs.shopz.commands;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHelp{
    private static String usage = null;
    private static String[] commands = null;
    private static final List<String> arguments = Arrays.asList("help", "setcost", "setstep");

    public static boolean run(CommandSender sender, String arg){
        createStatics();

        FileConfiguration localization = ShopzPlugin.getLocalization();
        String message = sender instanceof Player ? ShopzPlugin.getPrefix() : "";

        if(!sender.hasPermission("shopz." + arg))
            sender.sendMessage(localization.getString("no_permission"));
        else{
            String s = localization.getString("help_" + arg);
            if(s != null)
                sender.sendMessage(message + s);
            else{
                sender.sendMessage(message + localization.getString("no_such_command").replace("@arg", arg));
            }
        }

        return true;
    }

    public static boolean run(CommandSender sender){
        createStatics();

        List<String> permitted = new ArrayList<>();
        for(String cmd : commands)
            if(sender.hasPermission("shopz." + cmd)) permitted.add(cmd);

        FileConfiguration localization = ShopzPlugin.getLocalization();
        String message = sender instanceof Player ? ShopzPlugin.getPrefix() : "";
        if(permitted.isEmpty())
            sender.sendMessage(localization.getString("no_permission"));
        else{
            sender.sendMessage(message + localization.getString("help_intro"));
            for(String perm : permitted){
                String argument = CommandHelp.arguments.contains(perm) ? " <arg>" : "";
                sender.sendMessage("§6/shops " + perm + argument + ": §7" + localization.getString("help_" + perm));
            }
        }
        return true;
    }

    private static void createStatics(){
        if(CommandHelp.usage == null){
            CommandHelp.usage = (String) ShopzPlugin.getPluginDescription().getCommands().get("shopz").get("usage");
            CommandHelp.commands = usage.split("\\[")[1].replace("]", "").split(", ");
        }
    }
}
