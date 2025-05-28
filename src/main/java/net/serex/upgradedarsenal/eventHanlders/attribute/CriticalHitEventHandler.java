package net.serex.upgradedarsenal.eventHanlders.attribute;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.ArsenalAttributes;
import net.serex.upgradedarsenal.util.EventUtil;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class CriticalHitEventHandler extends AttributeEventHandler {
    private static final Random RANDOM = new Random();

    @Override
    public Attribute getAttribute() {
        return ArsenalAttributes.CRITICAL_CHANCE.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (EventUtil.rollForCriticalHit(player)) {
                float baseDamage = event.getAmount();
                float criticalDamage = baseDamage * EventUtil.getCriticalDamageMultiplier(player);
                event.setAmount(criticalDamage);
                LivingEntity target = event.getEntity();
                Level level = target.level();
                if (level instanceof ServerLevel serverLevel) {
                    double x = target.getX();
                    double y = target.getY() + target.getBbHeight() * 0.5;
                    double z = target.getZ();
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
                 player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                              SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.0F);

            }
        }
    }
}
