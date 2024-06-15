package fr.crazycat256.palavanilla.features;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PotionLauncher extends ItemFeature {

    private static final Random itemRand = new Random();

    public PotionLauncher(String name, Material item) {
        super(name, item);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        int slot = -1;
        ItemStack stack = null;
        for (int i = 0; i < 36; i++) {
             stack = player.getInventory().getItem(i);

            // See ItemPotion#isSplash from forge
            if (stack != null && stack.getType() == Material.POTION && (stack.getDurability() & 16384) != 0) {
                slot = i;
                break;
            }
        }

        if (slot == -1) {
            return;
        }

        ThrownPotion pot = throwPot(stack, player.getWorld(), player);

        if (stack.getAmount() <= 0) {
            player.getInventory().setItem(slot, null);
        }

        pot.setVelocity(pot.getVelocity().multiply(1.35));
        player.updateInventory();
    }


    public ThrownPotion throwPot(ItemStack potionStack, World world, Player player) {
        world.playSound(player.getLocation(), Sound.SHOOT_ARROW, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        ThrownPotion thrownPotion = player.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(potionStack.clone());

        if (player.getGameMode() != GameMode.CREATIVE) {
            potionStack.setAmount(potionStack.getAmount() - 1);
        }

        return thrownPotion;
    }
}
