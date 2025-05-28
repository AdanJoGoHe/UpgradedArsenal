package net.serex.upgradedarsenal.modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class ModifierPool {
    private final List<ModifierRegistry> modifiers = new ArrayList<ModifierRegistry>();
    private int totalWeight = 0;

    public void add(ModifierRegistry modifier) {
        this.modifiers.add(modifier);
        this.totalWeight += modifier.weight;
    }

    public void clear() {
        modifiers.clear();
    }

    public void remove(ModifierRegistry modifier) {
        if (this.modifiers.remove(modifier)) {
            this.totalWeight -= modifier.weight;
        }
    }

    public List<ModifierRegistry> getModifiers() {
        return new ArrayList<ModifierRegistry>(this.modifiers);
    }

    public ModifierRegistry roll(RandomSource random) {
        if (totalWeight <= 0 || modifiers.isEmpty()) {
            System.err.println("[ModifierPool] Pool vacío o sin peso válido. Devolviendo 'unchanged'.");
            return Modifiers.getModifier(new ResourceLocation("upgradedarsenal", "unchanged"));
        }

        int roll = random.nextInt(totalWeight);
        for (ModifierRegistry modifier : modifiers) {
            roll -= modifier.weight;
            if (roll < 0) return modifier;
        }

        // Fallback robusto
        System.err.println("[ModifierPool] Edge case: ningún modificador elegido tras roll. Usando 'unchanged'.");
        return Modifiers.getModifier(new ResourceLocation("upgradedarsenal", "unchanged"));
    }
}
