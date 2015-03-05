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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

import eu.msmit.uuid.v1.UUIDv1;
import eu.msmit.uuid.v1.UUIDv1Generator;
import eu.msmit.uuid.v1.clock.Clock;
import eu.msmit.uuid.v1.generator.BufferedGenerator;
import eu.msmit.uuid.v1.generator.DefaultGenerator;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestGenerator extends TestCase {

	@Test
	public void testGenerateNext() throws Exception {
		UUIDv1Generator gen = new DefaultGenerator();
		UUID next = gen.next();
		assertNotNull(next);
		System.out.println(next);
	}

	@Test
	public void testSpeed() throws Exception {
		long ms = System.currentTimeMillis();
		UUIDv1Generator generator = UUIDv1Generator.getInstance();

		for (int i = 0; i < 10000; i++) {
			generator.next();
		}

		assertFalse((System.currentTimeMillis() - ms) > 300);
	}

	@Test
	public void testGenerateBatch() throws Exception {
		UUIDv1Generator gen = new DefaultGenerator();
		int testAmount = (int) (Clock.INTERVALS_PER_MS * 10);

		List<UUID> next = gen.next(testAmount, 1, TimeUnit.DAYS);
		assertEquals(testAmount, next.size());

		Set<UUID> uniqCheck = new HashSet<>();
		uniqCheck.addAll(next);
		assertEquals(testAmount, uniqCheck.size());
	}

	@Test
	public void testGenerateBufferBatch() throws Exception {
		UUIDv1Generator gen = new BufferedGenerator(new DefaultGenerator());
		int testAmount = (int) (Clock.INTERVALS_PER_MS * 10);

		List<UUID> next = gen.next(testAmount, 1, TimeUnit.DAYS);
		assertEquals(testAmount, next.size());

		Set<UUID> uniqCheck = new HashSet<>();
		uniqCheck.addAll(next);
		assertEquals(testAmount, uniqCheck.size());
	}
}
