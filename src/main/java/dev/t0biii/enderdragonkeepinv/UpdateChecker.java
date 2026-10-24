package dev.t0biii.enderdragonkeepinv;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Checks the latest GitHub release and logs a console message if it is newer than the running version. */
public final class UpdateChecker {

    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private final EnderDragonKeepInv plugin;
    private final String repository;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private volatile String lastAnnounced;

    public UpdateChecker(EnderDragonKeepInv plugin, String repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    /** Blocking; call from an async thread. */
    public void check() {
        String current = plugin.getPluginMeta().getVersion();
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("https://api.github.com/repos/" + repository + "/releases/latest"))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "EnderDragonKeepInv/" + current)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                // 404 simply means no release has been published yet
                plugin.getLogger().fine("Update check returned HTTP " + response.statusCode());
                return;
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String latest = json.get("tag_name").getAsString();
            String url = json.has("html_url") ? json.get("html_url").getAsString()
                    : "https://github.com/" + repository + "/releases";
            if (isNewer(latest, current) && !latest.equals(lastAnnounced)) {
                lastAnnounced = latest;
                plugin.getLogger().warning("A new version is available: " + latest
                        + " (you are running " + current + ")");
                plugin.getLogger().warning("Download: " + url);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "Update check failed", e);
        }
    }

    static boolean isNewer(String latest, String current) {
        List<Integer> a = numbers(latest);
        List<Integer> b = numbers(current);
        for (int i = 0; i < Math.max(a.size(), b.size()); i++) {
            int x = i < a.size() ? a.get(i) : 0;
            int y = i < b.size() ? b.get(i) : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }

    private static List<Integer> numbers(String version) {
        String core = version.startsWith("v") || version.startsWith("V") ? version.substring(1) : version;
        int cut = core.indexOf('-');
        if (cut >= 0) {
            core = core.substring(0, cut);
        }
        List<Integer> result = new ArrayList<>();
        Matcher m = NUMBER.matcher(core);
        while (m.find()) {
            try {
                result.add(Integer.parseInt(m.group()));
            } catch (NumberFormatException e) {
                result.add(Integer.MAX_VALUE);
            }
        }
        return result;
    }
}
