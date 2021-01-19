package com.xX_deadbush_Xx.chessmod.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoard;
import com.xX_deadbush_Xx.chessmod.network.ClientRequestTimePacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;
import com.xX_deadbush_Xx.chessmod.network.ServerChessDrawPacket.DrawReason;
import com.xX_deadbush_Xx.chessmod.network.ServerPlayerDefeatedPacket;
import com.xX_deadbush_Xx.chessmod.network.ServerPlayerDefeatedPacket.LoseReason;

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
	
	public boolean isPlayingComputer = false;
	public PieceColor challengerColor = PieceColor.WHITE;
	public boolean playing = false;
	public boolean waitingForPromotion = false;
	public boolean waitingForChallenged = false;
	public int computerPromotionPiece = -1;
	
	public ChessBoardTile() {
		super(ModRegistry.CHESS_BOARD_TILE.get());
		this.board = new ChessBoard();
	}
	
	@Override
	public void tick() {
		if(this.playing && !(this.waitingForPromotion || this.waitingForChallenged)) {
			if(challenger.isPresent() && (challenged.isPresent() || isPlayingComputer)) {
				if(this.challengerColor == this.board.toPlay) {
					if(challengerTime > 0)
						this.challengerTime--;
					else if(challengerTime == 0 && !this.world.isRemote) {
						this.playing = false;
						PacketHandler.sendToNearby(this.getWorld(), this.getPos(), new ServerPlayerDefeatedPacket(false, LoseReason.TIMEOUT));
					}
				} else if(!this.isPlayingComputer) {
					this.challengedTime--;
					if(challengedTime == 0 && !this.world.isRemote) {
						this.playing = false;
						PacketHandler.sendToNearby(this.getWorld(), this.getPos(), new ServerPlayerDefeatedPacket(true, LoseReason.TIMEOUT));
					}
				}
				
				if((this.challengedTime + this.challengerTime) % 10 == 5 && this.world.isRemote) {
					PacketHandler.sendToServer(this.getWorld(), new ClientRequestTimePacket());
				}
			} else {
				this.playing = false;
				System.out.println("Error: missing chesboard data");
			}
		} else if(this.playing && this.isPlayingComputer && this.waitingForPromotion) {
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
							computerPromotionPiece = -1;
							this.waitingForPromotion = false;
							return;
						}
					}
				}
			}
		}
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
		this.playing = false;

		
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
		
		this.challenged = Optional.empty();
		this.challenger = Optional.empty();
	}
	
	@SuppressWarnings("resource")
	public void endGameInDraw(DrawReason reason) {
		this.playing = false;
		this.challenged = Optional.empty();
		this.challenger = Optional.empty();
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
		if(compound.contains("promoting")) 
			this.waitingForPromotion = compound.getBoolean("promoting");
		if(compound.contains("waitingforchallenged")) 
			this.waitingForChallenged = compound.getBoolean("waitingforchallenged");
		if(compound.contains("challenger")) 
			this.challenger = Optional.of(NBTUtil.readUniqueId(compound.getCompound("challenger")));
		if(!isPlayingComputer && compound.contains("challenged")) 
			this.challenged = Optional.of(NBTUtil.readUniqueId(compound.getCompound("challenged")));
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
		compound.putBoolean("promoting", this.waitingForPromotion);
		compound.putBoolean("waitingforchallenged", this.waitingForChallenged);
		
		if (this.challenger.isPresent())
			compound.put("challenger", NBTUtil.writeUniqueId(this.challenger.get()));
		
		if (!isPlayingComputer && this.challenged.isPresent())
			compound.put("challenged", NBTUtil.writeUniqueId(this.challenged.get()));
		
		compound.putInt("challengertime", this.challengerTime);
		compound.putInt("challengedtime", this.challengedTime);
		compound.putInt("color", this.challengerColor.ordinal());
		compound.putInt("strength", this.computerStrength);

		return compound;
	}
	
	@Nullable
	@Override
	 public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT comp = new CompoundNBT();
		this.write(comp);
		return new SUpdateTileEntityPacket(this.pos, 69, comp); //nice
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.read(pkt.getNbtCompound());
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
	    CompoundNBT tag = new CompoundNBT();
	    this.write(tag);
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
}
