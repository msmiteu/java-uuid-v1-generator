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

import java.util.UUID;

import org.junit.Test;

import junit.framework.TestCase;
import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestMutableUUID extends TestCase {

	@Test
	public void testTimestamp() throws Exception {
		UUID original = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

		UUIDv1 uuid = new UUIDv1(original);
		uuid.changeTime(original.timestamp());
		assertEquals(original.toString(), uuid.toString());

		uuid.changeTime(original.timestamp());
		assertTrue(original.equals(uuid.toUUID()));

		uuid.changeTime(original.timestamp() + 1);
		assertFalse(original.equals(uuid.toUUID()));
	}
}
