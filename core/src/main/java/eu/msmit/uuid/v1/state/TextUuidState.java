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
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import eu.msmit.uuid.v1.UUIDv1;

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class TextUuidState {

	public static final String STATE_HEADER = "UUID State v1.0";

	public static final String STATE_PROPERTY_TS = "Generated: ";
	public static final String STATE_PROPERTY_HASH = "Hash: ";
	public static final String STATE_PROPERTY_UUID = "UUID: ";

	/**
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static TextUuidState read(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		if (!STATE_HEADER.equals(reader.readLine())) {
			throw new IOException();
		}

		String generatedLine = reader.readLine();

		if (generatedLine == null
				|| generatedLine.startsWith(STATE_PROPERTY_TS)) {
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

		List<UUIDv1> uuids = new ArrayList<>();
		String uuidLine = null;
		int uuidOffset = STATE_PROPERTY_UUID.length();
		while ((uuidLine = reader.readLine()) != null) {
			if (uuidLine.startsWith(STATE_PROPERTY_UUID)) {
				uuids.add(new UUIDv1(uuidLine.substring(uuidOffset)));
			}

			digest.update(uuidLine.getBytes());
		}

		String hash = hex(digest.digest());

		if (!hash.equals(validateHash)) {
			throw new IOException("Hash values did not match");
		}

		return new TextUuidState(uuids);
	}

	/**
	 * @param nodes
	 * @return
	 */
	public static TextUuidState initialize(List<UUIDv1> uuids) {
		return new TextUuidState(uuids);
	}

	private static MessageDigest createDigest() throws IOException {
		MessageDigest digest = null;

		try {
			digest = MessageDigest.getInstance("sha1");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		return digest;
	}

	private static String hex(byte[] digest) {
		if (digest == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(digest.length * 2);
		for (byte b : digest) {
			String hex = Integer.toString(b, 16);
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

	private final List<UUIDv1> state_;

	/**
	 * @param state
	 */
	private TextUuidState(List<UUIDv1> state) {
		if (state == null) {
			throw new IllegalArgumentException();
		}

		state_ = state;
	}

	/**
	 * @param index
	 * @param state
	 */
	public void set(int index, UUIDv1 state) {
		state_.set(index, state);
	}

	/**
	 * @param index
	 * @return
	 */
	public UUIDv1 get(int index) {
		return state_.get(index);
	}

	/**
	 * @return
	 */
	public int size() {
		return state_.size();
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		StringWriter uuidBuf = new StringWriter();
		PrintWriter uuidWriter = new PrintWriter(uuidBuf);
		MessageDigest digest = createDigest();

		for (UUIDv1 uuid : state_) {
			String line = STATE_PROPERTY_UUID + uuid.toString();
			digest.update(line.getBytes());
			uuidWriter.println(line);
		}

		PrintWriter writer = new PrintWriter(out);
		writer.println(STATE_HEADER);

		writer.print(STATE_PROPERTY_TS);
		writer.println(DATEFORMAT.format(Calendar.getInstance(UTC).getTime()));

		writer.print(STATE_PROPERTY_HASH);
		writer.println(hex(digest.digest()));

		writer.write(uuidBuf.toString());
	}
}
