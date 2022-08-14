package me.mykindos.betterpvp.clans.champions.skills.skills.gladiator.sword;

import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.types.ChannelSkill;
import me.mykindos.betterpvp.clans.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.clans.champions.skills.types.EnergySkill;
import me.mykindos.betterpvp.clans.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilBlock;
import me.mykindos.betterpvp.core.utilities.UtilEntity;
import me.mykindos.betterpvp.core.utilities.UtilMath;
import me.mykindos.betterpvp.core.utilities.UtilVelocity;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.UUID;

@Singleton
@BPvPListener
public class BattleTaunt extends ChannelSkill implements InteractSkill, CooldownSkill, EnergySkill, Listener {

    @Inject
    public BattleTaunt(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Battle Taunt";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{"Hold Block with a sword to Channel.",
                "",
                "While channelling, any enemies within " + ChatColor.GREEN + (2 + level) + ChatColor.GRAY + " blocks",
                "are slowly pulled in towards you",
                "",
                "Energy / Second: " + ChatColor.GREEN + getEnergy(level)};
    }

    @Override
    public Role getClassType() {
        return Role.GLADIATOR;
    }

    @Override
    public SkillType getType() {
        return SkillType.SWORD;
    }


    @UpdateEvent
    public void energy() {
        Iterator<UUID> activeIterator = active.iterator();
        while (activeIterator.hasNext()) {
            UUID uuid = activeIterator.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (player.isHandRaised()) {
                    int level = getLevel(player);
                    if(level <= 0){
                        activeIterator.remove();
                    }else if (!championsManager.getEnergy().use(player, getName(), getEnergy(level) / 2, true)) {
                        activeIterator.remove();
                    } else if (!player.getInventory().getItemInMainHand().getType().name().contains("SWORD")) {
                        activeIterator.remove();
                    } else if (UtilBlock.isInLiquid(player)) {
                        activeIterator.remove();
                    } else {

                        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, Material.DIAMOND_BLOCK);

                        for (int i = 0; i <= (2 + level); i++) {
                            pull(player, player.getEyeLocation().add(player.getLocation().getDirection().multiply(i)));
                        }
                    }
                }
            } else {
                activeIterator.remove();
            }
        }


    }

    private void pull(Player player, Location location) {
        for (LivingEntity target : UtilEntity.getNearbyEnemies(player, location, 2.0)) {
            if (target instanceof Player) {

                if (UtilMath.offset(player.getLocation(), target.getLocation()) >= 2.0D) {
                    UtilVelocity.velocity(target, UtilVelocity.getTrajectory(target, player), 0.3D, false, 0.0D, 0.0D, 1.0D, true);
                }

            } else {
                UtilVelocity.velocity(target, UtilVelocity.getTrajectory(target, player), 0.3D, false, 0.0D, 0.0D, 1.0D, true);
            }
        }
    }


    @Override
    public float getEnergy(int level) {

        return energy - ((level - 1));
    }

    @Override
    public void activate(Player player, int level) {
        active.add(player.getUniqueId());
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }

    @Override
    public double getCooldown(int level) {
        return 0.5;
    }

    @Override
    public boolean showCooldownFinished(){
        return false;
    }
}
