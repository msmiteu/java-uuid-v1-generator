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

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import eu.msmit.uuid.v1.clock.Clock;
import eu.msmit.uuid.v1.clock.SystemClock;
import eu.msmit.uuid.v1.node.MacAddressNode;
import eu.msmit.uuid.v1.node.Node;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class Generator {
	public static void main(String[] args) throws TimeoutException,
			InterruptedException {

		Clock clock = new SystemClock();
		SecureRandom rnd = new SecureRandom();
		Node node = new MacAddressNode();

		UUIDv1 uuid = new UUIDv1();
		uuid.changeNode(node);
		uuid.randomClockSequence(rnd);

		TimeBasedGenerator gen = Generators.timeBasedGenerator(EthernetAddress
				.fromInterface());

		for (int i = 0; i < 500; i++) {
			long ts = clock.getTimestamp(1, TimeUnit.MINUTES);
			uuid.changeTime(ts);

			UUID uuid2 = gen.generate();
			// uuid.updateClockSequence(uuid2.clockSequence());

			System.out.println(ts - uuid2.timestamp());

			System.out.println("a>" + uuid.toString());
			System.out.println("b>" + uuid2.toString());
		}

		// ec0724b9-7cab-1ddf-d64b-098b986d3e26
		// f1dad7b2-7cab-1ddf-f64e-098b986d3e26

	}

}
