package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.EX.util.Id;

/**
 * A data item represents an entity stored on a hard disk, that can be accessed
 * by an applications. Examples of data items are files. Another example is a
 * portion of database records, which are often accessed together.
 * 
 * @author nikolay.grozev
 * 
 */
public class DataItem {

    /** The id. */
    private final int id = Id.pollId(DataItem.class);

    private final int dataSize;

    /**
     * Constr.
     * 
     * @param dataSize
     *            - the size of the data in this data item in megabytes.
     */
    public DataItem(final int dataSize) {
        super();
        this.dataSize = dataSize;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Return the data size in megabytes.
     * 
     * @return the data size in megabytes.
     */
    public int getDataSize() {
        return dataSize;
    }

}
