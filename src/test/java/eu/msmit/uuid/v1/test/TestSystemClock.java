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

import junit.framework.TestCase;

import org.junit.Test;

import eu.msmit.uuid.v1.DefaultGenerator;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestSystemClock extends TestCase {

	private static long INTERVALS_PER_MS = 10000L;

	private class ClockTester extends DefaultGenerator {

		@Override
		public long nextTimestamp() {
			return super.nextTimestamp();
		}
	}

	@Test
	public void testOverruns() throws Exception {
		ClockTester tester = new ClockTester();
		long prev = 0;

		for (int i = 0; i < INTERVALS_PER_MS * 100; i++) {
			long ts = tester.nextTimestamp();

			if (ts <= prev) {
				String message = "ts=" + ts + ", prev=" + prev + ", delta="
						+ (prev - ts) + ", i=" + i;
				assertTrue(message, ts > prev);
			}

			prev = ts;
		}
	}
}
