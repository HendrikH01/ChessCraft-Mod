package com.xX_deadbush_Xx.chessmod.game_logic.inventory;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ToggleableSlot extends SlotItemHandler {

	private Supplier<Boolean> enabled;
	
	public ToggleableSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Supplier<Boolean> enabled) {
		super(itemHandler, index, xPosition, yPosition);
		this.enabled = enabled;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return this.enabled.get();
	}

	@Override
	public boolean isEnabled() {
		return enabled.get();
	}
	
	public static class BoolReference {
		
		private boolean value;
		
		public BoolReference(boolean init) {
			this.value = init;
		}
		
		public boolean get() {
			return value;
		}
		
		public void toggle() {
			this.value = !this.value;
		}
		
		public void set(boolean b) {
			this.value = b;
		}
	}
}
