package au.com.addstar.naturalhorses;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Animals)) {
            return;
        }
        
        if (event.getSpawnReason() != SpawnReason.NATURAL) {
            return;
        }

        Location location = event.getLocation();
        World world = location.getWorld();

        if (world.getBiome(location.getBlockX(), location.getBlockZ()) != Biome.PLAINS) {
            return;
        }

        if ((NaturalHorses.RandomGen.nextDouble() * 100) < NaturalHorses.SpawnChance) {
            event.setCancelled(true);

            Chunk chunk = world.getChunkAt(location);
            HorseUtil.checkNearbyChunks(world, chunk);

            int CX = chunk.getX();  // chunk X
            int CZ = chunk.getZ();  // chunk Z
            int BX = CX << 4;       // corner block position of chunk
            int BZ = CZ << 4;       // corner block position of chunk

            // How many horses to spawn?
            int h = NaturalHorses.RandomGen.nextInt(5) + 2;
            Date date = new Date();
            long now = date.getTime();

            boolean EntitySpawned = false;
            for (int i = 0; i < h; i++) {
                // Spread horses out randomly around the selected chunk
                int RX = BX + NaturalHorses.RandomGen.nextInt(8);
                int RZ = BZ + NaturalHorses.RandomGen.nextInt(8);
                Location loc = new Location(world, RX, world.getHighestBlockYAt(RX, RZ) - 1, RZ);
                // Only spawn on grass
                if (world.getBlockTypeIdAt(loc) == Material.GRASS.getId()) {
                    Location entloc = new Location(world, RX + 1, loc.getY() + 2, RZ + 1);

                    // Check if WorldGuard allows us to spawn here
                    if (NaturalHorses.getInstance().CanSpawnMob(entloc)) {
                        if ((NaturalHorses.getInstance().LastSpawn > 0) && (now < (NaturalHorses.getInstance().LastSpawn + (NaturalHorses.SpawnDelay * 1000)))) {
                            NaturalHorses.getInstance().Debug("Too soon.. Refusing to spawn anything");
                            return;
                        }

                        // Spawn the horse/donkey (YAY!! Horse API! :)
                        Horse horse = (Horse) world.spawnEntity(entloc, EntityType.HORSE);
                        horse = HorseUtil.getRandomHorse(horse);

                        NaturalHorses.getInstance().Debug(horse.getVariant() + " (" + horse.getColor() + " " + horse.getStyle() + ": " + horse.toString());
                        EntitySpawned = true;
                    }
                    else {
                        NaturalHorses.getInstance().Debug("Entity spawning disabled here");
                    }
                }
                else {
                    NaturalHorses.getInstance().Debug("Not grass at: " + world.getName() + " / X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
                }
            }

            // Tell everyone where the horse herd has been spawned (for debugging only)
            if (EntitySpawned) {
                NaturalHorses.getInstance().LastSpawn = now;

                String msg = ChatColor.YELLOW + "[NaturalHorses] " + ChatColor.WHITE + "Horses spawned (" + h + "): X:" + BX + " Y:" + world.getHighestBlockAt(BX, BZ).getY() + " Z:" + BZ;
                if (NaturalHorses.BroadcastLocation) {
                    NaturalHorses.getInstance().getServer().broadcastMessage(msg);
                }
                else if (NaturalHorses.DebugEnabled) {
                    NaturalHorses.getInstance().Debug(msg);
                }
            }
        }
    }
}
