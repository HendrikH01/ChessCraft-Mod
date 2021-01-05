package com.xX_deadbush_Xx.chessmod.game_logic;

import static com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper.*;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chessmod.ChessMod;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoard;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoardSquareSlot;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoardSquareSlot.HighlightMode;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessPieceStorageSlot;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ToggleableSlotInventory;
import com.xX_deadbush_Xx.chessmod.objects.ChessBoardTile;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;
import com.xX_deadbush_Xx.chessmod.objects.ModRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChessBoardContainer extends Container {

	private ChessBoard board;
	public int selectedSlot = -1;
	private Mode mode = Mode.PLAYING;
	private int checkedSquare = -1;
	
    public ChessBoardContainer(final int windowId, final PlayerInventory playerInventory, PacketBuffer buffer) {
		this(windowId, playerInventory, (ChessBoardTile)Util.getTileEntity(ChessBoardTile.class, playerInventory, buffer));
	}
    
    public ChessBoardContainer(final int windowId, final PlayerInventory playerInventory, ChessBoardTile tile) {
		super(ModRegistry.CHESS_BOARD_CONTAINER.get(), windowId);
		
		this.board = tile.getBoard();
		
		addSlots(playerInventory, tile);
	}
    
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if(slotId >= 0) {
			if(slotId < 64) {
				return handleBoardClick(slotId, dragType, player);
			} else if(slotId >= 112 && this.mode == Mode.BOARD_EDITOR) {
				//storage slots in board editor
				ChessPieceStorageSlot slot = (ChessPieceStorageSlot)this.inventorySlots.get(slotId);
				ItemStack slotstack = slot.getStack();
				ItemStack holding = player.inventory.getItemStack();
				
				//if stacks equal remove from hand
				if(slotstack.getItem() == holding.getItem() && ItemStack.areItemStackTagsEqual(slotstack, holding)) {
					ItemStack storage = this.inventorySlots.get(Util.getStorageSlotIndex(holding)).getStack();
					if(storage.getCount() == storage.getMaxStackSize()) {
						//if storage slot was filled while player was holding item do nothing 
						return slotstack;
					} else {
						storage.grow(1);
						player.inventory.setItemStack(ItemStack.EMPTY);
					}
						return slotstack;
				}
				if(!slotstack.isEmpty()) {
					//try take stack out or swap
					
					if(!holding.isEmpty()) {
						
						//if player is holding something return it into its storage slot
						ItemStack storage = this.inventorySlots.get(Util.getStorageSlotIndex(holding)).getStack();
						if(storage.getCount() == storage.getMaxStackSize()) {
							//if storage slot was filled while player was holding item do nothing 
							return slotstack;
						} else {
							storage.grow(1);
						}
					}
					
					//prepare new holding stack
					ItemStack newstack = slotstack.copy();
					newstack.setCount(1);
					
					slot.decrStackSize(1); //remove from storage
					player.inventory.setItemStack(newstack);
				}
				
				return slot.getStack();
			}
		}
		
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}
	
	private ItemStack handleBoardClick(int slotid, int dragType, PlayerEntity player) {
		//this.side == PieceColor.WHITE ? slotid : getX(slotid)*8 + (7 - getY(slotid))
		boolean whiteside = getSide() == PieceColor.WHITE;

		int original = whiteside ? slotid : getX(slotid) * 8 + 7 - getY(slotid);
		ChessBoardSquareSlot slot = (ChessBoardSquareSlot)this.inventorySlots.get(original);
		ItemStack inslot = slot.getStack();
		ItemStack holding = player.inventory.getItemStack();
		
		if (this.mode == Mode.BOARD_EDITOR) {
			//place piece and remove
			if(!holding.isEmpty()) {
				slot.clear(this);
				slot.insertStack(holding);
				
				Slot storage = this.inventorySlots.get(Util.getStorageSlotIndex(holding));
				if(storage.getHasStack()) {
					storage.decrStackSize(1);
				} else {
					holding.shrink(1);
				}
				
			//clear slot
			} else if(!inslot.isEmpty()) {
				slot.clear(this);
				return ItemStack.EMPTY;
				
			//if both empty do nothing
			} else return ItemStack.EMPTY;
			
			return slot.getStack();

		} else if (this.mode == Mode.PLAYING) {
			
			if(!inslot.isEmpty() && this.selectedSlot == -1 &&  ((ChessPiece)inslot.getItem()).color == this.board.getCurrentColor()) {
				this.selectedSlot = original;
			
			} else if(this.selectedSlot != -1) {
				if(this.selectedSlot == original) {
					this.selectedSlot = -1;
					return ItemStack.EMPTY;
				}
				
				ItemStack first = this.inventorySlots.get(whiteside ? selectedSlot : getX(selectedSlot) * 8 + 7 - getY(selectedSlot)).getStack();
				
				if(!first.isEmpty()) {
					if(!inslot.isEmpty() && ((ChessPiece)inslot.getItem()).color == this.board.getCurrentColor()) {
						this.selectedSlot = original;
					}
					else {
						ChessHelper.executeMove(this, this.selectedSlot, original, () -> {
							if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
								((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateSlotHighlights();
							}
								
							if(this.board.isInCheck(this.board.toPlay)) {
								int kingpos = this.board.getKingPos(this.board.toPlay);
								this.checkedSquare = kingpos;
							} else this.checkedSquare = -1;
						});
						this.selectedSlot = -1;
						return ItemStack.EMPTY;
					}
				} else {
					this.selectedSlot = -1;
				}
			}
			
			if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
				((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateSlotHighlights();
			}
		}
		
		return ItemStack.EMPTY;
	}

	public void clearBoard() {
		for(int i = 0; i < 64; i++) {
			((ChessBoardSquareSlot)this.inventorySlots.get(i)).clear(this);
			((ChessBoardSquareSlot) this.inventorySlots.get(i)).highlight = Optional.empty();
		}
	}
	
	public void setMode(Mode mode) {

		if(this.mode == Mode.PLAYING) {
			this.selectedSlot = -1;
		}
		this.mode = mode;
	}
	
	public Mode getMode() {
		return this.mode;
	}
	
	public PieceColor getSide() {
		if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
			return ((ChessBoardScreen)Minecraft.getInstance().currentScreen).getSide();
		}
		else return PieceColor.WHITE;
	}
	
	public ChessBoard getBoard() {
		return this.board;
	}
	
	public void handleUpdatePacket(byte event) {
		switch(event) {
		case 0: {
			//CLEAR BOARD
			this.clearBoard();
			break;
		}
		
		case 1: {
			//BUILD BOARD hardcoded yay
			
			if(this.inventorySlots.get(64).getStack().getCount() >= 8 &&
			   this.inventorySlots.get(65).getStack().getCount() >= 2 &&
			   this.inventorySlots.get(66).getStack().getCount() >= 2 &&
			   this.inventorySlots.get(67).getStack().getCount() >= 2 &&
			   this.inventorySlots.get(68).getStack().getCount() >= 1 &&
			   this.inventorySlots.get(69).getStack().getCount() >= 1 &&
			   this.inventorySlots.get(70).getStack().getCount() >= 8 &&
			   this.inventorySlots.get(71).getStack().getCount() >= 2 &&
			   this.inventorySlots.get(72).getStack().getCount() >= 2 &&
			   this.inventorySlots.get(73).getStack().getCount() >= 2 &&
			   this.inventorySlots.get(74).getStack().getCount() >= 1 &&
			   this.inventorySlots.get(75).getStack().getCount() >= 1) {
						   
				for(int i = 0; i < 8; i++) {
					this.board.placePieceAt(i, 6, this.inventorySlots.get(64).getStack().copy());
					this.board.placePieceAt(i, 1, this.inventorySlots.get(70).getStack().copy());
				}
				
				this.board.placePieceAt(7, 7, this.inventorySlots.get(67).getStack().copy());
				this.board.placePieceAt(0, 7, this.inventorySlots.get(67).getStack().copy());
				this.board.placePieceAt(7, 0, this.inventorySlots.get(73).getStack().copy());
				this.board.placePieceAt(0, 0, this.inventorySlots.get(73).getStack().copy());
				
				this.board.placePieceAt(1, 7, this.inventorySlots.get(65).getStack().copy());
				this.board.placePieceAt(6, 7, this.inventorySlots.get(65).getStack().copy());
				this.board.placePieceAt(1, 0, this.inventorySlots.get(71).getStack().copy());
				this.board.placePieceAt(6, 0, this.inventorySlots.get(71).getStack().copy());
				
				this.board.placePieceAt(2, 7, this.inventorySlots.get(66).getStack().copy());
				this.board.placePieceAt(5, 7, this.inventorySlots.get(66).getStack().copy());
				this.board.placePieceAt(2, 0, this.inventorySlots.get(72).getStack().copy());
				this.board.placePieceAt(5, 0, this.inventorySlots.get(72).getStack().copy());
				
				this.board.placePieceAt(3, 7, this.inventorySlots.get(68).getStack().copy());
				this.board.placePieceAt(4, 7, this.inventorySlots.get(69).getStack().copy());
				
				this.board.placePieceAt(3, 0, this.inventorySlots.get(74).getStack().copy());
				this.board.placePieceAt(4, 0, this.inventorySlots.get(75).getStack().copy());
				
				this.inventorySlots.get(64).getStack().shrink(8);
				this.inventorySlots.get(65).getStack().shrink(2);
				this.inventorySlots.get(66).getStack().shrink(2);
				this.inventorySlots.get(67).getStack().shrink(2);
				this.inventorySlots.get(68).getStack().shrink(1);
				this.inventorySlots.get(69).getStack().shrink(1);
				this.inventorySlots.get(70).getStack().shrink(8);
				this.inventorySlots.get(71).getStack().shrink(2);
				this.inventorySlots.get(72).getStack().shrink(2);
				this.inventorySlots.get(73).getStack().shrink(2);
				this.inventorySlots.get(74).getStack().shrink(1);
				this.inventorySlots.get(75).getStack().shrink(1);
				
				this.board.toPlay = PieceColor.WHITE;
				this.board.canCastle = new boolean[] {true, true, true, true};
				break;
			}
		}
		}
	}
	
	public int getCheckedSquare() {
		return this.checkedSquare;
	}
	
	@Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    	if(index < 64 || index >= 112) {
    		return ItemStack.EMPTY;
    	} else if(index < 76) {
    	      ItemStack newstack = ItemStack.EMPTY;
    	      Slot slot = this.inventorySlots.get(index);
				if (slot != null && slot.getHasStack()) {
					ItemStack stackinslot = slot.getStack();
					newstack = stackinslot.copy();
					if (!this.mergeItemStack(stackinslot, 76, this.inventorySlots.size(), true)) {
						return ItemStack.EMPTY;
					}

					if (stackinslot.isEmpty()) {
						slot.putStack(ItemStack.EMPTY);
					} else {
						slot.onSlotChanged();
					}
    	      }

    	      return newstack;
    	      
    	} else {
			ItemStack newstack = ItemStack.EMPTY;
			Slot slot = this.inventorySlots.get(index);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack stack = slot.getStack();
				if(stack.getItem() instanceof ChessPiece) {
					
					for(int i = 64; i < 76; i++) {	
						Slot pieceslot = this.inventorySlots.get(i);
						
						if(pieceslot.isItemValid(stack)) {
							if(!pieceslot.getHasStack()) {
								pieceslot.putStack(stack.copy());
								stack.setCount(0);
							}
							
							else {
								int count = pieceslot.getStack().getCount() + stack.getCount();
								int remaining = count < 64 ? 0 : count - 64;
								count -= remaining;
								pieceslot.getStack().setCount(count);
								
								if(remaining == stack.getCount())
									return ItemStack.EMPTY;
								
								stack.setCount(remaining);
								
								
								return stack;
							}
						}
					}
				}
			}
			
			return newstack;
		}
	}
    
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}
	
    private void addSlots(PlayerInventory playerInventory, ChessBoardTile tile) {
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				this.addSlot(new ChessBoardSquareSlot(this, j + i * 8, 22 + i * 18, 30 + j * 18, () -> mode == Mode.PLAYING || mode == Mode.BOARD_EDITOR));
			}
		}

		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 6; ++j) {
				this.addSlot(new ChessPieceStorageSlot(tile.getInventory(), j + i * 6, 20 + j * 20, 55 + i * 20, ChessPieceType.values()[j], PieceColor.values()[i], () -> mode == Mode.STORAGE));
			}
		}
	      
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new ToggleableSlotInventory(playerInventory, j + i * 9 + 9, j * 18 + 49, i * 18 + 118, () -> mode == Mode.STORAGE));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlot(new ToggleableSlotInventory(playerInventory, i, i * 18 + 49, 176, () -> mode == Mode.STORAGE));
		}
		
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 6; ++j) {
				this.addSlot(new ChessPieceStorageSlot(tile.getInventory(), i * 6 + j, 192 + i * 30, 66 + j * 18, ChessPieceType.values()[j], PieceColor.values()[i], () -> mode == Mode.BOARD_EDITOR));
			}
		}		
	}
	
	public static enum Mode {
		PLAYING(new ResourceLocation(ChessMod.MOD_ID, "textures/gui/playing.png")),
		BOARD_EDITOR(new ResourceLocation(ChessMod.MOD_ID, "textures/gui/board_editor.png")),
		STORAGE(new ResourceLocation(ChessMod.MOD_ID, "textures/gui/storage.png")),
		SETTINGS(new ResourceLocation(ChessMod.MOD_ID, "textures/gui/settings.png"));

		public final ResourceLocation texture;

		Mode(ResourceLocation resourceLocation) {
			this.texture = resourceLocation;
		}
	}
}
