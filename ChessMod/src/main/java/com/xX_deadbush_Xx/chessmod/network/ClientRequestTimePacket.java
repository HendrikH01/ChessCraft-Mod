package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientRequestTimePacket {
	
	private long time;
	
	public ClientRequestTimePacket() {
		this.time = System.currentTimeMillis();
	}
	
	private ClientRequestTimePacket(long time) {
		this.time = time;
	}

	public static void encode(ClientRequestTimePacket msg, PacketBuffer buf) {
		buf.writeLong(msg.time);
	}

	public static ClientRequestTimePacket decode(PacketBuffer buf) {
		return new ClientRequestTimePacket(buf.readLong());
	}

	public static void handle(ClientRequestTimePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					PacketHandler.sendToNearby(sender.world, sender, new ServerTimeReponsePacket(msg.time, ((ChessBoardContainer)sender.openContainer).tile.challengerTime, ((ChessBoardContainer)sender.openContainer).tile.challengedTime));
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
