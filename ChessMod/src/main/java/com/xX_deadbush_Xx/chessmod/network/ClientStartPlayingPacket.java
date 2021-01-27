package com.xX_deadbush_Xx.chessmod.network;

import java.util.Optional;
import java.util.function.Supplier;

import com.ibm.icu.math.MathContext;
import com.xX_deadbush_Xx.chessmod.ChessMod;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientStartPlayingPacket {
	private boolean playingcomputer;
	
	public ClientStartPlayingPacket(boolean playingcomputer) {
		this.playingcomputer = playingcomputer;
	}

	public static void encode(ClientStartPlayingPacket msg, PacketBuffer buf) {
		buf.writeBoolean(msg.playingcomputer);
	}

	public static ClientStartPlayingPacket decode(PacketBuffer buf) {
		return new ClientStartPlayingPacket(buf.readBoolean());
	}

	public static void handle(ClientStartPlayingPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@Override
			public void run() {
								
				PlayerEntity sender = context.getSender();
				 
				if(sender.openContainer instanceof ChessBoardContainer && sender.openContainer != null) {
					ChessBoardContainer container = (ChessBoardContainer) sender.openContainer;
					
					if(container.getBoard().validatePosition()) {
						container.tile.challenger = Optional.of(sender.getUniqueID());
						container.tile.playing = true;

						if(msg.playingcomputer) {
							container.tile.isPlayingComputer = true;
						} else {
							container.tile.waitingForChallenged = true;
							container.tile.isPlayingComputer = false;
						}
					}
				}
				
				PacketHandler.sendToNearby(sender.world, sender, new ServerStartPlayingPacket(sender.getUniqueID(), msg.playingcomputer));
			}
		});
		
		context.setPacketHandled(true);
	}
}
