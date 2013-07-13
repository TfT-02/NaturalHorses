package au.com.addstar.naturalhorses;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            return;
        }
        if (!event.getWorld().getName().equals(NaturalHorses.HorseWorld)) {
            return;
        }

        World world = event.getWorld();
        Chunk chunk = event.getChunk();

        // Get chunk information
        ChunkSnapshot chunkdata = chunk.getChunkSnapshot(false, true, false);

        String bname = chunkdata.getBiome(0, 0).name();
        if (chunkdata.getBiome(0, 0).name() == "PLAINS" && chunkdata.getBiome(0, 15).name() == "PLAINS" && chunkdata.getBiome(15, 0).name() == "PLAINS" && chunkdata.getBiome(15, 15).name() == "PLAINS") {
            bname = "FULL PLAINS";
        }

        // Only spawn horses in "full plains" (all four corners are plains)
        if (bname == "FULL PLAINS") {
            HorseUtil.handleHorseSpawning(world, chunk);
        }
    }
}
