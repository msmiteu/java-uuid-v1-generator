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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 27, 2016
 */
public class Type5Decorator implements Generator {

	/** UUIDv1 to Type 5 namespace **/
	private static final UUID NAMESPACE = UUID.fromString("df3deac8-092e-527c-8b1b-2d46f52ff852");

	/** The digester **/
	private static final ThreadLocal<MessageDigest> SHA1 = new ThreadLocal<MessageDigest>() {

		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("SHA1");
			} catch (NoSuchAlgorithmException nsae) {
				throw new InternalError("SHA1 not supported", nsae);
			}
		}
	};

	// The delegate
	private Generator delegate_;

	public Type5Decorator(Generator generator) {
		delegate_ = generator;
	}

	@Override
	public UUID next() {
		UUID next = delegate_.next();

		ByteBuffer buf = ByteBuffer.allocate(32);
		buf.putLong(NAMESPACE.getMostSignificantBits());
		buf.putLong(NAMESPACE.getLeastSignificantBits());
		buf.putLong(next.getMostSignificantBits());
		buf.putLong(next.getLeastSignificantBits());

		MessageDigest md = SHA1.get();
		byte[] sha1Bytes = md.digest(buf.array());
		sha1Bytes[6] &= 0x0f; /* clear version */
		sha1Bytes[6] |= 0x50; /* set to version 3 */
		sha1Bytes[8] &= 0x3f; /* clear variant */
		sha1Bytes[8] |= 0x80; /* set to IETF variant */

		buf = ByteBuffer.wrap(sha1Bytes);

		return new UUID(buf.getLong(), buf.getLong());
	}

}
