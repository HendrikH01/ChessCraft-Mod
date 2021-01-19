package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientPromotionPacket {
	private int pieceid;
	private PieceColor color;
	
	public ClientPromotionPacket(int pieceid, PieceColor color) {
		this.pieceid = pieceid;
		this.color = color;
	}

	public static void encode(ClientPromotionPacket msg, PacketBuffer buf) {
		buf.writeByte(msg.pieceid);
		buf.writeBoolean(msg.color == PieceColor.WHITE);
	}

	public static ClientPromotionPacket decode(PacketBuffer buf) {
		return new ClientPromotionPacket(buf.readByte(), buf.readBoolean() ? PieceColor.WHITE : PieceColor.BLACK);
	}

	public static void handle(ClientPromotionPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					PacketHandler.sendToNearby(sender.world, sender, new ServerPromotionPacket(msg.pieceid, msg.color));
					((ChessBoardContainer) sender.openContainer).tryPromotePawn(ChessPieceType.values()[msg.pieceid], msg.color);			
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
