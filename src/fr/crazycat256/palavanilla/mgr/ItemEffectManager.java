package fr.crazycat256.palavanilla.mgr;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static fr.crazycat256.palavanilla.PalaVanilla.server;

public class ItemEffectManager implements IManager {

    private static final Map<Material, List<ItemEffect>> itemEffects = new HashMap<>();


    public static void addItemEffect(Material item, PotionEffectType effect, double seconds, int level, EquipmentSlot... slots) {
        itemEffects.computeIfAbsent(item, k -> new ArrayList<>()).add(new ItemEffect(effect, (int) (seconds * 20), level - 1, slots));
    }


    @Override
    public void tick() {
        for (Player player : server.getOnlinePlayers()) {
            for (int i = 0; i < 36; i++) {
                processItem(player.getInventory().getItem(i), i, player);
            }

            for (int i = 0; i < 4; i++) {
                processItem(player.getInventory().getArmorContents()[i], 36 + i, player);
            }
        }
    }

    private void processItem(ItemStack stack, int slot, Player player) {
        if (stack == null) {
            return;
        }

        Material itemType = stack.getType();

        if (!itemEffects.containsKey(itemType)) {
            return;
        }

        List<ItemEffect> effects = itemEffects.get(itemType);
        for (ItemEffect itemEffect : effects) {

            for (PotionEffect activeEffect : player.getActivePotionEffects()) {
                if (activeEffect.getType().equals(itemEffect.effect.getType())) {
                    if (activeEffect.getAmplifier() > itemEffect.effect.getAmplifier()) {
                        return;
                    }
                    break;
                }
            }

            for (EquipmentSlot equipmentSlot : itemEffect.slots) {
                if (equipmentSlot == getSlot(player, slot)) {
                    player.removePotionEffect(itemEffect.effect.getType());
                    player.addPotionEffect(itemEffect.effect);
                }
            }
        }
    }

    private static EquipmentSlot getSlot(Player player, int slot) {
        if (slot == player.getInventory().getHeldItemSlot()) {
            return EquipmentSlot.HAND;
        } else {
            switch (slot) {
                case 36:
                    return EquipmentSlot.FEET;
                case 37:
                    return EquipmentSlot.LEGS;
                case 38:
                    return EquipmentSlot.CHEST;
                case 39:
                    return EquipmentSlot.HEAD;
                default:
                    return null;
            }
        }
    }

    private static class ItemEffect {
        private final PotionEffect effect;
        private final EquipmentSlot[] slots;

        public ItemEffect(PotionEffectType effect, int duration, int amplifier, EquipmentSlot... slots) {
            this.effect = new PotionEffect(effect, duration, amplifier, true);
            this.slots = slots;
        }
    }
}
