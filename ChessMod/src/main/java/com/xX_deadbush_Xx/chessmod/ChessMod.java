package com.xX_deadbush_Xx.chessmod;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import com.xX_deadbush_Xx.chessmod.client.ChessBoardRenderer;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessEngineManager;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;
import com.xX_deadbush_Xx.chessmod.objects.ChessBoardTile;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;
import com.xX_deadbush_Xx.chessmod.objects.ModRecipes;
import com.xX_deadbush_Xx.chessmod.objects.ModRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("chessmod")
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = ChessMod.MOD_ID)
public class ChessMod {
	public static final String MOD_ID = "chessmod";
	public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
	public static final ItemGroup GROUP = new ItemGroup(ItemGroup.GROUPS.length, MOD_ID + "_blocks") {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ModRegistry.WHITE_PAWN.get());
		}
	};

	public ChessMod() throws IOException {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonsetup);
		bus.addListener(this::clientsetup);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		ModRegistry.SOUNDS.register(bus);
		ModRegistry.ITEMS.register(bus);
		ModRegistry.BLOCKS.register(bus);
		ModRecipes.RECIPES.register(bus);
		ModRegistry.CONTAINERS.register(bus);
		ModRegistry.TILES.register(bus);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void commonsetup(final FMLCommonSetupEvent event) {
		PacketHandler.registerPackets();
		ChessEngineManager.init();
	}

	private void clientsetup(final FMLClientSetupEvent event) {
    	ScreenManager.registerFactory(ModRegistry.CHESS_BOARD_CONTAINER.get(), ChessBoardScreen::new);
		ClientRegistry.bindTileEntityRenderer(ModRegistry.CHESS_BOARD_TILE.get(), ChessBoardRenderer::new);
		
		Minecraft.getInstance().getItemColors().register((stack, light) -> {
			int i = ChessPiece.getColorFromStack(stack);
			if(i > 0)
				return i;
			else return ((ChessPiece)stack.getItem()).color == PieceColor.WHITE ? 0xD3B481 : 0x604228;
		},
				ModRegistry.WHITE_PAWN.get(), ModRegistry.WHITE_ROOK.get(), ModRegistry.WHITE_BISHOP.get(),
				ModRegistry.WHITE_HORSEY.get(), ModRegistry.WHITE_QUEEN.get(), ModRegistry.WHITE_KING.get(),
				ModRegistry.BLACK_PAWN.get(), ModRegistry.BLACK_ROOK.get(), ModRegistry.BLACK_BISHOP.get(),
				ModRegistry.BLACK_HORSEY.get(), ModRegistry.BLACK_QUEEN.get(), ModRegistry.BLACK_KING.get());
	}
	
	@SubscribeEvent
	public static void playerJoin(EntityJoinWorldEvent event) {
		if(event.getEntity() instanceof PlayerEntity) {
			event.getWorld().loadedTileEntityList.forEach(tile -> {
				if(tile instanceof ChessBoardTile) {
					ChessBoardTile te = (ChessBoardTile) tile;
					LOGGER.debug(String.format("[%s] Chess board data at %d, %d, %d {to play: %s, challenger color: %s, challenger time: %d, challenged time: %d, playing: %b, computer game: %b, offered draw: %b, offered challenge: %b}", 
							te.getWorld().isRemote ? "client" : "server", te.getPos().getX(), te.getPos().getX(), te.getPos().getX(), te.getBoard().toPlay.toString(), te.challengerColor.toString(), te.challengerTime, te.challengedTime,
							te.playing, te.isPlayingComputer, te.waitingForChallenged, te.challengerOfferedDraw));
				}
			});
		}
	}
}