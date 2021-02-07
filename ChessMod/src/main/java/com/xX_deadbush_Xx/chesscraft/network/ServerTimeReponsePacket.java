package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerTimeReponsePacket {

	private long time;
	private int challengedTime;
	private int challengerTime;
	
	public ServerTimeReponsePacket(long time, int challengerTime, int challengedTime) {
		this.challengerTime = challengerTime;
		this.challengedTime = challengedTime;
		this.time = time;
	}

	public static void encode(ServerTimeReponsePacket msg, PacketBuffer buf) {
		buf.writeLong(msg.time);
		buf.writeInt(msg.challengerTime);
		buf.writeInt(msg.challengedTime);
	}

	public static ServerTimeReponsePacket decode(PacketBuffer buf) {
		return new ServerTimeReponsePacket(buf.readLong(), buf.readInt(), buf.readInt());
	}

	public static void handle(ServerTimeReponsePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity sender = Minecraft.getInstance().player;
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					long estimatedLatency = (System.currentTimeMillis() - msg.time) / 2;
					int dticks = (int) (estimatedLatency / 50);
					
					((ChessBoardContainer) sender.openContainer).tile.challengedTime = msg.challengedTime + dticks;
					((ChessBoardContainer) sender.openContainer).tile.challengerTime = msg.challengerTime + dticks;
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
