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
package eu.msmit.uuid.v1.clock;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class SystemClock implements Clock {
	private long timestamp_;
	private long offset_;

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.clock.Clock#getTimestamp(long,
	 * java.util.concurrent.TimeUnit)
	 */
	@Override
	public long getTimestamp(long timeout, TimeUnit timeUnit)
			throws InterruptedException {
		long now = System.currentTimeMillis();

		synchronized (this) {
			if (timestamp_ != now) {
				timestamp_ = now;
				offset_ = 0;
			}

			//
			if (offset_ >= INTERVALS_PER_MS) {
				timestamp_ = awaitNextTimestamp(timestamp_, timeout, timeUnit);
				offset_ = 0;
			}

			// Set time as current time millis plus offset times 100 ns ticks
			long currentTime = (UUID_EPOCH_TO_UTC_EPOCH_MS + timestamp_)
					* INTERVALS_PER_MS;

			// Return the uuid time plus the artifical tick incremented
			return (currentTime + offset_++);
		}
	}

	/**
	 * Waits for next timestamp, this should be a very exceptional situation
	 */
	private long awaitNextTimestamp(final long timestamp, long timeout,
			TimeUnit timeUnit) throws InterruptedException {
		ExecutorService service = Executors.newSingleThreadExecutor();

		try {
			Future<Long> future = service.submit(new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					long now = 0;

					while ((now = System.currentTimeMillis()) <= timestamp
							&& !Thread.currentThread().isInterrupted()) {
						Thread.yield();
					}

					return now;
				}
			});

			try {
				return future.get();
			} catch (ExecutionException e) {
				throw new Error(e);
			}
		} finally {
			service.shutdownNow();
		}
	}
}
