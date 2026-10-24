package dev.t0biii.enderdragonkeepinv;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnderDragonKeepInv extends JavaPlugin {

    /** bStats service id. While this is 0, bStats is not started at all. */
    private static final int BSTATS_ID = 0;

    private static final String GITHUB_REPOSITORY = "T0biii/Enderdragon-KeepInv";
    private static final long UPDATE_CHECK_INTERVAL_TICKS = 12L * 60 * 60 * 20;

    private DragonTracker tracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        tracker = new DragonTracker(this);
        tracker.load();
        tracker.scanWorlds();

        getServer().getPluginManager().registerEvents(new DragonListener(this, tracker), this);

        PluginCommand command = getCommand("enderdragonkeepinv");
        if (command != null) {
            DragonCommand handler = new DragonCommand(this, tracker);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        setupMetrics();
        setupUpdateChecker();
    }

    @Override
    public void onDisable() {
        if (tracker != null) {
            tracker.save();
        }
    }

    private void setupMetrics() {
        if (!getConfig().getBoolean("metrics.enabled", true)) {
            getLogger().info("bStats metrics are disabled in the config.");
            return;
        }
        if (BSTATS_ID <= 0) {
            return;
        }
        new Metrics(this, BSTATS_ID);
    }

    private void setupUpdateChecker() {
        if (!getConfig().getBoolean("update-checker.enabled", true)) {
            return;
        }
        UpdateChecker updateChecker = new UpdateChecker(this, GITHUB_REPOSITORY);
        getServer().getScheduler().runTaskTimerAsynchronously(
                this, updateChecker::check, 20L, UPDATE_CHECK_INTERVAL_TICKS);
    }
}
