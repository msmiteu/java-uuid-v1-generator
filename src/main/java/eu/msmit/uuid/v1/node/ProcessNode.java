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

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class ProcessNode implements Node {

	private final byte[] bytes_;

	public ProcessNode() {
		this(new SystemNode());
	}

	public ProcessNode(Node base) {
		MessageDigest digest = NodeUtil.createDigest();
		digest.update(base.bytes());
		NodeUtil.digestProperties(digest, new String[] { "java.vm.version",
				"java.vm.vendor", "java.vm.name" });
		digest.update(getProcessId());
		digest.update(ByteBuffer
				.allocate(4)
				.putInt(System.identityHashCode(ProcessNode.class
						.getClassLoader())).array());
		bytes_ = NodeUtil.makeFinal(digest);
	}

	private ByteBuffer getProcessId() {
		ByteBuffer buf = ByteBuffer.allocate(8);
		NodeUtil.RND.nextBytes(buf.array());

		String jvmName = ManagementFactory.getRuntimeMXBean().getName();

		int index = jvmName.indexOf('@');
		if (index < 1) {
			return buf;
		}

		try {
			return buf.putLong(Long.parseLong(jvmName.substring(0, index)));
		} catch (NumberFormatException e) {
			return buf;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.msmit.uuid.v1.node.Node#bytes()
	 */
	@Override
	public byte[] bytes() {
		return bytes_;
	}
}
