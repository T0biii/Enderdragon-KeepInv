package dev.t0biii.enderdragonkeepinv;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of all living Ender Dragons. The set is persisted so that the state
 * survives restarts even while the dragon's chunks are not loaded.
 */
public final class DragonTracker {

    private final EnderDragonKeepInv plugin;
    private final File file;
    private final Set<UUID> dragons = ConcurrentHashMap.newKeySet();

    public DragonTracker(EnderDragonKeepInv plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public boolean isActive() {
        return !dragons.isEmpty();
    }

    public int count() {
        return dragons.size();
    }

    public void add(UUID id) {
        if (dragons.add(id)) {
            plugin.getLogger().info("Ender Dragon is alive - keep inventory is now ACTIVE.");
            save();
        }
    }

    public void remove(UUID id) {
        if (dragons.remove(id)) {
            if (dragons.isEmpty()) {
                plugin.getLogger().info("No Ender Dragon alive anymore - keep inventory is now INACTIVE.");
            }
            save();
        }
    }

    public void clear() {
        dragons.clear();
        save();
    }

    public void load() {
        if (!file.isFile()) {
            return;
        }
        List<String> stored = YamlConfiguration.loadConfiguration(file).getStringList("dragons");
        for (String raw : stored) {
            try {
                dragons.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid UUID in data.yml: " + raw);
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("dragons", dragons.stream().map(UUID::toString).toList());
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save data.yml: " + e.getMessage());
        }
    }

    /** Drops stale entries for loaded dragons and picks up dragons that are already loaded. */
    public void scanWorlds() {
        for (UUID id : dragons.toArray(UUID[]::new)) {
            Entity entity = Bukkit.getEntity(id);
            if (entity != null && (entity.isDead() || !entity.isValid())) {
                remove(id);
            }
        }
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.THE_END) {
                continue;
            }
            for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
                if (!dragon.isDead()) {
                    add(dragon.getUniqueId());
                }
            }
            DragonBattle battle = world.getEnderDragonBattle();
            if (battle != null) {
                EnderDragon dragon = battle.getEnderDragon();
                if (dragon != null && !dragon.isDead()) {
                    add(dragon.getUniqueId());
                }
            }
        }
    }
}
