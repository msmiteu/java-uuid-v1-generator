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
package eu.msmit.uuid.v1.state;

import java.util.concurrent.TimeUnit;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public interface SharedState {

	/**
	 * Acquire the shared lock.
	 * 
	 * @see http://www.ibm.com/developerworks/library/j-jtp05236/
	 * @throws InterruptedException
	 * @return the shared lock, or null if it could not be acquired
	 */
	SharedLock hold(long time, TimeUnit unit) throws InterruptedException;

	/**
	 * @param lock
	 *            the lock to release, it must be the same instance that was
	 *            returned from {@link #hold(long, TimeUnit)}
	 * @return false if the release was not successful, the result of the
	 *         calling function should then be considered unsuccessful
	 */
	boolean release(SharedLock lock);
}
