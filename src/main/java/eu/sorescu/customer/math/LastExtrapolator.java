package eu.sorescu.customer.math;


public class LastExtrapolator extends AbstractExtrapolator{
    private final double y;

    public LastExtrapolator(){
        this(0);
    }
    private LastExtrapolator(double y){
        this.y=y;
    }

    public LastExtrapolator with(double x, double y){
        return new LastExtrapolator(y);
    }
    public double extrapolateAt(double x){
        return y;
    }
}
