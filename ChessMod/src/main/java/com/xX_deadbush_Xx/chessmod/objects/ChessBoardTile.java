package com.xX_deadbush_Xx.chessmod.objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoard;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
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
	
	public ChessBoardTile() {
		super(ModRegistry.CHESS_BOARD_TILE.get());
		this.board = new ChessBoard();
	}
	
	@Override
	public void tick() {
		
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		if(compound.contains("inventory")) {
			this.inventory.deserializeNBT(compound.getCompound("inventory"));
		}
		
		if(compound.contains("board")) {
			this.board.readCompound(compound.getCompound("board"));
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.put("inventory", this.inventory.serializeNBT());
		compound.put("board", this.board.getCompound());

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
