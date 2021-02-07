package com.xX_deadbush_Xx.chesscraft.client.widgets;

import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;

public class ChallengeDisplay extends Button {

	public String challenger;

	public ChallengeDisplay(int widthIn, int heightIn) {
		super(widthIn, heightIn, 73, 62, "", (b) -> {});
	}


	@SuppressWarnings("resource")
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		if(challenger == null) return;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		
		int x = 64;
		int y = 176;
		
		//bg
		this.blit(this.x, this.y, x, y, 73, 62);
		
		int c = getFGColor();
		RenderSystem.scaled(0.5, 0.5, 0.5);
	    Minecraft.getInstance().fontRenderer.drawString(challenger, 2 * this.x + (73 - Minecraft.getInstance().fontRenderer.getStringWidth(challenger)/2), 2 * this.y + 14, c | MathHelper.ceil(this.alpha * 255.0F) << 24);
	    Minecraft.getInstance().fontRenderer.drawString("opened a challenge!", 2 * this.x + 20, 2 * this.y + 25, c | MathHelper.ceil(this.alpha * 255.0F) << 24);

	    Minecraft.getInstance().fontRenderer.drawString("Press 'play' to accept.", this.x * 2 + 14, this.y * 2 + 41, c | MathHelper.ceil(this.alpha * 255.0F) << 24);
		RenderSystem.scaled(2.0,  2.0,  2.0);
	}
	
	@Override
	public int getFGColor() {
		return 0x221407;
	}
}
