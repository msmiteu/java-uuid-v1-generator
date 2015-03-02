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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Enumeration;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class SystemNode implements Node {

	private final byte[] bytes_;

	public SystemNode() {
		MessageDigest digest = NodeUtil.createDigest();
		gatherOsName(digest);
		gatherInterfaces(digest);
		gatherHostname(digest);
		bytes_ = NodeUtil.makeFinal(digest);
	}

	private void gatherOsName(MessageDigest digest) {
		String[] properties = new String[] { "os.name", "os.arch", "os.version" };
		NodeUtil.digestProperties(digest, properties);
	}

	private void gatherInterfaces(MessageDigest digest) {
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				if (iface.isLoopback())
					continue;
				byte[] addr = iface.getHardwareAddress();
				if (addr == null) {
					addr = new byte[1];
				}
				digest.update(addr);
			}
		} catch (IOException e) {
		}
	}

	private void gatherHostname(MessageDigest digest) {
		try {
			digest.update(InetAddress.getLocalHost().getHostName().getBytes());
		} catch (UnknownHostException e) {
		}
	}

	@Override
	public byte[] bytes() {
		return bytes_;
	}

}
