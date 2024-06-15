package fr.crazycat256.palavanilla.features;

import fr.crazycat256.palavanilla.mgr.AttributeManager;
import fr.crazycat256.palavanilla.mgr.DurabilityManager;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public class PalaSword extends ItemFeature implements ITransformer {

    public PalaSword(String name, Material itemType) {
        super(name, itemType);
    }

    @Override
    public void transform() {
        DurabilityManager.setMaxDurability(itemType, 4999);
        AttributeManager.setAttributeModifier(itemType, "generic.attackDamage", 16.25, EquipmentSlot.HAND);
    }
}
