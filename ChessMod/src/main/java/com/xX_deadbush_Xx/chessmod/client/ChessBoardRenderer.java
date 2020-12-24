package com.xX_deadbush_Xx.chessmod.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoard;
import com.xX_deadbush_Xx.chessmod.objects.ChessBoardTile;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class ChessBoardRenderer extends TileEntityRenderer<ChessBoardTile> {

	private static final float W = 1.0f/16.0f*1.5f;
	
	public ChessBoardRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(ChessBoardTile tile, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ms.push();
		ms.rotate(Vector3f.YP.rotationDegrees(90));
		ms.translate(-1.0f, 0.0f, 0.0f);
		ms.translate(0.125f, 0.25f, 0.125f);

		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
		ChessBoard board = tile.getBoard();
		
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				ItemStack piece = board.getPieceAt(x, y);
				if(piece.isEmpty()) 
					continue;
				
				ms.push();
				ms.translate(x * W + 0.045, 0, y * W + 0.045);
				ms.scale(0.25f, 0.25f, 0.25f);
				
				if(((ChessPiece)piece.getItem()).color == PieceColor.BLACK)
					ms.rotate(Vector3f.YP.rotationDegrees(180));
				
				if(((ChessPiece)piece.getItem()).type == ChessPieceType.HORSEY) {
					ms.translate(0, 0, 0.04);
				}
				renderer.renderItem(piece, TransformType.FIXED, light, overlay, ms, buffer);
				ms.pop();
			}
		}
		
		ms.pop();
	}
}
