package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerPromotionPacket {
	private int pieceid;
	private PieceColor color;
	
	public ServerPromotionPacket(int pieceid, PieceColor color) {
		this.pieceid = pieceid;
		this.color = color;
	}

	public static void encode(ServerPromotionPacket msg, PacketBuffer buf) {
		buf.writeByte(msg.pieceid);
		buf.writeBoolean(msg.color == PieceColor.WHITE);
	}

	public static ServerPromotionPacket decode(PacketBuffer buf) {
		return new ServerPromotionPacket(buf.readByte(), buf.readBoolean() ? PieceColor.WHITE : PieceColor.BLACK);
	}

	public static void handle(ServerPromotionPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity sender = Minecraft.getInstance().player;
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer) sender.openContainer).tryPromotePawn(ChessPieceType.values()[msg.pieceid], msg.color);
					
					if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
						((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateMode(((ChessBoardContainer) sender.openContainer).getMode());
					}
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
