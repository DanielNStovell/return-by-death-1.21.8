package net.danielstovell.rbd;

import net.danielstovell.rbd.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import java.util.Collections;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReturnByDeath implements ModInitializer {
	public static final String MOD_ID = "return-by-death";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static int tickCounter = 0;

	private static final Map<UUID, BlockPos> savePoints = new HashMap<>();
	private static final Map<UUID, RegistryKey<World>> saveDimensions = new HashMap<>();
	private static final Map<UUID, Float> saveYaw = new HashMap<>();
	private static final Map<UUID, Float> savePitch = new HashMap<>();

	@Override
	public void onInitialize() {
		ModSounds.registerSounds();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter >= 1200) { // 200 ticks = 10 seconds
				tickCounter = 0;
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					savePoints.put(player.getUuid(), player.getBlockPos());
					saveDimensions.put(player.getUuid(), player.getWorld().getRegistryKey());
					saveYaw.put(player.getUuid(), player.getYaw());
					savePitch.put(player.getUuid(), player.getPitch());
					player.sendMessage(Text.literal("vem vem"), false);
				}
			}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerWorld overworld = server.getOverworld();
			if (overworld != null) {
				overworld.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			UUID uuid = newPlayer.getUuid();
			BlockPos savePoint = savePoints.get(uuid);
			RegistryKey<World> saveDim = saveDimensions.get(uuid);
			Float yaw = saveYaw.get(uuid);
			Float pitch = savePitch.get(uuid);
			if (savePoint != null && saveDim != null && yaw != null && pitch != null) {
				ServerWorld world = newPlayer.getServer().getWorld(saveDim);
				if (world != null) {
					ServerPlayerEntity.Respawn respawn = new ServerPlayerEntity.Respawn(
							saveDim,
							savePoint,
							0.0f,
							true
					);
					newPlayer.setSpawnPoint(respawn, true);
					newPlayer.teleport(
							world,
							savePoint.getX() + 0.5,
							(double) savePoint.getY(),
							savePoint.getZ() + 0.5,
							Collections.emptySet(),
							yaw,
							pitch,
							false
					);
					newPlayer.sendMessage(Text.literal("VEM VEM!"), false);
				}
			} else {
				newPlayer.sendMessage(Text.literal("VEM VEM :c"), false);
			}
			

			int random_number = (int)(Math.random() * 2 + 1); // 1 to 2
			if (random_number == 1) {
				newPlayer.getWorld().playSound(null, newPlayer.getBlockPos(), ModSounds.RBD_OH,
						SoundCategory.AMBIENT, 3f, 1f);
			} else {
				newPlayer.getWorld().playSound(null, newPlayer.getBlockPos(), ModSounds.RBD_UEH,
						SoundCategory.AMBIENT, 3f, 1f);
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof ServerPlayerEntity player) {

				LOGGER.info(player.getName().getString() + " died");

				double player_x = player.getX();
				double player_y = player.getY();
				double player_z = player.getZ();

				LOGGER.info("Died at: (" + player_x + ", " + player_y + ", " + player_z + ")");

				PlayerInventory player_inventory = player.getInventory();

				if (!player_inventory.isEmpty()) {
					LOGGER.info("Inventory is not empty");
				} else {
					LOGGER.info("Inventory is empty");
				}
			}
		});
	}
}