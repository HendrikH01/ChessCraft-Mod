package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientSetTimePacket {
	private int ticks;
	
	public ClientSetTimePacket(int ticks) {
		this.ticks = ticks;
	}

	public static void encode(ClientSetTimePacket msg, PacketBuffer buf) {
		buf.writeInt(msg.ticks);
	}

	public static ClientSetTimePacket decode(PacketBuffer buf) {
		return new ClientSetTimePacket(buf.readInt());
	}

	public static void handle(ClientSetTimePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer) sender.openContainer).tile.challengedTime = msg.ticks;
					((ChessBoardContainer) sender.openContainer).tile.challengerTime = msg.ticks;
					PacketHandler.sendToNearby(sender.world, sender, new ServerSetTimePacket(msg.ticks));

				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
