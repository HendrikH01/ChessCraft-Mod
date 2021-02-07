package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;

public class ErrorMessageDisplay extends Button {

	public String message = "";

	public ErrorMessageDisplay(int widthIn, int heightIn) {
		super(widthIn, heightIn, 62, 62, "", (b) -> {
			b.active = false;
			b.visible = false;
		});
	}


	@SuppressWarnings("resource")
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		
		int x = 4;
		int y = 105;
		
		//bg
		this.blit(this.x, this.y, x, y, 12, 11);

		int c = getFGColor();
	    Minecraft.getInstance().fontRenderer.drawString(message, this.x + 15, this.y + 3, c | MathHelper.ceil(this.alpha * 255.0F) << 24);

	}
	
	@Override
	public int getFGColor() {
		return 0x91160F;
	}
}
