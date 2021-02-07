package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerSetTimePacket {
	private int ticks;
	
	public ServerSetTimePacket(int ticks) {
		this.ticks = ticks;
	}

	public static void encode(ServerSetTimePacket msg, PacketBuffer buf) {
		buf.writeInt(msg.ticks);
	}

	public static ServerSetTimePacket decode(PacketBuffer buf) {
		return new ServerSetTimePacket(buf.readInt());
	}

	public static void handle(ServerSetTimePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity sender = Minecraft.getInstance().player;
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer) sender.openContainer).tile.challengedTime = msg.ticks;
					((ChessBoardContainer) sender.openContainer).tile.challengerTime = msg.ticks;
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
