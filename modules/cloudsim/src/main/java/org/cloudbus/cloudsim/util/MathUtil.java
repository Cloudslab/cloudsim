/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * A class containing multiple convenient math functions.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 * @todo Using Java 8 Stream, some methods here can be improved or removed.
 */
public class MathUtil {

	/**
	 * Sums a list of numbers.
	 * 
	 * @param list the list of numbers
	 * @return the double
	 */
	public static double sum(final List<? extends Number> list) {
		double sum = 0;
		for (Number number : list) {
			sum += number.doubleValue();
		}
		return sum;
	}

	/**
	 * Converts a List to array.
	 * 
	 * @param list the list of numbers
	 * @return the double[]
         * @todo The method {@link List#toArray()} could be used directly
         * instead of creating this method.
	 */
	public static double[] listToArray(final List<? extends Number> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i).doubleValue();
		}
		return array;
	}

	/**
	 * Gets the median from a list of numbers.
	 * 
	 * @param list the list of numbers
	 * @return the median
	 */
	public static double median(final List<Double> list) {
		return getStatistics(list).getPercentile(50);
	}

	/**
	 * Gets the median from an array of numbers.
	 * 
	 * @param list the array of numbers
	 * 
	 * @return the median
	 */
	public static double median(final double[] list) {
		return getStatistics(list).getPercentile(50);
	}

	/**
	 * Returns an object to compute descriptive statistics for an list of numbers.
	 * 
	 * @param list the list of numbers. Must not be null.
	 * @return descriptive statistics for the list of numbers.
	 */
	public static DescriptiveStatistics getStatistics(final List<Double> list) {
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics stats = new DescriptiveStatistics();

		// Add the data from the array
		for (Double d : list) {
			stats.addValue(d);
		}
		return stats;
	}

	/**
	 * Returns an object to compute descriptive statistics for an array of numbers.
	 * 
	 * @param list the array of numbers. Must not be null.
	 * @return descriptive statistics for the array of numbers.
	 */
	public static DescriptiveStatistics getStatistics(final double[] list) {
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics stats = new DescriptiveStatistics();

		// Add the data from the array
		for (int i = 0; i < list.length; i++) {
			stats.addValue(list[i]);
		}
		return stats;
	}

	/**
	 * Gets the average from a list of numbers.
	 * 
	 * @param list the list of numbers
	 * 
	 * @return the average
	 */
	public static double mean(final List<Double> list) {
		double sum = 0;
		for (Double number : list) {
			sum += number;
		}
		return sum / list.size();
	}

	/**
	 * Gets the Variance from a list of numbers.
	 * 
	 * @param list the list of numbers
	 * @return the variance
	 */
	public static double variance(final List<Double> list) {
		long n = 0;
		double mean = mean(list);
		double s = 0.0;

		for (double x : list) {
			n++;
			double delta = x - mean;
			mean += delta / n;
			s += delta * (x - mean);
		}
		// if you want to calculate std deviation
		// of a sample change this to (s/(n-1))
		return s / (n - 1);
	}

	/**
	 * Gets the standard deviation from a list of numbers.
	 * 
	 * @param list the list of numbers
	 * @return the standard deviation
	 */
	public static double stDev(final List<Double> list) {
		return Math.sqrt(variance(list));
	}

	/**
	 * Gets the Median absolute deviation (MAD) from a array of numbers.
	 * 
	 * @param data the array of numbers
	 * @return the mad
	 */
	public static double mad(final double[] data) {
		double mad = 0;
		if (data.length > 0) {
			double median = median(data);
			double[] deviationSum = new double[data.length];
			for (int i = 0; i < data.length; i++) {
				deviationSum[i] = Math.abs(median - data[i]);
			}
			mad = median(deviationSum);
		}
		return mad;
	}

	/**
	 * Gets the Interquartile Range (IQR) from an array of numbers.
	 * 
	 * @param data the array of numbers
	 * @return the IQR
	 */
	public static double iqr(final double[] data) {
		Arrays.sort(data);
		int q1 = (int) Math.round(0.25 * (data.length + 1)) - 1;
		int q3 = (int) Math.round(0.75 * (data.length + 1)) - 1;
		return data[q3] - data[q1];
	}

	/**
	 * Counts the number of values different of zero at the beginning of 
         * an array.
	 * 
	 * @param data the array of numbers
	 * @return the number of values different of zero at the beginning of the array
	 */
	public static int countNonZeroBeginning(final double[] data) {
		int i = data.length - 1;
		while (i >= 0) {
			if (data[i--] != 0) {
				break;
			}
		}
		return i + 2;
	}

	/**
	 * Gets the length of the shortest row in a given matrix
	 * 
	 * @param data the data matrix
	 * @return the length of the shortest row int he matrix
	 */
	public static int countShortestRow(final double[][] data) {
		int minLength = 0;
		for (double[] row : data) {
			if (row.length < minLength) {
				minLength = row.length;
			}
		}
		return minLength;
	}

	/**
	 * Trims zeros at the end of an array.
	 * 
	 * @param data the data array
	 * @return the trimmed array
	 */
	public static double[] trimZeroTail(final double[] data) {
		return Arrays.copyOfRange(data, 0, countNonZeroBeginning(data));
	}

	/**
	 * Gets the Local Regression (Loess) parameter estimates.
	 * 
	 * @param y the y array
	 * @return the Loess parameter estimates
	 */
	public static double[] getLoessParameterEstimates(final double[] y) {
		int n = y.length;
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = i + 1;
		}
		return createWeigthedLinearRegression(x, y, getTricubeWeigts(n))
				.regress().getParameterEstimates();
	}

	public static SimpleRegression createLinearRegression(final double[] x,
			final double[] y) {
		SimpleRegression regression = new SimpleRegression();
		for (int i = 0; i < x.length; i++) {
			regression.addData(x[i], y[i]);
		}
		return regression;
	}

	public static OLSMultipleLinearRegression createLinearRegression(
			final double[][] x, final double[] y) {
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(y, x);
		return regression;
	}

	public static SimpleRegression createWeigthedLinearRegression(
			final double[] x, final double[] y, final double[] weigths) {
		double[] xW = new double[x.length];
		double[] yW = new double[y.length];

		int numZeroWeigths = 0;
		for (int i = 0; i < weigths.length; i++) {
			if (weigths[i] <= 0) {
				numZeroWeigths++;
			}
		}

		for (int i = 0; i < x.length; i++) {
			if (numZeroWeigths >= 0.4 * weigths.length) {
				// See: http://www.ncsu.edu/crsc/events/ugw07/Presentations/Crooks_Qiao/Crooks_Qiao_Alt_Presentation.pdf
				xW[i] = Math.sqrt(weigths[i]) * x[i];
				yW[i] = Math.sqrt(weigths[i]) * y[i];
			} else {
				xW[i] = x[i];
				yW[i] = y[i];
			}
		}

		return createLinearRegression(xW, yW);
	}

	/**
	 * Gets the robust loess parameter estimates.
	 * 
	 * @param y the y array
	 * @return the robust loess parameter estimates
	 */
	public static double[] getRobustLoessParameterEstimates(final double[] y) {
		int n = y.length;
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = i + 1;
		}
		SimpleRegression tricubeRegression = createWeigthedLinearRegression(x,
				y, getTricubeWeigts(n));
		double[] residuals = new double[n];
		for (int i = 0; i < n; i++) {
			residuals[i] = y[i] - tricubeRegression.predict(x[i]);
		}
		SimpleRegression tricubeBySquareRegression = createWeigthedLinearRegression(
				x, y, getTricubeBisquareWeigts(residuals));

		double[] estimates = tricubeBySquareRegression.regress()
				.getParameterEstimates();
		if (Double.isNaN(estimates[0]) || Double.isNaN(estimates[1])) {
			return tricubeRegression.regress().getParameterEstimates();
		}
		return estimates;
	}

	/**
	 * Gets the tricube weigths.
	 * 
	 * @param n the number of weights
	 * @return an array of tricube weigths with n elements
         * @todo The word "weight" is misspelled in the method name.
	 */
	public static double[] getTricubeWeigts(final int n) {
		double[] weights = new double[n];
		double top = n - 1;
		double spread = top;
		for (int i = 2; i < n; i++) {
			double k = Math.pow(1 - Math.pow((top - i) / spread, 3), 3);
			if (k > 0) {
				weights[i] = 1 / k;
			} else {
				weights[i] = Double.MAX_VALUE;
			}
		}
		weights[0] = weights[1] = weights[2];
		return weights;
	}

	/**
	 * Gets the tricube bisquare weigths.
	 * 
	 * @param residuals the residuals array
	 * @return the tricube bisquare weigths
         * @todo The word "weight" is misspelled in the method name.
	 */
	public static double[] getTricubeBisquareWeigts(final double[] residuals) {
		int n = residuals.length;
		double[] weights = getTricubeWeigts(n);
		double[] weights2 = new double[n];
		double s6 = median(abs(residuals)) * 6;
		for (int i = 2; i < n; i++) {
			double k = Math.pow(1 - Math.pow(residuals[i] / s6, 2), 2);
			if (k > 0) {
				weights2[i] = (1 / k) * weights[i];
			} else {
				weights2[i] = Double.MAX_VALUE;
			}
		}
		weights2[0] = weights2[1] = weights2[2];
		return weights2;
	}

	/**
	 * Gets the absolute values of an array of values
	 * 
	 * @param data the array of values
	 * @return a new array with the absolute value of each element in the given array.
	 */
	public static double[] abs(final double[] data) {
		double[] result = new double[data.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Math.abs(data[i]);
		}
		return result;
	}

}