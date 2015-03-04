package eu.msmit.uuid.v1;

import eu.msmit.uuid.v1.generator.BufferedGenerator;
import eu.msmit.uuid.v1.generator.DefaultGenerator;
import eu.msmit.uuid.v1.node.ProcessNode;
import eu.msmit.uuid.v1.node.SystemNode;
import eu.msmit.uuid.v1.state.ProcessState;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 4, 2015
 */
public class Generators {
	private static final VersionOneGenerator SYSTEM_GENERATOR;
	private static final VersionOneGenerator PROCESS_GENERATOR;

	static {
		SYSTEM_GENERATOR = new BufferedGenerator(new DefaultGenerator());
		PROCESS_GENERATOR = new DefaultGenerator(new ProcessState(),
				new ProcessNode());
	}

	/**
	 * The system generator stores it's state in the OS it's temp directory. It
	 * synchronizes with a file lock and persists the last generated UUID. This
	 * is the most secure and reliable version of the generator. Note that the
	 * MAC address is not used but {@link SystemNode} instead.
	 * 
	 * @return the global system generator
	 */
	public static VersionOneGenerator getSystemGenerator() {
		return SYSTEM_GENERATOR;
	}

	/**
	 * The local process/classloader generator. This generator persists the
	 * state within the local process. This generator is faster than the system
	 * generator at cost of uniqueness. This is still a fine choice but if you
	 * are in doubt use the system generator instead.
	 * 
	 * @return the local process generator
	 */
	public static VersionOneGenerator getProcessGenerator() {
		return PROCESS_GENERATOR;
	}
}
