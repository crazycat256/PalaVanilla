package fr.crazycat256.palavanilla.mgr;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

import static fr.crazycat256.palavanilla.PalaVanilla.server;

/**
 * Utility class to modify the durability of items and synchronize the changes with the client.
 */
public class DurabilityManager implements IManager {

    private static final Map<Material, Short> durabilityOverrides = new HashMap<>();


    public static void setMaxDurability(Material item, int maxDurability) {
        if (item.getMaxDurability() == 0) {
            throw new IllegalArgumentException("Item " + item + " has no durability.");
        }
        durabilityOverrides.put(item, (short) maxDurability);
    }

    @Override
    public void tick() {
        for (Player player : server.getOnlinePlayers()) {
            for (int i = 0; i < 36; i++) {
                processItem(player.getInventory().getItem(i), i, player);
            }

            for (int i = 0; i < 4; i++) {
                processItem(player.getInventory().getArmorContents()[i], i + 36, player);
            }
        }
    }


    public void processItem(ItemStack stack, int index, Player player) {
        if (stack == null) {
            return;
        }

        Material itemType = stack.getType();
        if (!durabilityOverrides.containsKey(itemType)) {
            return;
        }

        short maxDurability = durabilityOverrides.get(itemType);
        double factor = maxDurability / (double) stack.getType().getMaxDurability();
        NBTItem nbtItem = new NBTItem(stack);
        PlayerInventory inventory = player.getInventory();

        if (nbtItem.hasTag("ServerDamage") && nbtItem.hasTag("PrevDamage")) {

            short prevDamage = nbtItem.getShort("PrevDamage");
            short serverDamage = nbtItem.getShort("ServerDamage");

            short diff = (short) (stack.getDurability() - prevDamage);

            if (diff != 0) {

                serverDamage += diff;
                short newDurability = (short) Math.ceil((double) (serverDamage * stack.getType().getMaxDurability()) / maxDurability);

                nbtItem.setShort("ServerDamage", serverDamage);
                nbtItem.setShort("PrevDamage", newDurability);

                stack = nbtItem.getItem();
                stack.setDurability(newDurability);

                if (index < 36) {
                    inventory.setItem(index, stack);
                } else {
                    ItemStack[] armorContents = inventory.getArmorContents();
                    armorContents[index - 36] = stack;
                    inventory.setArmorContents(armorContents);
                }
            }
        } else {
            nbtItem.setShort("ServerDamage", (short) (stack.getDurability() * factor));
            nbtItem.setShort("PrevDamage", stack.getDurability());
            if (index < 36) {
                inventory.setItem(index, nbtItem.getItem());
            } else {
                ItemStack[] armorContents = inventory.getArmorContents();
                armorContents[index - 36] = nbtItem.getItem();
                inventory.setArmorContents(armorContents);
            }
        }
    }
}
