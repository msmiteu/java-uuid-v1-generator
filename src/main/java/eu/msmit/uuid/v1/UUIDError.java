package eu.msmit.uuid.v1;

/**
 * Generating is considered to be a core and important process. The likelihood
 * of not being able to generate a UUID is very low, and if so, there is
 * probably something very wrong.
 * 
 * Therefore an uncaught unchecked exception is justified.
 * 
 * @author Marijn Smit (info@msmit.eu)
 * @since Mar 4, 2015
 */
public class UUIDError extends Error {
	private static final long serialVersionUID = -4601176836206723666L;

	public UUIDError() {
		super();
	}

	public UUIDError(String message, Throwable cause) {
		super(message, cause);
	}

	public UUIDError(String message) {
		super(message);
	}
}
