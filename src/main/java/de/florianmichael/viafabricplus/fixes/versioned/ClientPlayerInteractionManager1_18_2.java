/*
 * This file is part of ViaFabricPlus - https://github.com/FlorianMichael/ViaFabricPlus
 * Copyright (C) 2021-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and RK_01/RaphiMC
 * Copyright (C) 2023-2024 contributors
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

package de.florianmichael.viafabricplus.fixes.versioned;

import de.florianmichael.viafabricplus.ViaFabricPlus;
import de.florianmichael.viafabricplus.protocolhack.ProtocolHack;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.raphimc.vialoader.util.VersionEnum;
import org.apache.commons.lang3.tuple.Pair;

public class ClientPlayerInteractionManager1_18_2 {

    private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, PlayerActionC2SPacket.Action>, Pair<Vec3d, Vec2f>> unAckedActions = new Object2ObjectLinkedOpenHashMap<>();

    public void trackPlayerAction(final PlayerActionC2SPacket.Action action, final BlockPos blockPos) {
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;

        final Vec2f rotation;
        if (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_16_1)) {
            rotation = null;
        } else {
            rotation = new Vec2f(player.getYaw(), player.getPitch());
        }
        unAckedActions.put(Pair.of(blockPos, action), Pair.of(player.getPos(), rotation));
    }

    public void handleBlockBreakAck(final BlockPos blockPos, final BlockState expectedState, final PlayerActionC2SPacket.Action action, final boolean allGood) {
        final var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        final var world = MinecraftClient.getInstance().getNetworkHandler().getWorld();

        final var oldPlayerState = unAckedActions.remove(Pair.of(blockPos, action));
        final var actualState = world.getBlockState(blockPos);

        if ((oldPlayerState == null || !allGood || action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && actualState != expectedState) && (actualState != expectedState || ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_15_2))) {
            world.setBlockState(blockPos, expectedState, Block.NOTIFY_ALL | Block.FORCE_STATE);
            if (oldPlayerState != null && (ProtocolHack.getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_16_1) || (world == player.getWorld() && player.collidesWithStateAtPos(blockPos, expectedState)))) {
                final Vec3d oldPlayerPosition = oldPlayerState.getKey();
                if (oldPlayerState.getValue() != null) {
                    player.updatePositionAndAngles(oldPlayerPosition.x, oldPlayerPosition.y, oldPlayerPosition.z, oldPlayerState.getValue().x, oldPlayerState.getValue().y);
                } else {
                    player.updatePosition(oldPlayerPosition.x, oldPlayerPosition.y, oldPlayerPosition.z);
                }
            }
        }

        while (unAckedActions.size() >= 50) {
            ViaFabricPlus.global().getLogger().warn("Too many unacked block actions, dropping {}", unAckedActions.firstKey());
            unAckedActions.removeFirst();
        }
    }

}
