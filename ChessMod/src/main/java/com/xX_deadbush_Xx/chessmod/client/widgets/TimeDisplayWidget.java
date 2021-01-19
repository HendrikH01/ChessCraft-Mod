package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;

public class TimeDisplayWidget extends Widget {
	
	private ChessBoardContainer container;
	private boolean isChallanger;
	
	public TimeDisplayWidget(ChessBoardContainer container, boolean isChallanger, int xIn, int yIn) {
		super(xIn, yIn, 60, 24, "");
		this.container = container;
		this.isChallanger = isChallanger;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		@SuppressWarnings("resource")
		FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);

		this.blit(this.x, this.y, 20, 34, 60, 24);

		RenderSystem.scalef(1.5f, 1.5f, 1.0f);
		this.drawCenteredString(fontrenderer, this.getMessage(), (int)(this.x / 1.5) + this.width / 3, (int)(this.y / 1.5) + (this.height - 8) / 3, 0xFFF2FFA3);
		RenderSystem.scalef(0.6666667f, 0.6666667f, 1.0f);
	}
	
	@Override
	public String getMessage() {
		
		if(!this.isChallanger && this.container.tile.isPlayingComputer || this.isChallanger && this.container.tile.challengerTime == -1  || !this.isChallanger && this.container.tile.challengedTime == -1) {
			return "--:--";
		}
		
		int seconds = (int)Math.ceil((this.isChallanger ? this.container.tile.challengerTime : this.container.tile.challengedTime) / 20.0);
		int minutes = (seconds / 60) % 100; //cut off at 99
		seconds = seconds % 60;
		
		return String.format("%02d:%02d", minutes, seconds);
	}
}
