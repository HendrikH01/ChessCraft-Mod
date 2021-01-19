package com.xX_deadbush_Xx.chessmod.client.widgets;

import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class ResignButton extends Button {

	private ChessBoardContainer container;

	@SuppressWarnings("resource")
	public ResignButton(ChessBoardContainer container, int widthIn, int heightIn) {
		super(widthIn, heightIn, 20, 20, "", (button) -> {
			if(container.tile.playing) {
				UUID own = Minecraft.getInstance().player.getUniqueID();
				if(container.tile.isPlaying(own))
					PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 3));
				}
			});
		
		this.container = container;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 20;
		int j = 58;

		if (this.isHovered()) {
			i += 20;
		}

		this.blit(this.x, this.y, i, j, 20, 20);
		
		@SuppressWarnings("resource")
		UUID own = Minecraft.getInstance().player.getUniqueID();

		if(!this.container.tile.playing || !container.tile.isPlaying(own)) 
			this.blit(this.x, this.y, 0, 84, 20, 20);

	}
}
