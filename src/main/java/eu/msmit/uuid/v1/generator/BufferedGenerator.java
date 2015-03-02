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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import eu.msmit.uuid.v1.UuidBatch;
import eu.msmit.uuid.v1.VersionOneGenerator;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 2, 2015
 */
public class BufferedGenerator implements VersionOneGenerator {

	public static final int DEFAULT_BUFFER_SIZE = 1024;

	/**
	 * The feeding thread to the queue
	 */
	private final Runnable feeder_ = new Runnable() {

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					List<UUID> batch = generator_.next(new UuidBatch(buffer_),
							1, TimeUnit.MINUTES);
					for (UUID uuid : batch) {
						queue_.put(uuid);
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	};

	private final BlockingQueue<UUID> queue_;
	private final VersionOneGenerator generator_;
	private final int buffer_;

	public BufferedGenerator(VersionOneGenerator generator) {
		this(generator, DEFAULT_BUFFER_SIZE);
	}

	public BufferedGenerator(VersionOneGenerator generator, int buffer) {
		try {
			queue_ = new LinkedBlockingDeque<UUID>(buffer);
			buffer_ = buffer;
			generator_ = generator;

			Thread thread = new Thread(feeder_, getClass().getSimpleName());
			thread.setDaemon(true);
			thread.start();
		} catch (Exception e) {
			throw new Error(e);
		}
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
	 * @see eu.msmit.uuid.v1.VersionOneGenerator#next(long,
	 * java.util.concurrent.TimeUnit)
	 */
	@Override
	public UUID next(long timeout, TimeUnit timeUnit)
			throws InterruptedException {
		List<UUID> result = next(UuidBatch.ONE, timeout, timeUnit);
		if (result == null || result.isEmpty()) {
			return null;
		}

		return result.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.msmit.uuid.v1.VersionOneGenerator#next(eu.msmit.uuid.v1.UuidBatch,
	 * long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public List<UUID> next(UuidBatch batch, long timeout, TimeUnit timeUnit)
			throws InterruptedException {
		List<UUID> result = new ArrayList<UUID>(batch.getAmount());
		for (int b = 0; b < batch.getAmount(); b++) {
			UUID next = queue_.poll(timeout, timeUnit);
			if (next == null) {
				return null;
			}
			result.add(next);
		}

		return result;
	}
}
