package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.network.ClientPromotionPacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class PromotionButton extends Button {

	public final ChessPieceType type;
	public PieceColor color = PieceColor.WHITE;
	private ChessBoardContainer container;
	
	@SuppressWarnings("resource")
	public PromotionButton(ChessBoardContainer container, int x, int y, ChessPieceType type) {
		super(x, y, 20, 20, "", (button) -> {	
			if(container.inventorySlots.get(64 + ((PromotionButton)button).type.ordinal() + ((PromotionButton)button).color.ordinal() * 6).getHasStack()) {			
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientPromotionPacket(type.ordinal(), ((PromotionButton)button).color));
			}
		});
		
		this.type = type;
		this.container = container;
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		int i = 0;
		int j = 0;
		
		if (this.isHovered()) {
			i += 20;
		}

		if(!container.inventorySlots.get(64 + this.type.ordinal() + this.color.ordinal() * 6).getHasStack()) {			
			this.blit(this.x, this.y, 0, 64, 20, 20);
			RenderSystem.color4f(0.6F, 0.6F, 0.6F, 1.0F);
		} else {
			this.blit(this.x, this.y, i, j, 20, 20);
		}
		
		int textureY = color == PieceColor.BLACK ? 0 : 16;
		int textureX = 16 * type.ordinal();
		
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_PIECES);
	    blit(this.x + 2, this.y + 2, 0, (float)textureX, (float)textureY, 16, 16, 512, 512);
	    
	}
}
