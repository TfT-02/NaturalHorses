package au.com.addstar.naturalhorses;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class NaturalHorses extends JavaPlugin {
    public static NaturalHorses instance;
    public PluginDescriptionFile pdfFile = null;
    public PluginManager pm = null;
    public ConfigManager cfg = new ConfigManager(this);
    public WorldGuardPlugin WG;
    public RegionManager RM;
    public long LastSpawn = 0;
    public static Random RandomGen = new Random();

    // Pluing settings
    public static boolean DebugEnabled = false;
    public static boolean BroadcastLocation = false;
    public static String HorseWorld = "survival";
    public static int SpawnDelay = 30;
    public static int ChunkRadius = 2;
    public static double SpawnChance = 0.01;
    public static double DonkeyChance = 10;
    public static int SpawnedHorses = 0;
    public static int MaxHorses = 15;

    public static NaturalHorses getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        // Register necessary events
        pdfFile = this.getDescription();
        pm = this.getServer().getPluginManager();

        // Read (or initialise) plugin config file
        cfg.LoadConfig(getConfig());

        // Save the default config (if one doesn't exist)
        saveDefaultConfig();

        // Make sure the world is valid
        if (getServer().getWorld(HorseWorld) == null) {
            Log("World \"" + HorseWorld + "\" does not exist!");
            Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has NOT been enabled!");
            this.setEnabled(false);
            return;
        }

        WG = getWorldGuard();
        if (WG == null) {
            Log("WorldGuard not detected, integration disabled.");
        }
        else {
            try {
                RM = WG.getRegionManager(getServer().getWorld(HorseWorld));
                if (RM == null) {
                    Warn("WorldGuard integration failed! getRegionManager returned null");
                    WG = null;
                }
                else {
                    Log("WorldGuard integration successful.");
                }
            }
            catch (Exception e) {
                Warn("WorldGuard integration failed! Exception in getRegionManager: " + e.hashCode() + " - " + e.getLocalizedMessage());
                RM = null;
                WG = null;
            }
        }

        pm.registerEvents(new ChunkListener(), this);
//        pm.registerEvents(new EntityListener(), this);
        Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled");
    }

    @Override
    public void onDisable() {
        getConfig().set("spawned-horses", SpawnedHorses);
        this.saveConfig();
    }

    public void Log(String data) {
        getLogger().info(data);
    }

    public void Warn(String data) {
        getLogger().warning(data);
    }

    public void Debug(String data) {
        if (DebugEnabled) {
            getLogger().info(data);
        }
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    public boolean CanSpawnMob(Location loc) {
        if (WG == null) {
            return true;
        }
        ApplicableRegionSet set = RM.getApplicableRegions(loc);
        if (set == null) {
            return true;
        }
        return set.allows(DefaultFlag.MOB_SPAWNING);
    }
}
