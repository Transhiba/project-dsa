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

/**
 * Tracks and renders blue blots on the player based on hit count.
 * Converts hits to blot visuals using the sprite system.
 *
 * Logic:
 * - Mỗi lần player bị hurt: tăng hitCount.
 * - Đủ 3 hit (HITS_PER_BLOT) => reset hitCount, tăng blotCount lên 1.
 * - Nếu blotCount >= 2 => gọi die() cho Game.player (game over).
 */
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

	/**
	 * Load blue blot texture images and register them with the sprite linker.
	 */
	private static void loadBlotTextures() {
		try {
			// Load blot textures from resources
			BufferedImage blot1Image = ImageIO.read(BlueBlotSystem.class.getResourceAsStream("/assets/rules/blueblot_1.png"));
			BufferedImage blot2Image = null;

			// Try to load the second blot image, fallback to first if not present
			try {
				blot2Image = ImageIO.read(BlueBlotSystem.class.getResourceAsStream("/assets/rules/blue_blot_2.png"));
			} catch (Exception e) {
				blot2Image = blot1Image; // Fallback to first image if second not found
			}

			// Convert to MinicraftImage and register with sprite linker
			if (Renderer.spriteLinker != null) {
				MinicraftImage img1 = new MinicraftImage(blot1Image);
				MinicraftImage img2 = new MinicraftImage(blot2Image);

				// Register sprites with the sprite system
				Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Item, "blueblot_1", img1);
				Renderer.spriteLinker.setSprite(SpriteLinker.SpriteType.Item, "blue_blot_2", img2);

				// Create linked sprites
				blotSprite1 = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "blueblot_1");
				blotSprite2 = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "blue_blot_2");
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

		// Tăng hitCount mỗi lần bị đánh
		int hitCount = playerHitCounts.getOrDefault(player, 0) + 1;
		playerHitCounts.put(player, hitCount);

		// Đủ HITS_PER_BLOT hit => +1 blot, reset hitCount
		if (hitCount >= HITS_PER_BLOT) {
			playerHitCounts.put(player, 0); // reset để đếm đợt sau

			int blotCount = playerBlotCounts.getOrDefault(player, 0) + 1;
			playerBlotCounts.put(player, blotCount);

			// Nếu có từ 2 đốm xanh trở lên thì cho player chết (game over logic sẽ xử lý tiếp)
			if (blotCount >= 2 && player == Game.player) {
				player.die();
			}
		}
	}

	/**
	 * Adds a blue blot to the player (manual add, nếu cần).
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
	 * Resets the blot & hit count for a player (dùng khi respawn / reset rule).
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

