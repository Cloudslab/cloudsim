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

import flanagan.analysis.Regression;
import flanagan.analysis.Stat;

/**
 * A class containing multiple convenient math functions. To use some of the function you must
 * download Michael Thomas Flanagan's Java Scientific Library:
 * 
 * http://www.ee.ucl.ac.uk/~mflanaga/java/
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class MathUtil {

	/**
	 * Sums a list of numbers.
	 * 
	 * @param list the list
	 * @return the double
	 */
	public static double sum(List<? extends Number> list) {
		double sum = 0;
		for (Number number : list) {
			sum += number.doubleValue();
		}
		return sum;
	}

	/**
	 * List to array.
	 * 
	 * @param list the list
	 * @return the double[]
	 */
	public static double[] listToArray(List<? extends Number> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i).doubleValue();
		}
		return array;
	}

	/**
	 * Gets the median.
	 * 
	 * @param list the list
	 * @return the median
	 */
	public static double median(List<Double> list) {
		return Stat.median(listToArray(list));
	}

	/**
	 * Gets the average.
	 * 
	 * @param list the list
	 * 
	 * @return the average
	 */
	public static double mean(List<Double> list) {
		double sum = 0;
		for (Double number : list) {
			sum += number;
		}
		return sum / list.size();
	}

	/**
	 * Variance.
	 * 
	 * @param list the list
	 * @return the double
	 */
	public static double variance(List<Double> list) {
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
	 * Gets the standard deviation.
	 * 
	 * @param list the list
	 * @return the double
	 */
	public static double stDev(List<Double> list) {
		return Math.sqrt(variance(list));
	}

	/**
	 * Gets the mad.
	 * 
	 * @param data the data
	 * @return the mad
	 */
	public static double mad(double[] data) {
		double mad = 0;
		if (data.length > 0) {
			double median = Stat.median(data);
			double[] deviationSum = new double[data.length];
			for (int i = 0; i < data.length; i++) {
				deviationSum[i] = Math.abs(median - data[i]);
			}
			mad = Stat.median(deviationSum);
		}
		return mad;
	}

	/**
	 * Gets the IQR.
	 * 
	 * @param data the data
	 * @return the IQR
	 */
	public static double iqr(double[] data) {
		Arrays.sort(data);
		int q1 = (int) Math.round(0.25 * (data.length + 1)) - 1;
		int q3 = (int) Math.round(0.75 * (data.length + 1)) - 1;
		return data[q3] - data[q1];
	}

	/**
	 * Count non zero beginning of the data.
	 * 
	 * @param data the data
	 * @return the int
	 */
	public static int countNonZeroBeginning(double[] data) {
		int i = data.length - 1;
		while (i >= 0) {
			if (data[i--] != 0) {
				break;
			}
		}
		return i + 2;
	}

	/**
	 * Count shortest row.
	 * 
	 * @param data the data
	 * @return the int
	 */
	public static int countShortestRow(double[][] data) {
		int minLength = 0;
		for (double[] row : data) {
			if (row.length < minLength) {
				minLength = row.length;
			}
		}
		return minLength;
	}

	/**
	 * Trim zero tail.
	 * 
	 * @param data the data
	 * @return the double[]
	 */
	public static double[] trimZeroTail(double[] data) {
		return Arrays.copyOfRange(data, 0, countNonZeroBeginning(data));
	}

	/**
	 * Gets the loess parameter estimates.
	 * 
	 * @param y the y
	 * @return the loess parameter estimates
	 */
	public static double[] getLoessParameterEstimates(double[] y) {
		int n = y.length;
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = i + 1;
		}
		Regression regression = new Regression(x, y, getTricubeWeigts(n));
		regression.linear();
		double[] estimates = regression.getBestEstimates();
		if (estimates[0] == Double.NaN || estimates[1] == Double.NaN) {
			return regression.getBestEstimates();
		}
		return estimates;
	}

	/**
	 * Gets the robust loess parameter estimates.
	 * 
	 * @param y the y
	 * @return the robust loess parameter estimates
	 */
	public static double[] getRobustLoessParameterEstimates(double[] y) {
		int n = y.length;
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = i + 1;
		}
		Regression regression = new Regression(x, y, getTricubeWeigts(n));
		regression.linear();
		Regression regression2 = new Regression(x, y, getTricubeBisquareWeigts(regression.getResiduals()));
		regression2.linear();
		double[] estimates = regression2.getBestEstimates();
		if (estimates[0] == Double.NaN || estimates[1] == Double.NaN) {
			return regression.getBestEstimates();
		}
		return estimates;
	}

	/**
	 * Gets the tricube weigts.
	 * 
	 * @param n the n
	 * @return the tricube weigts
	 */
	public static double[] getTricubeWeigts(int n) {
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
	 * Gets the tricube bisquare weigts.
	 * 
	 * @param residuals the residuals
	 * @return the tricube bisquare weigts
	 */
	public static double[] getTricubeBisquareWeigts(double[] residuals) {
		int n = residuals.length;
		double[] weights = getTricubeWeigts(n);
		double[] weights2 = new double[n];
		double s6 = Stat.median(abs(residuals)) * 6;
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
	 * Abs.
	 * 
	 * @param data the data
	 * @return the double[]
	 */
	public static double[] abs(double[] data) {
		double[] result = new double[data.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Math.abs(data[i]);
		}
		return result;
	}

}