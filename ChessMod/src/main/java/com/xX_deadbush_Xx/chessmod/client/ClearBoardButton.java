package com.xX_deadbush_Xx.chessmod.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class ClearBoardButton extends Button {

	public ClearBoardButton(int widthIn, int heightIn, IPressable onPress) {
		super(widthIn, heightIn, 13, 16, "", onPress);
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 0;
		int j = 21;
		if (this.isHovered()) {
			i += 14;
		}

		this.blit(this.x, this.y, i, j, 13, 16);
	}
}
