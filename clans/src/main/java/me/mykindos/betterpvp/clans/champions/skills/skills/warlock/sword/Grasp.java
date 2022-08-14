package me.mykindos.betterpvp.clans.champions.skills.skills.warlock.sword;

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
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.framework.customtypes.CustomArmourStand;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

@Singleton
@BPvPListener
public class Grasp extends Skill implements InteractSkill, CooldownSkill, Listener {

    private final WeakHashMap<Player, ArrayList<LivingEntity>> cooldownJump = new WeakHashMap<>();
    private final HashMap<ArmorStand, Long> stands = new HashMap<>();

    @Inject
    public Grasp(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Grasp";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with a sword to activate.",
                "",
                "Create a wall of skulls that closes in on you from a distance",
                "and drags all enemies with it.",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level),
                "Max range: " + ChatColor.GREEN + (10 + ((level * 10) / 2)),
                "Damage: " + ChatColor.GREEN + (1 + (level - 1))

        };
    }

    @Override
    public Role getClassType() {
        return Role.WARLOCK;
    }


    private void createArmourStand(Player player, Location loc, int level) {
        CustomArmourStand as = new CustomArmourStand(((CraftWorld) loc.getWorld()).getHandle());
        ArmorStand test = (ArmorStand) as.spawn(loc);
        test.setVisible(false);
        // ArmorStand test = (ArmorStand) p.getWorld().spawnEntity(tempLoc, EntityType.ARMOR_STAND);
        test.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
        test.setGravity(false);

        test.setSmall(true);
        test.setHeadPose(new EulerAngle(UtilMath.randomInt(360), UtilMath.randomInt(360), UtilMath.randomInt(360)));

        stands.put(test, System.currentTimeMillis() + 200);

        for (LivingEntity target : UtilEntity.getNearbyEnemies(player, loc, 1)) {
            if (target.getLocation().distance(player.getLocation()) < 3) continue;
            Location targetLocation = player.getLocation();
            targetLocation.add(targetLocation.getDirection().normalize().multiply(2));

            if (!cooldownJump.get(player).contains(target)) {

                UtilDamage.doCustomDamage(new CustomDamageEvent(target, player, null, EntityDamageEvent.DamageCause.CUSTOM, level, false, getName()));
                cooldownJump.get(player).add(target);
                UtilVelocity.velocity(target, UtilVelocity.getTrajectory(target.getLocation(), targetLocation), 1.0, false, 0, 0.5, 1, true);
            }
        }

    }


    @UpdateEvent
    public void onUpdate() {
        Iterator<Map.Entry<ArmorStand, Long>> it = stands.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ArmorStand, Long> next = it.next();
            if (next.getValue() - System.currentTimeMillis() <= 0) {
                next.getKey().remove();
                it.remove();
            }
        }
    }



    @Override
    public SkillType getType() {
        return SkillType.SWORD;
    }

    @Override
    public void activate(Player player, int level) {
        Block block = player.getTargetBlock(null, (10 + (level * 10) / 2));
        Location startPos = player.getLocation();

        final Vector v = player.getLocation().toVector().subtract(block.getLocation().toVector()).normalize().multiply(0.2);
        v.setY(0);

        final Location loc = block.getLocation().add(v);
        cooldownJump.put(player, new ArrayList<>());

        final BukkitTask runnable = new BukkitRunnable() {

            @Override
            public void run() {

                boolean skip = false;
                if ((loc.getBlock().getType() != Material.AIR)
                        && UtilBlock.solid(loc.getBlock())) {

                    loc.add(0.0D, 1.0D, 0.0D);
                    if ((loc.getBlock().getType() != Material.AIR)
                            && UtilBlock.solid(loc.getBlock())) {
                        skip = true;
                    }

                }


                Location compare = loc.clone();
                compare.setY(startPos.getY());
                if (compare.distance(startPos) < 1) {
                    cancel();
                    return;
                }


                if ((loc.clone().add(0.0D, -1.0D, 0.0D).getBlock().getType() == Material.AIR)) {
                    loc.add(0.0D, -1.0D, 0.0D);
                }


                for (int i = 0; i < 10; i++) {

                    loc.add(v);
                    if (!skip) {
                        Location tempLoc = new Location(player.getWorld(), loc.getX() + UtilMath.randDouble(-2D, 2.0D), loc.getY() + UtilMath.randDouble(0.0D, 0.5D) - 0.50,
                                loc.getZ() + UtilMath.randDouble(-2.0D, 2.0D));

                        createArmourStand(player, tempLoc.clone(), level);
                        createArmourStand(player, tempLoc.clone().add(0, 1, 0), level);
                        createArmourStand(player, tempLoc.clone().add(0, 2, 0), level);

                        if (i % 2 == 0) {
                            player.getWorld().playSound(tempLoc, Sound.ENTITY_VEX_DEATH, 0.3f, 0.3f);
                        }
                    }
                }


            }

        }.runTaskTimer(clans, 0, 2);


        new BukkitRunnable() {

            @Override
            public void run() {
                runnable.cancel();
                cooldownJump.get(player).clear();

            }

        }.runTaskLater(clans, 40);


    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }

    @Override
    public double getCooldown(int level) {
        return cooldown - (level * 1.5);
    }


    @Override
    public boolean canUse(Player player) {
        int level = getLevel(player);
        Block block = player.getTargetBlock(null, (20 + (level * 10) / 2));
        if (block.getLocation().distance(player.getLocation()) < 3) {
            UtilMessage.message(player, getClassType().getName(), "You cannot use " + ChatColor.GREEN + getName() + ChatColor.GRAY + " this close.");
            return false;
        }

        return true;
    }
}
