package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;

public class ToggleOptionButton extends Button {

	public boolean yes = true; //hmm
	
	public ToggleOptionButton(int x, int y, String text, IPressable onPress) {
		super(x, y, 88, 13, text, onPress);
	}
	
	@SuppressWarnings("resource")
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 80;
		int j = 78;

		if(this.yes) {
			j += 13;
		}
		
		if (this.isHovered()) {
			i += 88;
		}

		this.blit(this.x, this.y, i, j, 88, 13);
		
		int c = getFGColor();
	    Minecraft.getInstance().fontRenderer.drawString(this.getMessage(), this.x + 3, this.y + 2, c | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}
	
	@Override
	public int getFGColor() {
		return 0x221407;
	}

}
