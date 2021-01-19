package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientSetSidePacket {
	private PieceColor side;
	
	public ClientSetSidePacket(PieceColor side) {
		this.side = side;
	}

	public static void encode(ClientSetSidePacket msg, PacketBuffer buf) {
		buf.writeBoolean(msg.side == PieceColor.WHITE);
	}

	public static ClientSetSidePacket decode(PacketBuffer buf) {
		return new ClientSetSidePacket(buf.readBoolean() ? PieceColor.WHITE : PieceColor.BLACK);
	}

	public static void handle(ClientSetSidePacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer)sender.openContainer).tile.sides.put(sender.getUniqueID(), msg.side);
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
