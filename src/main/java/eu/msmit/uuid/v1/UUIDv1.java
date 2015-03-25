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
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 25, 2015
 */
public class UUIDv1 {
	private static final Generator GENERATOR;

	static {
		ServiceLoader<Generator> serviceLoader = ServiceLoader
				.load(Generator.class);
		Iterator<Generator> it = serviceLoader.iterator();

		Generator generator = it.hasNext() ? it.next() : null;

		if (generator == null) {
			generator = new ParallelGenerator();
		}

		GENERATOR = generator;
	}

	/**
	 * @return the system generator
	 */
	public static Generator getInstance() {
		return GENERATOR;
	}

	/**
	 * @return the next {@link UUID} from the system generator. By default this
	 *         would be the {@link ParallelGenerator}
	 */
	public static UUID next() {
		return GENERATOR.next();
	}
}
