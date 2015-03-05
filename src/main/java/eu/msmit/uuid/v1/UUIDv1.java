/**
 * Copyright 2015 Marijn Smit (info@msmit.eu)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.msmit.uuid.v1;

import java.util.Random;
import java.util.UUID;

import eu.msmit.uuid.v1.node.Node;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class UUIDv1 {
	private static final long MAX_CLOCK_SEQUENCE = 0x3FFFL;

	private long msb_;
	private long lsb_;

	public UUIDv1() {
		this(0x1000, 0x8000000000000000L);
	}

	public UUIDv1(String name) {
		this(UUID.fromString(name));
	}

	public UUIDv1(UUID current) {
		this(current.getMostSignificantBits(), current
				.getLeastSignificantBits());
	}

	public UUIDv1(long msb, long lsb) {
		msb_ = msb;
		lsb_ = lsb;

		if (version() != 1 || variant() != 2) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @param timestamp
	 */
	public void incrementTime(long timestamp) {
		long current = timestamp();
		if (timestamp <= current) {
			throw new IllegalArgumentException();
		}

		changeTime(timestamp);
	}

	/**
	 * @param timestamp
	 */
	public void changeTime(long timestamp) {
		long timeLow = timestamp & 0xFFFFFFFFL;
		long timeMid = timestamp >>> 32 & 0xFFFFL;
		long timeHiAndVer = (timestamp >>> 48 & 0xFFFL) | 0x1000L;

		msb_ = (timeLow << 32) | (timeMid << 16) | (timeHiAndVer);
	}

	/**
	 * @return
	 */
	public void incrementClockSequence() {
		int clock = clockSequence();
		updateClockSequence(clock + 1);
	}

	/**
	 * @param clock
	 *            the new clock sequence, the maximum value of the sequence is
	 *            {@value #MAX_CLOCK_SEQUENCE}
	 */
	public void updateClockSequence(long clock) {
		lsb_ = (lsb_ & 0xC000FFFFFFFFFFFFL)
				| ((clock & MAX_CLOCK_SEQUENCE) << 48);
	}

	/**
	 * @param random
	 *            the given {@link Random}
	 */
	public void randomClockSequence(Random random) {
		updateClockSequence(random.nextLong());
	}

	/**
	 * This function is more explicit and safe than using the 6 least
	 * significant bytes of a long.
	 * 
	 * @param node
	 *            the new node to change to
	 */
	public void changeNode(Node node) {
		lsb_ = nodeToLsb(node);
	}

	private long nodeToLsb(Node node) {
		if (node == null) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = node.bytes();
		if (bytes.length != 6) {
			throw new IllegalArgumentException();
		}

		long lsb = lsb_ & 0xFFFF000000000000L;
		lsb |= bytes[5] & 0xFFL;
		lsb |= (bytes[4] & 0xFFL) << 8;
		lsb |= (bytes[3] & 0xFFL) << 16;
		lsb |= (bytes[2] & 0xFFL) << 24;
		lsb |= (bytes[1] & 0xFFL) << 32;
		lsb |= (bytes[0] & 0xFFL) << 40;
		return lsb;
	}

	/**
	 * Check if the given node equals the node contained here
	 * 
	 * @param node
	 *            the node
	 * @return
	 */
	public boolean nodeEquals(Node node) {
		return Long.compare(lsb_, nodeToLsb(node)) == 0;
	}

	/**
	 * @return The version number of this {@code UUIDv1} (always 1)
	 */
	public int version() {
		return (int) ((msb_ >> 12) & 0x0f);
	}

	/**
	 * @return The variant number of this {@code UUIDv1} (always 2)
	 */
	private int variant() {
		return (int) ((lsb_ >>> (64 - (lsb_ >>> 62))) & (lsb_ >> 63));
	}

	/**
	 * The timestamp value associated with this UUID.
	 *
	 * <p>
	 * The 60 bit timestamp value is constructed from the time_low, time_mid,
	 * and time_hi fields of this {@code UUIDv1}. The resulting timestamp is
	 * measured in 100-nanosecond units since midnight, October 15, 1582 UTC.
	 * 
	 * @return The timestamp of this {@code UUIDv1}.
	 */
	public long timestamp() {
		return (msb_ & 0x0FFFL) << 48 | ((msb_ >> 16) & 0x0FFFFL) << 32
				| msb_ >>> 32;
	}

	/**
	 * The clock sequence value associated with this UUID.
	 *
	 * <p>
	 * The 14 bit clock sequence value is constructed from the clock sequence
	 * field of this UUID. The clock sequence field is used to guarantee
	 * temporal uniqueness in a time-based UUID.
	 * 
	 * @return The clock sequence of this {@code UUIDv1}
	 */
	public int clockSequence() {
		return (int) ((lsb_ & 0x3FFF000000000000L) >>> 48);
	}

	/**
	 * The node value associated with this UUID.
	 *
	 * <p>
	 * The 48 bit node value is constructed from the node field of this UUID.
	 * This field is intended to hold the IEEE 802 address of the machine that
	 * generated this UUID to guarantee spatial uniqueness.
	 *
	 * @return The node value of this {@code UUIDv1}
	 */
	public long node() {
		return lsb_ & 0x0000FFFFFFFFFFFFL;
	}

	// Object Inherited Methods

	/**
	 * @see UUID#toString()
	 * @return A string representation of this {@code UUIDv1}
	 */
	public String toString() {
		return (digits(msb_ >> 32, 8) + "-" + digits(msb_ >> 16, 4) + "-"
				+ digits(msb_, 4) + "-" + digits(lsb_ >> 48, 4) + "-" + digits(
					lsb_, 12));
	}

	/**
	 * Returns val represented by the specified number of hex digits.
	 */
	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

	/**
	 * Returns a hash code for this {@code UUIDv1}.
	 *
	 * @return A hash code value for this {@code UUIDv1}
	 */
	public int hashCode() {
		long hilo = msb_ ^ lsb_;
		return ((int) (hilo >> 32)) ^ (int) hilo;
	}

	/**
	 * Compares this object to the specified object. The result is {@code true}
	 * if and only if the argument is not {@code null}, is a {@code UUIDv1}
	 * object, has the same variant, and contains the same value, bit for bit,
	 * as this {@code UUIDv1}.
	 *
	 * @param obj
	 *            The object to be compared
	 *
	 * @return {@code true} if the objects are the same; {@code false} otherwise
	 */
	public boolean equals(Object obj) {
		if ((null == obj) || (obj.getClass() != UUIDv1.class))
			return false;
		UUIDv1 id = (UUIDv1) obj;
		return (msb_ == id.msb_ && lsb_ == id.lsb_);
	}

	/**
	 * @return a java {@link UUID} instance
	 */
	public UUID toUUID() {
		return new UUID(msb_, lsb_);
	}

}
