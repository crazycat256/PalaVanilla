package fr.crazycat256.palavanilla;

import de.tr7zw.nbtapi.NBTEntity;
import fr.crazycat256.palavanilla.features.*;
import fr.crazycat256.palavanilla.mgr.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class of the PalaVanilla plugin.
 * @author crazycat256
 */
public class PalaVanilla extends JavaPlugin implements Listener {

    public static Server server;

    private static final List<IManager> managers = new ArrayList<>();
    private static final List<Feature> features = new ArrayList<>();

    private static PalaArmor palaArmor;


    @Override
    public void onEnable() {

        server = Bukkit.getServer();

        populate();

        for (Feature feature : features) {
            if (feature instanceof ITransformer) {
                ((ITransformer) feature).transform();
            }
        }

        Bukkit.getScheduler().runTaskTimer(this, this::onTick, 0, 1);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private static void populate() {

        // Managers
        managers.add(new DurabilityManager());
        managers.add(new AttributeManager());
        managers.add(new ItemEffectManager());


        // Features
        palaArmor = addFeature(new PalaArmor("Pala Armor", Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS));
        addFeature(new PalaSword("Pala Sword", Material.DIAMOND_SWORD));
        addFeature(new PotionLauncher("Potion Launcher", Material.SADDLE));

        addFeature(new Stick("Stick of God", Material.DIAMOND_HOE, 8, 24, player -> {
            player.removePotionEffect(PotionEffectType.SPEED); // This is needed to override the effect of the PalaBoots
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 1));
            NBTEntity nbtEntity = new NBTEntity(player);
            nbtEntity.setInteger("AbsorptionAmount", 6);
        }));

        addFeature(new Stick("Heal Stick", Material.GOLD_HOE, 15, 8.5, player -> {
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 6));
        }));

        addFeature(new Stick("Strength stick", Material.IRON_HOE, 15, 14, player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 2));
        }));

    }

    public void onTick() {
        managers.forEach(IManager::tick);
        features.forEach(Feature::tick);
    }


    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) return;

        for (Feature feature : features) {
            if (feature instanceof ItemFeature) {
                ItemFeature itemHandler = (ItemFeature) feature;
                itemHandler.click(event);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player && event.getCause() != EntityDamageEvent.DamageCause.CONTACT)) {
            return;
        }

        Player target = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = target.getInventory().getArmorContents()[i];
            if (stack == null || !palaArmor.getArmorTypes().contains(stack.getType())) {
                return;
            }
        }

        if (damager.getInventory().getItemInHand().getType() != Material.DIAMOND_SWORD) {
            return;
        }


        // Set damage to 0 to prevent the random effect of protection
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            try {
                event.setDamage(modifier, 0);
            } catch (UnsupportedOperationException e) {
                // Empty catch block
            }
        }


        Block block = damager.getLocation().getBlock();
        Material[] badBlocks = {Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.LADDER, Material.VINE, Material.WEB};
        boolean crit = !damager.isOnGround() && damager.getFallDistance() > 0 && Arrays.stream(badBlocks).noneMatch(b -> b == block.getType() && !damager.hasPotionEffect(PotionEffectType.BLINDNESS));
        int sharpnessLevel = damager.getInventory().getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        double protectionLevel = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = target.getInventory().getArmorContents()[i];
            if (stack == null) {
                continue;
            }
            protectionLevel += stack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        }
        protectionLevel /= 4;

        int strengthLevel = 0;
        for (PotionEffect effect : damager.getActivePotionEffects()) {

            // Idk why but comparing effect.getType() == PotionEffectType.INCREASE_DAMAGE doesn't work
            if (effect.getType().getId() == 5) {
                strengthLevel = effect.getAmplifier() + 1;
                break;
            }
        }

        double initialHealth = target.getHealth();
        NBTEntity nbtTarget = new NBTEntity(target);
        double absorption = nbtTarget.getDouble("AbsorptionAmount");

        double initialDamage = 1.27;
        double protectionReduction = 0.1575;
        double sharpnessBonus = 0.162;
        double strengthBonus = 0.36;
        double critFactor = 1.5;

        double damage = initialDamage - protectionLevel * protectionReduction + sharpnessLevel * sharpnessBonus + strengthLevel * strengthBonus;
        if (crit) {
            damage *= critFactor;
        }

        if (initialHealth + absorption - damage <= 0) {
            event.setDamage(100);
        } else {
            double remainingDamage = damage;
            double newAbsorption = Math.max(0, absorption - remainingDamage);
            remainingDamage = remainingDamage - (absorption - newAbsorption);
            double newHealth = Math.max(0, initialHealth - remainingDamage);

            nbtTarget.setDouble("AbsorptionAmount", newAbsorption);
            target.setHealth(newHealth);
        }

    }

    private static <T extends Feature> T addFeature(T t) {
        features.add(t);
        return t;
    }
}
