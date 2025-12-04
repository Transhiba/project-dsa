package minicraft.util.rules;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.entity.mob.Player;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class BlueBlotSystem {
	private static final int HITS_PER_BLOT = 3; // 3 hits = 1 blot
	private static final Map<Player, Integer> playerHitCounts = new HashMap<>();  // hitCount cho từng player
	private static final Map<Player, Integer> playerBlotCounts = new HashMap<>(); // blotCount cho từng player
	private static SpriteLinker.LinkedSprite blotSprite1 = null; // 1 blot sprite
	private static SpriteLinker.LinkedSprite blotSprite2 = null; // 2+ blots sprite

	static {
		// Load blue blot textures into the sprite system
		loadBlotTextures();
	}

	
	private static void loadBlotTextures() {
		try {
			BufferedImage blot1Image = ImageIO.read(
				BlueBlotSystem.class.getResourceAsStream("/assets/rules/blueblot_1.png")
			);
			BufferedImage blot2Image = blot1Image; 

			try {
				blot2Image = ImageIO.read(BlueBlotSystem.class.getResourceAsStream("/assets/rules/blueblot_2.png"));
			} catch (Exception ignored) {
				blot2Image = blot1Image; 
			}

			if (Renderer.spriteLinker != null) {
				MinicraftImage img1 = new MinicraftImage(blot1Image);
				MinicraftImage img2 = new MinicraftImage(blot2Image);

				Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Item, "blueblot_1", img1);
				Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Item, "blueblot_2", img2);

				blotSprite1 = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "blueblot_1");
				blotSprite2 = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "blueblot_2");
			}
		} catch (IOException e) {
			System.err.println("Warning: Could not load blue blot textures: " + e.getMessage());
		}
	}

	/**
	 * Called when the player is hurt. Increments hit count and converts to blots.
	 */
	public static void onPlayerHurt(Player player) {
		if (player == null) return;

		// increase hit count
		int hitCount = playerHitCounts.getOrDefault(player, 0) + 1;
		playerHitCounts.put(player, hitCount);

		// 3 hits = 1 blot
		if (hitCount >= HITS_PER_BLOT) {
			playerHitCounts.put(player, 0); 

			// if no blot, add a new blot
			// if already has 1 blot, add a new blot
			addBlueBlot(player);
			// when player has 2 or more blots, die (game over)
			int blotCount = getBlueBlotCount(player);
			if (blotCount >= 2 && player == Game.player) {
				player.die();
			}
		}
	}

	/**
	 * Adds a blue blot to the player 
	 */
	public static void addBlueBlot(Player player) {
		if (player == null) return;
		int currentBlots = getBlueBlotCount(player);
		playerBlotCounts.put(player, currentBlots + 1);
	}

	/**
	 * Gets the current blue blot count for a player.
	 */
	public static int getBlueBlotCount(Player player) {
		if (player == null) return 0;
		return playerBlotCounts.getOrDefault(player, 0);
	}

	/**
	 * Resets the blot & hit count for a player 
	 */
	public static void resetBlueBlots(Player player) {
		if (player == null) return;
		playerHitCounts.remove(player);
		playerBlotCounts.remove(player);
	}

	/**
	 * Gets the appropriate sprite for the given blot count.
	 */
	private static SpriteLinker.LinkedSprite getBlotSprite(int blotCount) {
		if (blotCount >= 2 && blotSprite2 != null) {
			return blotSprite2;
		} else if (blotCount >= 1 && blotSprite1 != null) {
			return blotSprite1;
		}
		return null;
	}

	/**
	 * Renders blue blots on the player.
	 * Called from Renderer.renderLevel() to display blots on the player sprite.
	 */
	public static void renderBlueBlots(Screen screen, Player player) {
		if (player == null) return;

		int blotCount = getBlueBlotCount(player);
		if (blotCount > 0) {
			SpriteLinker.LinkedSprite sprite = getBlotSprite(blotCount);
			if (sprite != null) {
				// Render the blot indicator above/on the player
				int renderX = player.x - 8; // Offset to position relative to player
				int renderY = player.y - 16; // Above the player
				screen.render(renderX, renderY, sprite);
			}
		}
	}
}
