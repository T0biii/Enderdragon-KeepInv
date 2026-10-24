package dev.t0biii.enderdragonkeepinv;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DragonListener implements Listener {

    private final EnderDragonKeepInv plugin;
    private final DragonTracker tracker;

    public DragonListener(EnderDragonKeepInv plugin, DragonTracker tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
    }

    /** Fires for freshly spawned dragons as well as dragons loaded from disk. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonAdded(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon && !dragon.isDead()) {
            tracker.add(dragon.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon) {
            tracker.remove(dragon.getUniqueId());
        }
    }

    /** Covers dragons that vanish without dying (e.g. /kill, plugins). Chunk unloads keep the dragon alive. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonRemoved(EntityRemoveEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon
                && event.getCause() != EntityRemoveEvent.Cause.UNLOAD) {
            tracker.remove(dragon.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!tracker.isActive()) {
            return;
        }
        event.setKeepInventory(true);
        event.getDrops().clear();
        if (plugin.getConfig().getBoolean("keep-inventory.keep-level", true)) {
            event.setKeepLevel(true);
            event.setShouldDropExperience(false);
        }
    }
}
