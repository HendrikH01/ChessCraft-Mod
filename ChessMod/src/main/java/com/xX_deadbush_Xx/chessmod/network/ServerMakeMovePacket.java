package com.xX_deadbush_Xx.chessmod.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerMakeMovePacket {
	private int first;
	private int second;
	private PieceColor playing;
	private UUID sender;
	
	public ServerMakeMovePacket(UUID sender, PieceColor playing, int first, int second) {
		this.first = first;
		this.second = second;
		this.playing = playing;
		this.sender = sender;
	}

	public static void encode(ServerMakeMovePacket msg, PacketBuffer buf) {
		buf.writeUniqueId(msg.sender);
		buf.writeBoolean(msg.playing == PieceColor.WHITE);
		buf.writeInt(msg.first);
		buf.writeInt(msg.second);
	}

	public static ServerMakeMovePacket decode(PacketBuffer buf) {
		return new ServerMakeMovePacket(buf.readUniqueId(), buf.readBoolean() ? PieceColor.WHITE : PieceColor.BLACK, buf.readInt(), buf.readInt());
	}

	public static void handle(ServerMakeMovePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				@SuppressWarnings("resource")
				PlayerEntity player = Minecraft.getInstance().player;
				
				ChessBoardContainer container = (ChessBoardContainer)player.openContainer;
				
				container.checkForMate(msg.playing.getOpposite());
				System.out.println(container.tile.waitingForComputerMove);

				if(!msg.sender.equals(player.getUniqueID())) {
					ChessHelper.executeMove(container, msg.first, msg.second, "");
					container.getBoard().toPlay = msg.playing.getOpposite();
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
