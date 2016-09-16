package org.cloudbus.cloudsim.googletrace;

public class PriorityHostSkin implements Comparable<PriorityHostSkin>{
	
	private GoogleHost host;
	private int prioritySkin;
	
	public PriorityHostSkin(GoogleHost host, int prioritySkin) {
		this.host = host;
		this.prioritySkin = prioritySkin;
	}

	@Override
	public int compareTo(PriorityHostSkin other) {
		/*
		 * If this object has bigger amount of available mips it should be
		 * considered before the other one.
		 */
		int result = (-1)
				* (new Double( host.getAvailableMipsByPriority(getPrioritySkin()))
						.compareTo(new Double(other.getHost().getAvailableMipsByPriority(prioritySkin))));

		if (result == 0)
			return new Integer(host.getId()).compareTo(new Integer(other
					.getHost().getId()));

		return result;
	}
	
	public GoogleHost getHost() {
		return host;
	}
	public int getPrioritySkin() {
		return prioritySkin;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PriorityHostSkin) {
			PriorityHostSkin other = (PriorityHostSkin) obj;
			return getHost().equals(other.getHost())
					&& getPrioritySkin() == other.getPrioritySkin();
		}
		return false;
	}	
}