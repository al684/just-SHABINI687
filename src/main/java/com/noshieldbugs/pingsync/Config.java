package com.noshieldbugs.pingsync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
	public static class Data {
		public boolean enabled = true;
		public boolean renderHitboxes = true;
		public boolean elytraUpright = true;
		/** LAST_TICK | ALL_TICKS | ONLY_OLDEST */
		public String hitboxRenderMode = "LAST_TICK";
		/** CYAN | GREEN | PURPLE */
		public String hitboxColorPreset = "CYAN";
		public int ghostFadeTicks = 5;
		public float ghostAlpha = 0.6f;
		public int maxRenderDistance = 64;
		public boolean trackPlayers = true;
		public boolean trackHostile = true;
		public boolean trackPassive = false;
		public boolean trackOther = false;
		public boolean killEffects = true;
		/** EXPLOSION | TOTEM | FLAME | HEART */
		public String killEffectType = "TOTEM";
		public boolean swingEnabled = false;
		public float swingSpeed = 1.5f;
		public boolean chatHidden = false;
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pingsync.json");
	public static Data data = new Data();

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				try (Reader r = Files.newBufferedReader(FILE)) {
					Data d = GSON.fromJson(r, Data.class);
					if (d != null) data = d;
				}
			} else save();
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void save() {
		try (Writer w = Files.newBufferedWriter(FILE)) {
			GSON.toJson(data, w);
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static float[] color() {
		return switch (data.hitboxColorPreset.toUpperCase()) {
			case "GREEN" -> new float[]{0.2f, 1f, 0.3f};
			case "PURPLE" -> new float[]{0.7f, 0.3f, 1f};
			default -> new float[]{0f, 0.9f, 1f};
		};
	}
}
