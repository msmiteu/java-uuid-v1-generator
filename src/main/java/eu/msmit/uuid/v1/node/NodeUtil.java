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
package eu.msmit.uuid.v1.node;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
class NodeUtil {
	static SecureRandom RND = new SecureRandom();

	static MessageDigest createDigest() {
		try {
			return MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			throw new Error();
		}
	}

	/**
	 * @param digest
	 * @return
	 */
	static byte[] makeFinal(MessageDigest digest) {
		byte[] hash = digest.digest();
		byte[] result = new byte[6];
		System.arraycopy(hash, hash.length - result.length, result, 0,
				result.length);
		multicastBit(result);
		return result;
	}

	/**
	 * @param node
	 */
	static void multicastBit(byte[] node) {
		node[0] |= 0x1;
	}

	/**
	 * @param digest
	 * @param properties
	 */
	static void digestProperties(MessageDigest digest, String[] properties) {
		for (String property : properties) {
			String val = System.getProperty(property);
			if (val == null) {
				val = "null";
			}
			digest.update(val.getBytes());
		}
	}
}
