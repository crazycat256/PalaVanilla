package fr.crazycat256.palavanilla.features;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static fr.crazycat256.palavanilla.PalaVanilla.server;

public abstract class ItemFeature extends Feature {

    public final Material itemType;

    public ItemFeature(String name, Material itemType) {
        super(name);
        this.itemType = itemType;
    }

    public void click(PlayerInteractEvent event) {
        ItemStack stack = event.getPlayer().getItemInHand();
        if (stack.getType() == itemType && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            onClick(event);
        }
    }

    public void onClick(PlayerInteractEvent player) {}

    @Override
    public void tick() {
        for (Player player : server.getOnlinePlayers()) {
            for (int i = 0; i < 36; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack != null && stack.getType() == itemType) {
                    renameItemStack(stack, "§6" + name + "§r");
                }
            }
            for (int i = 0; i < 4; i++) {
                ItemStack stack = player.getInventory().getArmorContents()[i];
                if (stack != null && stack.getType() == itemType) {
                    renameItemStack(stack, "§6" + name + "§r");
                }
            }
        }
    }
}
