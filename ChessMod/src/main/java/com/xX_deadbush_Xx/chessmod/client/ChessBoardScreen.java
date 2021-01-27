package com.xX_deadbush_Xx.chessmod.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chessmod.ChessMod;
import com.xX_deadbush_Xx.chessmod.Config;
import com.xX_deadbush_Xx.chessmod.client.widgets.BuildBoardButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.CastleOptionButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.ChallengeDisplay;
import com.xX_deadbush_Xx.chessmod.client.widgets.ChallengerColorButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.ChessBoardGuiSwitch;
import com.xX_deadbush_Xx.chessmod.client.widgets.ClearBoardButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.ComputerStrengthSlider;
import com.xX_deadbush_Xx.chessmod.client.widgets.DebugButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.DrawOfferDisplay;
import com.xX_deadbush_Xx.chessmod.client.widgets.ErrorMessageDisplay;
import com.xX_deadbush_Xx.chessmod.client.widgets.FlipBoardButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.GameEndMessageDisplay;
import com.xX_deadbush_Xx.chessmod.client.widgets.HintButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.OfferDrawButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.PlayButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.PlayComputerButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.PlayingColorButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.PromotionButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.ResignButton;
import com.xX_deadbush_Xx.chessmod.client.widgets.TimeDisplayWidget;
import com.xX_deadbush_Xx.chessmod.client.widgets.TimeLimitSlider;
import com.xX_deadbush_Xx.chessmod.client.widgets.ToggleOptionButton;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer.Mode;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessEngineManager;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoardSquareSlot;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoardSquareSlot.HighlightMode;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessPieceStorageSlot;
import com.xX_deadbush_Xx.chessmod.network.ClientSetSidePacket;
import com.xX_deadbush_Xx.chessmod.network.PacketHandler;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;

public class ChessBoardScreen extends ContainerScreen<ChessBoardContainer> {
	public static final ResourceLocation TEX_ICONS = new ResourceLocation(ChessMod.MOD_ID, "textures/gui/icons.png");
	public static final ResourceLocation TEX_INV = new ResourceLocation(ChessMod.MOD_ID, "textures/gui/inventory.png");
	public static final ResourceLocation TEX_PIECES = new ResourceLocation(ChessMod.MOD_ID, "textures/gui/pieces_atlas.png");
	
	private static final Map<Class<? extends Widget>, Function<Widget, List<String>>> buttonTooltips = new HashMap<>();
	
	private CastleOptionButton[] castlebuttons;
	private PromotionButton[] promtionbuttons;
	private ToggleOptionButton[] options;
	private ClearBoardButton clearButton;
	private BuildBoardButton buildButton;
	private FlipBoardButton flipbutton;

	private PlayButton playButton;
	private ResignButton resignButton;
	private OfferDrawButton drawButton;
	private HintButton hintButton;
	private PlayComputerButton playComputerButton;
	private PlayingColorButton playingColorButton;
	private ChallengerColorButton challengerColorButton;
	
	private ComputerStrengthSlider computerStrengthSlider;
	private ErrorMessageDisplay errorMessageDisplay;
	private TimeDisplayWidget timeDisplaySelf;
	private TimeDisplayWidget timeDisplayOpponent;
	
	private Optional<Pair<Integer, Integer>> hintmove = Optional.empty();
	private PieceColor side = PieceColor.WHITE;
	
	public ChallengeDisplay challengeDisplay;
	public GameEndMessageDisplay gameEndDisplay;
	public DrawOfferDisplay drawDisplay;
	public TimeLimitSlider timeSlider;

	private DebugButton debug;
	
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
		this.addButton(new ChessBoardGuiSwitch(this.container, this.guiLeft, this.guiTop, 0));
		this.addButton(new ChessBoardGuiSwitch(this.container, this.guiLeft + 64, this.guiTop, 1));
		this.addButton(new ChessBoardGuiSwitch(this.container, this.guiLeft + 128, this.guiTop, 2));
		this.addButton(new ChessBoardGuiSwitch(this.container, this.guiLeft + 192, this.guiTop, 3));

