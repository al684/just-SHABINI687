package com.noshieldbugs.mixin;

import com.noshieldbugs.pingsync.Config;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** SwingSpeed: scales the swing animation duration of the local player. */
@Mixin(LivingEntity.class)
public class LivingEntitySwingMixin {
	@Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true, require = 0)
	private void noshieldbugs$swing(CallbackInfoReturnable<Integer> cir) {
		if (!Config.data.enabled || !Config.data.swingEnabled) return;
		if (!((Object) this instanceof LocalPlayer)) return;
		int d = Math.max(1, Math.round(cir.getReturnValue() / Config.data.swingSpeed));
		cir.setReturnValue(d);
	}
}
