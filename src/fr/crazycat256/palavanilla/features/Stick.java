package fr.crazycat256.palavanilla.features;

import fr.crazycat256.palavanilla.mgr.DurabilityManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static fr.crazycat256.palavanilla.PalaVanilla.server;

public class Stick extends ItemFeature implements ITransformer {

    private final int itemDurability;
    private final double cooldown;
    private final Consumer<Player> action;
    private final Map<Player, Long> lastUse;

    public Stick(String name, Material item, int itemDurability, double cooldown, Consumer<Player> action) {
        super(name, item);
        this.itemDurability = itemDurability;
        this.cooldown = cooldown * 1000;
        this.action = action;
        this.lastUse = new HashMap<>();
    }

    @Override
    public void transform() {
        DurabilityManager.setMaxDurability(itemType, itemDurability);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getInventory().getItemInHand();
        if (stack.getType() != itemType) {
            return;
        }
        event.setCancelled(true);
        if (lastUse.containsKey(player) && System.currentTimeMillis() - lastUse.get(player) < cooldown) {
            player.sendMessage("You must wait " + (int) (cooldown - (System.currentTimeMillis() - lastUse.get(player))) / 1000 + " seconds before using §6" + name + "§r again.");
            return;
        }
        lastUse.put(player, System.currentTimeMillis());
        action.accept(player);
        if (player.getGameMode() != GameMode.CREATIVE) {
            damageItem(player.getInventory().getItemInHand());
            if (stack.getDurability() > stack.getType().getMaxDurability()) {
                player.getInventory().setItemInHand(null);
            }
            player.updateInventory();
        }
    }

    @Override
    public void tick() {
        super.tick();
        lastUse.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > cooldown);
        for (Player player : server.getOnlinePlayers()) {
            for (int i = 0; i < 36; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack != null && stack.getType() == itemType) {
                    if (lastUse.get(player) == null || System.currentTimeMillis() - lastUse.get(player) > cooldown) {
                        if (stack.getEnchantments().get(Enchantment.DURABILITY) == null || stack.getEnchantments().get(Enchantment.DURABILITY) < 3) {
                            stack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
                            player.updateInventory();
                        }
                    } else {
                        stack.removeEnchantment(Enchantment.DURABILITY);
                        player.updateInventory();
                    }
                }
            }
        }
    }


    private static void damageItem(ItemStack item) {

        ItemMeta meta = item.getItemMeta();

        int unbreakingLevel = meta.getEnchantLevel(Enchantment.DURABILITY);

        if (unbreakingLevel > 0 && Math.random() > 1.0 / (unbreakingLevel + 1.0)) {
            return;
        }

        item.setDurability((short) (item.getDurability() + 1));
    }
}
