package com.xX_deadbush_Xx.chessmod.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xX_deadbush_Xx.chessmod.ChessMod;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.objects.ChessBoardTile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class DebugButton extends Button {

	public DebugButton(ChessBoardContainer container, int widthIn, int heightIn) {
		super(widthIn, heightIn, 12, 13, "", (b) -> {
			ChessBoardTile te = container.tile;
			ChessMod.LOGGER.debug(String.format("[%s] Chess board data at %d, %d, %d {to play: %s, challenger color: %s, challenger time: %d, challenged time: %d, playing: %b, computer game: %b, offered draw: %b, offered challenge: %b}", 
					te.getWorld().isRemote ? "client" : "server", te.getPos().getX(), te.getPos().getX(), te.getPos().getX(), te.getBoard().toPlay.toString(), te.challengerColor.toString(), te.challengerTime, te.challengedTime,
					te.playing, te.isPlayingComputer, te.waitingForChallenged, te.challengerOfferedDraw));
			
				te.getBoard().getFEN(te.getBoard().toPlay);
		});
	}
	
	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(ChessBoardScreen.TEX_ICONS);
		this.blit(this.x, this.y, 3, 136, 13, 12);
	}
}
