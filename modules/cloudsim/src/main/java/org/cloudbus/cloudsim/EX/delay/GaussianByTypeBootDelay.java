package org.cloudbus.cloudsim.EX.delay;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.EX.billing.BaseCustomerVmBillingPolicy;
import org.cloudbus.cloudsim.EX.vm.VmEX;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A boot delay generator, which for a given vm (specified by type and OS)
 * returns a number, which is an instance of a normal distribution N(m, stdev).
 * The mappings [vm-type, OS] -> [m, stdev] are provided upon construction.
 * 
 * <br/>
 * <br/>
 * 
 * These mappings can contain "wildcard" entries of type [null, OS] -> [m,
 * stdev] or [vm-type, null] -> [m, stdev]. Whenever the boot time of a vm is
 * evaluated and a precise match with key [vm.type, vm.OS] is not found, then
 * the mathching wilcards are used.
 * 
 * 
 * @author nikolay.grozev
 * 
 */
public class GaussianByTypeBootDelay implements IVmBootDelayDistribution {

    private final double defaultValue;
    private final Map<Pair<String, String>, NumberGenerator<Double>> delayGenerators = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param delayDefs
     *            - a mapping of type [vm-type, OS] -> [m, stdev]. The boot time
     *            of VMs which are not present in this mapping is considered 0.
     */
    public GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs) {
        this(delayDefs, (byte[]) null, 0.0);
    }

    /**
     * Constructor.
     * 
     * @param delayDefs
     *            - a mapping of type [vm-type, OS] -> [m, stdev].
     * @param defaultVal
     *            - a value to be returned when evaluating VMs, which are not
     *            present in the aforementioned mapping.
     */
    public GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs,
            final double defaultVal) {
        this(delayDefs, (byte[]) null, defaultVal);
    }

    /**
     * Constructor.
     * 
     * @param delayDefs
     *            - a mapping of type [vm-type, OS] -> [m, stdev].The boot time
     *            of VMs which are not present in this mapping is considered 0.
     * @param seed
     *            - a seed for the generator. If null, then no seed is used.
     */
    public GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs, final byte[] seed) {
        this(delayDefs, seed, 0);
    }

    /**
     * Constructor.
     * 
     * @param delayDefs
     *            - a mapping of type [vm-type, OS] -> [m, stdev].The boot time
     *            of VMs which are not present in this mapping is considered 0.
     * @param seedGen
     *            - the seed generator to use. If null or erronous, then default
     *            seed gen policy is used.
     */
    public GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs,
            final SeedGenerator seedGen) {
        this(delayDefs, seedGen, 0);
    }

    /**
     * Constructor.
     * 
     * @param delayDefs
     *            - a mapping of type [vm-type, OS] -> [m, stdev].
     * @param seed
     *            - a seed for the generator. If null, then no seed is used.
     * @param defaultVal
     *            - a value to be returned when evaluating VMs, which are not
     *            present in the aforementioned mapping.
     */
    public GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs, final byte[] seed,
                                   final double defaultVal) {
        this(delayDefs, seed, null, defaultVal);
    }

    /**
     * Constructor.
     * 
     * @param delayDefs
     *            - a mapping of type [vm-type, OS] -> [m, stdev].
     * @param seedGen
     *            - the seed generator to use. If null or erronous, then default
     *            seed gen policy is used.
     * @param defaultVal
     *            - a value to be returned when evaluating VMs, which are not
     *            present in the aforementioned mapping.
     */
    public GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs,
            final SeedGenerator seedGen, final double defaultVal) {
        this(delayDefs, null, seedGen, defaultVal);
    }

    private GaussianByTypeBootDelay(final Map<Pair<String, String>, Pair<Double, Double>> delayDefs, final byte[] seed,
                                    SeedGenerator seedGen, final double defaultVal) {
        Random merseneGenerator = null;
        if (seed == null) {
            try {
                merseneGenerator = seedGen == null ? new MersenneTwisterRNG() : new MersenneTwisterRNG(seedGen);
            } catch (SeedException e) {
                merseneGenerator = new MersenneTwisterRNG();
            }
        } else {
            merseneGenerator = new MersenneTwisterRNG(seed);
        }

        this.defaultValue = defaultVal;

        for (Map.Entry<Pair<String, String>, Pair<Double, Double>> entry : delayDefs.entrySet()) {
            this.delayGenerators.put(entry.getKey(), new GaussianGenerator(entry.getValue().getLeft(), entry.getValue()
                    .getRight(), merseneGenerator));
        }
    }

    @Override
    public double getDelay(final GuestEntity guest) {
        double result = defaultValue;
        if (guest instanceof VmEX vmex) {
            NumberGenerator<Double> gaussianGenerator = null;
            Pair<String, String> key = BaseCustomerVmBillingPolicy.keyOf(vmex);
            Pair<String, String> partialKey1 = ImmutablePair.of(vmex.getMetadata().getType(), null);
            Pair<String, String> partialKey2 = ImmutablePair.of(null, vmex.getMetadata().getOS());

            if (delayGenerators.containsKey(key)) {
                gaussianGenerator = delayGenerators.get(key);
            } else if (delayGenerators.containsKey(partialKey1)) {
                gaussianGenerator = delayGenerators.get(partialKey1);
            } else if (delayGenerators.containsKey(partialKey2)) {
                gaussianGenerator = delayGenerators.get(partialKey2);
            }

            if (gaussianGenerator != null) {
                result = gaussianGenerator.nextValue();
            }
        }
        return result;
    }
}
