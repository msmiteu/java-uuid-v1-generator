/**
 * Copyright 2016 Marijn Smit (info@msmit.eu)
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

import java.util.UUID;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 27, 2016
 */
public class Type5Wrapper extends DigestWrapper {

	@Override
	protected String getDigest() {
		return "SHA1";
	}

	@Override
	protected byte getVersion() {
		return 0x50;
	}

	@Override
	protected Generator newWrapper(final Generator generator) {
		return new Generator() {
			@Override
			public UUID next() {
				return wrap(generator.next());
			}
		};
	}
}
