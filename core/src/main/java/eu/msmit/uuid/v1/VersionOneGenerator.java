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
	 * @return
	 * @throws InterruptedException
	 */
	UUID next() throws InterruptedException;

	/**
	 * @param timeout
	 * @param timeUnit
	 * @return
	 * @throws InterruptedException
	 */
	UUID next(long timeout, TimeUnit timeUnit) throws InterruptedException;

	/**
	 * @param batch
	 * @param timeout
	 * @param timeUnit
	 * @return
	 * @throws InterruptedException
	 */
	List<UUID> next(UuidBatch batch, long timeout, TimeUnit timeUnit)
			throws InterruptedException;
}
