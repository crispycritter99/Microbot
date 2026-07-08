package net.runelite.client.plugins.microbot.valetotemsbeta.handlers;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.valetotemsbeta.ValeTotemBetaConfig;
import net.runelite.client.plugins.microbot.valetotemsbeta.utils.FletchingItemMapper;
import net.runelite.client.plugins.microbot.valetotemsbeta.utils.InventoryUtils;

import java.awt.event.KeyEvent;

import static net.runelite.client.plugins.microbot.util.Global.*;

/**
 * Handles fletching operations for the Vale Totems minigame
 * Includes optimization for fletching while walking and log basket support for extended routes
 * 
 * Key features:
 * - Automatic log basket emptying when insufficient logs in inventory
 * - Support for both standard (5 totems) and extended routes (8 totems) 
 * - Backward compatibility with existing methods
 * - GameSession-based tracking for extended route optimization
 */
public class FletchingHandler {

    private static final int FLETCHING_INTERFACE_TIMEOUT_MS = 3000; // 3 seconds
    private static final long FLETCHING_ANIMATION_DELAY_MS = 1800; // Time per fletching action
    private static final int FLETCHING_INTERFACE_WIDGET_ID = 270; // Fletching interface widget ID

    // State tracking for fletching while walking
    private static boolean isFletchingWhileWalking = false;
    private static int targetBowsToFletch = 0;
    
    // Configuration instance
    private static ValeTotemBetaConfig config;

    /**
     * Set the configuration for this handler
     * @param config the ValeTotemBetaConfig instance
     */
    public static void setConfig(ValeTotemBetaConfig config) {
        FletchingHandler.config = config;
    }

