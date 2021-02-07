package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.ChessMod;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

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
					((ChessBoardContainer) sender.openContainer).handleUpdatePacket(sender.getUniqueID(), msg.event);
					
					if(msg.event != 3) //ServerPlayerDefeatedPackets will be sent instead
						PacketHandler.sendToNearby(sender.world, sender, new ServerChessBoardUpdatePacket(sender.getUniqueID(), msg.event));
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
