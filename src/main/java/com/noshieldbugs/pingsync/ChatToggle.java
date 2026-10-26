package com.noshieldbugs.pingsync;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.ChatVisiblity;

/** Chat toggle: hides / shows chat through the vanilla chat visibility option. */
public class ChatToggle {
	public static void toggle() {
		Minecraft mc = Minecraft.getInstance();
		Config.data.chatHidden = !Config.data.chatHidden;
		mc.options.chatVisibility().set(Config.data.chatHidden ? ChatVisiblity.HIDDEN : ChatVisiblity.FULL);
		Config.save();
	}
}
