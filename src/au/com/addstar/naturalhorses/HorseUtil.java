package au.com.addstar.naturalhorses;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;

public class HorseUtil {

    public static void handleHorseSpawning(World world, Chunk chunk) {
        int CX = chunk.getX();  // chunk X
        int CZ = chunk.getZ();  // chunk Z
        int BX = CX << 4;       // corner block position of chunk
        int BZ = CZ << 4;       // corner block position of chunk

        if ((NaturalHorses.RandomGen.nextDouble() * 100) < NaturalHorses.SpawnChance) {

            checkNearbyChunks(world, chunk);

            // How many horses to spawn?
            int amount = NaturalHorses.RandomGen.nextInt(2) + 2;
            Date date = new Date();
            long now = date.getTime();

            boolean EntitySpawned = false;
            for (int i = 0; i < amount; i++) {
                if (NaturalHorses.SpawnedHorses >= NaturalHorses.MaxHorses) {
                    NaturalHorses.getInstance().Debug("Maximum horses reached! (" + NaturalHorses.SpawnedHorses + "/" + NaturalHorses.MaxHorses + ")");
                    return;
                }

                // Spread horses out randomly around the selected chunk
                int RX = BX + NaturalHorses.RandomGen.nextInt(8);
                int RZ = BZ + NaturalHorses.RandomGen.nextInt(8);
                Location loc = new Location(world, RX, world.getHighestBlockYAt(RX, RZ) - 1, RZ);

                // Only spawn on grass
                if (world.getBlockTypeIdAt(loc) != Material.GRASS.getId()) {
                    NaturalHorses.getInstance().Debug("Not grass at: " + world.getName() + " / X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
                    return;
                }

                Location entloc = new Location(world, RX + 1, loc.getY() + 2, RZ + 1);

                // Check if WorldGuard allows us to spawn here
                if (!NaturalHorses.getInstance().CanSpawnMob(entloc)) {
                    NaturalHorses.getInstance().Debug("Entity spawning disabled here");
                    return;
                }

                if ((NaturalHorses.getInstance().LastSpawn > 0) && (now < (NaturalHorses.getInstance().LastSpawn + (NaturalHorses.SpawnDelay * 1000)))) {
                    NaturalHorses.getInstance().Debug("Too soon.. Refusing to spawn anything");
                    return;
                }

                // Spawn the horse/donkey (YAY!! Horse API! :)
                Horse horse = (Horse) world.spawnEntity(entloc, EntityType.HORSE);
                horse = HorseUtil.getRandomHorse(horse);
                NaturalHorses.SpawnedHorses += amount;

                NaturalHorses.getInstance().Debug(horse.getVariant() + " (" + horse.getColor() + " " + horse.getStyle() + ": " + horse.toString());
                EntitySpawned = true;
            }

            // Tell everyone where the horse herd has been spawned (for debugging only)
            if (EntitySpawned) {
                NaturalHorses.getInstance().LastSpawn = now;

                String msg = ChatColor.YELLOW + "[NaturalHorses] " + ChatColor.WHITE + "Horses spawned (" + amount + "): X:" + BX + " Y:" + world.getHighestBlockAt(BX, BZ).getY() + " Z:" + BZ;
                if (NaturalHorses.BroadcastLocation) {
                    NaturalHorses.getInstance().getServer().broadcastMessage(msg);
                }
                else if (NaturalHorses.DebugEnabled) {
                    NaturalHorses.getInstance().Debug(msg);
                }

            }
        }
    }

    public static void checkNearbyChunks(World world, Chunk chunk) {
        int CX = chunk.getX();  // chunk X
        int CZ = chunk.getZ();  // chunk Z
        int BX = CX << 4;       // corner block position of chunk
        int BZ = CZ << 4;       // corner block position of chunk

        // Surrounding chunks must also be empty (grid loop)
        int from = (0 - NaturalHorses.ChunkRadius);
        int to = NaturalHorses.ChunkRadius;

        for (int x = from; x < to; x++) {
            for (int z = from; z < to; z++) {
                Entity[] ents = world.getChunkAt(CX + x, CZ + z).getEntities();
                for (Entity ent : ents) {
                    // Look for other living entities
                    if (ent.getType().isAlive()) {
                        NaturalHorses.getInstance().Debug("Area not empty: " + (CX + x) + " / " + (CZ + z) + " (not spawning horses)");
                        return;
                    }
                }
            }
        }

        NaturalHorses.getInstance().Debug("Chunk: " + world.getName() + ": " + CX + "/" + CZ + " = " + BX + " / " + BZ);
    }

    public static Horse getRandomHorse(Horse horse) {
        // Donkey or horse?
        if ((NaturalHorses.RandomGen.nextDouble() * 100) < NaturalHorses.DonkeyChance) {
            horse.setVariant(Variant.DONKEY);
        }
        else {
            horse.setVariant(Variant.HORSE);
        }

        // Horse colours/markings
        if (horse.getVariant() == Variant.HORSE || horse.getVariant() == Variant.DONKEY) {
            int style = NaturalHorses.RandomGen.nextInt(Style.values().length);
            int color = NaturalHorses.RandomGen.nextInt(Color.values().length);

            horse.setStyle(Style.values()[style]);
            horse.setColor(Color.values()[color]);
        }

        // Set the horse MaxHealth: 15 - 30 half hearts 
        int maxHealth = 15 + NaturalHorses.RandomGen.nextInt(8) + NaturalHorses.RandomGen.nextInt(9);
        horse.setMaxHealth(maxHealth);

        // Prevent horses from despawning
        horse.setRemoveWhenFarAway(false);

        return horse;
    }
}
