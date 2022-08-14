package me.mykindos.betterpvp.clans.champions.skills.skills.gladiator.passives;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.Skill;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.types.PassiveSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@Singleton
@BPvPListener
public class Overwhelm extends Skill implements PassiveSkill {

    @Inject
    public Overwhelm(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Overwhelm";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "You deal 1 bonus damage for every",
                "2 more health you have than your",
                "target. You can deal a maximum of",
                ChatColor.GREEN + String.format("%.1f", (0.0 + (level * 0.5))) + ChatColor.GRAY + " bonus damage."
        };
    }

    @Override
    public Role getClassType() {
        return Role.GLADIATOR;
    }

    @Override
    public SkillType getType() {
        return SkillType.PASSIVE_B;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(CustomDamageEvent event) {
        if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof Player player)) return;
        int level = getLevel(player);
        if (level > 0) {
            LivingEntity ent = event.getDamagee();
            double difference = (player.getHealth() - ent.getHealth()) / 2;
            if (difference > 0) {
                difference = Math.min(difference, (level * 0.5));
                event.setDamage(event.getDamage() + difference);
            }
        }
    }


}
