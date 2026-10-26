package com.noshieldbugs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noshieldbugs.pingsync.Config;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Elytra Upright: skip the forward tilt applied while gliding (when the mod is on). */
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Redirect(
		method = "setupRotations",
		at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
			ordinal = 0),
		require = 0)
	private void noshieldbugs$skipForwardTilt(PoseStack poseStack, Quaternionf rotation) {
		if (!(Config.data.enabled && Config.data.elytraUpright)) {
			poseStack.mulPose(rotation);
		}
	}
}
