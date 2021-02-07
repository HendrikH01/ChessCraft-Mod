package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientSetCastlingOptionPacket {
	private int num;
	private boolean bool;
	
	public ClientSetCastlingOptionPacket(int num, boolean bool) {
		this.num = num;
		this.bool = bool;
	}

	public static void encode(ClientSetCastlingOptionPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.num);
		buf.writeBoolean(msg.bool);
	}

	public static ClientSetCastlingOptionPacket decode(PacketBuffer buf) {
		return new ClientSetCastlingOptionPacket(buf.readInt(), buf.readBoolean());
	}

	public static void handle(ClientSetCastlingOptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer)sender.openContainer).getBoard().canCastle[msg.num] = msg.bool;
					PacketHandler.sendToNearby(sender.world, sender, new ServerSetCastlingOptionPacket(msg.num, msg.bool));
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
