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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Represents an instance of a node on a host, with x network interfaces, in the
 * specified environment, with process info x, and instance of the Node class
 * with ID x.
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 24, 2015
 */
public class Node {
	private static final long INIT_TIME = System.currentTimeMillis();
	private static final byte[] EMPTY_NETWORK_INTERFACE = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	private static final String[] ENV = new String[] { "os.name", "os.arch", "os.version", "java.vm.version",
			"java.vm.vendor", "java.vm.name", "java.class.path", "sun.java.command" };

	private final long node_;

	public Node() {
		try {
			List<String> nodeElms = new ArrayList<>(64);
			digestNetworkInterfaces(nodeElms);
			digestEnv(nodeElms);
			digestProcess(nodeElms);
			digestInstance(nodeElms);

			MessageDigest digest = MessageDigest.getInstance("md5");
			for (String nodeElm : nodeElms) {
				digest.update(nodeElm.getBytes());
			}
			byte[] buf = digest.digest();

			long v = (buf[5] & 0xFFL) << 40;
			v |= (buf[4] & 0xFFL) << 32;
			v |= (buf[3] & 0xFFL) << 24;
			v |= (buf[2] & 0xFFL) << 16;
			v |= (buf[1] & 0xFFL) << 8;
			v |= (buf[0] & 0xFFL);
			v |= 0x010000000000L; // Raise multicast bit;

			node_ = v;
			digest = null; // hint the GC
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

	protected void digestInstance(List<String> nodeElms) {
		nodeElms.add("instanceId=" + getInstanceId());
	}

	protected int getInstanceId() {
		return System.identityHashCode(this);
	}

	protected void digestProcess(List<String> nodeElms) {
		// Start and uptime are quite unique to identify a process
		long startTime = INIT_TIME;
		long upTime = System.currentTimeMillis() - INIT_TIME;

		nodeElms.add("proc=" + startTime + ";" + upTime);
		nodeElms.add("cwd=" + new File(".").getAbsolutePath());
	}

	protected void digestEnv(List<String> nodeElms) {
		for (String prop : ENV) {
			try {
				String val = System.getProperty(prop);
				nodeElms.add(prop + "=" + val);
			} catch (Exception e) {
				nodeElms.add(prop + "=?");
			}
		}
	}

	protected void digestNetworkInterfaces(List<String> nodeElms) {
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();

			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();

				if (iface.isLoopback()) {
					continue;
				}

				byte[] addr = iface.getHardwareAddress();

				if (addr == null) {
					continue;
				}

				nodeElms.add("hwAddr=" + Arrays.toString(addr));

				if (iface.isUp()) {
					for (InetAddress inetAddr : Collections.list(iface.getInetAddresses())) {
						String hostAddr = inetAddr.getHostAddress();
						nodeElms.add("hostAddr=" + hostAddr);
					}
				}
			}
		} catch (IOException e) {
			nodeElms.add("hwAddr=" + Arrays.toString(EMPTY_NETWORK_INTERFACE));
		}
	}

	/**
	 * @return the value of the node, with the first 6 significant bytes filled
	 *         with the node value
	 */
	public long getValue() {
		return node_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Long.hashCode(node_);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Node)) {
			return false;
		}
		return ((Node) obj).node_ == node_;
	}

}
