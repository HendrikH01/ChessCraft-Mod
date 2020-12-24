package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerChessBoardUpdatePacket {
	private byte event;

	public ServerChessBoardUpdatePacket(byte event) {
		this.event = event;
	}

	public static void encode(ServerChessBoardUpdatePacket msg, PacketBuffer buf) {
		buf.writeByte(msg.event);
	}

	public static ServerChessBoardUpdatePacket decode(PacketBuffer buf) {
		return new ServerChessBoardUpdatePacket(buf.readByte());
	}

	public static void handle(ServerChessBoardUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				@SuppressWarnings("resource")
				PlayerEntity player = Minecraft.getInstance().player;
				if (player.openContainer instanceof ChessBoardContainer && player.openContainer != null) {
					((ChessBoardContainer) player.openContainer).handleUpdatePacket(msg.event);
				}
			}
		});

		context.setPacketHandled(true);
	}
}
