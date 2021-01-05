package com.xX_deadbush_Xx.chessmod.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer.Mode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientSetChessBoardTabPacket {
	private int tab;
	
	public ClientSetChessBoardTabPacket(int tab) {
		this.tab = tab;
	}

	public static void encode(ClientSetChessBoardTabPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.tab);
	}

	public static ClientSetChessBoardTabPacket decode(PacketBuffer buf) {
		return new ClientSetChessBoardTabPacket(buf.readInt());
	}

	public static void handle(ClientSetChessBoardTabPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer)sender.openContainer).setMode(Mode.values()[msg.tab]);
					PacketHandler.sendToNearby(sender.world, sender, new ServerSetChessBoardTabPacket(msg.tab));
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
