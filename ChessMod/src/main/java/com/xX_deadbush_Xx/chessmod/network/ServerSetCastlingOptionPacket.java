package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerSetCastlingOptionPacket {
	private int num;
	private boolean bool;
	
	public ServerSetCastlingOptionPacket(int num, boolean bool) {
		this.num = num;
		this.bool = bool;
	}

	public static void encode(ServerSetCastlingOptionPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.num);
		buf.writeBoolean(msg.bool);
	}

	public static ServerSetCastlingOptionPacket decode(PacketBuffer buf) {
		return new ServerSetCastlingOptionPacket(buf.readInt(), buf.readBoolean());
	}

	public static void handle(ServerSetCastlingOptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity player = Minecraft.getInstance().player;
				if(player.openContainer instanceof ChessBoardContainer && player.openContainer != null) {
					((ChessBoardContainer)player.openContainer).getBoard().canCastle[msg.num] = msg.bool;
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
