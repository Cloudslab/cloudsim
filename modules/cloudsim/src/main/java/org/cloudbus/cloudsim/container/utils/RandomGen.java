package org.cloudbus.cloudsim.container.utils;

import java.util.Random;

/**
 * Created by sareh on 13/08/15.
 */
public class RandomGen {
    Random random;

    public RandomGen() {
        setRandom(new Random());
//        random.setSeed(123456789);
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public int getNum(int i){

        return getRandom().nextInt(i);
    }
}
