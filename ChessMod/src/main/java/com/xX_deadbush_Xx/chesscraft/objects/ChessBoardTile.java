package com.xX_deadbush_Xx.chesscraft.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xX_deadbush_Xx.chesscraft.ChessMod;
import com.xX_deadbush_Xx.chesscraft.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessEngineManager;
import com.xX_deadbush_Xx.chesscraft.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;
import com.xX_deadbush_Xx.chesscraft.game_logic.inventory.ChessBoard;
import com.xX_deadbush_Xx.chesscraft.network.ClientRequestTimePacket;
import com.xX_deadbush_Xx.chesscraft.network.PacketHandler;
import com.xX_deadbush_Xx.chesscraft.network.ServerChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chesscraft.network.ServerPlayerDefeatedPacket;
import com.xX_deadbush_Xx.chesscraft.network.ServerChessDrawPacket.DrawReason;
import com.xX_deadbush_Xx.chesscraft.network.ServerPlayerDefeatedPacket.LoseReason;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ChessBoardTile extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
	
	private ChessBoard board;
	private ItemStackHandler inventory = new ItemStackHandler(12);
	public Map<UUID, PieceColor> sides = new HashMap<>();
	public int computerStrength = 1;
	
	public Optional<UUID> challenger = Optional.empty();
	public int challengerTime = 0;
	
	public Optional<UUID> challenged = Optional.empty();
	public int challengedTime = 0;
	
	public Optional<Boolean> challengerOfferedDraw = Optional.empty();
	
	public Optional<PieceColor> promotionColor = Optional.empty();
	
	public boolean isPlayingComputer = false;
	public PieceColor challengerColor = PieceColor.WHITE;
	public boolean playing = false;
	public boolean waitingForChallenged = false;
	public boolean waitingForComputerMove = false;
	public int computerPromotionPiece = -1;
	
	public ChessBoardTile() {
		super(ModRegistry.CHESS_BOARD_TILE.get());
		this.board = new ChessBoard();
	}
	
	@Override
	public void tick() {		
		if(this.playing && !(this.promotionColor.isPresent() || this.waitingForChallenged)) {
			if(challenger.isPresent() && (challenged.isPresent() || isPlayingComputer)) {
				if(this.challengerColor == this.board.toPlay) {
					if(challengerTime > 0)
						this.challengerTime--;
					else if(challengerTime == 0 && !this.world.isRemote) {
						this.stopPlaying();
						if(!this.isPlayingComputer)
							PacketHandler.sendToNearby(this.getWorld(), this.getPos(), new ServerPlayerDefeatedPacket(false, LoseReason.TIMEOUT));
						else if(challenger.isPresent()) {
							PacketHandler.sendTo((ServerPlayerEntity) this.getWorld().getPlayerByUuid(challenger.get()), new ServerPlayerDefeatedPacket(false, LoseReason.TIMEOUT));
							PacketHandler.sendToNearby(this.getWorld(), this.getPos(), new ServerChessBoardUpdatePacket(null, (byte)8));
						}
					}
				} else if(!this.isPlayingComputer) {
					this.challengedTime--;
					if(challengedTime == 0 && !this.world.isRemote) {
						this.stopPlaying();
						PacketHandler.sendToNearby(this.getWorld(), this.getPos(), new ServerPlayerDefeatedPacket(true, LoseReason.TIMEOUT));
					}
				}
				
				if((this.challengedTime + this.challengerTime) % 10 == 5 && this.world.isRemote) {
					PacketHandler.sendToServer(this.getWorld(), new ClientRequestTimePacket());
				}
			} else {
				this.stopPlaying();
				ChessMod.LOGGER.warn("Error: missing chessboard data");
			}
		} else if(this.playing && this.isPlayingComputer && this.promotionColor.isPresent()) {
			//computer waiting for piece
			
			if(computerPromotionPiece > 0 && computerPromotionPiece < 5) {
				PieceColor color = this.challengerColor.getOpposite();
				ChessPieceType type = ChessPieceType.values()[this.computerPromotionPiece];
				
				if(this.inventory.getStackInSlot(color.ordinal() * 6 + type.ordinal()).isEmpty()) {
					ItemStack promoted = this.getInventory().getStackInSlot(color.ordinal() * 6 + type.ordinal()).copy();
					this.getInventory().getStackInSlot(color.ordinal() * 6 + type.ordinal()).shrink(1);
					promoted.setCount(1);
					
					for(int i = 0; i < 8; i++) {
						if(((ChessPiece)this.board.getPieceAt(i, color == PieceColor.WHITE ? 7 : 0).getItem()).type == ChessPieceType.PAWN) {
							this.board.setStackInSlot(i * 8 + (color == PieceColor.WHITE ? 7 : 0), promoted);
							this.computerPromotionPiece = -1;
							this.promotionColor = Optional.empty();
							return;
						}
					}
				}
			}
		}
		
		//computermove
		if(this.world.isRemote && this.playing && this.isPlayingComputer && !(this.promotionColor.isPresent() || this.waitingForComputerMove)) {
			if(this.challengerColor != this.board.toPlay && this.challenger.isPresent() && this.challenger.get().equals(Minecraft.getInstance().player.getUniqueID())) {
				
				if(Minecraft.getInstance().player.openContainer instanceof ChessBoardContainer && Minecraft.getInstance().player.openContainer != null) {

					this.waitingForComputerMove = true;

					((ChessBoardContainer)Minecraft.getInstance().player.openContainer).makeComputerMove(new Runnable() {

						@Override
						public void run() {
							ChessBoardTile.this.board.toPlay = ChessBoardTile.this.challengerColor;
						}
					});
				}
			}
		}
	}
	
	public void stopPlaying() {
		this.playing = false;
	}
	
	public void stopWaiting() {
		this.waitingForComputerMove = false;
	}
	
	public boolean isPlaying(UUID player) {
		return this.challenger.isPresent() && player.equals(this.challenger.get()) || 
				this.challenged.isPresent() && player.equals(this.challenged.get());
	}
	
	public void sendDefeated(LoseReason reason, UUID loser) {
		if(!this.world.isRemote && this.challenger.isPresent() && this.challenger.get().equals(loser)) {
			if(this.challenged.isPresent())
				PacketHandler.sendTo((ServerPlayerEntity) this.world.getPlayerByUuid(this.challenged.get()), new ServerPlayerDefeatedPacket(false, LoseReason.RESIGN));
			
			PacketHandler.sendTo((ServerPlayerEntity) this.world.getPlayerByUuid(this.challenger.get()), new ServerPlayerDefeatedPacket(false, LoseReason.RESIGN));
			
			this.endGameInWin(false, reason);
		}
		
		if(!this.world.isRemote && this.challenger.isPresent() && this.challenged.isPresent() && this.challenged.get().equals(loser)) {
			
			PacketHandler.sendTo((ServerPlayerEntity) this.world.getPlayerByUuid(this.challenger.get()), new ServerPlayerDefeatedPacket(true, reason));
			
			if(this.challenged.isPresent())
				PacketHandler.sendTo((ServerPlayerEntity) this.world.getPlayerByUuid(this.challenged.get()), new ServerPlayerDefeatedPacket(true, reason));
			
			this.endGameInWin(true, reason);
		}
	}
	
	@SuppressWarnings("resource")
	public void endGameInWin(boolean challengerWon, LoseReason reason) {
		stopPlaying();
		this.isPlayingComputer = false;
		if(reason == LoseReason.TIMEOUT) {
			if(challengerWon) {
				this.challengerTime = 0;
			} else {
				this.challengedTime = 0;
			}
		}
		
		try {
			if(this.world.isRemote) {
				if(Minecraft.getInstance().currentScreen instanceof ChessBoardScreen && Minecraft.getInstance().currentScreen != null) {
					ChessBoardScreen screen = (ChessBoardScreen) Minecraft.getInstance().currentScreen;
					screen.gameEndDisplay.active = true;
					screen.gameEndDisplay.visible = true;
					screen.gameEndDisplay.title = challengerWon ^ Minecraft.getInstance().player.getUniqueID().equals(this.challenger.get()) ? "YOU WON" : "YOU LOST";					
					screen.gameEndDisplay.reason = challengerWon ^ Minecraft.getInstance().player.getUniqueID().equals(this.challenger.get()) ? reason.winnerMessage() : reason.loserMessage();
					screen.gameEndDisplay.winningColor = challengerWon ? this.challengerColor : this.challengerColor.getOpposite();
				}
			}
		} catch(NoSuchElementException e) {
			e.printStackTrace();
		}
		
		this.challenged = Optional.empty();
		this.challenger = Optional.empty();
		this.challengerOfferedDraw = Optional.empty();
		this.stopWaiting();
		this.waitingForChallenged = false;
		this.promotionColor = Optional.empty();
	}
	
	@SuppressWarnings("resource")
	public void endGameInDraw(DrawReason reason) {
		stopPlaying();
		this.isPlayingComputer = false;
		
		if(this.world.isRemote) {
			if(Minecraft.getInstance().currentScreen instanceof ChessBoardScreen && Minecraft.getInstance().currentScreen != null) {
				ChessBoardScreen screen = (ChessBoardScreen) Minecraft.getInstance().currentScreen;
				screen.gameEndDisplay.active = true;
				screen.gameEndDisplay.visible = true;
				screen.gameEndDisplay.title =  "DRAW";
				screen.gameEndDisplay.reason = reason.message();
			}
		}
		
		this.challenged = Optional.empty();
		this.challenger = Optional.empty();
		this.challengerOfferedDraw = Optional.empty();
		this.stopWaiting();
		this.waitingForChallenged = false;
		this.promotionColor = Optional.empty();
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		if(compound.contains("inventory")) 
			this.inventory.deserializeNBT(compound.getCompound("inventory"));
		if(compound.contains("board")) 
			this.board.readCompound(compound.getCompound("board"));
		if(compound.contains("playing")) 
			this.playing = compound.getBoolean("playing");
		if(compound.contains("playingcomputer")) 
			this.isPlayingComputer = compound.getBoolean("playingcomputer");
		if(compound.contains("waitingforchallenged")) 
			this.waitingForChallenged = compound.getBoolean("waitingforchallenged");
		
		if(compound.contains("challenger")) 
			this.challenger = Optional.of(NBTUtil.readUniqueId(compound.getCompound("challenger")));
		
		if(!isPlayingComputer && compound.contains("challenged")) 
			this.challenged = Optional.of(NBTUtil.readUniqueId(compound.getCompound("challenged")));
		
		if(compound.contains("promoting")) 
			this.promotionColor = Optional.of(compound.getBoolean("promoting") ? PieceColor.WHITE : PieceColor.BLACK);
		else this.promotionColor = Optional.empty();
		
		if(compound.contains("challengertime")) 
			this.challengerTime = compound.getInt("challengertime");
		if(compound.contains("challengedtime")) 
			this.challengedTime = compound.getInt("challengedtime");
		if(compound.contains("color")) 
			this.challengerColor = PieceColor.values()[compound.getInt("color")];
		if(compound.contains("strength")) 
			this.computerStrength = compound.getInt("strength");
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.put("inventory", this.inventory.serializeNBT());
		compound.put("board", this.board.getCompound());
		compound.putBoolean("playingcomputer", this.isPlayingComputer);
		compound.putBoolean("playing", this.playing );
		compound.putBoolean("waitingforchallenged", this.waitingForChallenged);

		if (this.challenger.isPresent())
			compound.put("challenger", NBTUtil.writeUniqueId(this.challenger.get()));
		
		if (isPlayingComputer && this.challenged.isPresent())
			compound.put("challenged", NBTUtil.writeUniqueId(this.challenged.get()));
		
		if (this.promotionColor.isPresent())
			compound.putBoolean("promoting", this.promotionColor.get() == PieceColor.WHITE);
		
		compound.putInt("challengertime", this.challengerTime);
		compound.putInt("challengedtime", this.challengedTime);
		compound.putInt("color", this.challengerColor.ordinal());
		compound.putInt("strength", this.computerStrength);

		return compound;
	}
	
	@Nullable
	@Override
	 public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT tag = new CompoundNBT();
		this.write(tag);
		
		if (this.challengerOfferedDraw.isPresent())
			tag.putBoolean("offeredDraw", this.challengerOfferedDraw.get());
		
		return new SUpdateTileEntityPacket(this.pos, 69, tag); //nice
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.read(pkt.getNbtCompound());
		
		if(pkt.getNbtCompound().contains("offeredDraw")) 
			this.challengerOfferedDraw = Optional.of(pkt.getNbtCompound().getBoolean("offeredDraw"));
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
	    CompoundNBT tag = new CompoundNBT();
	    this.write(tag);
	    
		if (this.challengerOfferedDraw.isPresent())
			tag.putBoolean("offeredDraw", this.challengerOfferedDraw.get());
		
	    return tag;
	}
	
	@Override
	public void markDirty() {
		if(this.world != null) { //prevents crash on structure generation
			this.world.markBlockRangeForRenderUpdate(this.pos, this.getBlockState(), this.getBlockState());
			this.world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
		}
		
		super.markDirty();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> inventory));
	}
	
	
	public ChessBoard getBoard() {
		return board;
	}
	
	public IItemHandler getInventory() {
		return this.inventory;
	}
	
	@Override
	public Container createMenu(int id, PlayerInventory player, PlayerEntity playerEntity) {
		return new ChessBoardContainer(id, player, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Chessboard");
	}

	public List<ItemStack> getInventoryContents() {
		List<ItemStack> list = new ArrayList<>();
		for(int i = 0; i < 64; i++) {
			list.add(this.board.getStackInSlot(i));
		}
		
		for(int i = 0; i < this.inventory.getSlots(); i++) {
			list.add(this.inventory.getStackInSlot(i));
		}
		
		return list;
	}
}
