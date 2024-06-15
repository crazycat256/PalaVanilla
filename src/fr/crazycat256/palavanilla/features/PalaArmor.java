package fr.crazycat256.palavanilla.features;

import fr.crazycat256.palavanilla.mgr.DurabilityManager;
import fr.crazycat256.palavanilla.mgr.ItemEffectManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static fr.crazycat256.palavanilla.PalaVanilla.server;
import static org.bukkit.potion.PotionEffectType.*;

public class PalaArmor extends Feature implements ITransformer {

    private final ArmorTransform[] transforms;
    private final Map<Material, ArmorTransform> armorTypes;

    public PalaArmor(String name, Material helmet, Material chestplate, Material leggings, Material boots) {
        super(name);
        transforms = new ArmorTransform[] {
            // The .9 are to prevent effect display mods (such as lunar client's one) from blinking
            new ArmorTransform("Pala Helmet", helmet, 2860, NIGHT_VISION, 60.9, 2),
            new ArmorTransform("Pala Chestplate", chestplate, 4160, FIRE_RESISTANCE, 15.9, 1),
            new ArmorTransform("Pala Leggings", leggings, 3900, FAST_DIGGING, 15.9, 1),
            new ArmorTransform("Pala Boots", boots, 3380, SPEED, 15.9, 2)
        };

        armorTypes = new HashMap<>();
        for (ArmorTransform transform : transforms) {
            armorTypes.put(transform.itemType, transform);
        }
    }

    @Override
    public void transform() {
        for (ArmorTransform transform : transforms) {

            DurabilityManager.setMaxDurability(transform.itemType, transform.maxDamage);
            ItemEffectManager.addItemEffect(transform.itemType, transform.effectType, transform.effectSeconds, transform.effectLevel, EquipmentSlot.values());
        }
    }


    @Override
    public void tick() {
        Set<Material> armorTypesSet = this.getArmorTypes();

        for (Player player : server.getOnlinePlayers()) {
            for (int i = 0; i < 36; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                processArmor(armorTypesSet, stack);
            }
            for (int i = 0; i < 4; i++) {
                ItemStack stack = player.getInventory().getArmorContents()[i];
                processArmor(armorTypesSet, stack);
            }
        }
    }

    private void processArmor(Set<Material> armorTypesSet, ItemStack stack) {
        if (stack == null || !armorTypesSet.contains(stack.getType())) {
            return;
        }

        ArmorTransform transform = armorTypes.get(stack.getType());
        renameItemStack(stack, "§6" + transform.name + "§r");

    }

    public Set<Material> getArmorTypes() {
        return armorTypes.keySet();
    }

    private static class ArmorTransform {

        public final String name;
        public final Material itemType;
        public final int maxDamage;
        private final PotionEffectType effectType;
        private final double effectSeconds;
        private final int effectLevel;

        public ArmorTransform(String name, Material itemType, int maxDamage, PotionEffectType effectType, double effectSeconds, int effectLevel) {
            this.name = name;
            this.itemType = itemType;
            this.maxDamage = maxDamage;
            this.effectType = effectType;
            this.effectSeconds = effectSeconds;
            this.effectLevel = effectLevel;
        }
    }
}
