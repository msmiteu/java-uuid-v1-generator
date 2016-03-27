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

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.UUID;

/**
 * Implements reference implementation of a UUID as described in
 * http://www.ietf.org/rfc/rfc4122.txt
 * 
 * Underlying implementations and wrappers provide the necessary speed
 * improvements suggested by the document.
 * 
 * For example the {@link ParallelGenerator} will provide unprecedented speed
 * with a theoretical 25ns per UUID.
 * 
 * For this implementation, 'system' is considered to be a Java process with a
 * Generator instance. Shared stable store would be process memory in this case.
 * 
 * <pre>
 *    o  Obtain a system-wide global lock
 * 
 *    o  From a system-wide shared stable store (e.g., a file), read the
 *       UUID generator state: the values of the timestamp, clock sequence,
 *       and node ID used to generate the last UUID.
 * 
 *    o  Get the current time as a 60-bit count of 100-nanosecond intervals
 *       since 00:00:00.00, 15 October 1582.
 * 
 *    o  Get the current node ID.
 * 
 *    o  If the state was unavailable (e.g., non-existent or corrupted), or
 *       the saved node ID is different than the current node ID, generate
 *       a random clock sequence value.
 * 
 *    o  If the state was available, but the saved timestamp is later than
 *       the current timestamp, increment the clock sequence value.
 * 
 *    o  Save the state (current timestamp, clock sequence, and node ID)
 *       back to the stable store.
 * 
 *    o  Release the global lock.
 * 
 *    o  Format a UUID from the current timestamp, clock sequence, and node
 *       ID values according to the steps in Section 4.2.2.
 * </pre>
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 25, 2015
 */
public class UUIDv1 {
	private static final Generator GENERATOR;
	private static final Type5Decorator TYPE5_GENERATOR;

	static {
		ServiceLoader<Generator> serviceLoader = ServiceLoader.load(Generator.class);
		Iterator<Generator> it = serviceLoader.iterator();

		Generator generator = it.hasNext() ? it.next() : null;

		if (generator == null) {
			generator = new ParallelGenerator();
		}

		GENERATOR = generator;
		TYPE5_GENERATOR = new Type5Decorator(GENERATOR);
	}

	/**
	 * @return the system generator
	 */
	public static Generator getGenerator() {
		return GENERATOR;
	}

	/**
	 * @return the next {@link UUID} from the system generator. By default this
	 *         would be the {@link ParallelGenerator}
	 */
	public static UUID next() {
		return GENERATOR.next();
	}

	/**
	 * @return the next {@link UUID} from the system generator. The v1 UUID is
	 *         hashed and then outputted as a version 5 UUID;
	 */
	public static UUID nextv5() {
		return TYPE5_GENERATOR.next();
	}
}
