package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class BuildBoardButton extends Button {

	@SuppressWarnings("resource")
	public BuildBoardButton(int p_i51141_1_, int p_i51141_2_) {
		super(p_i51141_1_, p_i51141_2_, 40, 14, "", (button) -> {	
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 1));
		});
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 0;
		int j = 20;
		if (this.isHovered()) {
			i += 40;
		}

		this.blit(this.x, this.y, i, j, 40, 14);
	}
}
