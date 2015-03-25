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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * Represents an instance of a node on a host, with x network interfaces, in the
 * specified environment, with process ID x, and instance of the Node class with
 * ID x.
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 24, 2015
 */
public class Node {
	private static final byte[] LOCALHOST = "localhost".getBytes();
	private static final byte[] EMPTY_NETWORK_INTERFACE = new byte[] {
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF };
	private static final String[] ENV = new String[] { "os.name", "os.arch",
			"os.version", "java.vm.version", "java.vm.vendor", "java.vm.name" };
	private static final byte[] UNKNOWN_PROC_ID = new byte[4];

	private final long node_;

	public Node() {
		try {
			MessageDigest digest = MessageDigest.getInstance("md5");
			digestHost(digest);
			digestNetworkInterfaces(digest);
			digestEnv(digest);
			digestProcess(digest);
			digestInstance(digest);

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

	protected void digestInstance(MessageDigest digest) {
		digest.update(intToBuf(getInstanceId()));
	}

	protected int getInstanceId() {
		return System.identityHashCode(this);
	}

	protected void digestProcess(MessageDigest digest) {
		RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
		if (mxBean == null) {
			digest.update(UNKNOWN_PROC_ID);
			return;
		}

		String jvmName = mxBean.getName();
		int index = jvmName.indexOf('@');

		if (index < 1) {
			digest.update(UNKNOWN_PROC_ID);
			return;
		}

		try {
			int procId = Integer.parseInt(jvmName.substring(0, index));
			byte[] buf = intToBuf(procId);
			digest.update(buf);
		} catch (NumberFormatException e) {
			digest.update(UNKNOWN_PROC_ID);
		}

	}

	private byte[] intToBuf(int procId) {
		byte[] buf = new byte[4];
		buf[3] = (byte) (procId >>> 24);
		buf[2] = (byte) (procId >>> 16);
		buf[1] = (byte) (procId >>> 8);
		buf[0] = (byte) (procId);
		return buf;
	}

	protected void digestEnv(MessageDigest digest) {
		for (String prop : ENV) {
			try {
				String val = System.getProperty(prop);
				digest.update(prop.getBytes());
				if (val != null) {
					digest.update(val.getBytes());
				}
			} catch (Exception e) {
				digest.update(prop.getBytes());
			}
		}
	}

	protected void digestHost(MessageDigest digest) {
		try {
			InetAddress localhost = InetAddress.getLocalHost();

			if (localhost == null) {
				digest.update(LOCALHOST);
				return;
			}

			String name = localhost.getHostName();
			if (name == null) {
				digest.update(LOCALHOST);
				return;
			}

			digest.update(name.getBytes());
		} catch (UnknownHostException e) {
			digest.update(LOCALHOST);
		}
	}

	protected void digestNetworkInterfaces(MessageDigest digest) {
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces();

			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();

				if (iface.isLoopback()) {
					continue;
				}

				byte[] addr = iface.getHardwareAddress();

				if (addr == null) {
					continue;
				}

				digest.update(addr);
			}
		} catch (IOException e) {
			digest.update(EMPTY_NETWORK_INTERFACE);
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
