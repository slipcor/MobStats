package net.slipcor.mobstats.math;

import net.slipcor.mobstats.classes.EntityStatistic;

/**
 * The Formula Interface
 *
 * This interface deals with evaluating a formula which can consist of complex sub parts, all inheriting this interface.
 */
public interface Formula {
    double evaluate(EntityStatistic stats);
}
