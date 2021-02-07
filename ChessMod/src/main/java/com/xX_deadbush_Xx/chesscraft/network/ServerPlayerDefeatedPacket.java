package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerPlayerDefeatedPacket {

	private LoseReason reason;
	private boolean challengerLose;
	
	public ServerPlayerDefeatedPacket(boolean challengerLose, LoseReason reason) {
		this.reason = reason;
		this.challengerLose = challengerLose;
	}

	public static void encode(ServerPlayerDefeatedPacket msg, PacketBuffer buf) {
		buf.writeBoolean(msg.challengerLose);
		buf.writeInt(msg.reason.ordinal());
	}

	public static ServerPlayerDefeatedPacket decode(PacketBuffer buf) {
		return new ServerPlayerDefeatedPacket(buf.readBoolean(), LoseReason.values()[buf.readInt()]);
	}

	public static void handle(ServerPlayerDefeatedPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity player = Minecraft.getInstance().player;
				if(player.openContainer instanceof ChessBoardContainer && player.openContainer != null) {
					((ChessBoardContainer)player.openContainer).tile.endGameInWin(!msg.challengerLose, msg.reason);
					((ChessBoardContainer)player.openContainer).tile.markDirty();
				}
			}
		});
		
		context.setPacketHandled(true);
	}
	
	public static enum LoseReason {
		RESIGN("Opponent resigned", "You resigned"),
		CHECKMATE("By checkmate", "Checkmate!"),
		TIMEOUT("Opponent Flagged", "You ran out of time");

		private String winner;
		private String loser;

		LoseReason(String winner, String loser) {
			this.winner = winner;
			this.loser = loser;
		}

		public String loserMessage() {
			return loser;
		}

		public String winnerMessage() {
			return winner;
		}
	}
}
