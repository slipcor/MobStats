package net.slipcor.mobstats.math;

import net.slipcor.mobstats.api.InformationType;
import net.slipcor.mobstats.classes.EntityStatistic;

import java.util.EnumMap;
import java.util.Map;

/**
 * Statistic Formula Class
 *
 * Evaluation simply means replacing its value with the statistic value of the information type given.
 */
public class StatisticFormula implements Formula {
    private static final Map<InformationType, Formula> formulas;

    static {
        formulas = new EnumMap<>(InformationType.class);

        formulas.put(InformationType.DEATHS, new StatisticFormula(InformationType.DEATHS));
        formulas.put(InformationType.KILLS, new StatisticFormula(InformationType.KILLS));
        formulas.put(InformationType.CURRENTSTREAK, new StatisticFormula(InformationType.CURRENTSTREAK));
        formulas.put(InformationType.STREAK, new StatisticFormula(InformationType.STREAK));
    }

    private final InformationType type;

    private StatisticFormula(InformationType type) {
        this.type = type;
    }

    @Override
    public double evaluate(EntityStatistic stats) {
        switch (type) {
            case DEATHS:
                return stats.getDeaths();
            case KILLS:
                return stats.getKills();
            case CURRENTSTREAK:
                return stats.getCurrentStreak();
            case STREAK:
                return stats.getMaxStreak();
        }
        throw new IllegalArgumentException("not a valid number stat: " + type);
    }

    @Override
    public String toString() {
        return type.name();
    }

    public static Formula from(InformationType key) {
        return formulas.get(key);
    }
}
