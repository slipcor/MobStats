package net.slipcor.mobstats.classes;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.slipcor.core.LanguageEntry;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.EntityStatisticsBuffer;
import net.slipcor.mobstats.api.LeaderboardBuffer;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Hook class to hook into the Placeholder API
 *
 * Created by Yaël on 27/02/2016.
 * Updated with code by extendedclip on 15/05/2019
 */
public class PlaceholderAPIAbbreviationHook extends PlaceholderExpansion {
    long lastError = 0;
    static Map<String, LanguageEntry> stringToEntry = new HashMap<>();
    static {
        stringToEntry.put("LINE", Language.MSG.STATISTIC_SEPARATOR);
        stringToEntry.put("KILLS", Language.MSG.STATISTIC_HEADLINE_KILLS);
        stringToEntry.put("DEATHS", Language.MSG.STATISTIC_HEADLINE_DEATHS);
        stringToEntry.put("STREAK", Language.MSG.STATISTIC_HEADLINE_STREAK);
        stringToEntry.put("RATIO", Language.MSG.STATISTIC_HEADLINE_RATIO);
        stringToEntry.put("ELO", Language.MSG.STATISTIC_HEADLINE_ELO);
    }

    @Override
    public String getIdentifier() {
        return "sms";
    }

    @Override
    public String getAuthor() {
        return "SLiPCoR";
    }

    @Override
    public String getVersion() {
        return "0.0.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String s) {
        if (s.equals("k")) {
            return String.valueOf(EntityStatisticsBuffer.getKills(player.getUniqueId()));
        }

        if (s.equals("d")) {
            return String.valueOf(EntityStatisticsBuffer.getDeaths(player.getUniqueId()));
        }

        if (s.equals("s")) {
            return String.valueOf(EntityStatisticsBuffer.getStreak(player.getUniqueId()));
        }

        if (s.equals("m")) {
            return String.valueOf(EntityStatisticsBuffer.getMaxStreak(player.getUniqueId()));
        }

        if (s.equals("r")) {
            return String.format("%.2f", EntityStatisticsBuffer.getRatio(player.getUniqueId()));
        }

        if (s.startsWith("t_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            Language.MSG.valueOf("HEAD_" + name).parse());
                }

                String[] top = LeaderboardBuffer.top(pos, name, 0);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    MobStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("f_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            Language.MSG.valueOf("HEAD_" + name).parse());
                }

                String[] top = LeaderboardBuffer.flop(pos, name);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }


                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    MobStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("tp_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(split[2]);
                int days = Integer.parseInt(split[3]);

                String name = split[1].toUpperCase();

                if (split.length > 4) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            stringToEntry.get(name).parse());
                }

                String[] top = LeaderboardBuffer.topPlus(pos, name, days);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    MobStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("tw_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(split[2]);
                String world = split[3];
                int days = Integer.parseInt(split[4]);

                String name = split[1].toUpperCase();

                if (split.length > 5) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            stringToEntry.get(name).parse());
                }

                String[] top = LeaderboardBuffer.topWorld(pos, name, world, days);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }


                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
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
