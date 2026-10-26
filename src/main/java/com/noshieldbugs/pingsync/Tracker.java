package com.noshieldbugs.pingsync;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class Tracker {
	/** entity id -> recent bounding boxes (oldest first) */
	public static final Map<Integer, Deque<AABB>> HISTORY = new HashMap<>();

	public static void onTick(Minecraft mc) {
		if (mc.level == null || mc.player == null || !Config.data.enabled) {
			HISTORY.clear();
			return;
		}
		int max = Math.max(1, Config.data.ghostFadeTicks);
		double maxDistSq = Math.pow(Config.data.maxRenderDistance, 2);
		Set<Integer> seen = new HashSet<>();
		for (Entity e : mc.level.entitiesForRendering()) {
			if (e == mc.player || !shouldTrack(e)) continue;
			if (e.distanceToSqr(mc.player) > maxDistSq) continue;
			seen.add(e.getId());
			Deque<AABB> q = HISTORY.computeIfAbsent(e.getId(), k -> new ArrayDeque<>());
			q.addLast(e.getBoundingBox());
			while (q.size() > max + 1) q.pollFirst();
		}
		HISTORY.keySet().retainAll(seen);
	}

	private static boolean shouldTrack(Entity e) {
		if (e instanceof Player) return Config.data.trackPlayers;
		if (e instanceof Enemy) return Config.data.trackHostile;
		if (e instanceof Animal) return Config.data.trackPassive;
		return Config.data.trackOther;
	}
}
