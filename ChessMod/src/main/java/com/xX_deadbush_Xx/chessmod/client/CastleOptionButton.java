package com.xX_deadbush_Xx.chessmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.network.ClientSetCastlingOptionPacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class CastleOptionButton extends Button {

	private final int num;
	private final ChessBoardContainer container;
	
	@SuppressWarnings("resource")
	public CastleOptionButton(ChessBoardContainer container, int p_i51141_1_, int p_i51141_2_, int num) {
		super(p_i51141_1_, p_i51141_2_, 10, 10, "", b -> {
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetCastlingOptionPacket(num, !container.getBoard().canCastle[num]));
		});
		
		this.num = num;
		this.container = container;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 80;
		int j = 0;
		
		i  += num % 2 * 10;
		j += num / 2 * 10;
		
		if (this.isHovered()) {
			i += 20;
		} else {
			if(container.getBoard().canCastle[num]) {
				i += 40;
			}
			
		}

		this.blit(this.x, this.y, i, j, 10, 10);
	}
}
