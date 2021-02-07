package com.xX_deadbush_Xx.chesscraft.game_logic;

import static com.xX_deadbush_Xx.chesscraft.game_logic.ChessHelper.*;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chesscraft.ChessMod;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.client.ClientProxy;
import com.xX_deadbush_Xx.chesscraft.game_logic.inventory.ChessBoard;
import com.xX_deadbush_Xx.chesscraft.game_logic.inventory.ChessBoardSquareSlot;
import com.xX_deadbush_Xx.chesscraft.game_logic.inventory.ChessPieceStorageSlot;
import com.xX_deadbush_Xx.chesscraft.game_logic.inventory.ToggleableSlotInventory;
import com.xX_deadbush_Xx.chesscraft.network.ClientEngineMakeMovePacket;
import com.xX_deadbush_Xx.chesscraft.network.ClientMakeMovePacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;
import com.xX_deadbush_Xx.chesscraft.network.ServerChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chesscraft.network.ServerChessDrawPacket;
import com.xX_deadbush_Xx.chesscraft.network.ServerPlayerDefeatedPacket;
import com.xX_deadbush_Xx.chesscraft.network.ServerChessDrawPacket.DrawReason;
import com.xX_deadbush_Xx.chesscraft.network.ServerPlayerDefeatedPacket.LoseReason;
import com.xX_deadbush_Xx.chesscraft.objects.ChessBoardTile;
import com.xX_deadbush_Xx.chesscraft.objects.ChessPiece;
import com.xX_deadbush_Xx.chesscraft.objects.ModRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ChessBoardContainer extends Container {

	public final ChessBoardTile tile;
	private ChessBoard board;
	public int selectedSlot = -1;
	private Mode mode = Mode.PLAYING;
	public int checkedSquare = -1;
	
    public ChessBoardContainer(final int windowId, final PlayerInventory playerInventory, PacketBuffer buffer) {
		this(windowId, playerInventory, (ChessBoardTile)Util.getTileEntity(ChessBoardTile.class, playerInventory, buffer));
	}
    
    public ChessBoardContainer(final int windowId, final PlayerInventory playerInventory, ChessBoardTile tile) {
		super(ModRegistry.CHESS_BOARD_CONTAINER.get(), windowId);
		
		this.tile = tile;
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
				if(slotstack.getItem() == holding.getItem() && ItemStack.areItemStackTagsEqual(slotstack, holding) && !holding.isEmpty()) {
					ChessHelper.putPieceBack(this, holding);
					player.inventory.setItemStack(ItemStack.EMPTY);
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
	
	@SuppressWarnings("resource")
	private ItemStack handleBoardClick(int slotid, int dragType, PlayerEntity player) {
		boolean whiteside = getSide(player) == PieceColor.WHITE;

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
				
			//if both empty do nothing
			} else return ItemStack.EMPTY;
			
			this.boardChanged();
			
			return slot.getStack();

		} else if (this.mode == Mode.PLAYING && this.tile.getWorld().isRemote) {
			
			//if playing and not your turn you cant modify the board
			if(this.tile.playing) {
				boolean ischallenger = this.tile.challenger.isPresent() && this.tile.challenger.get().equals(player.getUniqueID());
				if(!(ischallenger && this.tile.challengerColor == this.board.toPlay || !ischallenger && this.tile.challengerColor != this.board.toPlay)) {
					return ItemStack.EMPTY;
				}			
			}
			
			if(!inslot.isEmpty() && this.selectedSlot == -1 && ((ChessPiece)inslot.getItem()).color == this.board.toPlay) {
				this.selectedSlot = original;
			
			} else if(this.selectedSlot != -1) {
				if(this.selectedSlot == original) {
					this.selectedSlot = -1;
					return ItemStack.EMPTY;
				}
				
				ItemStack moved = this.inventorySlots.get(selectedSlot).getStack();
				
					if(!moved.isEmpty()) {
						if(!inslot.isEmpty() && ((ChessPiece)inslot.getItem()).color == this.board.getCurrentColor()) {
						this.selectedSlot = original;
					}
					
					else if(this.tile.getWorld().isRemote){
						
						final int first = this.selectedSlot;
						getLegalMoves(this, this.getBoard().toPlay, moves -> {
							
							if(moves.contains(Pair.of(first, original))) {
									
									ClientProxy.playSound(ModRegistry.MOVE_PIECE.get());

									PacketHandler.sendToServer(this.tile.getWorld(), new ClientMakeMovePacket(this.board.toPlay, first, original));
									ChessPiece item = (ChessPiece)moved.getItem();
									ChessHelper.executeMove(this, first, original, "");
									this.board.toPlay = this.board.toPlay.getOpposite();

									if(item.type == ChessPieceType.PAWN) {
										if(getY(original) == 0 && item.color == PieceColor.WHITE || getY(original) == 7 && item.color == PieceColor.BLACK) {
											
											//promotion
											this.tile.promotionColor = Optional.of(this.board.toPlay.getOpposite());
											if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
												((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateMode(this.getMode());
											}
										}
									}
									
									if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
										((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateSlotHighlights();
									}
								}
						});
						
						this.selectedSlot = -1;
						return ItemStack.EMPTY;
					}
				} else {
					this.selectedSlot = -1;
				}
			}
			
			if(this.tile.getWorld().isRemote) {
				if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
					((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateSlotHighlights();
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	@SuppressWarnings("resource")
	private void boardChanged() {
		if(this.tile.getWorld().isRemote) {
			if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
				ChessBoardScreen screen = (ChessBoardScreen)Minecraft.getInstance().currentScreen;

				screen.updateSlotHighlights();
			}
		}
	}

	public void clearBoard() {
		for(int i = 0; i < 64; i++) {
			((ChessBoardSquareSlot)this.inventorySlots.get(i)).clear(this);
			((ChessBoardSquareSlot) this.inventorySlots.get(i)).highlight = Optional.empty();
		}
	}
	
	public void setMode(Mode mode) {
		if(this.tile.playing) {
			this.mode = Mode.PLAYING;
			return;
		}
		
		if(this.mode == Mode.PLAYING) {
			this.selectedSlot = -1;
		}
		
		if(mode == Mode.PLAYING && !this.tile.getWorld().isRemote) {
			this.boardChanged();
		}
		
		this.mode = mode;
	}
	
	public Mode getMode() {
		return this.mode;
	}
	
	@SuppressWarnings("resource")
	public PieceColor getSide(PlayerEntity player) {
		if(player.world.isRemote) {
			if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
				return ((ChessBoardScreen)Minecraft.getInstance().currentScreen).getSide();
			}
		}
		
		if(this.tile.sides.containsKey(player.getUniqueID())) {
			if(player.openContainer.equals(this))
				return this.tile.sides.getOrDefault(player.getUniqueID(), PieceColor.WHITE);
			else this.tile.sides.remove(player.getUniqueID());
		}
		return PieceColor.WHITE;
	}
	
	public ChessBoard getBoard() {
		return this.board;
	}
	
	public void tryPromotePawn(ChessPieceType type, PieceColor color) {
		if(type == ChessPieceType.PAWN || type == ChessPieceType.KING) return;
		
		Slot storage = this.inventorySlots.get(64 + type.ordinal() + color.ordinal() * 6);
		if(storage.getHasStack()) {
			ItemStack piece = storage.getStack().copy();
			storage.getStack().shrink(1);
			
			for(int x = 0; x < 8; x++) {
				int y = color == PieceColor.WHITE ? 0 : 7;
				if(!this.getBoard().getPieceAt(x, y).isEmpty()) {
					if(((ChessPiece)this.getBoard().getPieceAt(x, y).getItem()).type == ChessPieceType.PAWN) {
						this.getBoard().removePieceAt(this, x, y);
						this.getBoard().placePieceAt(x, y, piece);
						
						this.tile.promotionColor = Optional.empty();
						
						break;
					}
				}
			}
		}
	}
	
 	public void makeComputerMove(@Nullable Runnable callback) {
		String fen = this.board.getFEN(this.tile.challengerColor.getOpposite());
		ChessEngineManager.getNextMove(fen, this.tile.computerStrength, result -> {
			
			if(result.equals("(none)")) return;
			
			try {
 				ChessHelper.getLegalMoves(this, this.tile.challengerColor.getOpposite(), moves -> {
 					int f = ChessHelper.convertNumberFormat(result.substring(0, 2));
					int s = ChessHelper.convertNumberFormat(result.substring(2, 4));
					String p = "";
					if(result.length() == 5) {
						p = result.substring(4, 5);
					}
					
					if(moves.contains(Pair.of(f, s))) {
						PacketHandler.sendToServer(this.tile.getWorld(), new ClientEngineMakeMovePacket(f, s, p));
						
						if(callback != null)
							callback.run();
					}
				});
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			finally {
				System.out.println("yee");
				this.tile.stopWaiting();
			}
		});
 	}
 	
	@SuppressWarnings("resource")
	public void checkForMate(PieceColor mated) {
		if(this.board.isInCheck(mated)) {
			this.checkedSquare = this.board.getKingPos(mated);
			
			if(this.tile.getWorld().isRemote) return;
			
			//checkmate check!
			ChessEngineManager.getLegalMoves(this.board.getFEN(mated), (result) -> {
				if(result.isEmpty()) {
					//mate
					PacketHandler.sendToNearby(this.tile.getWorld(), this.tile.getPos(), new ServerPlayerDefeatedPacket(mated != this.tile.challengerColor, LoseReason.CHECKMATE));
				}
			});
		} else {
			this.checkedSquare = -1;
			
			ChessEngineManager.getLegalMoves(this.board.getFEN(mated), (result) -> {
				if(result.isEmpty() && !this.tile.getWorld().isRemote) {
					//stalemate
					PacketHandler.sendToNearby(this.tile.getWorld(), this.tile.getPos(), new ServerChessDrawPacket(DrawReason.STALEMATE));
				}
			});
		}
	}
	
	@SuppressWarnings("resource")
	public void handleUpdatePacket(@Nullable UUID sender, byte event) {
		switch(event) {
		case 0: {
			//CLEAR BOARD
			this.clearBoard();
			break;
		}
		
		case 1: {
			//BUILD BOARD
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
						
				buildBoard();
			}
			break;
		}
		case 2: {
			//offer draw
			if(this.tile.playing && this.tile.challenger.isPresent() && this.tile.challenged.isPresent()) {
				
				this.tile.challengerOfferedDraw = Optional.of(sender.equals(this.tile.challenger.get()));
				
				if(this.tile.getWorld().isRemote && !Minecraft.getInstance().player.getUniqueID().equals(sender)) {
					if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
						ChessBoardScreen screen = ((ChessBoardScreen)Minecraft.getInstance().currentScreen);
						PlayerEntity player = this.tile.getWorld().getPlayerByUuid(sender);
						if(player == null) return;
						
						screen.drawDisplay.active = true;
						screen.drawDisplay.visible = true;
						screen.drawDisplay.challenger = player.getName().getString();
					}
				}
			}
			break;
		}
		case 3: {
			//resign
			//only on server
			if(this.tile.getWorld().isRemote || !this.tile.challenger.isPresent() || !this.tile.isPlayingComputer && !this.tile.challenged.isPresent()) 
				return;
			
			this.tile.sendDefeated(LoseReason.RESIGN, sender);
			PacketHandler.sendToNearby(this.tile.getWorld(), this.tile.getPos(), new ServerChessBoardUpdatePacket(sender, (byte)8));
			break;
		}
		case 4: { 
			this.tile.challengerColor = PieceColor.WHITE;
			break;
		}
		case 5: {
			this.tile.challengerColor = PieceColor.BLACK;
			break;
		}
		case 6: {
			this.board.toPlay = PieceColor.WHITE;
			break;
		}
		case 7: {
			this.board.toPlay = PieceColor.BLACK;
			break;
		}
		case 8: {
			//stahp
			this.tile.stopPlaying();
			this.tile.isPlayingComputer = false;
			this.tile.challenged = Optional.empty();
			this.tile.challenger = Optional.empty();
			this.tile.challengerOfferedDraw = Optional.empty();
			if(!this.tile.getWorld().isRemote) this.tile.markDirty();
			break;
		}
		case 9: {
			//cancel challenge
			this.tile.waitingForChallenged = false;
			this.tile.challenger = Optional.empty();
			this.tile.stopPlaying();
			break;
		}
		case 10: {
			//challenge accepted
			this.tile.waitingForChallenged = false;
			this.tile.waitingForComputerMove = false;
			this.tile.isPlayingComputer = false;
			this.tile.playing = true;
			this.tile.challenged = Optional.of(sender);
			break;
		}
		case 11: {
			//update highlights
			if(this.tile.getWorld().isRemote && !sender.equals(Minecraft.getInstance().player.getUniqueID())) {
				if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
					((ChessBoardScreen)Minecraft.getInstance().currentScreen).updateSlotHighlights();
				}
			}
			
			break;
		}
		case 12: {
			//draw
			if(!this.tile.getWorld().isRemote) {
				PacketHandler.sendToNearby(this.tile.getWorld(), this.tile.getPos(), new ServerChessDrawPacket(DrawReason.ACCEPTED));
				}
			}
		
			break;
		}
	}

	public void buildBoard() {
		clearBoard();
		
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
	}
	
	@Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    	if(index < 64 || index >= 112) {
    		return ItemStack.EMPTY;
    	} else if(index < 76) {
    	      ItemStack itemstack = ItemStack.EMPTY;
    	      Slot slot = this.inventorySlots.get(index);
    	      
    	      if (slot != null && slot.getHasStack()) {
    	         ItemStack itemstack1 = slot.getStack();
    	         itemstack = itemstack1.copy();
    	         
    	         if (!this.mergeItemStack(itemstack1, 76, 112, true)) {
    	              return ItemStack.EMPTY;
    	         }

    	         if (itemstack1.isEmpty()) {
    	            slot.putStack(ItemStack.EMPTY);
    	         } else {
    	            slot.onSlotChanged();
    	         }
    	      }

    	      return itemstack;
    	      
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
				this.addSlot(new ChessBoardSquareSlot(this, j + i * 8, 22 + i * 18, 30 + j * 18, () -> mode == Mode.PLAYING && !this.tile.promotionColor.isPresent() || mode == Mode.BOARD_EDITOR));
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
