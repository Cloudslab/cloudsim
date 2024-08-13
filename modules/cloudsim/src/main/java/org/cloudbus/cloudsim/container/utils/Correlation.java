package org.cloudbus.cloudsim.container.utils;

import java.util.Arrays;

/**
 * Created by sareh on 7/08/15.
 */
public class Correlation {
//    Ref : http://en.wikipedia.org/wiki/Correlation_and_dependence

    /*
     * Compute correlation between xs1 and ys1.
     * 
     * If xs1 and ys1 have different lengths, the excess at the beginning of the longest array is not used.
     */
    public static double getCor(double[] xs1, double[] ys1) {
        int x_beg, y_beg, n;
        if (xs1.length > ys1.length) {
            x_beg = xs1.length - ys1.length;
            y_beg = 0;
            n = ys1.length;
        } else if (xs1.length < ys1.length) {
            x_beg = 0;
            y_beg = ys1.length - xs1.length;
            n = xs1.length;
        } else {
            x_beg = 0;
            y_beg = 0;
            n = xs1.length;
        }

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        for (int i = 0; i < n; ++i) {
            double x = xs1[x_beg + i];
            double y = ys1[y_beg + i];

            sx += x;
            sy += y;
            sxx += x * x;
            syy += y * y;
            sxy += x * y;
        }

        // covariation
        double cov = sxy / n - sx * sy / n / n;
        // standard error of x
        double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
        // standard error of y
        double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay;
    }

}
