package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientChessBoardUpdatePacket {
	private byte event;
	
	public ClientChessBoardUpdatePacket(byte event) {
		this.event = event;
	}

	public static void encode(ClientChessBoardUpdatePacket msg, PacketBuffer buf) {
		buf.writeByte(msg.event);
	}

	public static ClientChessBoardUpdatePacket decode(PacketBuffer buf) {
		return new ClientChessBoardUpdatePacket(buf.readByte());
	}

	public static void handle(ClientChessBoardUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer) sender.openContainer).handleUpdatePacket(msg.event);
					PacketHandler.sendToNearby(sender.world, sender, new ServerChessBoardUpdatePacket(msg.event));
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
