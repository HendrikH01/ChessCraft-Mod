package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class OfferDrawButton extends Button {

	private ChessBoardContainer container;

	@SuppressWarnings("resource")
	public OfferDrawButton(ChessBoardContainer container, int widthIn, int heightIn) {
		super(widthIn, heightIn, 20, 20, "", (button) -> {
			if(!container.tile.isPlayingComputer && container.tile.playing)
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 2));
		});
		
		this.container = container;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 20;
		int j = 98;
		
		if (this.isHovered()) {
			i += 20;
		}

		this.blit(this.x, this.y, i, j, 20, 20);
		
		if(!this.container.tile.playing || this.container.tile.isPlayingComputer)
			this.blit(this.x, this.y, 0, 84, 20, 20);
	}
}
