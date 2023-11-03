package me.mykindos.betterpvp.clans.clans.leveling.perk;

import me.mykindos.betterpvp.clans.clans.leveling.ClanPerk;
import me.mykindos.betterpvp.core.utilities.model.ItemView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DummyPerkTres implements ClanPerk {
    @Override
    public String getName() {
        return "Dummy Feature 3";
    }

    @Override
    public int getMinimumLevel() {
        return 45;
    }

    @Override
    public Component[] getDescription() {
        return new Component[] {
                Component.text("This is a dummy perk number trois!", NamedTextColor.GRAY)
        };
    }

    @Override
    public ItemStack getIcon() {
        return ItemView.builder().material(Material.LECTERN).build().toItemStack();
    }
}