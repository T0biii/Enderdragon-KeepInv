package dev.t0biii.enderdragonkeepinv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

public final class DragonCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("status", "reload", "reset");

    private final EnderDragonKeepInv plugin;
    private final DragonTracker tracker;

    public DragonCommand(EnderDragonKeepInv plugin, DragonTracker tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "status" -> sender.sendMessage("EnderDragonKeepInv v" + plugin.getPluginMeta().getVersion()
                    + ": keep inventory is " + (tracker.isActive() ? "ACTIVE" : "inactive")
                    + " (" + tracker.count() + " tracked dragon(s)).");
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("Config reloaded. Metrics and update checker changes need a restart.");
            }
            case "reset" -> {
                tracker.clear();
                tracker.scanWorlds();
                sender.sendMessage("Dragon tracking reset and rescanned: " + tracker.count() + " dragon(s) tracked.");
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return SUBCOMMANDS.stream().filter(s -> s.startsWith(prefix)).toList();
    }
}
