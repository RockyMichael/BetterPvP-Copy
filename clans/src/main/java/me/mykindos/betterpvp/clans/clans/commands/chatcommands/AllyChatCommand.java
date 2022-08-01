package me.mykindos.betterpvp.clans.clans.commands.chatcommands;

import com.google.inject.Inject;
import me.mykindos.betterpvp.clans.gamer.Gamer;
import me.mykindos.betterpvp.clans.gamer.GamerManager;
import me.mykindos.betterpvp.clans.gamer.properties.GamerProperty;
import me.mykindos.betterpvp.core.client.Client;
import me.mykindos.betterpvp.core.command.Command;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;

public class AllyChatCommand extends Command {

    private final GamerManager gamerManager;

    @Inject
    public AllyChatCommand(GamerManager gamerManager){
        this.gamerManager = gamerManager;

        aliases.add("ac");
    }

    @Override
    public String getName() {
        return "allychat";
    }

    @Override
    public String getDescription() {
        return "Toggle ally only chat";
    }

    @Override
    public void execute(Player player, Client client, String... args) {
        Optional<Gamer> gamerOptional = gamerManager.getObject(player.getUniqueId().toString());
        if(gamerOptional.isPresent()) {
            boolean allyChatEnabled = true;
            Gamer gamer = gamerOptional.get();
            Optional<Boolean> clanChatEnabledOptional = gamer.getProperty(GamerProperty.ALLY_CHAT.toString());
            if(clanChatEnabledOptional.isPresent()){
                allyChatEnabled = !clanChatEnabledOptional.get();
            }

            gamer.putProperty(GamerProperty.ALLY_CHAT.toString(), allyChatEnabled);
            gamer.putProperty(GamerProperty.CLAN_CHAT.toString(), false);
            UtilMessage.message(player, "Command", "Ally Chat: "
                    + (allyChatEnabled ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
        }
    }
}
