package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chesscraft.ChessMod;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessHelper;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientMakeMovePacket {
	private int first;
	private int second;
	private PieceColor playing;
	
	public ClientMakeMovePacket(PieceColor playing, int first, int second) {
		this.first = first;
		this.second = second;
		this.playing = playing;
	}

	public static void encode(ClientMakeMovePacket msg, PacketBuffer buf) {
		buf.writeBoolean(msg.playing == PieceColor.WHITE);
		buf.writeInt(msg.first);
		buf.writeInt(msg.second);
	}

	public static ClientMakeMovePacket decode(PacketBuffer buf) {
		return new ClientMakeMovePacket(buf.readBoolean() ? PieceColor.WHITE : PieceColor.BLACK, buf.readInt(), buf.readInt());
	}

	public static void handle(ClientMakeMovePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {
			
			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					ChessBoardContainer container = (ChessBoardContainer) sender.openContainer;
					
					if(msg.playing != container.getBoard().toPlay) {
						return;
					}
					
					ChessHelper.getLegalMoves(container, msg.playing, (moves) -> {
						
						if(moves.contains(Pair.of(msg.first, msg.second))) {
							ChessHelper.executeMove(container, msg.first, msg.second, "");

							container.getBoard().toPlay = msg.playing.getOpposite();
							container.checkForMate(msg.playing.getOpposite());
							
							PacketHandler.sendToNearby(sender.world, sender, new ServerChessBoardUpdatePacket(sender.getUniqueID(), (byte)11));
							PacketHandler.sendToNearby(sender.world, sender, new ServerMakeMovePacket(sender.getUniqueID(), msg.playing, msg.first, msg.second));
						}
					});
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
