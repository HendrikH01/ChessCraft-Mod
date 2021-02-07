package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.network.ClientSetTimePacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;

public class TimeLimitSlider extends AbstractSlider {
	
	public TimeLimitSlider(int xIn, int yIn, int widthIn, double valueIn) {
		super(null, xIn, yIn, widthIn, 20, MathHelper.clamp(valueIn, 0, 1));
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		String msg = "60:00";
		if(value < 0.125)
			msg = "--:--";
		else if(value < 0.25)
			msg = "01:00";
		else if(value < 0.375)
			msg = "03:00";
		else if(value < 0.5)
			msg = "05:00";
		else if(value < 0.625)
			msg = "10:00";
		else if(value < 0.75)
			msg = "20:00";
		else if(value < 0.875)
			msg = "30:00";
		
		this.setMessage(String.valueOf(msg));
	}

	@SuppressWarnings("resource")
	@Override
	protected void applyValue() {
		int ticks = this.getTicks();
		PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetTimePacket(ticks));
	}
	
	public int getTicks() {
		if(value < 0.125)
			return -1;
		if(value < 0.25)
			return 1200;
		if(value < 0.375)
			return 3600;
		if(value < 0.5)
			return 6000;
		if(value < 0.625)
			return 12000;
		if(value < 0.75)
			return 24000;
		if(value < 0.875)
			return 36000;
		return 72000;
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

		fontrenderer.drawString(getMessage(), this.x - 40, this.y + (this.height - 8) / 2 + 1, 0xFF221407); 
	}

	public static double getInitialVal(int challengedTime) {
		if(challengedTime == -1)
			return -1.0;
		if(challengedTime <= 1200)
			return 0.125;
		if(challengedTime <= 3600)
			return 0.25;
		if(challengedTime <= 6000)
			return 0.375;
		if(challengedTime <= 12000)
			return 0.5;
		if(challengedTime <= 24000)
			return 0.625;
		if(challengedTime <= 36000)
			return 0.75;
		return 0.875;
	}
}
