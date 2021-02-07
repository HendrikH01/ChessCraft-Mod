package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class FlipBoardButton extends Button {
	
	public FlipBoardButton(int widthIn, int heightIn, IPressable onPress) {
		super(widthIn, heightIn, 20, 20, "", onPress);
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 80;
		int j = 20;
		if (this.isHovered()) {
			i += 20;
		}

		this.blit(this.x, this.y, i, j, 20, 20);
	}
}
