package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.Config;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class HintButton extends Button {

	private ChessBoardContainer container;


	public HintButton(ChessBoardContainer container, int widthIn, int heightIn, IPressable onPress) {
		super(widthIn, heightIn, 20, 20, "", onPress);
		this.container = container;
	}


	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 20;
		int j = 138;
		
		if (this.isHovered()) {
			i += 20;
		}

		this.blit(this.x, this.y, i, j, 20, 20);
		
		if(!Config.COMMON.allowHints.get() || !this.container.tile.isPlayingComputer && this.container.tile.playing) {
			this.blit(this.x, this.y, 0, 84, 20, 20);
		} 
	}
}
