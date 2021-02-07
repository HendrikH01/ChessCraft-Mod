package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chesscraft.network.ClientSetTimePacket;
import com.xX_deadbush_Xx.chesscraft.network.ClientStartPlayingPacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class PlayComputerButton extends Button {

	private ChessBoardContainer container;

	@SuppressWarnings("resource")
	public PlayComputerButton(ChessBoardContainer container, int widthIn, int heightIn) {
		super(widthIn, heightIn, 20, 20, "", (button) -> {
			
			if(!container.tile.playing) {
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientStartPlayingPacket(true));
				
				if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
					PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetTimePacket(((ChessBoardScreen)Minecraft.getInstance().currentScreen).timeSlider.getTicks()));
				}
			}
			
			else if(container.tile.isPlayingComputer && container.tile.isPlaying(Minecraft.getInstance().player.getUniqueID()))
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 8));
			
			System.out.println(container.tile.waitingForComputerMove);

		});
		this.container = container;

	}

	@SuppressWarnings("resource")
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 20;
		int j = 118;
		
		if (this.isHovered()) {
			i += 20;
		}
		
		if(this.container.tile.playing) {
			if(this.container.tile.isPlayingComputer && container.tile.isPlaying(Minecraft.getInstance().player.getUniqueID())) {
				j += 40;
			} else {
				this.blit(this.x, this.y, i, j, 20, 20);
				this.blit(this.x, this.y, 0, 84, 20, 20);
				return;
			}
		}

		this.blit(this.x, this.y, i, j, 20, 20);

	}
}
