package org.cloudbus.cloudsim.EX.vm;

import java.lang.reflect.Field;

/**
 * Represents VM metadata - e.g. type, OS etc. All properties can be null.
 * 
 * @author nikolay.grozev
 * 
 */
public class VMMetadata implements Cloneable {

    private String type;
    private String os;

    //These values are the base values for this type. The actual ram/mips for this VM can change during vertical scalings
    private double mips;

    public void setMips(double mips) {
        this.mips = mips;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    private double ram;

    public double getMips() {
        return mips;
    }

    public double getRam() {
        return ram;
    }


    // @TODO new properties go here...

    /**
     * Returns the type of the VM.
     * 
     * @return the type of the VM.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the VM type.
     * 
     * @param type
     *            - the VM type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the OS.
     * 
     * @return the OS.
     */
    public String getOS() {
        return os;
    }

    /**
     * Sets the os.
     * 
     * @param os
     *            - the os.
     */
    public void setOS(String os) {
        this.os = os;
    }

    /**
     * Returns a deep copy of this instance.
     * 
     * @return a deep copy of this instance.
     */
    @Override
    public VMMetadata clone() {
        if (!getClass().equals(VMMetadata.class)) {
            throw new IllegalStateException("The operation is undefined for subclass: " + getClass().getCanonicalName());
        }
        VMMetadata result = new VMMetadata();
        for (Field f : getClass().getDeclaredFields()) {
            try {
                // @TODO take care of copying collections/arrays
                f.set(result, f.get(this));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }
}
