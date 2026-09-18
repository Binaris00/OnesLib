package com.binaris.oneslib.test;

import java.util.List;

import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneMorph;
import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.common.entity.OneProjectile;
import com.binaris.oneslib.common.state.OneAnimation;
import com.binaris.oneslib.common.state.OneState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tocraft.walkers.api.PlayerShape;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientSelfTest {

    private static int ticks;
    private static int phase;
    private static String lastAnimation = "none";
    private static double maxProjectileDistance;

    private ClientSelfTest() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Boolean.getBoolean("oneslib.clienttest")) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.hasSingleplayerServer()) {
            return;
        }

        MinecraftServer server = minecraft.getSingleplayerServer();
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
        if (serverPlayer == null) {
            return;
        }

        ticks++;

        switch (phase) {
            case 0 -> {
                if (ticks >= 20) {
                    serverPlayer.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
                    advance();
                }
            }
            case 1 -> {
                morph(serverPlayer, "siren_head");
                activate(serverPlayer, "siren_head_laser");
                advance();
            }
            case 2 -> {
                watchAnimation(player, "siren_head");
                if (ticks % 20 == 0) {
                    probe(player);
                }
                if (ticks >= 160) {
                    OnesApi.clearCooldown(serverPlayer, "siren_head_laser");
                    TestMod.LOGGER.info("[selftest] before replay: active={} cooldown={}",
                            OnesApi.isActive(serverPlayer, "siren_head_laser"),
                            OnesApi.isOnCooldown(serverPlayer, "siren_head_laser"));
                    activate(serverPlayer, "siren_head_laser");
                    advance();
                }
            }
            case 3 -> {
                watchAnimation(player, "siren_head");
                if (ticks % 20 == 0) {
                    probe(player);
                }
                if (ticks >= 60) {
                    demorph(serverPlayer);
                    advance();
                }
            }
            case 4 -> {
                if (ticks >= 5) {
                    logFeet(player);
                    advance();
                }
            }
            case 5 -> {
                morph(serverPlayer, "tails");
                activate(serverPlayer, "tails_fly");
                advance();
            }
            case 6 -> {
                watchAnimation(player, "tails");
                if (ticks >= 15) {
                    logMayfly(serverPlayer, "tails");
                    demorph(serverPlayer);
                    advance();
                }
            }
            case 7 -> {
                if (ticks >= 5) {
                    logFeet(player);
                    logMayfly(serverPlayer, "after demorph");
                    advance();
                }
            }
            case 8 -> {
                morph(serverPlayer, "dragon");
                serverPlayer.setXRot(-60.0F);
                serverPlayer.setYRot(0.0F);
                activate(serverPlayer, "dragon_beam");
                activate(serverPlayer, "dragon_fireball");
                maxProjectileDistance = 0.0D;
                advance();
            }
            case 9 -> {
                watchAnimation(player, "dragon");
                maxProjectileDistance = Math.max(maxProjectileDistance, projectileDistance(player));
                if (ticks >= 120) {
                    logMayfly(serverPlayer, "dragon");
                    TestMod.LOGGER.info("[selftest] projectile max distance: {}",
                            String.format("%.1f", maxProjectileDistance));
                    demorph(serverPlayer);
                    advance();
                }
            }
            case 10 -> {
                if (ticks >= 5) {
                    logFeet(player);
                    logMayfly(serverPlayer, "after demorph");
                    advance();
                }
            }
            case 11 -> {
                morph(serverPlayer, "imp");
                activate(serverPlayer, "imp_attack");
                advance();
            }
            case 12 -> {
                watchAnimation(player, "imp");
                if (ticks >= 25) {
                    demorph(serverPlayer);
                    advance();
                }
            }
            case 13 -> {
                if (ticks >= 5) {
                    logFeet(player);
                    advance();
                }
            }
            case 14 -> {
                morph(serverPlayer, "giant_ogre");
                activate(serverPlayer, "giant_ogre_grab");
                advance();
            }
            case 15 -> {
                watchAnimation(player, "giant_ogre");
                if (ticks >= 25) {
                    demorph(serverPlayer);
                    advance();
                }
            }
            case 16 -> {
                if (ticks >= 5) {
                    logFeet(player);
                    advance();
                }
            }
            case 17 -> {
                if (ticks >= 20) {
                    TestMod.LOGGER.info("[selftest] done");
                    advance();
                }
            }
            default -> minecraft.stop();
        }
    }

    private static void morph(ServerPlayer serverPlayer, String oneId) {
        OnesApi.morph(serverPlayer, oneId);
        TestMod.LOGGER.info("[selftest] morph: {}",
                OnesApi.currentOne(serverPlayer).map(One::id).orElse("none"));
    }

    private static void activate(ServerPlayer serverPlayer, String abilityId) {
        TestMod.LOGGER.info("[selftest] activate {}: {}", abilityId,
                OnesApi.activate(serverPlayer, abilityId));
    }

    private static void demorph(ServerPlayer serverPlayer) {
        OnesApi.demorph(serverPlayer);
        TestMod.LOGGER.info("[selftest] demorph");
    }

    private static void probe(LocalPlayer player) {
        LivingEntity shape = PlayerShape.getCurrentShape(player);
        OneAnimation data = shape instanceof OneMorph morph ? morph.oneAnimation() : null;
        OneAnimation read = shape == null ? null : OneState.read(shape);
        TestMod.LOGGER.info("[selftest] probe data: {} shapeFeet: {} ownerFeet: {} read: {}",
                data == null ? "none" : data.name(),
                shape == null ? "-" : shape.getItemBySlot(EquipmentSlot.FEET).getItem(),
                player.getItemBySlot(EquipmentSlot.FEET).getItem(),
                read == null ? "none" : read.name());
    }

    private static void watchAnimation(LocalPlayer player, String oneId) {
        LivingEntity shape = PlayerShape.getCurrentShape(player);
        OneAnimation animation = shape == null ? null : OneState.read(shape);
        String name = animation == null ? "none" : animation.name();
        if (!name.equals(lastAnimation)) {
            lastAnimation = name;
            TestMod.LOGGER.info("[selftest] {} animation -> {}", oneId, name);
        }
    }

    private static double projectileDistance(LocalPlayer player) {
        List<OneProjectile> projectiles = player.level().getEntitiesOfClass(OneProjectile.class,
                player.getBoundingBox().inflate(128.0D));
        if (projectiles.isEmpty()) {
            return 0.0D;
        }
        return Math.sqrt(player.distanceToSqr(projectiles.get(0)));
    }

    private static void logFeet(LocalPlayer player) {
        TestMod.LOGGER.info("[selftest] feet: {}", player.getItemBySlot(EquipmentSlot.FEET).getItem());
    }

    private static void logMayfly(ServerPlayer serverPlayer, String label) {
        TestMod.LOGGER.info("[selftest] {} mayfly: {} flying: {}", label,
                serverPlayer.getAbilities().mayfly, serverPlayer.getAbilities().flying);
    }

    private static void advance() {
        phase++;
        ticks = 0;
    }
}