		this.castlebuttons = new CastleOptionButton[] {		
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 201, this.guiTop + 29, 0)),
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 211, this.guiTop + 29, 1)),
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 201, this.guiTop + 39, 2)),
			this.addButton(new CastleOptionButton(this.container, this.guiLeft + 211, this.guiTop + 39, 3)),
		};
		
		this.promtionbuttons = new PromotionButton[] {		
			this.addButton(new PromotionButton(this.container, this.guiLeft + 53, this.guiTop + 91, ChessPieceType.BISHOP)),
			this.addButton(new PromotionButton(this.container, this.guiLeft + 73, this.guiTop + 91, ChessPieceType.HORSEY)),
			this.addButton(new PromotionButton(this.container, this.guiLeft + 93, this.guiTop + 91, ChessPieceType.ROOK)),
			this.addButton(new PromotionButton(this.container, this.guiLeft + 113, this.guiTop + 91, ChessPieceType.QUEEN)),
		};
		
		this.options = new ToggleOptionButton[] {
				this.addButton(new ToggleOptionButton(this.guiLeft + 30, this.guiTop + 102, "Coordinates", (b) -> {
					//coordinates
					((ToggleOptionButton)b).yes = !((ToggleOptionButton)b).yes;
				})),
				this.addButton(new ToggleOptionButton(this.guiLeft + 138, this.guiTop + 102, "Highlights", (b) -> {
					//highlights
					((ToggleOptionButton)b).yes = !((ToggleOptionButton)b).yes;
					this.updateSlotHighlights();
				})),
			};
		
		this.timeDisplaySelf = addButton(new TimeDisplayWidget(this.container, false, this.guiLeft + 180, this.guiTop + 29));
		this.timeDisplayOpponent = addButton(new TimeDisplayWidget(this.container, true, this.guiLeft + 180, this.guiTop + 149));
		this.clearButton = this.addButton(new ClearBoardButton(this.guiLeft + 181, this.guiTop + 29));
		this.playingColorButton = this.addButton(new PlayingColorButton(this.container, this.guiLeft + 221, this.guiTop + 29));
		this.challengerColorButton = this.addButton(new ChallengerColorButton(this.container, this.guiLeft + 221, this.guiTop + 46));
		this.buildButton = this.addButton(new BuildBoardButton(this.guiLeft + 181, this.guiTop + 49));
		this.playButton = this.addButton(new PlayButton(this.container, this.guiLeft + 180, this.guiTop + 81));
		this.resignButton = this.addButton(new ResignButton(this.container, this.guiLeft + 200, this.guiTop + 81));
		this.drawButton = this.addButton(new OfferDrawButton(this.container, this.guiLeft + 220, this.guiTop + 81 ));
		this.playComputerButton = this.addButton(new PlayComputerButton(this.container, this.guiLeft + 180, this.guiTop + 101));
		this.hintButton = this.addButton(new HintButton(this.container, this.guiLeft + 220, this.guiTop + 101 , (button) -> {
			if(Config.COMMON.allowHints.get() && (this.container.tile.isPlayingComputer || !this.container.tile.playing)) {
				if(this.hintmove.isPresent()) {
					this.hintmove = Optional.empty();
				} else {
					ChessEngineManager.getNextMove(this.container.getBoard().getFEN(this.container.getBoard().toPlay), -1, result -> {
						if(result.equals("(none)")) return;
	
						int f = ChessHelper.convertNumberFormat(result.substring(0, 2));
						int s = ChessHelper.convertNumberFormat(result.substring(2, 4));
						
						this.hintmove = Optional.of(Pair.of(f,  s));
					});
				}
			}
		}));
		
		this.gameEndDisplay = addButton(new GameEndMessageDisplay(this.guiLeft + 63, this.guiTop + 70));
		this.challengeDisplay = addButton(new ChallengeDisplay(this.guiLeft + 63, this.guiTop + 70));
		this.drawDisplay = addButton(new DrawOfferDisplay(this.guiLeft + 63, this.guiTop + 70));
		this.errorMessageDisplay = addButton(new ErrorMessageDisplay(this.guiLeft + 46, this.guiTop + 185));

		this.computerStrengthSlider = this.addButton(new ComputerStrengthSlider(this.guiLeft + 152, this.guiTop + 46, 70, (double)this.container.tile.computerStrength / 8.0));
		
		this.timeSlider = this.addButton(new TimeLimitSlider(this.guiLeft + 152, this.guiTop + 72, 70, TimeLimitSlider.getInitialVal(this.container.tile.challengedTime)));
		this.flipbutton = this.addButton(new FlipBoardButton(this.guiLeft + 200, this.guiTop + 101, (button) -> {	
			this.updateSlotHighlights();
			
			this.side = this.side.getOpposite();
			PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetSidePacket(this.side));
		}));

		this.errorMessageDisplay.visible = false;
		this.errorMessageDisplay.active = false;
		
		if(Config.COMMON.debug.get())
			this.debug = this.addButton(new DebugButton(this.container, this.guiLeft - 20, this.guiTop + 20));
		
		this.updateMode(Mode.PLAYING);
	}
	
	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		if(!(this.hoveredSlot instanceof ChessBoardSquareSlot))
			super.renderHoveredToolTip(mouseX, mouseY);
				
		//render buttons
		for(Widget w : buttons) {
			if(w.isHovered() && buttonTooltips.get(w.getClass()) != null) {
				List<String> tooltip = buttonTooltips.get(w.getClass()).apply(w);
				if(tooltip != null) {
					this.renderTooltip(tooltip, mouseX, mouseY, font);
				}
			}
		}
	}  
	
	@Override
	public void resize(Minecraft m, int w, int h) {
		this.init(m, w, h);
		this.updateMode(this.container.getMode());
	}
	
	public void updateSlotHighlights() {
		for(int i = 0; i < 64; i++)
			((ChessBoardSquareSlot) this.container.inventorySlots.get(i)).highlight = Optional.empty();
		
		if(!this.options[1].yes) return;
		
		this.hintmove = Optional.empty();
		
		if(this.container.selectedSlot != -1) {
			ChessHelper.getLegalMoves(this.container, this.container.getBoard().toPlay, list -> {
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
	
	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if(this.container.getMode() == Mode.PLAYING) {
			this.hideDisplays();
		}
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}
	
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.minecraft.getTextureManager().bindTexture(this.container.getMode().texture);

		int left = this.guiLeft;
		int top = this.guiTop;
		this.blit(left, top, 0, 0, this.xSize, this.ySize);
		
		if(this.container.getMode() == Mode.PLAYING) {
			this.minecraft.getTextureManager().bindTexture(TEX_ICONS);
			
			if(this.container.getBoard().toPlay != this.container.tile.challengerColor) {
				this.blit(left + 184, top + 69, 5, 54, 7, 7);
			} else {
				this.blit(left + 184, top + 137, 5, 54, 7, 7);
			}
			
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
			
			boolean mirrored = this.side == PieceColor.BLACK;
			
			if(this.hintmove.isPresent()) {
				drawSlotHighlight(ChessHelper.getX(this.hintmove.get().getFirst()), mirrored ? 7 - ChessHelper.getY(this.hintmove.get().getFirst()) : ChessHelper.getY(this.hintmove.get().getFirst()), HighlightMode.HINT.color);
				drawSlotHighlight(ChessHelper.getX(this.hintmove.get().getSecond()), mirrored ? 7 - ChessHelper.getY(this.hintmove.get().getSecond()) : ChessHelper.getY(this.hintmove.get().getSecond()), HighlightMode.HINT.color);
			}
			
			if(this.container.getBoard().isInCheck(this.container.getBoard().toPlay)) {
				this.container.checkedSquare = this.container.getBoard().getKingPos(this.container.getBoard().toPlay);
				
			if(this.container.checkedSquare != -1) {
				drawSlotHighlight(ChessHelper.getX(this.container.checkedSquare), this.side == PieceColor.WHITE ? ChessHelper.getY(this.container.checkedSquare) : 7 - ChessHelper.getY(this.container.checkedSquare), HighlightMode.IN_CHECK.color);

			}
			}

			
			if(this.container.selectedSlot != -1) {
				drawSlotHighlight(ChessHelper.getX(this.container.selectedSlot), 
						ChessHelper.getY(this.side == PieceColor.WHITE ? this.container.selectedSlot : 
							ChessHelper.getX(this.container.selectedSlot) * 8 + 7 - ChessHelper.getY(this.container.selectedSlot)), 0x8fE5D400);
			}
		}
		
		if(this.options[0].yes && (this.container.getMode() == Mode.PLAYING || this.container.getMode() == Mode.BOARD_EDITOR)) {
			//Coordinates
			RenderSystem.scalef(0.5f, 0.5f, 0.5f);
			for(int i = 0; i < 8; i++) {
				
				boolean r = this.side == PieceColor.WHITE;
				//number
				this.font.drawString(String.valueOf(i + 1), 2*(this.guiLeft + 22), 2*(this.guiTop + (r ? 157 - 18 * i : 31 + 18 * i)), i % 2 == 0 ^ !r ? 0xFFFFFFFF : 0xFF4C7C31);
				
				//letter
				this.font.drawString(ChessHelper.LETTERS.get(i), 2*(this.guiLeft + 35 + 18 * i), 2*(this.guiTop + 168), i % 2 == 0 ? 0xFFFFFFFF : 0xFF4C7C31);
			}
			
			RenderSystem.scalef(2.0f, 2.0f, 2.0f);
		}
	}
	
	private void drawSlotHighlight(int slotx, int sloty, int color) {
		int x = this.guiLeft + slotx * 18 + 21;
		int y = this.guiTop + sloty * 18 + 29;
		fill(x, y, x + 18, y + 18, color);
	}
	
	@Override
	public void drawSlot(Slot slotIn) {
		if (slotIn instanceof ChessBoardSquareSlot || (slotIn instanceof ChessPieceStorageSlot && this.container.getMode() == Mode.BOARD_EDITOR)) {
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
		setButtonEnabled(this.playingColorButton, mode == Mode.BOARD_EDITOR);
		setButtonEnabled(this.challengerColorButton, mode == Mode.BOARD_EDITOR);
		setButtonEnabled(this.flipbutton, mode == Mode.PLAYING);
		setButtonEnabled(this.playButton, mode == Mode.PLAYING);
		setButtonEnabled(this.resignButton, mode == Mode.PLAYING);
		setButtonEnabled(this.drawButton, mode == Mode.PLAYING);
		setButtonEnabled(this.timeDisplayOpponent, mode == Mode.PLAYING);
		setButtonEnabled(this.timeDisplaySelf, mode == Mode.PLAYING);
		setButtonEnabled(this.hintButton, mode == Mode.PLAYING);
		setButtonEnabled(this.playComputerButton, mode == Mode.PLAYING);
		setButtonEnabled(this.timeSlider, mode == Mode.SETTINGS);
		setButtonEnabled(this.computerStrengthSlider, mode == Mode.SETTINGS);
		
		for(CastleOptionButton b : this.castlebuttons) {
			setButtonEnabled(b, mode == Mode.BOARD_EDITOR);
		}
				
		for(PromotionButton b : this.promtionbuttons) {
			setButtonEnabled(b, mode == Mode.PLAYING && this.container.tile.promotionColor.isPresent());
			if(this.container.tile.promotionColor.isPresent()) b.color = this.container.tile.promotionColor.get();
		}
		
		for(ToggleOptionButton b : this.options) {
			setButtonEnabled(b, mode == Mode.SETTINGS);
		}
		
		this.hideDisplays();
		
		if(mode != Mode.PLAYING) {
			this.errorMessageDisplay.visible = false;
			this.errorMessageDisplay.active = false;
			
			if(mode == Mode.BOARD_EDITOR) {
				this.side = PieceColor.WHITE;
				PacketHandler.sendToServer(Minecraft.getInstance().world, new ClientSetSidePacket(PieceColor.WHITE));
			}
		}
	}
	
	private void hideDisplays() {
		this.gameEndDisplay.visible = false;
		this.gameEndDisplay.active = false;
		this.challengeDisplay.visible = false;
		this.challengeDisplay.active = false;
		this.drawDisplay.visible = false;
		this.drawDisplay.active = false;
	}
	
	@Override
	public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
		return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_) 
			&&( (this.computerStrengthSlider.isHovered() && this.computerStrengthSlider.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_)
			|| (this.timeSlider.isHovered() && this.timeSlider.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_))));
	}
	
	
	private void setButtonEnabled(Widget widget, boolean enabled) {
		widget.active = enabled;
		widget.visible = enabled;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialticks) {		
		this.renderBackground();
		if (this.container.getMode() == Mode.BOARD_EDITOR || this.container.getMode() == Mode.PLAYING) {
			int left = this.guiLeft;
			int top = this.guiTop;
			this.drawGuiContainerBackgroundLayer(partialticks, mouseX, mouseY);

			RenderSystem.pushMatrix();
			RenderSystem.translatef((float) left, (float) top, 0.0F);
			this.hoveredSlot = null;

			RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

			for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
				Slot slot = this.container.inventorySlots.get(i);
				if (slot.isEnabled() || this.container.getMode() == Mode.PLAYING && slot instanceof ChessBoardSquareSlot && this.container.tile.promotionColor.isPresent()) {
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
			
			RenderSystem.popMatrix();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableRescaleNormal();
			RenderSystem.disableDepthTest();
			
			for (int i = 0; i < this.buttons.size(); ++i) {
				this.buttons.get(i).render(mouseX, mouseY, partialticks);
			}
			
			RenderSystem.enableRescaleNormal();
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float) left, (float) top, 0.0F);
			
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
			
			if(this.container.tile.playing && this.container.getMode() == Mode.PLAYING) {

				if (this.container.tile.challenger.isPresent()) {
					PlayerEntity player = this.container.tile.getWorld().getPlayerByUuid(this.container.tile.challenger.get());
					if(player != null) drawName(player.getName().getString(), 184, 125);
				}
				if (this.container.tile.isPlayingComputer) 
					drawName(Config.CLIENT.computername.get(), 184, 57);
				else if (this.container.tile.challenged.isPresent()) {
					PlayerEntity player = this.container.tile.getWorld().getPlayerByUuid(this.container.tile.challenged.get());
					if(player != null) drawName(player.getDisplayName().getString(), 184, 57);
				}
			}
			
			RenderSystem.popMatrix();
			RenderSystem.enableDepthTest();
		} else {
			super.render(mouseX, mouseY, partialticks);
		}
		
		this.renderHoveredToolTip(mouseX, mouseY);
	}
	
	private void drawName(String name, int x, int y) {
		if(this.font.getStringWidth(name) > 52) {
			RenderSystem.scalef(0.5f, 0.5f, 0.5f);
			this.font.drawString(name, x * 2 + 2, y * 2 + 4 , 0x331F0D);
			RenderSystem.scalef(2.0f, 2.0f, 2.0f);
		} else {
			this.font.drawString(name, x, y, 0x331F0D);
		}
	}
	
	public void displayInvalidPosError() {
		this.errorMessageDisplay.message = "Invalid chess position!";
		this.errorMessageDisplay.active = true;
		this.errorMessageDisplay.visible = true;
	}

	public PieceColor getSide() {
		return this.side ;
	}
	
	{
		//init descriptions
		buttonTooltips.put(PlayButton.class, (w) -> ImmutableList.of("Challenge another Player"));
		buttonTooltips.put(PlayComputerButton.class, (w) -> {
			if(this.container.tile.isPlayingComputer && this.container.tile.playing)
				return ImmutableList.of("Stop the game");
			else return ImmutableList.of("Play against the " + Config.CLIENT.computername.get());
		});
		
		//play
		buttonTooltips.put(HintButton.class,  (w) -> {
			if(this.container.tile.playing && !this.container.tile.isPlayingComputer)
				return ImmutableList.of("Only available when playing against computer!");
			else 
				return ImmutableList.of("Get a hint");
		});
		
		buttonTooltips.put(OfferDrawButton.class,  (w) -> ImmutableList.of("Offer draw"));
		buttonTooltips.put(ResignButton.class,  (w) -> ImmutableList.of("Resign the game"));
		buttonTooltips.put(FlipBoardButton.class, (w) -> ImmutableList.of("Flip Board"));
		
		//options
		buttonTooltips.put(ComputerStrengthSlider.class,  (w) -> ImmutableList.of("Set the " + Config.CLIENT.computername.get() + "'s playing level", "0 - 3: Beginner", "4 - 6: Intermediate", "7+: Pro"));
		buttonTooltips.put(TimeLimitSlider.class, (w) -> ImmutableList.of("Set the time each player gets", "1 minute: bullet", "3 - 5 minutes: blitz", "10 minutes: rapid",  "20 minutes+: classical"));
		buttonTooltips.put(CastleOptionButton.class, (w) -> ImmutableList.of("Enable / Disable casteling"));
		buttonTooltips.put(ClearBoardButton.class, (w) -> ImmutableList.of("Clear the board"));
		buttonTooltips.put(BuildBoardButton.class, (w) -> ImmutableList.of("Set up the board", "only works when all the needed pieces are in storage"));
		buttonTooltips.put(PlayingColorButton.class, (w) -> ImmutableList.of("Set color that makes the next move"));
		buttonTooltips.put(ChallengerColorButton.class, (w) -> ImmutableList.of("Set color that the person who starts the next challenge plays"));
	}
}
