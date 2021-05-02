package net.slipcor.mobstats.math;

import net.slipcor.mobstats.classes.EntityStatistic;

/**
 * Prefix Formula Class
 *
 * Evaluation simply means getting the formula we are attached to and negating the sign if our symbol is '-'.
 */
public class PrefixFormula implements Formula {
    final String symbol;    // the symbol that defines the formula
    final Formula argument; // the argument it is attached to

    public PrefixFormula(String symbol, Formula argument) {
        this.symbol = symbol;
        this.argument = argument;
    }

    @Override
    public double evaluate(EntityStatistic stats) {
        double sign = symbol.equals("-") ? -1 : 1;
        return sign * argument.evaluate(stats);
    }
}
