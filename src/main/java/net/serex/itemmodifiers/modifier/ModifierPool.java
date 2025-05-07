package net.serex.itemmodifiers.modifier;/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 */

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

    public void remove(Modifier modifier) {
        if (this.modifiers.remove(modifier)) {
            this.totalWeight -= modifier.weight;
        }
    }

    public List<Modifier> getModifiers() {
        return new ArrayList<Modifier>(this.modifiers);
    }

    public Modifier roll(RandomSource random) {
        if (this.totalWeight == 0 || this.modifiers.isEmpty()) {
            return null;
        }
        int roll = random.nextInt(this.totalWeight);
        int currentWeight = 0;
        for (Modifier modifier : this.modifiers) {
            currentWeight += modifier.weight;
            if (roll < currentWeight) {
                return modifier;
            }
        }
        return this.modifiers.get(this.modifiers.size() - 1);
    }

    public int getTotalWeight() {
        return this.totalWeight;
    }
}
