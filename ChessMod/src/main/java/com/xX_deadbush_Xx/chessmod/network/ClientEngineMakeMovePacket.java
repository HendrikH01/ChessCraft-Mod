package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientEngineMakeMovePacket {
	private int first;
	private int second;
	private String piece; //promotion
	
	public ClientEngineMakeMovePacket(int first, int second, String piece) {
		this.first = first;
		this.second = second;
		this.piece = piece;
	}

	public static void encode(ClientEngineMakeMovePacket msg, PacketBuffer buf) {
		buf.writeInt(msg.first);
		buf.writeInt(msg.second);
		buf.writeString(msg.piece);
	}

	public static ClientEngineMakeMovePacket decode(PacketBuffer buf) {
		return new ClientEngineMakeMovePacket(buf.readInt(), buf.readInt(), buf.readString(2));
	}

	public static void handle(ClientEngineMakeMovePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					ChessBoardContainer container = (ChessBoardContainer) sender.openContainer;
					ChessHelper.getLegalMoves(container, container.tile.challengerColor.getOpposite(), (moves) -> {
						
						if(moves.contains(Pair.of(msg.first, msg.second))) {
							ChessHelper.executeMoveRaw(container, msg.first, msg.second, msg.piece);
							container.getBoard().toPlay = container.tile.challengerColor;
							container.checkForMate(container.tile.challengerColor);
							
							PacketHandler.sendToNearby(sender.world, sender, new ServerEngineMakeMovePacket(msg.first, msg.second, msg.piece));
						}
					});
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
