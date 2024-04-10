package eu.sorescu.customer.math;

public class LinearExtrapolator extends AbstractExtrapolator{
        public final int n;
        public final double sx, sx2, sy, sxy;
        public LinearExtrapolator() {
            this(0,0,0,0,0);
        }
        public LinearExtrapolator(double sumx,double sumy,double sumx2,double sumxy,int n) {
            this.sx = sumx;
            this.sy = sumy;
            this.sx2 =sumx2;
            this.n=n;
            this.sxy =sumxy;
        }
        public LinearExtrapolator with(double x,double y) {
            return new LinearExtrapolator(sx +x, sy +y, sx2 +x*x, sxy +x*y,n+1);
        }

        public double extrapolateAt(double x) {
            double xbar = sx / n;
            double ybar = sy / n;
            double xxbar= sx2 -2* sx *xbar+n*xbar*xbar;
            double xybar= sxy - sy *xbar- sx *ybar+n*xbar*ybar;
            double slope  = xybar / xxbar;
            double intercept = ybar - slope * xbar;
            return slope*x + intercept;
        }

    public static void main(String[] args) {
        System.out.println(new LinearExtrapolator().with(1,2).with(2,3).extrapolateAt(3));
    }
}
