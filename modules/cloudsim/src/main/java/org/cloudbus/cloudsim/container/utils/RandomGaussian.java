package org.cloudbus.cloudsim.container.utils;

import java.util.Random;

/**
 * The class is generated to produce an integer with a gaussian/normal distribution
 * Created by sareh on 16/12/15.
 */


public class RandomGaussian {
    Random random;

    public RandomGaussian() {
        setRandom(new Random());

    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Random getRandom() {
        return this.random;
    }
}
