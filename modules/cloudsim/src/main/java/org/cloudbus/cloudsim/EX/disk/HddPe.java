package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.EX.util.Id;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Harddisk processing element.
 * 
 * @author nikolay.grozev
 * 
 */
public class HddPe extends Pe {

    private final Map<Integer, DataItem> data = new LinkedHashMap<>();

    /**
     * Constr.
     * 
     * @param peProvisioner
     *            - the provisioner policy.
     * @param dataItems
     *            - the data items in this harddisk. Must not be null.
     */
    public HddPe(final PeProvisioner peProvisioner, final Collection<DataItem> dataItems) {
        super(Id.pollId(HddPe.class), peProvisioner);
        for (DataItem item : dataItems) {
            data.put(item.getId(), item);
        }
    }

    /**
     * Constr.
     * 
     * @param peProvisioner
     *            - the provisioner policy.
     * @param dataItems
     *            - the data items in this harddisk. Must not be null.
     */
    public HddPe(final PeProvisioner peProvisioner, final DataItem... dataItems) {
        this(peProvisioner, Arrays.asList(dataItems));
    }

    /**
     * Returns a view of the data items stored in this harddisk.
     * 
     * @return a view of the data items stored in this harddisk.
     */
    public Collection<DataItem> getData() {
        return data.values();
    }

    /**
     * Returns the data item with the specified id, if it is stored on this
     * harddisk. If not on the disk - then null is returned.
     * 
     * @param id
     *            - the id of the stored item.
     * @return - the data item with the specified id, if it is stored on this
     *         harddisk. If not on the disk - then null is returned.
     */
    public DataItem getDataItem(final int id) {
        return data.get(id);
    }

    /**
     * Returns if a data item with the specified id.
     * 
     * @param id
     *            - the id.
     * @return if a data item with the specified id.
     */
    public boolean containsDataItem(final int id) {
        return data.containsKey(id);
    }

}
