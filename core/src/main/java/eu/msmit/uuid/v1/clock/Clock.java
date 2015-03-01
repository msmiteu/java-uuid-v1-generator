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
package eu.msmit.uuid.v1.clock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public interface Clock {

	/**
	 * System.longTimeMillis() returns time from january 1st 1970. UUID time
	 * starts with Gregorian calendar (15-oct-1582).
	 */
	static long UUID_EPOCH_TO_UTC_EPOCH_MS = 0xb1d069b5400L;

	/**
	 * Maximum ticks per millisecond interval (1 millisecond = 1 000 000
	 * nanoseconds) / 100
	 */
	static long INTERVALS_PER_MS = 10000L;

	/**
	 * This interface assumes that the result will always be a valid timestamp,
	 * within the specified timeout. If the timeout expires, a
	 * {@link TimeoutException} should be thrown. The timestamp should then be
	 * considered as unavailable. The {@link InterruptedException} is thrown
	 * when the requesting thread is interrupted.
	 * 
	 * @throws InterruptedException
	 * @return the next timestamp for this system or <= 0 if the timestamp could
	 *         not be generated
	 */
	long getTimestamp(long timeout, TimeUnit timeUnit)
			throws InterruptedException;
}
