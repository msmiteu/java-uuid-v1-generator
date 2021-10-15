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
 * Wraps a version 1 UUID to another digest form, for example type 3 and 5.
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 27, 2016
 */
abstract class DigestWrapper {

	/** UUIDv1 to wrapper namespace **/
	protected static final UUID NAMESPACE = UUID.fromString("df3deac8-092e-527c-8b1b-2d46f52ff852");

	/** The digester **/
	protected final ThreadLocal<MessageDigest> DIGEST = new ThreadLocal<MessageDigest>() {

		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance(getDigest());
			} catch (NoSuchAlgorithmException e) {
				throw new InternalError(getDigest() + " not supported", e);
			}
		}
	};

	private final byte version_;

	protected DigestWrapper() {
		version_ = getVersion();
	}

	/**
	 * Decorate a generator with this digest UUID
	 * 
	 * @param generator
	 *            the generator
	 * @return a new generator
	 */
	public final Generator wrap(final Generator generator) {
		return newWrapper(generator);
	}

	/**
	 * @return the digest
	 */
	protected abstract String getDigest();

	/**
	 * @return the version number number
	 */
	protected abstract byte getVersion();

	/**
	 * Create a new wrapper, this function is abstract so in stacktraces it is
	 * possibly more obvious which generator is used
	 * 
	 * @param generator
	 *            the generator
	 * @return the new wrapped {@link Generator}
	 */
	protected abstract Generator newWrapper(Generator generator);

	/**
	 * Wrap a type 1 uuid with the given digest UUID
	 * 
	 * @param uuidv1
	 *            the uuid
	 * @return
	 */
	public final UUID wrap(UUID uuidv1) {
		if (uuidv1.version() != 1) {
			throw new IllegalArgumentException("The given UUID is not a version 1 UUID");
		}

		byte[] bytes = toBytes(uuidv1);
		MessageDigest md = DIGEST.get();

		byte[] sha1Bytes = md.digest(bytes);
		sha1Bytes[6] &= 0x0f; /* clear version */
		sha1Bytes[6] |= version_; /* set to version of #getVersion() */
		sha1Bytes[8] &= 0x3f; /* clear variant */
		sha1Bytes[8] |= 0x80; /* set to IETF variant */

		ByteBuffer buf = ByteBuffer.wrap(sha1Bytes);

		return new UUID(buf.getLong(), buf.getLong());
	}

	/**
	 * @param uuidv1
	 * @return the digestable bytes
	 */
	public final byte[] toBytes(UUID uuidv1) {
		ByteBuffer buf = ByteBuffer.allocate(32);
		buf.putLong(NAMESPACE.getMostSignificantBits());
		buf.putLong(NAMESPACE.getLeastSignificantBits());
		buf.putLong(uuidv1.getMostSignificantBits());
		buf.putLong(uuidv1.getLeastSignificantBits());
		return buf.array();
	}

}
