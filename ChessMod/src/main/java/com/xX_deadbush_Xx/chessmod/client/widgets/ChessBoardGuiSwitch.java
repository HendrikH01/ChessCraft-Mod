package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.network.ClientSetChessBoardTabPacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class ChessBoardGuiSwitch extends Button {

	private ChessBoardContainer container;

	@SuppressWarnings("resource")
	public ChessBoardGuiSwitch(ChessBoardContainer container, int widthIn, int heightIn, int mode) {
		super(widthIn, heightIn, 64, 12, "", (button) -> {
			if(!container.tile.playing)
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetChessBoardTabPacket(mode));
		});
		
		this.container = container;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);

		if(this.container.tile.playing)
			this.blit(this.x + 26, this.y - 12, 6, 120, 8, 11);
	}
}
