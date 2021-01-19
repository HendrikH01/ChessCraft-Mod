package com.xX_deadbush_Xx.chessmod.objects;

import com.xX_deadbush_Xx.chessmod.ChessMod;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRegistry {
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, ChessMod.MOD_ID);
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ChessMod.MOD_ID);
	public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ChessMod.MOD_ID);
	public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, ChessMod.MOD_ID);

	//BLOCKS
	public static final RegistryObject<Block> CHESS_BOARD = BLOCKS.register("chess_board", () -> new ChessBoardBlock(Block.Properties.create(Material.WOOD).notSolid().harvestLevel(1).hardnessAndResistance(1.0F, 5.0F).sound(SoundType.WOOD)));

	//ITEMS
	public static final RegistryObject<Item> WHITE_PAWN = ITEMS.register("white_pawn", () -> new ChessPiece(ChessPieceType.PAWN, PieceColor.WHITE, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> WHITE_ROOK = ITEMS.register("white_rook", () -> new ChessPiece(ChessPieceType.ROOK, PieceColor.WHITE, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> WHITE_BISHOP = ITEMS.register("white_bishop", () -> new ChessPiece(ChessPieceType.BISHOP, PieceColor.WHITE, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> WHITE_HORSEY = ITEMS.register("white_horsey", () -> new ChessPiece(ChessPieceType.HORSEY, PieceColor.WHITE, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> WHITE_QUEEN = ITEMS.register("white_queen", () -> new ChessPiece(ChessPieceType.QUEEN, PieceColor.WHITE, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> WHITE_KING = ITEMS.register("white_king", () -> new ChessPiece(ChessPieceType.KING, PieceColor.WHITE, new Item.Properties().group(ChessMod.GROUP)));
	
	public static final RegistryObject<Item> BLACK_PAWN = ITEMS.register("black_pawn", () -> new ChessPiece(ChessPieceType.PAWN, PieceColor.BLACK, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> BLACK_ROOK = ITEMS.register("black_rook", () -> new ChessPiece(ChessPieceType.ROOK, PieceColor.BLACK, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> BLACK_BISHOP = ITEMS.register("black_bishop", () -> new ChessPiece(ChessPieceType.BISHOP, PieceColor.BLACK, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> BLACK_HORSEY = ITEMS.register("black_horsey", () -> new ChessPiece(ChessPieceType.HORSEY, PieceColor.BLACK, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> BLACK_QUEEN = ITEMS.register("black_queen", () -> new ChessPiece(ChessPieceType.QUEEN, PieceColor.BLACK, new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> BLACK_KING = ITEMS.register("black_king", () -> new ChessPiece(ChessPieceType.KING, PieceColor.BLACK, new Item.Properties().group(ChessMod.GROUP)));
	
	public static final RegistryObject<Item> CHESS_BOARD_ITEM = ITEMS.register("chess_board_item", () -> new BlockItem(CHESS_BOARD.get(), new Item.Properties().group(ChessMod.GROUP)));
	public static final RegistryObject<Item> CARVING_KNIFE = ITEMS.register("carving_knife", () -> new Item(new Item.Properties().group(ChessMod.GROUP)));

	
	//TEs
	public static final RegistryObject<TileEntityType<ChessBoardTile>> CHESS_BOARD_TILE = TILES.register("chess_board_tile", () -> TileEntityType.Builder.create(ChessBoardTile::new, CHESS_BOARD.get()).build(null));

	//CONTAINERS
	public static final RegistryObject<ContainerType<ChessBoardContainer>> CHESS_BOARD_CONTAINER = CONTAINERS.register("chess_board_container", () -> IForgeContainerType.create(ChessBoardContainer::new));
}
