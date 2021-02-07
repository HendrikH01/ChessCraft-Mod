package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerChessDrawPacket {

	private DrawReason reason;
	
	public ServerChessDrawPacket(DrawReason reason) {
		this.reason = reason;
	}

	public static void encode(ServerChessDrawPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.reason.ordinal());
	}

	public static ServerChessDrawPacket decode(PacketBuffer buf) {
		return new ServerChessDrawPacket(DrawReason.values()[buf.readInt()]);
	}

	public static void handle(ServerChessDrawPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity player = Minecraft.getInstance().player;
				if(player.openContainer instanceof ChessBoardContainer && player.openContainer != null) {
					((ChessBoardContainer)player.openContainer).tile.endGameInDraw(msg.reason);
					((ChessBoardContainer)player.openContainer).tile.markDirty();
				}
			}
		});
		
		context.setPacketHandled(true);
	}
	
	public static enum DrawReason {
		STALEMATE("Stalemate"),
		INSUFFICIENT("Insufficient material"),
		REPETITION("Draw by repetition"),
		MOVE50("50 moves without capture"),
		ACCEPTED("A draw was accepted");

		private String msg;

		DrawReason(String msg) {
			this.msg = msg;
		}

		public String message() {
			return msg;
		}
	}
}
