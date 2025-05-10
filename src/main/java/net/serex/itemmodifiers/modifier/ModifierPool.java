package net.serex.itemmodifiers.modifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

public class ModifierPool {
    private final List<Modifier> modifiers = new ArrayList<Modifier>();
    private int totalWeight = 0;

    public void add(Modifier modifier) {
        this.modifiers.add(modifier);
        this.totalWeight += modifier.weight;
    }

    public void clear() {
        modifiers.clear();
    }

    public void remove(Modifier modifier) {
        if (this.modifiers.remove(modifier)) {
            this.totalWeight -= modifier.weight;
        }
    }

    public List<Modifier> getModifiers() {
        return new ArrayList<Modifier>(this.modifiers);
    }

    public Modifier roll(RandomSource random) {
        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        for (Modifier modifier : modifiers) {
            roll -= modifier.weight;
            if (roll < 0) return modifier;
        }

        return null; //Edge case
    }

    public int getTotalWeight() {
        return this.totalWeight;
    }
}
