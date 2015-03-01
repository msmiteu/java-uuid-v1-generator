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
package eu.msmit.uuid.v1.state;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class FileStateFormat {

	public static final String STATE_HEADER = "UUID State v1.0";
	public static final String STATE_PROPERTY_TS = "Generated: ";
	public static final String STATE_PROPERTY_HASH = "Hash: ";
	public static final String STATE_PROPERTY_UUID = "UUID: ";

	private static String hex(byte[] digest) {
		if (digest == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(digest.length * 2);
		for (byte b : digest) {
			String hex = Integer.toString(b & 0xFF, 16);
			if (hex.length() == 1) {
				result.append('0');
			}
			result.append(hex);
		}
		return result.toString();
	}

	private static final DateFormat DATEFORMAT = SimpleDateFormat
			.getDateTimeInstance();
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	/**
	 * Read a state from the given input stream
	 * 
	 * @param in
	 *            the {@link InputStream}
	 * @throws IOException
	 *             if some kind of read error occurs
	 */
	public UUIDv1 read(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		if (!STATE_HEADER.equals(reader.readLine())) {
			throw new IOException();
		}

		String generatedLine = reader.readLine();

		if (generatedLine == null
				|| !generatedLine.startsWith(STATE_PROPERTY_TS)) {
			throw new IOException();
		}

		String hashLine = reader.readLine();
		int hashOffset = STATE_PROPERTY_HASH.length();

		if (hashLine == null || !hashLine.startsWith(STATE_PROPERTY_HASH)
				|| hashLine.length() < hashOffset) {
			throw new IOException();
		}

		String validateHash = hashLine.substring(hashOffset);
		MessageDigest digest = createDigest();

		String uuidLine = reader.readLine();
		if (!uuidLine.startsWith(STATE_PROPERTY_UUID)) {
			throw new IOException();
		}

		digest.update(uuidLine.getBytes());
		String hash = hex(digest.digest());

		if (!hash.equals(validateHash)) {
			throw new IOException("Hash values did not match");
		}

		try {
			return new UUIDv1(uuidLine.substring(STATE_PROPERTY_UUID.length()));
		} catch (IllegalArgumentException e) {
			throw new IOException("Illegal UUID format", e);
		}
	}

	/**
	 * @param writeThis
	 *            {@link UUIDv1} to write
	 * @param out
	 *            the {@link OutputStream} to write to
	 * @throws IOException
	 *             if some kind of write error occurs
	 */
	public void write(UUIDv1 writeThis, OutputStream out) throws IOException {
		String uuidLine = STATE_PROPERTY_UUID
				+ (writeThis == null ? "null" : writeThis.toString());

		MessageDigest digest = createDigest();
		digest.update(uuidLine.getBytes());

		PrintWriter writer = new PrintWriter(out);
		writer.println(STATE_HEADER);

		writer.print(STATE_PROPERTY_TS);
		writer.println(DATEFORMAT.format(Calendar.getInstance(UTC).getTime()));

		writer.print(STATE_PROPERTY_HASH);
		writer.println(hex(digest.digest()));

		writer.println(uuidLine);
		writer.flush();
	}

	protected MessageDigest createDigest() throws IOException {
		MessageDigest digest = null;

		try {
			digest = MessageDigest.getInstance("sha1");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		return digest;
	}
}
