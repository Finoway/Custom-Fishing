package net.momirealms.customfishing.api.util;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InventoryUtils {

    public static Inventory createInventory(InventoryHolder inventoryHolder, int size, Component component) {
        try {
            boolean isSpigot = CustomFishingPlugin.get().getVersionManager().isSpigot();
            Method createInvMethod = ReflectionUtils.bukkitClass.getMethod(
                    "createInventory",
                    InventoryHolder.class,
                    int.class,
                    isSpigot ? String.class : ReflectionUtils.componentClass
            );
            return (Inventory) createInvMethod.invoke(
                    null,
                    inventoryHolder,
                    size,
                    isSpigot ? CustomFishingPlugin.get().getAdventure().componentToLegacy(component) : CustomFishingPlugin.get().getAdventure().shadedComponentToPaperComponent(component)
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static Inventory createInventory(InventoryHolder inventoryHolder, InventoryType type, Component component) {
        try {
            boolean isSpigot = CustomFishingPlugin.get().getVersionManager().isSpigot();
            Method createInvMethod = ReflectionUtils.bukkitClass.getMethod(
                    "createInventory",
                    InventoryHolder.class,
                    InventoryType.class,
                    isSpigot ? String.class : ReflectionUtils.componentClass
            );
            return (Inventory) createInvMethod.invoke(
                    null,
                    inventoryHolder,
                    type,
                    isSpigot ? CustomFishingPlugin.get().getAdventure().componentToLegacy(component) : CustomFishingPlugin.get().getAdventure().shadedComponentToPaperComponent(component)
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static @NotNull String stacksToBase64(ItemStack[] contents) {
        if (contents.length == 0) {
            return "";
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(contents.length);
            for (ItemStack itemStack : contents) {
                dataOutput.writeObject(itemStack);
            }
            dataOutput.close();
            byte[] byteArr = outputStream.toByteArray();
            outputStream.close();
            return Base64Coder.encodeLines(byteArr);
        } catch (IOException e) {
            LogUtils.warn("Encoding error", e);
        }
        return "";
    }

    /**
     * Get itemStacks from base64
     * @param base64 base64
     * @return itemStacks
     */
    @Nullable
    public static ItemStack[] getInventoryItems(String base64) {
        ItemStack[] itemStacks = null;
        try {
            itemStacks = stacksFromBase64(base64);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
        }
        return itemStacks;
    }

    private static ItemStack[] stacksFromBase64(String data) {
        if (data == null || data.equals("")) return new ItemStack[]{};

        ByteArrayInputStream inputStream;
        try {
            inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        } catch (IllegalArgumentException e) {
            return new ItemStack[]{};
        }
        BukkitObjectInputStream dataInput = null;
        ItemStack[] stacks = null;
        try {
            dataInput = new BukkitObjectInputStream(inputStream);
            stacks = new ItemStack[dataInput.readInt()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stacks == null) return new ItemStack[]{};
        for (int i = 0; i < stacks.length; i++) {
            try {
                stacks[i] = (ItemStack) dataInput.readObject();
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                try {
                    dataInput.close();
                } catch (IOException exception) {
                    LogUtils.severe("Failed to read fishing bag data");
                }
                return null;
            }
        }
        try {
            dataInput.close();
        } catch (IOException ignored) {
        }
        return stacks;
    }
}