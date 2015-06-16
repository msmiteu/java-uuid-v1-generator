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
package eu.msmit.uuid.v1.test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.UUID;

import com.fasterxml.sort.SortConfig;
import com.fasterxml.sort.Sorter;
import com.fasterxml.sort.std.RawTextLineReader;
import com.fasterxml.sort.std.RawTextLineWriter;

public class UUIDReader {

	static class DupsComparator implements Comparator<byte[]> {

		@Override
		public int compare(byte[] o1, byte[] o2) {
			int diff = ByteBuffer.wrap(o1).compareTo(ByteBuffer.wrap(o2));

			if (diff != 0) {
				return diff;
			}

			System.out.println("duplicate " + new String(o1));
			return diff;
		}

	}

	static class ByteSorter extends Sorter<byte[]> {
		public ByteSorter() {
			super(new SortConfig(), RawTextLineReader.factory(),
					RawTextLineWriter.factory(), new DupsComparator());
		}
	}

	public static void main(String[] args) throws Exception {
		new UUIDReader().run(args);
	}

	private void run(String[] args) throws Exception {
		File dir = new File("C:\\Project\\UUIDTest\\seeds");
		File bufFile = new File(dir, "__buffer");
		File sortFile = new File(dir, "__sorted");
		OutputStream outChannel = new BufferedOutputStream(
				new FileOutputStream(bufFile, true));
		try {
			for (File file : dir.listFiles()) {
				if (file.getName().startsWith("_")) {
					continue;
				}
				System.out.println("Packing " + file.getName());
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						outChannel.write(line.getBytes());
						outChannel.write('\n');
					}
				} finally {
					reader.close();
				}

				file.delete();
			}
		} finally {
			outChannel.close();
		}

		BufferedReader reader = new BufferedReader(new FileReader(bufFile));
		String line = null;
		while ((line = reader.readLine()) != null) {
			UUID.fromString(line);
		}
		reader.close();

		ByteSorter sorter = new ByteSorter();
		sorter.sort(new FileInputStream(bufFile),
				new FileOutputStream(sortFile));
		sortFile.delete();
	}
}
