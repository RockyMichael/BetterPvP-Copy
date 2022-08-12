package me.mykindos.betterpvp.clans.champions.skills.skills.paladin.sword;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Data;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillWeapons;
import me.mykindos.betterpvp.clans.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.clans.champions.skills.types.PrepareSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilEntity;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import me.mykindos.betterpvp.core.utilities.UtilTime;
import me.mykindos.betterpvp.core.utilities.events.EntityProperty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Singleton
@BPvPListener
public class Pestilence extends PrepareSkill implements CooldownSkill {

    private final HashMap<UUID, PestilenceData> pestilenceData = new HashMap<>();

    private double infectionDuration;

    @Inject
    public Pestilence(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Pestilence";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with a sword to activate.",
                "",
                "Your next hit will apply Pestilence to the target.",
                "Pestilence poisons the target, and spreads to",
                "nearby enemies. While enemies are infected,",
                "they deal 20% reduced damage",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)
        };
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }


    @UpdateEvent(delay = 500)
    public void spread() {
        pestilenceData.entrySet().removeIf(entry -> Bukkit.getPlayer(entry.getKey()) == null);

        pestilenceData.forEach((key, value) -> {
            Player player = Bukkit.getPlayer(key);
            if (player == null) return;
            for (LivingEntity entity : value.getCurrentlyInfected().keySet()) {
                for (var data : UtilEntity.getNearbyEntities(player, entity.getLocation(), 5.0, EntityProperty.ENEMY)) {
                    LivingEntity target = data.get();
                    if (value.getCurrentlyInfected().containsKey(target)) continue;
                    if (value.getOldInfected().containsKey(target)) continue;

                    value.addInfection(target, (long) infectionDuration * 1000);
                }
            }
        });

    }

    @UpdateEvent(delay = 500)
    public void updatePestilence() {
        pestilenceData.forEach((key, value) -> {
            value.processInfections();
        });
    }

    @UpdateEvent(delay = 1000)
    public void displayPestilence() {
        pestilenceData.forEach((key, value) -> {
            value.currentlyInfected.keySet().forEach(infected -> {
                for (int q = 0; q <= 10; q++) {
                    final float x = (float) (1 * Math.cos(q));
                    final float z = (float) (1 * Math.sin(q));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(clans,
                            () -> Particle.VILLAGER_HAPPY.builder()
                                    .location(infected.getLocation().add(x, 1, z))
                                    .receivers(30)
                                    .extra(0)
                                    .spawn(),
                            q * 5L);

                }
            });
        });

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onApplyInfection(CustomDamageEvent event) {
        if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!UtilPlayer.isHoldingItem(damager, SkillWeapons.SWORDS)) return;
        if (!active.contains(damager.getUniqueId())) return;

        int level = getLevel(damager);
        if (level > 0) {
            PestilenceData data = new PestilenceData();
            data.addInfection(event.getDamagee(), (long) infectionDuration * 1000);
            pestilenceData.put(damager.getUniqueId(), data);
            active.remove(damager.getUniqueId());
        }

    }

    @EventHandler
    public void onDamageReduction(CustomDamageEvent event) {
        if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!event.getDamager().hasPotionEffect(PotionEffectType.POISON)) return;

        if (isInfected(event.getDamager())) {
            event.setDamage(event.getDamage() * 0.80);
        }

    }

    public boolean isInfected(LivingEntity entity) {
        return pestilenceData.values().stream().anyMatch(value -> value.currentlyInfected.containsKey(entity));
    }

    @Override
    public void activate(Player player, int level) {
        active.add(player.getUniqueId());
    }

    @Override
    public Role getClassType() {
        return Role.PALADIN;
    }

    @Override
    public SkillType getType() {

        return SkillType.SWORD;
    }

    @Override
    public double getCooldown(int level) {

        return cooldown - ((level - 1) * 3);
    }

    @Override
    public void loadSkillConfig() {
        infectionDuration = getConfig("duration", 5.0, Double.class);
    }

    @Data
    private static class PestilenceData {

        private final WeakHashMap<LivingEntity, DamageData> oldInfected = new WeakHashMap<>();
        private final WeakHashMap<LivingEntity, DamageData> currentlyInfected = new WeakHashMap<>();

        public void addInfection(LivingEntity entity, long length) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) ((length / 1000) * 20), 0));
            currentlyInfected.put(entity, new DamageData(length));
        }

        public void processInfections() {
            currentlyInfected.forEach((key, value) -> {
                if (UtilTime.elapsed(value.getStartTime(), value.getLength())) {
                    oldInfected.put(key, value);
                }
            });

            currentlyInfected.entrySet().removeIf(entry -> oldInfected.containsKey(entry.getKey()));
            if (currentlyInfected.isEmpty()) {
                oldInfected.clear();
            }
        }

        @Data
        private static class DamageData {

            private final long startTime;
            private final long length;

            public DamageData(long length) {
                this.startTime = System.currentTimeMillis();
                this.length = length;
            }

        }

    }
}
