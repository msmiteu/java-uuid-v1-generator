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

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Implements reference implementation of a UUID as described in
 * http://www.ietf.org/rfc/rfc4122.txt
 * 
 * Underlying implementations and wrappers provide the necessary speed
 * improvements suggested by the document.
 * 
 * For example the {@link ParallelGenerator} will provide unprecedented speed.
 * 
 * For this implementation, 'system' is considered to be a Java process with a
 * Generator instance. Shared stable store would be process memory in this case.
 * 
 * 
 * <pre>
 *    o  Obtain a system-wide global lock
 * 
 *    o  From a system-wide shared stable store (e.g., a file), read the
 *       UUID generator state: the values of the timestamp, clock sequence,
 *       and node ID used to generate the last UUID.
 * 
 *    o  Get the current time as a 60-bit count of 100-nanosecond intervals
 *       since 00:00:00.00, 15 October 1582.
 * 
 *    o  Get the current node ID.
 * 
 *    o  If the state was unavailable (e.g., non-existent or corrupted), or
 *       the saved node ID is different than the current node ID, generate
 *       a random clock sequence value.
 * 
 *    o  If the state was available, but the saved timestamp is later than
 *       the current timestamp, increment the clock sequence value.
 * 
 *    o  Save the state (current timestamp, clock sequence, and node ID)
 *       back to the stable store.
 * 
 *    o  Release the global lock.
 * 
 *    o  Format a UUID from the current timestamp, clock sequence, and node
 *       ID values according to the steps in Section 4.2.2.
 * </pre>
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 24, 2015
 */
public class DefaultGenerator implements Generator {
	/**
	 * For clock sequence and -don't care- bits
	 */
	private static final Random RANDOM = new SecureRandom();

	/**
	 * The maximum clock sequence
	 */
	private static final long MAX_CLOCK_SEQUENCE = 0x3FFFL;

	/**
	 * System.longTimeMillis() returns time from january 1st 1970. UUID time
	 * starts with Gregorian calendar (15-oct-1582).
	 */
	private static long UUID_EPOCH_TO_UTC_EPOCH_MS = 0xB1D069B5400L;

	/**
	 * Maximum ticks per millisecond interval (1 millisecond = 1 000 000
	 * nanoseconds) / 100
	 */
	private static long INTERVALS_PER_MS = 10000L;

	private final long node_;

	private long tsnow_;
	private long tsoff_;
	private UUID prevUUID_;

	public DefaultGenerator() {
		this(new Node());
	}

	public DefaultGenerator(Node node) {
		node_ = node.getValue();
	}

	/**
	 * @return
	 */
	public UUID next() {
		synchronized (this) {
			UUID current = prevUUID_;
			try {
				long timestamp = nextTimestamp();

				if (current == null || current.node() != node_) {
					current = newUUID(timestamp);
				} else {
					if (current.timestamp() >= timestamp) {
						current = incrementClockSequence(current, timestamp);
					} else {
						current = incrementTime(current, timestamp);
					}
				}

				return current;
			} finally {
				prevUUID_ = current;
			}
		}
	}

	/**
	 * @return the next random clock sequence
	 */
	private int randomClock() {
		return RANDOM.nextInt();
	}

	/**
	 * Increment the clock sequence, setting the given new timestamp.
	 */
	private UUID incrementClockSequence(UUID current, long timestamp) {
		return createUUID(timestamp, node_, current.clockSequence() + 1);
	}

	/**
	 * Create a new UUID with time incremented.
	 * 
	 * @throws IllegalArgumentException
	 *             when timestamp is eq or less than timestamp in UUID
	 */
	private UUID incrementTime(UUID current, long timestamp) {
		if (current.timestamp() >= timestamp) {
			throw new IllegalArgumentException("Timestamp was not incremented");
		}

		return createUUID(timestamp, node_, current.clockSequence());
	}

	/**
	 * Create a new UUID with a random clock seq.
	 */
	private UUID newUUID(long timestamp) {
		return createUUID(timestamp, node_, randomClock());
	}

	/**
	 * Create a new UUID from the given (valid) components.
	 */
	protected UUID createUUID(long timestamp, long node, int clock) {
		long timeLow = timestamp & 0xFFFFFFFFL;
		long timeMid = timestamp >>> 32 & 0xFFFFL;
		long timeHiAndVer = (timestamp >>> 48 & 0xFFFL) | 0x1000L;
		long msb = (timeLow << 32) | (timeMid << 16) | (timeHiAndVer);

		long lsb = 0x8000000000000000L;
		lsb |= (clock & MAX_CLOCK_SEQUENCE) << 48;
		lsb |= node & 0xFFFFFFFFFFFFL;

		return new UUID(msb, lsb);
	}

	/**
	 * Waits for next available timestamp (for ever). In case of time skew, the
	 * time could also be less than the previous one. This is totally valid.
	 * 
	 * @return the next timestamp, unequal to the previous.
	 */
	protected long nextTimestamp() {
		long now = System.currentTimeMillis();

		synchronized (this) {
			if (tsnow_ != now) {
				tsnow_ = now;
				tsoff_ = 0;
			}

			//
			if (tsoff_ >= INTERVALS_PER_MS) {
				tsnow_ = awaitNextTimestamp(tsnow_);
				tsoff_ = 0;
			}

			// Set time as current time millis plus offset times 100 ns ticks
			long currentTime = (UUID_EPOCH_TO_UTC_EPOCH_MS + tsnow_)
					* INTERVALS_PER_MS;

			// Return the uuid time plus the artifical tick incremented
			return (currentTime + tsoff_++);
		}
	}

	/**
	 * Waits for next timestamp, when pounding against {@link #INTERVALS_PER_MS}
	 * 
	 * @return a timestamp neq to timestamp, could be less in case of time skew
	 */
	private long awaitNextTimestamp(final long timestamp) {
		long now;

		while ((now = System.currentTimeMillis()) == timestamp
				&& !Thread.currentThread().isInterrupted()) {
			Thread.yield();
		}

		return now;
	}
}