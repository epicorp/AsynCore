package net.devtech.asyncore.blocks.containers.fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a container of fluids, the amount of fluid is stored at nanobucket precision
 */
public class FluidInventory {
	private String type;
	private long nanoBuckets;

	public FluidInventory(String type, long nanoBuckets) {
		this.nanoBuckets = nanoBuckets;
		this.type = type;
	}

	/**
	 * return the type of the fluid, or null if there is no fluid
	 */
	@Nullable
	public String getType() {
		return this.nanoBuckets <= 0 ? null : this.type;
	}

	/**
	 * return the exact type of the fluid, even if there is no fluid in the container
	 */
	@NotNull
	public String getExactType() {
		return this.type;
	}

	/**
	 * set the type of the fluid, you'll probably have to add some later
	 */
	public void setType(@NotNull String type) {
		this.type = type;
	}

	/**
	 * get the number of buckets in the fluid
	 */
	public double getBuckets() {
		return this.nanoBuckets/100000000000d;
	}

	/**
	 * set the number of buckets in the fluid, only accurate to the nanobucket
	 */
	public void setBuckets(double buckets) {
		this.nanoBuckets = (long) (buckets*100000000000d);
	}

	/**
	 * get the number of nanobuckets in the fluid
	 */
	public long getNanoBuckets() {
		return this.nanoBuckets;
	}

	/**
	 * set the number of nanobuckets in the fluid
	 */
	public void setNanoBuckets(long nanoBuckets) {
		this.nanoBuckets = nanoBuckets;
	}

	/**
	 * add an amount of nanobuckets to the fluid
	 */
	public void addNanoBuckets(long nanoBuckets) {
		this.nanoBuckets += nanoBuckets;
	}

	/**
	 * add some buckets to the fluid
	 */
	public void addBuckets(double buckets) {
		this.nanoBuckets += (long) (buckets*100000000000d);
	}
}
