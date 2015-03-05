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
package eu.msmit.uuid.v1.test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

import eu.msmit.uuid.v1.UUIDv1;
import eu.msmit.uuid.v1.state.FileState;
import eu.msmit.uuid.v1.state.SharedLock;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestSharedFileState extends TestCase {

	@Test
	public void testState() throws Exception {
		File tmpFile = File.createTempFile("uuid.", ".test");
		tmpFile.deleteOnExit();

		FileState file = new FileState(tmpFile);
		SharedLock state = file.hold(30, TimeUnit.SECONDS);
		assertNotNull(state);
		file.release(state, state.get());
	}

	@Test
	public void testLock() throws Exception {
		final File tmpFile = File.createTempFile("uuid.", ".test");
		tmpFile.deleteOnExit();

		ExecutorService executor = Executors.newFixedThreadPool(3);
		
		// Locks the file for 5 secs
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					FileState file = new FileState(tmpFile);
					SharedLock state = file.hold(1, TimeUnit.MILLISECONDS);
					assertNotNull(state);
					Thread.sleep(500);
					file.release(state, state.get());
				} catch (Exception e) {
					assertNull(e);
				}
			}
		});
		
		Thread.sleep(50);

		// Should not result in lock
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					FileState file = new FileState(tmpFile);
					SharedLock state = file.hold(100, TimeUnit.MILLISECONDS);
					assertNull(state);
				} catch (Exception e) {
					assertNull(e);
				}
			}
		});

		Thread.sleep(50);

		// Must result in lock
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					FileState file = new FileState(tmpFile);
					SharedLock state = file.hold(6, TimeUnit.SECONDS);
					assertNotNull(state);
					file.release(state, state.get());
				} catch (Exception e) {
					assertNull(e);
				}
			}
		});

		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
	}

	@Test
	public void testStateRecover() throws Exception {
		UUIDv1 uuid = new UUIDv1("9af46100-c10d-11e4-9c16-255acb2167b8");

		File tmpFile = File.createTempFile("uuid.", ".test");
		tmpFile.deleteOnExit();

		FileState file = new FileState(tmpFile);
		SharedLock state = file.hold(30, TimeUnit.SECONDS);
		assertNotNull(state);
		file.release(state, uuid);

		state = file.hold(30, TimeUnit.SECONDS);
		assertNotNull(state);
		assertEquals(uuid, state.get());
		file.release(state, null);
	}
}
