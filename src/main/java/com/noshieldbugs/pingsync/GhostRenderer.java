package com.noshieldbugs.pingsync;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws outlines of where tracked entities were in previous ticks. */
public class GhostRenderer {
	private static boolean broken = false;

	public static void render(WorldRenderContext ctx) {
		if (broken) return;
		try {
			renderInner(ctx);
		} catch (Throwable t) {
			broken = true;
			t.printStackTrace();
		}
	}

	private static void renderInner(WorldRenderContext ctx) {
		Config.Data c = Config.data;
		if (!c.enabled || !c.renderHitboxes) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		PoseStack pose = ctx.matrices();
		VertexConsumer vc = ctx.consumers().getBuffer(RenderTypes.lines());
		Vec3 cam = mc.gameRenderer.getMainCamera().position();
		float[] col = Config.color();

		for (Deque<AABB> q : Tracker.HISTORY.values()) {
			if (q.size() < 2) continue;
			List<AABB> past = new ArrayList<>(q);
			past.remove(past.size() - 1); // drop current tick
			List<AABB> draw = switch (c.hitboxRenderMode.toUpperCase()) {
				case "ALL_TICKS" -> past;
				case "ONLY_OLDEST" -> List.of(past.get(0));
				default -> List.of(past.get(past.size() - 1));
			};
			for (int i = 0; i < draw.size(); i++) {
				float fade = (i + 1f) / draw.size();
				AABB b = draw.get(i).move(-cam.x, -cam.y, -cam.z);
				box(vc, pose.last(), b, col[0], col[1], col[2], c.ghostAlpha * fade);
			}
		}
	}

	private static void box(VertexConsumer vc, PoseStack.Pose p, AABB b, float r, float g, float bl, float a) {
		float x0 = (float) b.minX, y0 = (float) b.minY, z0 = (float) b.minZ;
		float x1 = (float) b.maxX, y1 = (float) b.maxY, z1 = (float) b.maxZ;
		// bottom
		line(vc, p, x0, y0, z0, x1, y0, z0, r, g, bl, a);
		line(vc, p, x1, y0, z0, x1, y0, z1, r, g, bl, a);
		line(vc, p, x1, y0, z1, x0, y0, z1, r, g, bl, a);
		line(vc, p, x0, y0, z1, x0, y0, z0, r, g, bl, a);
		// top
		line(vc, p, x0, y1, z0, x1, y1, z0, r, g, bl, a);
		line(vc, p, x1, y1, z0, x1, y1, z1, r, g, bl, a);
		line(vc, p, x1, y1, z1, x0, y1, z1, r, g, bl, a);
		line(vc, p, x0, y1, z1, x0, y1, z0, r, g, bl, a);
		// verticals
		line(vc, p, x0, y0, z0, x0, y1, z0, r, g, bl, a);
		line(vc, p, x1, y0, z0, x1, y1, z0, r, g, bl, a);
		line(vc, p, x1, y0, z1, x1, y1, z1, r, g, bl, a);
		line(vc, p, x0, y0, z1, x0, y1, z1, r, g, bl, a);
	}

	private static void line(VertexConsumer vc, PoseStack.Pose p,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float r, float g, float b, float a) {
		float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0f) return;
		dx /= len; dy /= len; dz /= len;
		vc.addVertex(p, x1, y1, z1).setColor(r, g, b, a).setNormal(p, dx, dy, dz).setLineWidth(2.0f);
		vc.addVertex(p, x2, y2, z2).setColor(r, g, b, a).setNormal(p, dx, dy, dz).setLineWidth(2.0f);
	}
}
