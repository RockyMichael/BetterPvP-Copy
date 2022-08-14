package me.mykindos.betterpvp.clans.champions.skills.skills.gladiator.axe;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.Skill;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.clans.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.effects.EffectType;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

@Singleton
public class ThreateningShout extends Skill implements InteractSkill, CooldownSkill {

    private int radius;
    private double duration;

    @Inject
    public ThreateningShout(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Threatening Shout";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with a axe to activate.",
                "",
                "Release a roar, which frightens all enemies",
                "within " + ChatColor.GREEN + (radius + level) + ChatColor.GRAY + " blocks",
                "and grants them Vulnerability for " + ChatColor.GREEN + (duration + level),
                "seconds.",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)
        };
    }

    @Override
    public Role getClassType() {
        return Role.GLADIATOR;
    }

    @Override
    public SkillType getType() {

        return SkillType.AXE;
    }

    @Override
    public double getCooldown(int level) {

        return cooldown - ((level - 1));
    }


    @Override
    public void activate(Player player, int level) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0F, 2.0F);
        for (Player target : UtilPlayer.getNearbyEnemies(player, player.getLocation(), radius + level)) {
            championsManager.getEffects().addEffect(target, EffectType.VULNERABILITY, (long) ((duration + level) * 1000L));
        }

    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }

    @Override
    public void loadSkillConfig() {
        radius = getConfig("radius", 4, Integer.class);
        duration = getConfig("duration", 3.0, Double.class);
    }
}
