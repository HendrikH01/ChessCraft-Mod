package com.xX_deadbush_Xx.chesscraft.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;
import com.xX_deadbush_Xx.chesscraft.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class PlayingColorButton extends Button {
	
	private ChessBoardContainer container;

	@SuppressWarnings("resource")
	public PlayingColorButton(ChessBoardContainer container, int p_i51141_1_, int p_i51141_2_) {
		super(p_i51141_1_, p_i51141_2_, 17, 17, "", b -> {
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte)(6 + ((PlayingColorButton)b).container.getBoard().toPlay.getOpposite().ordinal())));
		});
		
		this.container = container;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 140;
		int j = 0;

		if (this.isHovered()) {
			i += 17;
		}
		if(this.container.getBoard().toPlay == PieceColor.WHITE) {
			j += 17;
		}

		this.blit(this.x, this.y, i, j, 17, 17);
	}
}
