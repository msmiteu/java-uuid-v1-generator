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

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * The use of this class is discouraged, but is kept for implementation
 * correctness.
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class MacAddressNode implements Node {

	private static NetworkInterface firstEthernetAddress() throws IOException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface
				.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			if (iface.isLoopback())
				continue;
			byte[] addr = iface.getHardwareAddress();
			if (addr == null)
				continue;
			return iface;
		}

		return null;
	}

	/**
	 * @return a random address with the multicast bit set
	 */
	private static byte[] newRandomAddress() {
		byte[] bytes = new byte[6];
		NodeUtil.RND.nextBytes(bytes);
		NodeUtil.multicastBit(bytes);
		return bytes;
	}

	/**
	 * @param iface
	 *            the network interface
	 * @return the first available real or pseudo random address
	 */
	private static byte[] bestOption(NetworkInterface iface) {
		try {
			if (iface == null) {
				iface = firstEthernetAddress();
			}
			if (iface != null) {
				return iface.getHardwareAddress();
			}
		} catch (IOException e) {
		}

		return newRandomAddress();
	}

	private final byte[] bytes_;

	public MacAddressNode() {
		this(null);
	}

	public MacAddressNode(NetworkInterface iface) {
		bytes_ = bestOption(iface);
	}

	@Override
	public byte[] bytes() {
		return bytes_;
	}
}
