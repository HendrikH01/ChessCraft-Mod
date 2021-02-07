package com.xX_deadbush_Xx.chesscraft.network;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerChessBoardUpdatePacket {
	private byte event;
	private UUID sender;
	
	public ServerChessBoardUpdatePacket(@Nullable UUID sender, byte event) {
		this.event = event;
		this.sender = sender;
	}

	public static void encode(ServerChessBoardUpdatePacket msg, PacketBuffer buf) {
		buf.writeBoolean(msg.sender == null);
		if(msg.sender != null) buf.writeUniqueId(msg.sender);
		buf.writeByte(msg.event);
	}

	public static ServerChessBoardUpdatePacket decode(PacketBuffer buf) {
		return new ServerChessBoardUpdatePacket(buf.readBoolean() ? null :  buf.readUniqueId(), buf.readByte());
	}

	public static void handle(ServerChessBoardUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity player = Minecraft.getInstance().player;
				if (player.openContainer instanceof ChessBoardContainer && player.openContainer != null) {
					((ChessBoardContainer) player.openContainer).handleUpdatePacket(msg.sender, msg.event);
				}
			}
		});

		context.setPacketHandled(true);
	}
}
