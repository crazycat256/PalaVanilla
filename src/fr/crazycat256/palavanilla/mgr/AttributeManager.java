package fr.crazycat256.palavanilla.mgr;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static fr.crazycat256.palavanilla.PalaVanilla.server;

public class AttributeManager implements IManager {

    private static final Map<Material, ItemAttributeModifier> attributeModifiers = new HashMap<>();

    public static void setAttributeModifier(Material item, String attributeName, double value, EquipmentSlot... slots) {
        attributeModifiers.put(item, new ItemAttributeModifier(attributeName, value, slots));
    }

    @Override
    public void tick() {
        for (Player player : server.getOnlinePlayers()) {
            for (int i = 0; i < 36; i++) {

                ItemStack stack = player.getInventory().getItem(i);
                if (stack == null) {
                    continue;
                }
                Material itemType = stack.getType();

                if (!attributeModifiers.containsKey(itemType)) {
                    continue;
                }

                ItemAttributeModifier modifier = attributeModifiers.get(itemType);
                modifier.apply(stack);
            }
            for (int i = 0; i < 4; i++) {
                ItemStack stack = player.getInventory().getArmorContents()[i];
                if (stack == null) {
                    continue;
                }

                Material itemType = stack.getType();

                if (!attributeModifiers.containsKey(itemType)) {
                    continue;
                }

                ItemAttributeModifier modifier = attributeModifiers.get(itemType);
                modifier.apply(stack);
            }
        }
    }


    private static class ItemAttributeModifier {

        private static final Random r = new Random();

        private final String attributeName;
        private final double value;
        private final EquipmentSlot[] slots;

        public ItemAttributeModifier(String attributeName, double value, EquipmentSlot... slots) {
            this.attributeName = attributeName;
            this.value = value;
            this.slots = slots;
        }

        public void apply(ItemStack stack) {
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                return;
            }
            NBTItem nbt = new NBTItem(stack);

            if (!nbt.hasTag("AttributeModifiers")) {
                nbt.addCompound("AttributeModifiers");
            }

            NBTCompoundList attributeModifiers = nbt.getCompoundList("AttributeModifiers");

            NBTListCompound compound = nbt.getCompoundList("AttributeModifiers").addCompound(); // Déplacez cette ligne en dehors de la boucle

            slotLoop:
            for (EquipmentSlot slot : slots) {

                for (ReadWriteNBT existingModifier : attributeModifiers) {
                    if (existingModifier.getString("AttributeName").equals(attributeName)
                            && existingModifier.getString("Slot").equals(slot.name())) {
                        if (existingModifier.getDouble("Amount") != value) {
                            existingModifier.setDouble("Amount", value);
                            nbt.applyNBT(stack);
                        }
                        continue slotLoop;
                    }
                }

                compound.setString("AttributeName", attributeName);
                compound.setString("Name", attributeName);
                compound.setString("Slot", slot.name());
                compound.setDouble("Amount", value);
                compound.setInteger("Operation", 0);
                compound.setLong("UUIDMost", r.nextLong());
                compound.setLong("UUIDLeast", r.nextLong());
            }

            nbt.applyNBT(stack);
        }
    }
}
