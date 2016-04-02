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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import eu.msmit.uuid.v1.Type3Wrapper;
import eu.msmit.uuid.v1.UUIDv1;
import junit.framework.TestCase;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestType3 extends TestCase {

	@Test
	public void testVersion() throws Exception {
		UUID next = UUIDv1.nextv3();
		assertEquals(3, next.version());
		assertEquals(2, next.variant());
	}

	@Test
	public void testJavaEq() throws Exception {
		UUID uuid = UUIDv1.next();

		Type3Wrapper type3 = new Type3Wrapper();
		byte[] bytes = type3.toBytes(uuid);

		UUID type3Java = UUID.nameUUIDFromBytes(bytes);
		UUID type3This = type3.wrap(uuid);

		assertEquals(type3Java, type3This);
	}

	@Test
	public void testGenerateNext() throws Exception {
		for (int i = 0; i < 10; i++) {
			UUID next = UUIDv1.nextv3();
			assertNotNull(next);
			System.out.println(next);
		}
	}

	@Test
	public void testGenerateBatch() throws Exception {
		int testAmount = 1000000;
		Set<UUID> uniqCheck = new HashSet<UUID>();

		for (int i = 0; i < testAmount; i++)
			uniqCheck.add(UUIDv1.nextv3());

		assertEquals(testAmount, uniqCheck.size());
	}
}
