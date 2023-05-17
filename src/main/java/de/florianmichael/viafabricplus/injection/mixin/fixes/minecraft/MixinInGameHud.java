/*
 * This file is part of ViaFabricPlus - https://github.com/FlorianMichael/ViaFabricPlus
 * Copyright (C) 2021-2023 FlorianMichael/EnZaXD and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.florianmichael.viafabricplus.injection.mixin.fixes.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.florianmichael.viafabricplus.base.settings.groups.VisualSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Shadow
    private int scaledWidth;

    @Shadow private int scaledHeight;

    // Removing newer elements

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void removeExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) ci.cancel();
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"), cancellable = true)
    public void removeMountJumpBar(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    public void removeMountHealth(DrawContext context, CallbackInfo ci) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) ci.cancel();
    }

    @Inject(method = "getHeartCount", at = @At("HEAD"), cancellable = true)
    public void removeHungerBar(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) {
            cir.setReturnValue(1);
        }
    }

    // Moving down all remaining elements

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I", opcode = Opcodes.GETFIELD))
    private int moveHealthDown(int originalValue) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) return originalValue + 6;
        return originalValue;
    }

    @ModifyArg(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 0)), index = 1)
    private int moveArmor(int old) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) return scaledWidth - old - 9;
        return old;
    }

    @ModifyArg(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 0)), index = 2)
    private int moveArmorDown(int old) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) return scaledWidth - 39 + 6;
        return old;
    }

    @ModifyArg(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 2),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V")), index = 1)
    private int moveAir(int old) {
        if (VisualSettings.INSTANCE.removeNewerHudElements.getValue()) return scaledWidth - old - 9;
        return old;
    }
}