    /**
     * Start fletching bows using knife and logs
     * @return true if fletching interface opened successfully
     */
    public static boolean startFletching() {
        try {
            if (!InventoryUtils.hasKnife() || InventoryUtils.getLogCount() == 0) {
                System.err.println("Missing materials for fletching");
                return false;
            }

            System.out.println("Starting fletching operation");

            // Use knife on log 
            boolean used = InventoryUtils.startFletching();
            if (used) {
                // Wait for fletching interface to appear
                long startTime = System.currentTimeMillis();
                while (!isFletchingInterfaceOpen() && 
                       System.currentTimeMillis() - startTime < FLETCHING_INTERFACE_TIMEOUT_MS) {
                    sleep(100);
                }

                Microbot.log("Fletching interface opened");
                
                return isFletchingInterfaceOpen();
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error starting fletching: " + e.getMessage());
            return false;
        }
    }

    /**
     * Select target to fletch from the fletching interface
     * @param quantity number of bows to make (1, 5, 10, or All)
     * @return true if selection was successful
     */
    public static boolean selectBow(int quantity) {
        try {
            if (!isFletchingInterfaceOpen()) {
                return false;
            }

            String quantityOption = quantity == 1 || quantity == 5 || quantity == 10
                    ? String.valueOf(quantity)
                    : "All";
            Rs2Widget.enableQuantityOption(quantityOption);

            String optionText = config != null
                    ? FletchingItemMapper.getCombinationOptionText(config.logType(), config.bowType())
                    : "Yew longbow";
            boolean selected = Rs2Dialogue.keyPressForCombinationOption(optionText);
            if (selected) {
                Microbot.log("Selected " + optionText + " using its combination-dialogue hotkey");
            }
            return selected;

        } catch (Exception e) {
            System.err.println("Error selecting target to fletch: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fletch a specific number of bows
     * @param quantity number of bows to fletch
     * @return true if fletching completed successfully
     */
    public static boolean fletchBows(int quantity) {
        return fletchBows(quantity, null);
    }

    /**
     * Fletch a specific number of bows with log basket support
     * @param quantity number of bows to fletch
     * @param gameSession the game session for log basket tracking (null if not using extended route)
     * @return true if fletching completed successfully
     */
    public static boolean fletchBows(int quantity, net.runelite.client.plugins.microbot.valetotemsbeta.models.GameSession gameSession) {
        try {
            // Ensure we have enough materials - try to empty log basket if needed
            int availableLogs = InventoryUtils.getLogCount();
            
            // If we don't have enough logs and we have a log basket, try to empty it
            if (availableLogs <= quantity && gameSession != null && InventoryUtils.hasLogBasket()) {
                int logsInBasket = InventoryUtils.getLogBasketLogCount(gameSession);
                if (logsInBasket > 0) {
                    System.out.println("Not enough logs in inventory (" + availableLogs + "/" + quantity + "). Emptying log basket with " + logsInBasket + " logs");
                    if (InventoryUtils.emptyLogBasket(gameSession)) {
                        availableLogs = InventoryUtils.getLogCount(); // Refresh count after emptying
                        System.out.println("After emptying basket: " + availableLogs + " logs available");
                    } else {
                        System.err.println("Failed to empty log basket");
                    }
                }
            }
            
            int actualQuantity = Math.min(quantity, availableLogs);
            
            if (actualQuantity <= 0) {
                System.err.println("No logs available for fletching");
                return false;
            }

            String fletchingDesc = config != null ? 
                FletchingItemMapper.getFletchingDescription(config.logType(), config.bowType()) : 
                "Yew Longbow (u)";
            System.out.println("Fletching " + actualQuantity + " " + fletchingDesc);

            // Start fletching interface
            if (!startFletching()) {
                return false;
            }

            // Select target to fletch and quantity
            if (!selectBow(actualQuantity)) {
                return false;
            }

            // Wait for fletching to complete
            return waitForFletchingCompletion(actualQuantity);

        } catch (Exception e) {
            System.err.println("Error fletching bows: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fletch the exact number of bows needed for one totem (4 bows)
     * @return true if 4 bows were successfully fletched
     */
    public static boolean fletchBowsForOneTotem() {
        return fletchBowsForOneTotem(null);
    }

    /**
     * Fletch the exact number of bows needed for one totem (4 bows) with log basket support
     * @param gameSession the game session for log basket tracking (null if not using extended route)
     * @return true if 4 bows were successfully fletched
     */
    public static boolean fletchBowsForOneTotem(net.runelite.client.plugins.microbot.valetotemsbeta.models.GameSession gameSession) {
        int needed = 4 - InventoryUtils.getBowCount();
        if (needed <= 0) {
            return true; // Already have enough
        }

        return fletchBows(needed, gameSession);
    }

    public static boolean stringAvailableBows() {
        if (config == null || !config.useBowStringSpool()
                || config.logType() == ValeTotemBetaConfig.LogType.REDWOOD
                || !InventoryUtils.hasBowStringSpool()
                || InventoryUtils.getUnstrungBowCount() == 0) {
            return true;
        }
        int initial = InventoryUtils.getUnstrungBowCount();
        if (!net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.combine(
                InventoryUtils.BOW_STRING_SPOOL_ID, InventoryUtils.getBowId())) {
            return false;
        }
        if (!sleepUntil(FletchingHandler::isFletchingInterfaceOpen, FLETCHING_INTERFACE_TIMEOUT_MS)) {
            return false;
        }
        Rs2Widget.enableQuantityOption("All");
        String optionText = FletchingItemMapper.getCombinationOptionText(config.logType(), config.bowType());
        if (!Rs2Dialogue.keyPressForCombinationOption(optionText)) {
            return false;
        }
        return sleepUntil(() -> InventoryUtils.getUnstrungBowCount() < initial, 5000);
    }

    /**
     * Start fletching while walking (optimization feature)
     * This allows fletching during movement to save time
     * @param bowsNeeded number of bows to fletch during travel
     * @return true if fletching while walking was initiated
     */
    public static boolean startFletchingWhileWalking(int bowsNeeded) {
        return startFletchingWhileWalking(bowsNeeded, null);
    }

    /**
     * Start fletching while walking with log basket support (optimization feature)
     * This allows fletching during movement to save time
     * @param bowsNeeded number of bows to fletch during travel
     * @param gameSession the game session for log basket tracking (null if not using extended route)
     * @return true if fletching while walking was initiated
     */
    public static boolean startFletchingWhileWalking(int bowsNeeded, net.runelite.client.plugins.microbot.valetotemsbeta.models.GameSession gameSession) {
        try {
            if (config != null && config.decorationSource() == ValeTotemBetaConfig.DecorationSource.PREMADE) {
                return false;
            }
            // Check if we have enough materials to start fletching
            if (bowsNeeded > 0 && Rs2Player.isMoving() && !isFletchingWhileWalking && !Rs2Player.isAnimating()) {
                // Check available logs and try to empty basket if needed
                int availableLogs = InventoryUtils.getLogCount();
                sleepGaussian(100, 50);

                // If we don't have enough logs and we have a log basket, try to empty it
                if (availableLogs <= bowsNeeded && gameSession != null && InventoryUtils.hasLogBasket()) {
                    int logsInBasket = InventoryUtils.getLogBasketLogCount(gameSession);
                    if (logsInBasket > 0) {
                        System.out.println("Not enough logs for fletching while walking (" + availableLogs + "/" + bowsNeeded + "). Emptying log basket with " + logsInBasket + " logs");
                        if (InventoryUtils.emptyLogBasket(gameSession)) {
                            availableLogs = InventoryUtils.getLogCount(); // Refresh count after emptying
                            System.out.println("After emptying basket for walking fletching: " + availableLogs + " logs available");
                        } else {
                            System.err.println("Failed to empty log basket for walking fletching");
                        }
                        return false;
                    }
                }

                if (availableLogs < bowsNeeded) {
                    System.out.println("Not enough logs for fletching while walking (" + availableLogs + "/" + bowsNeeded + "). Stopping fletching");
                    return false;
                }

                String fletchingDesc = config != null ? 
                    FletchingItemMapper.getFletchingDescription(config.logType(), config.bowType()) : 
                    "Yew Longbow (u)";
                System.out.println("Starting fletching while walking: " + bowsNeeded + " " + fletchingDesc);
                
                // Try to start fletching immediately if materials are available
                if (InventoryUtils.hasKnife() && InventoryUtils.getLogCount() > 0) {
                    if (!startFletching() || !selectBow(bowsNeeded)) {
                        return false;
                    }
                    isFletchingWhileWalking = true;
                    targetBowsToFletch = bowsNeeded;
                    Rs2Antiban.moveMouseOffScreen(20);
                    sleep(500);
                    return true;
                }
                return false;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error starting fletching while walking: " + e.getMessage());
            return false;
        }
    }

    /**
     * Stop fletching while walking
     */
    public static void stopFletchingWhileWalking() {
        isFletchingWhileWalking = false;
        targetBowsToFletch = 0;
        System.out.println("Stopped fletching while walking");
    }

    /**
     * Check if currently fletching while walking
     * @return true if fletching while walking is active
     */
    public static boolean isFletchingWhileWalking() {
        return isFletchingWhileWalking;
    }

    /**
     * Wait for fletching animation to complete
     * @param expectedBows number of bows expected to be created
     * @return true if fletching completed successfully
     */
    private static boolean waitForFletchingCompletion(int expectedBows) {
        try {
            int initialBows = InventoryUtils.getBowCount();
            int targetBows = initialBows + expectedBows;
            
            // Wait for fletching to complete
            long startTime = System.currentTimeMillis();
            long maxWaitTime = expectedBows * FLETCHING_ANIMATION_DELAY_MS + 5000; // Extra buffer
            
            while (InventoryUtils.getBowCount() < targetBows && 
                   System.currentTimeMillis() - startTime < maxWaitTime) {
                
                sleep(100);
            }

            int actualBows = InventoryUtils.getBowCount() - initialBows;
            System.out.println("Fletched " + actualBows + " bows (expected " + expectedBows + ")");
            
            return actualBows >= expectedBows;

        } catch (Exception e) {
            System.err.println("Error waiting for fletching completion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if the fletching interface is currently open
     * @return true if fletching interface is visible
     */
    private static boolean isFletchingInterfaceOpen() {
        // This may need adjustment based on actual RuneLite widget IDs
        return Rs2Widget.getWidget(FLETCHING_INTERFACE_WIDGET_ID, 0) != null;
    }

    /**
     * Get fletching status summary
     * @return formatted string with fletching status
     */
    public static String getFletchingStatus() {
        int logs = InventoryUtils.getLogCount();
        int bows = InventoryUtils.getBowCount();
        int maxPossible = logs + bows;
        
        String fletchingType = config != null ? 
            FletchingItemMapper.getFletchingDescription(config.logType(), config.bowType()) : 
            "Yew Longbow (u)";
        
        String walkingStatus = isFletchingWhileWalking ? 
            " | Walking: " + targetBowsToFletch + " target" : "";
        
        return String.format("Fletching (%s): Logs=%d, Bows=%d, Max=%d%s [Keyboard optimized]", 
                fletchingType, logs, bows, maxPossible, walkingStatus);
    }

    /**
     * Emergency stop fletching (for error recovery)
     */
    public static void emergencyStopFletching() {
        isFletchingWhileWalking = false;
        targetBowsToFletch = 0;
        
        // Try to close any open interfaces
        Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
        
        System.out.println("Emergency stop fletching executed");
    }

    /**
     * Calculate optimal fletching strategy based on current situation
     * @param bowsNeeded number of bows needed
     * @param isWalking whether the player is currently walking
     * @return recommended fletching approach
     */
    public static FletchingStrategy getOptimalStrategy(int bowsNeeded, boolean isWalking) {
        int currentBows = InventoryUtils.getBowCount();
        int currentLogs = InventoryUtils.getLogCount();
        int deficit = Math.max(0, bowsNeeded - currentBows);
        
        if (deficit == 0) {
            return FletchingStrategy.NO_FLETCHING_NEEDED;
        }
        
        if (currentLogs < deficit) {
            return FletchingStrategy.INSUFFICIENT_LOGS;
        }
        
        if (isWalking && deficit <= 5) {
            return FletchingStrategy.FLETCH_WHILE_WALKING;
        }
        
        return FletchingStrategy.FLETCH_STATIONARY;
    }


    /**
     * Enum for fletching strategies
     */
    public enum FletchingStrategy {
        NO_FLETCHING_NEEDED,
        FLETCH_WHILE_WALKING,
        FLETCH_STATIONARY,
        INSUFFICIENT_LOGS
    }
} 
