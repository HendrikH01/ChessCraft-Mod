package com.xX_deadbush_Xx.chessmod.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class BuildBoardButton extends Button {

	public BuildBoardButton(int p_i51141_1_, int p_i51141_2_, IPressable p_i51141_6_) {
		super(p_i51141_1_, p_i51141_2_, 34, 11, "Build", p_i51141_6_);
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 33;
		int j = 24;
		if (this.isHovered()) {
			//i += 14;
		}

		this.blit(this.x, this.y, i, j, 34, 11);
	}
}
