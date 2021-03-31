package net.slipcor.mobstats.classes;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.LeaderboardBuffer;
import net.slipcor.mobstats.api.EntityStatisticsBuffer;
import net.slipcor.mobstats.core.Language;
import org.bukkit.OfflinePlayer;

/**
 * Hook class to hook into the Placeholder API
 * <p>
 * Created by Yaël on 27/02/2016.
 * Updated with code by extendedclip on 15/05/2019
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
    long lastError = 0;

    @Override
    public String getIdentifier() {
        return "slipcormobstats";
    }

    @Override
    public String getAuthor() {
        return "SLiPCoR";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String s) {
        if (s.equals("kills")) {
            return String.valueOf(EntityStatisticsBuffer.getKills(player.getUniqueId()));
        }

        if (s.equals("deaths")) {
            return String.valueOf(EntityStatisticsBuffer.getDeaths(player.getUniqueId()));
        }

        if (s.equals("streak")) {
            return String.valueOf(EntityStatisticsBuffer.getStreak(player.getUniqueId()));
        }

        if (s.equals("maxstreak")) {
            return String.valueOf(EntityStatisticsBuffer.getMaxStreak(player.getUniqueId()));
        }

        if (s.equals("ratio")) {
            return String.format("%.2f", EntityStatisticsBuffer.getRatio(player.getUniqueId()));
        }

        if (s.startsWith("top_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3) {
                    return Language.HEAD_HEADLINE.toString(
                            String.valueOf(pos),
                            Language.valueOf("HEAD_" + name).toString());
                }

                String[] top = LeaderboardBuffer.top(pos, name);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return Language.INFO_NUMBERS.toString(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    MobStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("flop_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3) {
                    return Language.HEAD_HEADLINE.toString(
                            String.valueOf(pos),
                            Language.valueOf("HEAD_" + name).toString());
                }

                String[] top = LeaderboardBuffer.flop(pos, name);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return Language.INFO_NUMBERS.toString(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    MobStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        }

        // slipcormobstats_top_kills_10_head

        // slipcormobstats_top_kills_1
        // slipcormobstats_top_deaths_1
        // slipcormobstats_top_streak_1
        // slipcormobstats_top_k-d_1

        return null;
    }
}
