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

import java.util.UUID;

/**
 * The near parallel generator synchronizes only on a pointer increment that
 * points to the current generator. Performs worse than {@link DefaultGenerator}
 * on a single thread, but better on multiple threads.
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 25, 2015
 */
public class ParallelGenerator implements Generator {
	private static final int DEFAULT_CONCURRENCY = 4;

	private final Generator[] pool_;
	private final int concurrency_;
	private volatile int pointer_ = 0;

	/**
	 * Create a new {@link ParallelGenerator} with {@link #DEFAULT_CONCURRENCY}
	 */
	public ParallelGenerator() {
		this(DEFAULT_CONCURRENCY);
	}

	/**
	 * Create a generator with the given concurrency
	 * 
	 * @param concurrency
	 *            anywhere above zero
	 */
	public ParallelGenerator(int concurrency) {
		if (concurrency <= 0) {
			throw new IllegalArgumentException();
		}

		pool_ = new Generator[concurrency];
		concurrency_ = concurrency;

		for (int p = 0; p < concurrency; p++) {
			try {
				pool_[p] = new DefaultGenerator();
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.UUIDv1Gen#next()
	 */
	@Override
	public UUID next() {
		int cur;

		synchronized (this) {
			cur = pointer_++;
			if (pointer_ >= concurrency_) {
				pointer_ = 0;
			}
		}

		return pool_[cur].next();
	}
}
