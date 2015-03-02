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

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

import eu.msmit.uuid.v1.clock.Clock;
import eu.msmit.uuid.v1.clock.SystemClock;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 1, 2015
 */
public class TestSystemClock extends TestCase {

	@Test
	public void testOverruns() throws Exception {
		SystemClock clock = new SystemClock();
		long prev = 0;

		for (int i = 0; i < Clock.INTERVALS_PER_MS * 100; i++) {
			long ts = clock.getTimestamp(1, TimeUnit.MINUTES);

			if (ts <= prev) {
				String message = "ts=" + ts + ", prev=" + prev + ", delta="
						+ (prev - ts) + ", i=" + i;
				assertTrue(message, ts > prev);
			}

			prev = ts;
		}
	}
}
