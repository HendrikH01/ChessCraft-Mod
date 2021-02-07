package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientSetStrengthPacket {
	private int strength;
	
	public ClientSetStrengthPacket(int strength) {
		this.strength = strength;
	}

	public static void encode(ClientSetStrengthPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.strength);
	}

	public static ClientSetStrengthPacket decode(PacketBuffer buf) {
		return new ClientSetStrengthPacket(buf.readInt());
	}

	public static void handle(ClientSetStrengthPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
				PlayerEntity sender = context.getSender();
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer) sender.openContainer).tile.computerStrength = msg.strength;
					PacketHandler.sendToNearby(sender.world, sender, new ServerSetStrengthPacket(msg.strength));
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
