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
package eu.msmit.uuid.v1;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public interface VersionOneGenerator {

	/**
	 * @return the next UUID waiting forever for it to come available
	 * @throws InterruptedException
	 *             when the executing thread is interrupted
	 */
	UUID next() throws InterruptedException;

	/**
	 * Behaves exactly like calling VersionOneGenerator.next(1, long, TimeUnit)
	 * 
	 * @param timeout
	 *            the maximum time to wait for the batch
	 * @param timeUnit
	 *            the unit of time
	 * @return the next UUID within the given time or null when the wait expires
	 *         or something else went wrong
	 * @throws InterruptedException
	 *             when the executing thread is interrupted
	 */
	UUID next(long timeout, TimeUnit timeUnit) throws InterruptedException;

	/**
	 * Generate a batch of UUID's.
	 * 
	 * @param batch
	 *            the amount of UUID's to generate
	 * @param timeout
	 *            the maximum time to wait for the batch
	 * @param timeUnit
	 *            the unit of time
	 * @return a batch of the given size within the give timeout or an empty
	 *         list when the wait expires or something else went wrong
	 * @throws InterruptedException
	 *             when the executing thread is interrupted
	 */
	List<UUID> next(int batch, long timeout, TimeUnit timeUnit)
			throws InterruptedException;
}
