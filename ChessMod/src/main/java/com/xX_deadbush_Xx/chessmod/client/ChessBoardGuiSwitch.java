package com.xX_deadbush_Xx.chessmod.client;

import com.xX_deadbush_Xx.chessmod.network.ClientSetChessBoardTabPacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class ChessBoardGuiSwitch extends Button {

	public ChessBoardGuiSwitch(int widthIn, int heightIn, int mode) {
		super(widthIn, heightIn, 64, 12, "", (button) -> {
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetChessBoardTabPacket(mode));
		});
	}

	@Override
	public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {}
}
