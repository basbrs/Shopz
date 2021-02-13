package de.babrs.shopz.listerners;

import de.babrs.shopz.ShopzPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;

public class TabCompleteListener implements Listener{
    private static String[] completions;

    @EventHandler
    public void onTabComplete(TabCompleteEvent event){
        if(TabCompleteListener.completions == null)
            loadAliases();

        if(event.getBuffer().startsWith("/shopz ")){
            if(!event.getBuffer().substring(7).contains(" ")){
                List<String> senderCompletion = new ArrayList<>();

                for(String completion : completions){
                    if(event.getSender().hasPermission("shopz." + completion))
                        senderCompletion.add(completion);
                }
                event.setCompletions(senderCompletion);
            }else{
                event.setCompletions(Collections.EMPTY_LIST);
            }
        }
    }

    private void loadAliases(){
        PluginDescriptionFile desc = ShopzPlugin.getPluginDescription();
        Map<String, Map<String, Object>> commands = desc.getCommands();
        String usage = ((String) commands.get("shopz").get("usage")).split("\\[")[1].replace("]", "");

        TabCompleteListener.completions = usage.split(", ");
    }
}
