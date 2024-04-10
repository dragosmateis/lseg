package eu.sorescu.customer.math;

public abstract class AbstractExtrapolator {
    public abstract AbstractExtrapolator with(double x,double y);
    public abstract double extrapolateAt(double x);
}
