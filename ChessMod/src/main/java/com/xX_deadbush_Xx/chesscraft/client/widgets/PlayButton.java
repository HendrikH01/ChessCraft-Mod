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

public class PlayButton extends Button {

	private ChessBoardContainer container;

	@SuppressWarnings("resource")
	public PlayButton(ChessBoardContainer container, int widthIn, int heightIn) {
		super(widthIn, heightIn, 20, 20, "", (button) -> {
			if(!container.tile.playing) {		
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientStartPlayingPacket(false));
	
				if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
					PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetTimePacket(((ChessBoardScreen)Minecraft.getInstance().currentScreen).timeSlider.getTicks()));
				}
			} else if(container.tile.waitingForChallenged) {
				
				if(container.tile.challenger.get().equals(Minecraft.getInstance().player.getUniqueID())) {
					PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte)9));
				} else {
					PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte)10));
				}
			}
		});
		this.container = container;
	}

	@SuppressWarnings("resource")
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 20;
		int j = 78;
		
		if (this.isHovered()) {
			i += 20;
		}
		
		if(this.container.tile.waitingForChallenged && this.container.tile.challenger.isPresent()) {
			if(this.container.tile.challenger.get().equals(Minecraft.getInstance().player.getUniqueID()))
				j += 120;
			else
				j += 100;
		}
		
		this.blit(this.x, this.y, i, j, 20, 20);
		
		if(this.container.tile.playing && !this.container.tile.waitingForChallenged)
			this.blit(this.x, this.y, 0, 84, 20, 20);
	}
}
