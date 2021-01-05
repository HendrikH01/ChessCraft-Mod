package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerMovePiecePacket {
	private int first;
	private int second;

	public ServerMovePiecePacket(int first, int second) {
		this.first = first;
		this.second = second;
	}

	public static void encode(ServerMovePiecePacket msg, PacketBuffer buf) {
		buf.writeInt(msg.first);
		buf.writeInt(msg.second);
	}

	public static ServerMovePiecePacket decode(PacketBuffer buf) {
		return new ServerMovePiecePacket(buf.readInt(), buf.readInt());
	}

	public static void handle(ServerMovePiecePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
