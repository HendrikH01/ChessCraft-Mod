package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;

public class GameEndMessageDisplay extends Button {

	public PieceColor winningColor;
	public String title = "";
	public String reason = "";

	public GameEndMessageDisplay(int widthIn, int heightIn) {
		super(widthIn, heightIn, 61, 62, "", (b) -> {
			b.active = false;
			b.visible = false;
		});
	}


	@SuppressWarnings("resource")
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		
		int x = 186;
		
		if(title.equals("YOU WON") || title.equals("YOU LOST")) {
			
			if(this.winningColor == PieceColor.BLACK) {
				x = 64;
			} else {
				x = 125;
			}
		}
		int y = 114;
		
		//bg
		this.blit(this.x, this.y, x, y, 61, 62);
		
		int c = getFGColor();
		
	    Minecraft.getInstance().fontRenderer.drawString(title, this.x + 31 - Minecraft.getInstance().fontRenderer.getStringWidth(title) / 2, this.y + 7, c | MathHelper.ceil(this.alpha * 255.0F) << 24);
		RenderSystem.scaled(0.5, 0.5, 0.5);
		
	    Minecraft.getInstance().fontRenderer.drawString(reason, this.x * 2 + 14, this.y * 2 + 41, c | MathHelper.ceil(this.alpha * 255.0F) << 24);
		RenderSystem.scaled(2.0,  2.0,  2.0);
	}
	
	@Override
	public int getFGColor() {
		return 0x221407;
	}
}
