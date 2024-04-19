package org.cloudbus.cloudsim.web;

/**
 * A generator, which generates the same value again and again.
 * 
 * @author nikolay.grozev
 * 
 * @param <T>
 *            - the type of the generated objects. Must no be null.
 */
public class ConstGenerator<T> implements IGenerator<T> {

    private final T value;

    /**
     * Constr.
     * 
     * @param value
     *            - the value for the generator. Must not be null
     */
    public ConstGenerator(final T value) {
        super();
        if (value == null) {
            throw new IllegalArgumentException("Only non-null values allowed as values of this genrator.");
        }
        this.value = value;
    }

    @Override
    public T peek() {
        return value;
    }

    @Override
    public T poll() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void notifyOfTime(final double time) {
        // Do nothing
    }

}
