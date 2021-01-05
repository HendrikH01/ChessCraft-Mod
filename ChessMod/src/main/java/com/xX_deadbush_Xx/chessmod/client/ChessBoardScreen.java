package com.xX_deadbush_Xx.chessmod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chessmod.ChessMod;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer.Mode;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoardSquareSlot;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoardSquareSlot.HighlightMode;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessPieceStorageSlot;
import com.xX_deadbush_Xx.chessmod.network.ClientChessBoardUpdatePacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChessBoardScreen extends ContainerScreen<ChessBoardContainer> {
	public static final ResourceLocation TEX_ICONS = new ResourceLocation(ChessMod.MOD_ID, "textures/gui/icons.png");
	public static final ResourceLocation TEX_INV = new ResourceLocation(ChessMod.MOD_ID, "textures/gui/inventory.png");
	public static final ResourceLocation TEX_PIECES = new ResourceLocation(ChessMod.MOD_ID, "textures/gui/pieces_atlas.png");
	private CastleOptionButton[] castlebuttons;
	private ClearBoardButton clearButton;
	private BuildBoardButton buildButton;
	private FlipBoardButton flipbutton;
	private PieceColor side = PieceColor.WHITE;
	
	public ChessBoardScreen(ChessBoardContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.ySize = 210;
		this.xSize = 256;
		this.guiTop = 20;
		this.guiLeft = 110;
	}

	@SuppressWarnings("resource")
	@Override
	protected void init() {
		this.addButton(new ChessBoardGuiSwitch(this.guiLeft, this.guiTop, 0));
		this.addButton(new ChessBoardGuiSwitch(this.guiLeft + 64, this.guiTop, 1));
		this.addButton(new ChessBoardGuiSwitch(this.guiLeft + 128, this.guiTop, 2));
		this.addButton(new ChessBoardGuiSwitch(this.guiLeft + 192, this.guiTop, 3));

		this.castlebuttons = new CastleOptionButton[] {		

			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 201, this.guiTop + 29, 0)),
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 211, this.guiTop + 29, 1)),
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 201, this.guiTop + 39, 2)),
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 211, this.guiTop + 39, 3)),
		};
		
		this.clearButton = this.addButton(new ClearBoardButton(this.guiLeft + 181, this.guiTop + 29, (button) -> {	
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 0));
		}));
		
		this.buildButton = this.addButton(new BuildBoardButton(this.guiLeft + 181, this.guiTop + 49, (button) -> {	
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientChessBoardUpdatePacket((byte) 1));
		}));
		
		this.flipbutton = this.addButton(new FlipBoardButton(this.guiLeft + 180, this.guiTop + 29, (button) -> {	
			this.updateMode(this.container.getMode());
			
			this.side = this.side.getOpposite();
		}));
		
		this.updateMode(Mode.PLAYING);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void updateSlotHighlights() {
		for(int i = 0; i < 64; i++)
			((ChessBoardSquareSlot) this.container.inventorySlots.get(i)).highlight = Optional.empty();

		if(this.container.selectedSlot != -1) {
			ChessHelper.getLegalMoves(this.container.getBoard(), list -> {
				for(Pair<Integer, Integer> move : list) {
					if(move.getFirst() == this.container.selectedSlot) {
						ChessBoardSquareSlot slot = (ChessBoardSquareSlot) this.container.inventorySlots.get(this.side == PieceColor.WHITE ? move.getSecond() : ChessHelper.getX(move.getSecond()) * 8 + 7 - ChessHelper.getY(move.getSecond()));
						ChessBoardSquareSlot reversed = (ChessBoardSquareSlot) this.container.inventorySlots.get(move.getSecond());

						if(reversed.getHasStack()) {
							slot.mark(HighlightMode.ATTACKING);
						}
						else {
							slot.mark(HighlightMode.VISIBLE);
						}
					}
				}
			});
		}
	}
	
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.minecraft.getTextureManager().bindTexture(this.container.getMode().texture);

		int left = this.guiLeft;
		int top = this.guiTop;
		this.blit(left, top, 0, 0, this.xSize, this.ySize);
		
		if(this.container.getMode() == Mode.PLAYING) {
			this.minecraft.getTextureManager().bindTexture(TEX_ICONS);
			
			//slots
			List<Pair<Integer, Integer>> attackedSquares = new ArrayList<>();
			
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
			
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					ChessBoardSquareSlot slot = (ChessBoardSquareSlot) this.container.inventorySlots.get(i * 8 + j);
					if(slot.highlight.isPresent()) {
						if(slot.highlight.get() == HighlightMode.VISIBLE) {
							blit(this.guiLeft + i * 18 + 22, this.guiTop + j * 18 + 30, 0, 37, 16, 16);
						} else if(slot.highlight.get() == HighlightMode.ATTACKING) {
							attackedSquares.add(Pair.of(i, j));
						}
					}
				}
			}
			
			RenderSystem.disableBlend();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	
			for(Pair<Integer, Integer> s : attackedSquares) {
				drawSlotHighlight(s.getFirst(), s.getSecond(), HighlightMode.ATTACKING.color);
			}
			
			if(this.container.getCheckedSquare() != -1) {
				drawSlotHighlight(ChessHelper.getX(this.container.getCheckedSquare()), ChessHelper.getY(this.container.getCheckedSquare()), HighlightMode.IN_CHECK.color);

			}
			
			if(this.container.selectedSlot != -1) {
				drawSlotHighlight(ChessHelper.getX(this.container.selectedSlot), 
						ChessHelper.getY(this.side == PieceColor.WHITE ? this.container.selectedSlot : 
							ChessHelper.getX(this.container.selectedSlot) * 8 + 7 - ChessHelper.getY(this.container.selectedSlot)), 0x8fE5D400);
			}
		}
	}
	
	private void drawSlotHighlight(int slotx, int sloty, int color) {
		int x = this.guiLeft + slotx * 18 + 21;
		int y = this.guiTop + sloty * 18 + 29;
		fill(x, y, x + 18, y + 18, color);
	}
	
	@Override
	protected void drawSlot(Slot slotIn) {
		if (slotIn instanceof ChessBoardSquareSlot || (slotIn instanceof ChessPieceStorageSlot && slotIn.isEnabled() && this.container.getMode() == Mode.BOARD_EDITOR)) {
			 
			int x = slotIn.xPos;
			int y = (this.container.getMode() == Mode.PLAYING && this.side == PieceColor.BLACK) ? 186 - slotIn.yPos : slotIn.yPos;
			ItemStack stack = slotIn.getStack();
			
			if(stack.isEmpty()) return;
			
			ChessPiece piece = (ChessPiece) stack.getItem();
			int colorIndex = ChessPiece.getColorIndex(stack);
			
			int textureY = piece.color == PieceColor.BLACK && colorIndex == -1 ? 16 : 32;
			int textureX = 16 * piece.type.ordinal();	
			textureY += colorIndex * 16;
			
			this.minecraft.getTextureManager().bindTexture(TEX_PIECES);
		    blit(x, y, 0, (float)textureX, (float)textureY, 16, 16, 512, 512);
		} else
			super.drawSlot(slotIn);
	}
	
	public void updateMode(Mode mode) {
		setButtonEnabled(this.clearButton, mode == Mode.BOARD_EDITOR);
		setButtonEnabled(this.buildButton, mode == Mode.BOARD_EDITOR);
		setButtonEnabled(this.flipbutton, mode == Mode.PLAYING);

		for(CastleOptionButton b : this.castlebuttons) {
			setButtonEnabled(b, mode == Mode.BOARD_EDITOR);
		}
	}
	
	private void setButtonEnabled(Button button, boolean enabled) {
		button.active = enabled;
		button.visible = enabled;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialticks) {
		this.renderBackground();
		
		if (this.container.getMode() == Mode.BOARD_EDITOR || this.container.getMode() == Mode.PLAYING) {
			int left = this.guiLeft;
			int top = this.guiTop;
			this.drawGuiContainerBackgroundLayer(partialticks, mouseX, mouseY);

			RenderSystem.disableRescaleNormal();
			RenderSystem.disableDepthTest();
			
			for (int i = 0; i < this.buttons.size(); ++i) {
				this.buttons.get(i).render(mouseX, mouseY, partialticks);
			}
			
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float) left, (float) top, 0.0F);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableRescaleNormal();
			this.hoveredSlot = null;

			RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

			for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
				Slot slot = this.container.inventorySlots.get(i);
				if (slot.isEnabled()) {
					this.drawSlot(slot);
				}

				if (this.isSlotSelected(slot, (double) mouseX, (double) mouseY) && slot.isEnabled()) {
					this.hoveredSlot = slot;
					RenderSystem.disableDepthTest();
					int x = slot.xPos;
					int y = slot.yPos;
					RenderSystem.colorMask(true, true, true, false);
					int slotColor = this.getSlotColor(i);
					this.fillGradient(x, y, x + 16, y + 16, slotColor, slotColor);
					RenderSystem.colorMask(true, true, true, true);
					RenderSystem.enableDepthTest();
				}
			}

			this.drawGuiContainerForegroundLayer(mouseX, mouseY);

			PlayerInventory playerinventory = this.minecraft.player.inventory;
			ItemStack stack = playerinventory.getItemStack();
			if (!stack.isEmpty()) {
				ChessPiece piece = (ChessPiece) stack.getItem();
				int colorIndex = ChessPiece.getColorIndex(stack);
				
				int textureY = piece.color == PieceColor.BLACK && colorIndex == -1 ? 16 : 32;
				int textureX = 16 * piece.type.ordinal();
				textureY += colorIndex * 16;
				
				this.minecraft.getTextureManager().bindTexture(TEX_PIECES);
			    blit(mouseX - left - 8, mouseY - top - 8, 0, (float)textureX, (float)textureY, 16, 16, 512, 512);
			}

			if (!this.returningStack.isEmpty()) {
				float f = (float) (Util.milliTime() - this.returningStackTime) / 100.0F;
				if (f >= 1.0F) {
					f = 1.0F;
					this.returningStack = ItemStack.EMPTY;
				}

				int l2 = this.returningStackDestSlot.xPos - this.touchUpX;
				int i3 = this.returningStackDestSlot.yPos - this.touchUpY;
				int l1 = this.touchUpX + (int) ((float) l2 * f);
				int i2 = this.touchUpY + (int) ((float) i3 * f);
				this.drawItemStack(this.returningStack, l1, i2, (String) null);
			}

			RenderSystem.popMatrix();
			RenderSystem.enableDepthTest();
		} else {
			super.render(mouseX, mouseY, partialticks);
		}
		
		if(!(hoveredSlot instanceof ChessBoardSquareSlot))
			this.renderHoveredToolTip(mouseX, mouseY);
	}

	public PieceColor getSide() {
		return this.side ;
	}
}
