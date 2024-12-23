/*
 * This file is part of ViaFabricPlus - https://github.com/ViaVersion/ViaFabricPlus
 * Copyright (C) 2021-2024 the original authors
 *                         - FlorianMichael/EnZaXD <florian.michael07@gmail.com>
 *                         - RK_01/RaphiMC
 * Copyright (C) 2023-2024 ViaVersion and contributors
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

package com.viaversion.viafabricplus.injection.mixin.features.ui.hud_element_changes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.base.settings.impl.VisualSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Function;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Unique
    private static final int viaFabricPlus$ARMOR_ICON_WIDTH = 8;

    @Inject(method = "playBurstSound", at = @At("HEAD"), cancellable = true)
    private void disableBubblePopSound(int bubble, PlayerEntity player, int burstBubbles, CallbackInfo ci) {
        if (VisualSettings.global().removeBubblePopSound.getValue()) {
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "renderAirBubbles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 2))
    private boolean disableEmptyBubbles(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, int width, int height) {
        return !VisualSettings.global().hideEmptyBubbleIcons.getValue();
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"))
    private boolean alwaysRenderCrosshair(Perspective instance, Operation<Boolean> original) {
        if (VisualSettings.global().alwaysRenderCrosshair.isEnabled()) {
            return true;
        } else {
            return original.call(instance);
        }
    }

    @Inject(method = {"renderMountJumpBar", "renderMountHealth"}, at = @At("HEAD"), cancellable = true)
    private void removeMountJumpBar(CallbackInfo ci) {
        if (VisualSettings.global().hideModernHUDElements.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getHeartCount", at = @At("HEAD"), cancellable = true)
    private void removeHungerBar(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (VisualSettings.global().hideModernHUDElements.isEnabled()) {
            cir.setReturnValue(1);
        }
    }

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"), require = 0)
    private int moveHealthDown(int value) {
        if (VisualSettings.global().hideModernHUDElements.isEnabled()) {
            return value + 6; // Magical offset
        } else {
            return value;
        }
    }

    @ModifyArgs(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"), require = 0)
    private static void moveArmorPositions(Args args, @Local(ordinal = 3, argsOnly = true) int x, @Local(ordinal = 6) int n) {
        if (!VisualSettings.global().hideModernHUDElements.isEnabled()) {
            return;
        }
        final MinecraftClient client = MinecraftClient.getInstance();

        final int armorWidth = 10 * viaFabricPlus$ARMOR_ICON_WIDTH;
        final int offset = n * viaFabricPlus$ARMOR_ICON_WIDTH;

        args.set(1, client.getWindow().getScaledWidth() - x - armorWidth + offset - 1);
        args.set(2, (int) args.get(2) + client.textRenderer.fontHeight + 1);
    }

    @ModifyArg(method = "renderAirBubbles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"),
            index = 2, require = 0)
    private int moveAirBubbles(int value) {
        if (VisualSettings.global().hideModernHUDElements.isEnabled()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            return client.getWindow().getScaledWidth() - value - client.textRenderer.fontHeight;
        } else {
            return value;
        }
    }

}