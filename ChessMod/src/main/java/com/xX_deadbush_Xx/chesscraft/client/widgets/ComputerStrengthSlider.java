package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.network.ClientSetStrengthPacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;

public class ComputerStrengthSlider extends AbstractSlider {
	
	public ComputerStrengthSlider(int xIn, int yIn, int widthIn, double valueIn) {
		super(null, xIn, yIn, widthIn, 20, MathHelper.clamp(valueIn, 0, 1));
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(String.valueOf((int)(this.value*9)));
	}

	@SuppressWarnings("resource")
	@Override
	protected void applyValue() {
		PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetStrengthPacket((int)(this.value*9 + 1)));
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer fontrenderer = minecraft.fontRenderer;
		minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		int i = this.getYImage(this.isHovered());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.blit(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
		this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
		this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);

		fontrenderer.drawString(getMessage(), this.x - 20, this.y + (this.height - 8) / 2 + 1, 0xFF221407); 
	}
}
