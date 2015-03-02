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
