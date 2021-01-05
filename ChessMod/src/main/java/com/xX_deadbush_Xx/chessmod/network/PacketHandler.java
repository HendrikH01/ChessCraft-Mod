package com.xX_deadbush_Xx.chessmod.network;

import com.xX_deadbush_Xx.chessmod.ChessMod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1.0";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(ChessMod.MOD_ID, "main"), 
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public static void registerPackets() {
		int id = 0;
		//CLIENT TO SERVER
		INSTANCE.registerMessage(id++, ClientSetChessBoardTabPacket.class, ClientSetChessBoardTabPacket::encode, ClientSetChessBoardTabPacket::decode, ClientSetChessBoardTabPacket::handle);
		INSTANCE.registerMessage(id++, ClientChessBoardUpdatePacket.class, ClientChessBoardUpdatePacket::encode, ClientChessBoardUpdatePacket::decode, ClientChessBoardUpdatePacket::handle);
		INSTANCE.registerMessage(id++, ClientSetCastlingOptionPacket.class, ClientSetCastlingOptionPacket::encode, ClientSetCastlingOptionPacket::decode, ClientSetCastlingOptionPacket::handle);

		//SERVER TO CLIENT
		INSTANCE.registerMessage(id++, ServerSetChessBoardTabPacket.class, ServerSetChessBoardTabPacket::encode, ServerSetChessBoardTabPacket::decode, ServerSetChessBoardTabPacket::handle);
		INSTANCE.registerMessage(id++, ServerChessBoardUpdatePacket.class, ServerChessBoardUpdatePacket::encode, ServerChessBoardUpdatePacket::decode, ServerChessBoardUpdatePacket::handle);
		INSTANCE.registerMessage(id++, ServerMovePiecePacket.class, ServerMovePiecePacket::encode, ServerMovePiecePacket::decode, ServerMovePiecePacket::handle);
		INSTANCE.registerMessage(id++, ServerSetCastlingOptionPacket.class, ServerSetCastlingOptionPacket::encode, ServerSetCastlingOptionPacket::decode, ServerSetCastlingOptionPacket::handle);

	}

	public static void sendToNearby(World worldIn, BlockPos pos, Object toSend) {
		sendToWithinRadius(worldIn, pos, 64, toSend);
	}

	public static void sendToNearby(World worldIn, Entity entity, Object toSend) {
		sendToNearby(worldIn, new BlockPos(entity), toSend);
	}

	public static void sendToAll(World worldIn, Object toSend) {
		INSTANCE.send(PacketDistributor.ALL.noArg(), toSend);
	}

	@SuppressWarnings("resource")
	public static void sendToWithinRadius(World worldIn, BlockPos pos, double radius, Object toSend) {
		if (worldIn instanceof ServerWorld) {
			ServerWorld serverworld = (ServerWorld) worldIn;

			serverworld.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false)
					.filter(p -> p.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < radius * radius)
					.forEach(p -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), toSend));
		}
	}

	public static void sendTo(ServerPlayerEntity playerMP, Object toSend) {
		INSTANCE.sendTo(toSend, playerMP.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToServer(World worldIn, Object toSend) {
		INSTANCE.send(PacketDistributor.SERVER.noArg(), toSend);
	}
}
