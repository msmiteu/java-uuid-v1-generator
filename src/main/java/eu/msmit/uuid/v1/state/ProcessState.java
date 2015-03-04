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
package eu.msmit.uuid.v1.state;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class ProcessState implements SharedState {

	private static ReentrantLock PROCESS_LOCK = new ReentrantLock();
	private static AtomicReference<UUIDv1> PROCCESS_REF = new AtomicReference<UUIDv1>();

	private static class Lock implements SharedLock {

		UUIDv1 uuid;

		@Override
		public boolean isDirty() {
			return false;
		}

		@Override
		public UUIDv1 get() {
			return uuid;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.state.SharedState#hold(long,
	 * java.util.concurrent.TimeUnit)
	 */
	@Override
	public SharedLock hold(long timeout, TimeUnit unit)
			throws InterruptedException {
		if (!PROCESS_LOCK.tryLock(timeout, unit)) {
			return null;
		}

		Lock lock = new Lock();
		lock.uuid = PROCCESS_REF.get();
		return lock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.msmit.uuid.v1.state.SharedState#release(eu.msmit.uuid.v1.state.SharedLock
	 * , eu.msmit.uuid.v1.UUIDv1)
	 */
	@Override
	public boolean release(SharedLock lock, UUIDv1 uuid) {
		if (!(lock instanceof Lock)) {
			return false;
		}

		try {
			return PROCCESS_REF.compareAndSet(lock.get(), uuid);
		} finally {
			PROCESS_LOCK.unlock();
		}
	}
}
