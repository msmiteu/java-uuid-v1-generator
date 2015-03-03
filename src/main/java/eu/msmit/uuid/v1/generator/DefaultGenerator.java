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
package eu.msmit.uuid.v1.generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.msmit.uuid.v1.UUIDv1;
import eu.msmit.uuid.v1.VersionOneGenerator;
import eu.msmit.uuid.v1.clock.Clock;
import eu.msmit.uuid.v1.clock.SystemClock;
import eu.msmit.uuid.v1.node.SystemNode;
import eu.msmit.uuid.v1.state.FileState;
import eu.msmit.uuid.v1.state.SharedLock;
import eu.msmit.uuid.v1.state.SharedState;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class DefaultGenerator implements VersionOneGenerator {

	private SharedState state_;
	private Clock clock_;
	private SystemNode node_;
	private Random rnd_;

	public DefaultGenerator() {
		state_ = new FileState();
		clock_ = new SystemClock();
		node_ = new SystemNode();
		rnd_ = new SecureRandom();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.VersionOneGenerator#next()
	 */
	@Override
	public UUID next() throws InterruptedException {
		return next(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.Processor#next(eu.msmit.uuid.v1.UuidBatch)
	 */
	public UUID next(long time, TimeUnit timeUnit) throws InterruptedException {
		List<UUID> result = next(1, time, timeUnit);
		if (result == null || result.isEmpty()) {
			return null;
		}

		return result.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.VersionOneGenerator#next(int, long,
	 * java.util.concurrent.TimeUnit)
	 */
	public List<UUID> next(int batch, long time, TimeUnit timeUnit)
			throws InterruptedException {
		if (batch <= 0) {
			return Collections.emptyList();
		}

		List<UUID> result = new ArrayList<>(batch);
		SharedLock lock = state_.hold(time, timeUnit);
		UUIDv1 current = null;

		if (lock == null) {
			return Collections.emptyList();
		}

		try {
			current = lock.get();

			// For batch time distribution, create custom clock

			for (int b = 0; b < batch; b++) {
				long timestamp = clock_.getTimestamp(time, timeUnit);

				if (timestamp == 0) {
					return Collections.emptyList();
				}

				if (current == null || !current.nodeEquals(node_)) {
					current = newUUID(timestamp);
				} else {
					if (current.timestamp() > timestamp) {
						current.incrementClockSequence();
						current.changeTime(timestamp);
					} else {
						current.incrementTime(timestamp);
					}
				}

				result.add(current.toUUID());
			}
		} finally {
			if (!state_.release(lock, current)) {
				return Collections.emptyList();
			}
		}

		return result;
	}

	/**
	 * Create a new UUID with the given timestamp
	 * 
	 * @param timestamp
	 *            the timestamp
	 * @return a {@link UUIDv1}
	 */
	private UUIDv1 newUUID(long timestamp) {
		UUIDv1 uuid = new UUIDv1();
		uuid.changeNode(node_);
		uuid.randomClockSequence(rnd_);
		uuid.changeTime(timestamp);
		return uuid;
	}

}
