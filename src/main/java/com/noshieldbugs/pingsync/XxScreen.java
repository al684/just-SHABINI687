package com.noshieldbugs.pingsync;

import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** x_x menu (Page Down). Tabs: General / PingSync / Combat. */
public class XxScreen extends Screen {
	private static int page = 0;

	public XxScreen() {
		super(Component.literal("x_x"));
	}

	private static String onOff(boolean b) { return b ? "ON" : "OFF"; }

	private void add(int col, int row, Supplier<String> label, Runnable action) {
		int x = this.width / 2 - 155 + col * 160;
		int y = 60 + row * 24;
		this.addRenderableWidget(Button.builder(Component.literal(label.get()), b -> {
			try { action.run(); } catch (Throwable ignored) { }
			Config.save();
			b.setMessage(Component.literal(label.get()));
		}).bounds(x, y, 150, 20).build());
	}

	private void tab(int index, String name) {
		int x = this.width / 2 - 155 + index * 104;
		Button b = Button.builder(Component.literal((page == index ? "> " : "") + name), btn -> {
			page = index;
			this.rebuildWidgets();
		}).bounds(x, 32, 98, 20).build();
		this.addRenderableWidget(b);
	}

	@Override
	protected void init() {
		Config.Data c = Config.data;
		tab(0, "General");
		tab(1, "PingSync");
		tab(2, "Combat");

		if (page == 0) {
			add(0, 0, () -> "Mod: " + onOff(c.enabled), () -> c.enabled = !c.enabled);
			add(1, 0, () -> "Elytra Upright: " + onOff(c.elytraUpright), () -> c.elytraUpright = !c.elytraUpright);
			add(0, 1, () -> "Chat: " + (c.chatHidden ? "HIDDEN" : "SHOWN"), () -> ChatToggle.toggle());
		} else if (page == 1) {
			add(0, 0, () -> "Hitboxes: " + onOff(c.renderHitboxes), () -> c.renderHitboxes = !c.renderHitboxes);
			add(1, 0, () -> "Mode: " + c.hitboxRenderMode, () -> c.hitboxRenderMode = switch (c.hitboxRenderMode.toUpperCase()) {
				case "LAST_TICK" -> "ALL_TICKS";
				case "ALL_TICKS" -> "ONLY_OLDEST";
				default -> "LAST_TICK";
			});
			add(0, 1, () -> "Color: " + c.hitboxColorPreset, () -> c.hitboxColorPreset = switch (c.hitboxColorPreset.toUpperCase()) {
				case "CYAN" -> "GREEN";
				case "GREEN" -> "PURPLE";
				default -> "CYAN";
			});
			add(1, 1, () -> "Alpha: " + Math.round(c.ghostAlpha * 100) + "%", () -> {
				float a = Math.round((c.ghostAlpha + 0.1f) * 10f) / 10f;
				c.ghostAlpha = a > 1.01f ? 0.1f : a;
			});
			add(0, 2, () -> "Distance: " + c.maxRenderDistance, () -> {
				int d = c.maxRenderDistance + 32;
				c.maxRenderDistance = d > 128 ? 32 : d;
			});
			add(1, 2, () -> "Ticks: " + c.ghostFadeTicks, () -> {
				int t = c.ghostFadeTicks + 1;
				c.ghostFadeTicks = t > 10 ? 1 : t;
			});
			add(0, 3, () -> "Players: " + onOff(c.trackPlayers), () -> c.trackPlayers = !c.trackPlayers);
			add(1, 3, () -> "Hostile: " + onOff(c.trackHostile), () -> c.trackHostile = !c.trackHostile);
			add(0, 4, () -> "Passive: " + onOff(c.trackPassive), () -> c.trackPassive = !c.trackPassive);
			add(1, 4, () -> "Other: " + onOff(c.trackOther), () -> c.trackOther = !c.trackOther);
		} else {
			add(0, 0, () -> "Kill Effects: " + onOff(c.killEffects), () -> c.killEffects = !c.killEffects);
			add(1, 0, () -> "Effect: " + c.killEffectType, () -> c.killEffectType = switch (c.killEffectType.toUpperCase()) {
				case "TOTEM" -> "EXPLOSION";
				case "EXPLOSION" -> "FLAME";
				case "FLAME" -> "HEART";
				default -> "TOTEM";
			});
			add(0, 1, () -> "Swing Speed: " + onOff(c.swingEnabled), () -> c.swingEnabled = !c.swingEnabled);
			add(1, 1, () -> "Speed: x" + c.swingSpeed, () -> {
				float[] opts = {0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 3.0f};
				int idx = 0;
				for (int i = 0; i < opts.length; i++) if (Math.abs(opts[i] - c.swingSpeed) < 0.01f) idx = i;
				c.swingSpeed = opts[(idx + 1) % opts.length];
			});
		}

		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
			.bounds(this.width / 2 - 75, this.height - 30, 150, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
	}
}
