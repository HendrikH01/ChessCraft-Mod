package com.xX_deadbush_Xx.chesscraft.game_logic.inventory;

import java.util.function.Supplier;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ToggleableSlotInventory extends Slot {
	
	private Supplier<Boolean> enabled;

	public ToggleableSlotInventory(IInventory inventoryIn, int index, int xPosition, int yPosition, Supplier<Boolean> enabled) {
		super(inventoryIn, index, xPosition, yPosition);
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled.get();
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return this.enabled.get();
	}
}
