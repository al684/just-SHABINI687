package com.noshieldbugs.pingsync;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class NoShieldBugsClient implements ClientModInitializer {
	public static final String MOD_ID = "noshieldbugs";
	private static KeyMapping toggleKey;
	private static KeyMapping menuKey;
	private static KeyMapping chatKey;

	@Override
	public void onInitializeClient() {
		Config.load();
		KillEffects.init();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.noshieldbugs.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, category));
		menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.noshieldbugs.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, category));
		chatKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.noshieldbugs.chat", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, category));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (toggleKey.consumeClick()) {
				Config.data.enabled = !Config.data.enabled;
				Config.save();
				if (mc.player != null)
					mc.player.displayClientMessage(Component.literal("x_x: " + (Config.data.enabled ? "ON" : "OFF")), true);
			}
			while (menuKey.consumeClick()) {
				if (mc.screen == null) mc.setScreen(new XxScreen());
			}
			while (chatKey.consumeClick()) {
				try { ChatToggle.toggle(); } catch (Throwable ignored) { }
			}
			try { Tracker.onTick(mc); } catch (Throwable t) { Tracker.HISTORY.clear(); }
			try { KillEffects.tick(mc); } catch (Throwable ignored) { }
		});

		WorldRenderEvents.AFTER_ENTITIES.register(GhostRenderer::render);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
			dispatcher.register(ClientCommandManager.literal("pingsync")
				.then(ClientCommandManager.literal("toggle").executes(ctx -> {
					Config.data.enabled = !Config.data.enabled; Config.save();
					ctx.getSource().sendFeedback(Component.literal("x_x: " + (Config.data.enabled ? "ON" : "OFF")));
					return 1;
				}))
				.then(ClientCommandManager.literal("hitboxes").executes(ctx -> {
					Config.data.renderHitboxes = !Config.data.renderHitboxes; Config.save();
					ctx.getSource().sendFeedback(Component.literal("Hitboxes: " + Config.data.renderHitboxes));
					return 1;
				}))
				.then(ClientCommandManager.literal("alpha")
					.then(ClientCommandManager.argument("v", FloatArgumentType.floatArg(0.05f, 1f)).executes(ctx -> {
						Config.data.ghostAlpha = FloatArgumentType.getFloat(ctx, "v"); Config.save(); return 1;
					})))
				.then(ClientCommandManager.literal("distance")
					.then(ClientCommandManager.argument("v", IntegerArgumentType.integer(8, 256)).executes(ctx -> {
						Config.data.maxRenderDistance = IntegerArgumentType.getInteger(ctx, "v"); Config.save(); return 1;
					})))
				.then(ClientCommandManager.literal("ticks")
					.then(ClientCommandManager.argument("v", IntegerArgumentType.integer(1, 20)).executes(ctx -> {
						Config.data.ghostFadeTicks = IntegerArgumentType.getInteger(ctx, "v"); Config.save(); return 1;
					})))
			));
	}
}
