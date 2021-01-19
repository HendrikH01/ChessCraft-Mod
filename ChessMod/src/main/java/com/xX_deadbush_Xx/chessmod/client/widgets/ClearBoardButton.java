package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class ClearBoardButton extends Button {

	@SuppressWarnings("resource")
	public ClearBoardButton(int widthIn, int heightIn) {
		super(widthIn, heightIn, 20, 20, "", (b) -> {
		PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 0));
		});
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 40;
		int j = 0;
		if (this.isHovered()) {
			i += 20;
		}

		this.blit(this.x, this.y, i, j, 20, 20);
	}
}
