package com.xX_deadbush_Xx.chesscraft.network;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.client.widgets.ChallengeDisplay;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerStartPlayingPacket {
	private boolean playingcomputer;
	private UUID challenger;
	
	public ServerStartPlayingPacket(UUID challenger, boolean playingcomputer) {
		this.playingcomputer = playingcomputer;
		this.challenger = challenger;
	}

	public static void encode(ServerStartPlayingPacket msg, PacketBuffer buf) {
		buf.writeUniqueId(msg.challenger);
		buf.writeBoolean(msg.playingcomputer);
	}

	public static ServerStartPlayingPacket decode(PacketBuffer buf) {
		return new ServerStartPlayingPacket(buf.readUniqueId(), buf.readBoolean());
	}

	public static void handle(ServerStartPlayingPacket msg, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(new Runnable() {

			@SuppressWarnings("resource")
			@Override
			public void run() {
				PlayerEntity player = Minecraft.getInstance().player;
				if(player.openContainer instanceof ChessBoardContainer && player.openContainer != null) {
					ChessBoardContainer container = (ChessBoardContainer) player.openContainer;
					
					if(container.getBoard().validatePosition()) {
						container.tile.playing = true;
						container.tile.challenger = Optional.of(msg.challenger);
						container.setMode(Mode.PLAYING);
						if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
							((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateMode(Mode.PLAYING);
						}
						
						
						if(!msg.playingcomputer) {
							container.tile.waitingForChallenged = true;
							container.tile.isPlayingComputer = false;
							
							if(!player.getUniqueID().equals(msg.challenger)) {
								if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
									ChallengeDisplay display = ((ChessBoardScreen)Minecraft.getInstance().currentScreen).challengeDisplay;
									display.active = true;
									display.visible = true;
									display.challenger = container.tile.getWorld().getPlayerByUuid(msg.challenger).getName().getString();
								}
							}
						} else {
							container.tile.isPlayingComputer = true;
							container.tile.waitingForChallenged = false;
						}
					} else if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
						((ChessBoardScreen)Minecraft.getInstance().currentScreen).displayInvalidPosError();
					}
				}
			}
		});
		
		context.setPacketHandled(true);
	}
}
