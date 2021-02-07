package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerEngineMakeMovePacket {

	private int second;
	private int first;
	private String piece;
	
	public ServerEngineMakeMovePacket(int first, int second, String piece) {
		this.first = first;
		this.second = second;
		this.piece = piece;
	}

	public static void encode(ServerEngineMakeMovePacket msg, PacketBuffer buf) {
		buf.writeInt(msg.first);
		buf.writeInt(msg.second);
		buf.writeString(msg.piece);
	}

	public static ServerEngineMakeMovePacket decode(PacketBuffer buf) {
		return new ServerEngineMakeMovePacket(buf.readInt(), buf.readInt(), buf.readString());
	}

	public static void handle(ServerEngineMakeMovePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity sender = Minecraft.getInstance().player;
 				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
 					ChessBoardContainer container = (ChessBoardContainer)sender.openContainer;
 					ChessHelper.executeMove(container, msg.first, msg.second, msg.piece);
					container.getBoard().toPlay = container.tile.challengerColor;

					container.checkForMate(container.tile.challengerColor);
 				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
