package com.noshieldbugs.pingsync;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Spawns client-side particles where the entity you just hit dies. */
public class KillEffects {
	private static Entity last;
	private static int ticks;

	public static void init() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			Minecraft mc = Minecraft.getInstance();
			if (world == mc.level && entity instanceof LivingEntity) {
				last = entity;
				ticks = 60;
			}
			return InteractionResult.PASS;
		});
	}

	public static void tick(Minecraft mc) {
		if (last == null) return;
		if (!Config.data.enabled || !Config.data.killEffects || mc.level == null) { last = null; return; }
		if (last instanceof LivingEntity le && (le.isDeadOrDying() || last.isRemoved())) {
			spawn(mc, last);
			last = null;
		} else if (--ticks <= 0) {
			last = null;
		}
	}

	private static void spawn(Minecraft mc, Entity e) {
		ParticleOptions type;
		int count;
		switch (Config.data.killEffectType.toUpperCase()) {
			case "EXPLOSION" -> { type = ParticleTypes.EXPLOSION; count = 3; }
			case "FLAME" -> { type = ParticleTypes.FLAME; count = 30; }
			case "HEART" -> { type = ParticleTypes.HEART; count = 10; }
			default -> { type = ParticleTypes.TOTEM_OF_UNDYING; count = 40; }
		}
		double cy = e.getY() + e.getBbHeight() / 2.0;
		for (int i = 0; i < count; i++) {
			double vx = (mc.level.random.nextDouble() - 0.5) * 0.5;
			double vy = mc.level.random.nextDouble() * 0.4;
			double vz = (mc.level.random.nextDouble() - 0.5) * 0.5;
			mc.level.addParticle(type, e.getX(), cy, e.getZ(), vx, vy, vz);
		}
	}
}
