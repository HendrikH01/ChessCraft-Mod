package com.xX_deadbush_Xx.chessmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvent;

public class ClientProxy {
	
	public static void playSound(SoundEvent sound) {
		 Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, 1.0F));
	}
	
}
