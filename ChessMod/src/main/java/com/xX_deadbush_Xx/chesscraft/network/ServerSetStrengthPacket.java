package com.xX_deadbush_Xx.chesscraft.network;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerSetStrengthPacket {
	private int strength;
	
	public ServerSetStrengthPacket(int strength) {
		this.strength = strength;
	}

	public static void encode(ServerSetStrengthPacket msg, PacketBuffer buf) {
		buf.writeInt(msg.strength);
	}

	public static ServerSetStrengthPacket decode(PacketBuffer buf) {
		return new ServerSetStrengthPacket(buf.readInt());
	}

	public static void handle(ServerSetStrengthPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity sender = Minecraft.getInstance().player;
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					((ChessBoardContainer) sender.openContainer).tile.computerStrength = msg.strength;
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
