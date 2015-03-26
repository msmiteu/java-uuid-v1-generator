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

import junit.framework.TestCase;

import org.junit.Test;

import eu.msmit.uuid.v1.DefaultGenerator;
import eu.msmit.uuid.v1.Generator;
import eu.msmit.uuid.v1.ParallelGenerator;
import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestGenerator extends TestCase {

	private static long INTERVALS_PER_MS = 10000L;

	private class UUIDTester extends DefaultGenerator {
		@Override
		public UUID createUUID(long timestamp, long node, int clock) {
			return super.createUUID(timestamp, node, clock);
		}
	}

	@Test
	public void testGenerateUUID() throws Exception {
		UUIDTester test = new UUIDTester();
		long timestamp = System.currentTimeMillis();
		long node = System.currentTimeMillis() & 0xFFFFFFFFFFFFL;
		int clock = 16383;

		UUID uuid = test.createUUID(timestamp, node, clock);

		assertEquals(timestamp, uuid.timestamp());
		assertEquals(node, uuid.node());
		assertEquals(clock, uuid.clockSequence());
		assertEquals(2, uuid.variant());
		assertEquals(1, uuid.version());

		clock = 0;
		uuid = test.createUUID(timestamp, node, clock);

		assertEquals(timestamp, uuid.timestamp());
		assertEquals(node, uuid.node());
		assertEquals(clock, uuid.clockSequence());
		assertEquals(2, uuid.variant());
		assertEquals(1, uuid.version());

		timestamp = 0;
		node = 0;
		uuid = test.createUUID(timestamp, node, clock);

		assertEquals(timestamp, uuid.timestamp());
		assertEquals(node, uuid.node());
		assertEquals(clock, uuid.clockSequence());
		assertEquals(2, uuid.variant());
		assertEquals(1, uuid.version());
	}

	@Test
	public void testGenerateNext() throws Exception {
		UUID next = new DefaultGenerator().next();
		assertNotNull(next);
		System.out.println(next);
	}

	@Test
	public void testCompareSpeed() throws Exception {
		Generator[] gen = new Generator[] { new DefaultGenerator(),
				new ParallelGenerator() };
		for (Generator g : gen) {
			for (int w = 0; w < 1000; w++)
				g.next();

			long ns = System.nanoTime();
			int i = 0;
			for (; i < 10000; i++) {
				g.next();
			}

			long time = (System.nanoTime() - ns) / i;
			System.out.println("Generation speed=" + time + "ns per UUID (" + g
					+ ")");
			assertFalse(time > 2000);
		}
	}

	@Test
	public void testGenerateBatch() throws Exception {
		Generator gen = UUIDv1.getGenerator();
		int testAmount = (int) (INTERVALS_PER_MS * 10);

		Set<UUID> uniqCheck = new HashSet<UUID>();

		for (int i = 0; i < testAmount; i++)
			uniqCheck.add(gen.next());

		assertEquals(testAmount, uniqCheck.size());
	}

}
