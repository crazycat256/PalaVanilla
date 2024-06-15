package fr.crazycat256.palavanilla.features;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Feature  {

    public final String name;

    public Feature(String name) {
        this.name = name;
    }

    public void tick() {}

    public static void renameItemStack(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§6" + name + "§r");
        stack.setItemMeta(meta);
    }
}
