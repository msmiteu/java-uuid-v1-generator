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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class FileState implements SharedState {

	private static final String DEFAULT_FILE_NAME = "uuid.state";

	private static File createTmp(String name) {
		if (name == null || name.length() < 4) {
			throw new IllegalArgumentException();
		}

		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File tmpFile = new File(tmpDir, name);

		return tmpFile;
	}

	private class Lock implements SharedLock {
		RandomAccessFile raf;
		FileChannel channel;
		TextUuidState state;
		FileLock flock;
		boolean dirty;

		public boolean isDirty() {
			return dirty;
		}

		public UUIDv1 get() {
			if (state == null) {
				return null;
			}
			return state.get(0);
		}

		public void change(UUIDv1 uuid) {
			if (state == null) {
				state = TextUuidState.initialize(Arrays.asList(uuid));
			}
			state.set(0, uuid);
		}
	}

	private final File file_;

	public FileState() {
		this(DEFAULT_FILE_NAME);
	}

	public FileState(String name) {
		this(createTmp(name));
	}

	public FileState(File file) {
		file_ = file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.state.SharedState#hold(long,
	 * java.util.concurrent.TimeUnit)
	 */
	public SharedLock hold(long timeout, TimeUnit unit)
			throws InterruptedException {
		final AtomicReference<Lock> found = new AtomicReference<>();
		final CountDownLatch latch = new CountDownLatch(1);

		Thread acquireThread = new Thread() {
			@Override
			public void run() {
				Lock lock = new Lock();
				int backoff = 0;

				try {
					lock.raf = new RandomAccessFile(file_, "rw");
				} catch (FileNotFoundException e) {
					latch.countDown();
					return;
				}

				lock.channel = lock.raf.getChannel();

				while (!Thread.interrupted()) {
					try {
						lock.flock = lock.channel.lock();
						found.set(lock);
						latch.countDown();
						break;

					} catch (OverlappingFileLockException e) {
						// move to wait
					} catch (IOException e) {
						// transfer exception to outer result
						break;
					}

					try {
						Thread.sleep(++backoff * 10);
					} catch (InterruptedException e) {
						break;
					}
				}

				// Too late, release the lock again
				if (latch.getCount() != 0) {
					releaseLock(lock);
					latch.countDown();
				}
			}
		};

		acquireThread.start();

		if (!latch.await(timeout, unit)) {
			acquireThread.interrupt();
			latch.await();
			return null;
		}

		Lock lock = found.get();

		try {
			lock.state = TextUuidState.read(Channels
					.newInputStream(lock.channel));
		} catch (IOException e) {
			lock.dirty = true;
		}

		return lock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.msmit.uuid.v1.state.SharedState#release(eu.msmit.uuid.v1.state.SharedLock
	 * )
	 */
	public boolean release(SharedLock state) {
		if (!(state instanceof Lock)) {
			throw new IllegalArgumentException();
		}

		Lock lock = (Lock) state;
		boolean success = true;

		try {
			lock.channel.position(0);
			lock.channel.truncate(0);
			if (lock.state != null) {
				lock.state.write(Channels.newOutputStream(lock.channel));
			}
		} catch (IOException e) {
			success = false;
		} finally {
			success &= releaseLock(lock);
		}

		return success;
	}

	private boolean releaseLock(Lock lock) {
		boolean success = true;

		if (lock.flock != null) {
			try {
				lock.flock.release();
			} catch (IOException e) {
				success = false;
			}
		}

		try {
			lock.channel.close();
			lock.raf.close();
		} catch (IOException e) {
			success = false;
		}

		return success;
	}
}
