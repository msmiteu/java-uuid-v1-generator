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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import eu.msmit.uuid.v1.UUIDError;
import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class FileState implements SharedState {

	public static final String DEFAULT_FILE_NAME = "uuid.state";

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
		UUIDv1 state;
		FileLock flock;
		boolean dirty;

		public boolean isDirty() {
			return dirty;
		}

		public UUIDv1 get() {
			return state;
		}
	}

	private final File file_;
	private final FileStateFormat format_;

	public FileState() {
		this(DEFAULT_FILE_NAME);
	}

	public FileState(String name) {
		this(createTmp(name));
	}

	public FileState(File file) {
		file_ = file;
		format_ = new FileStateFormat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.state.SharedState#hold(long,
	 * java.util.concurrent.TimeUnit)
	 */
	public SharedLock hold(long timeout, TimeUnit unit)
			throws InterruptedException {

		Lock lock = new Lock();

		try {
			lock.raf = new RandomAccessFile(file_, "rw");
		} catch (FileNotFoundException e) {
			throw new UUIDError("State file could not be created", e);
		}

		lock.channel = lock.raf.getChannel();

		try {
			lock.flock = lock.channel.tryLock();
		} catch (OverlappingFileLockException e) {
			lock.flock = null;
		} catch (IOException e) {
			throw new UUIDError("Could not lock state file", e);
		}

		if (lock.flock == null) {
			if (!awaitLock(lock, timeout, unit)) {
				return null;
			}
		}

		try {
			lock.state = format_.read(Channels.newInputStream(lock.channel));
		} catch (IOException e) {
			lock.dirty = true;
		}

		return lock;
	}

	private boolean awaitLock(final Lock lock, long timeout, TimeUnit unit)
			throws InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			Future<FileLock> future = executor.submit(new Callable<FileLock>() {
				@Override
				public FileLock call() throws Exception {
					FileLock flock = null;
					int backoff = 0;

					while (!Thread.interrupted() && flock == null) {
						try {
							flock = lock.channel.lock();
						} catch (OverlappingFileLockException e) {
							// move to wait
						}

						try {
							Thread.sleep(++backoff * 10);
						} catch (InterruptedException e) {
							break;
						}
					}

					return flock;
				}
			});

			try {
				lock.flock = future.get(timeout, unit);
				return true;
			} catch (ExecutionException | TimeoutException e) {
				releaseLock(lock);
				return false;
			}
		} finally {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.msmit.uuid.v1.state.SharedState#release(eu.msmit.uuid.v1.state.SharedLock
	 * , eu.msmit.uuid.v1.UUIDv1)
	 */
	public boolean release(SharedLock state, UUIDv1 uuid) {
		if (!(state instanceof Lock)) {
			throw new IllegalArgumentException();
		}

		Lock lock = (Lock) state;
		boolean success = true;

		try {
			lock.channel.position(0);
			lock.channel.truncate(0);

			if (uuid == null) {
				uuid = state.get();
			}
			if (uuid != null) {
				format_.write(uuid, Channels.newOutputStream(lock.channel));
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
