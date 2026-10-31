package com.leftycraft.leftypet.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;

import java.lang.reflect.Method;

/**
 * Clean reflection hook for RoseStacker.
 * Handles stacked ground items so Looter pet doesn't drop/delete remaining stacks > 64.
 */
public class RoseStackerHook {

    private static boolean initialized = false;
    private static boolean available = false;
    private static Object apiInstance = null;
    private static Method getStackedItemMethod = null;
    private static Method removeItemStackMethod = null;
    private static Method getStackSizeMethod = null;
    private static Method setStackSizeMethod = null;
    private static Method updateDisplayMethod = null;
    private static Method updateItemMethod = null;

    private static synchronized void checkInit() {
        if (initialized) return;
        initialized = true;

        if (!Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
            available = false;
            return;
        }

        try {
            Class<?> apiClass = Class.forName("dev.rosewood.rosestacker.api.RoseStackerAPI");
            Method getInstanceMethod = apiClass.getMethod("getInstance");
            apiInstance = getInstanceMethod.invoke(null);

            getStackedItemMethod = apiClass.getMethod("getStackedItem", Item.class);

            Class<?> stackedItemClass = Class.forName("dev.rosewood.rosestacker.stack.StackedItem");
            removeItemStackMethod = apiClass.getMethod("removeItemStack", stackedItemClass);
            getStackSizeMethod = stackedItemClass.getMethod("getStackSize");
            setStackSizeMethod = stackedItemClass.getMethod("setStackSize", int.class);

            try {
                updateDisplayMethod = stackedItemClass.getMethod("updateDisplay");
            } catch (Throwable ignored) {}

            try {
                updateItemMethod = stackedItemClass.getMethod("updateItem");
            } catch (Throwable ignored) {}

            available = true;
            Bukkit.getLogger().info("[LeftyPet] Successfully hooked into RoseStacker for stacked item looting!");
        } catch (Throwable t) {
            available = false;
            Bukkit.getLogger().warning("[LeftyPet] RoseStacker detected but hook failed: " + t.getMessage());
        }
    }

    public static boolean isAvailable() {
        checkInit();
        return available;
    }

    /**
     * Gets the stacked amount of the item if stacked by RoseStacker, or null if not stacked.
     */
    public static Integer getStackSize(Item item) {
        if (!isAvailable() || item == null || !item.isValid() || item.isDead()) {
            return null;
        }
        try {
            Object stackedItem = getStackedItemMethod.invoke(apiInstance, item);
            if (stackedItem != null) {
                Object size = getStackSizeMethod.invoke(stackedItem);
                if (size instanceof Number n) {
                    return n.intValue();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Updates the stacked item size in RoseStacker. If newSize <= 0, the stack is removed.
     */
    public static void updateStackSize(Item item, int newSize) {
        if (!isAvailable() || item == null) return;
        try {
            Object stackedItem = getStackedItemMethod.invoke(apiInstance, item);
            if (stackedItem != null) {
                if (newSize <= 0) {
                    removeItemStackMethod.invoke(apiInstance, stackedItem);
                } else {
                    setStackSizeMethod.invoke(stackedItem, newSize);
                    if (updateDisplayMethod != null) {
                        try {
                            updateDisplayMethod.invoke(stackedItem);
                        } catch (Throwable ignored) {}
                    }
                    if (updateItemMethod != null) {
                        try {
                            updateItemMethod.invoke(stackedItem);
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Removes the stacked item entry completely from RoseStacker.
     */
    public static void removeStackedItem(Item item) {
        if (!isAvailable() || item == null) return;
        try {
            Object stackedItem = getStackedItemMethod.invoke(apiInstance, item);
            if (stackedItem != null) {
                removeItemStackMethod.invoke(apiInstance, stackedItem);
            }
        } catch (Throwable ignored) {}
    }
}
