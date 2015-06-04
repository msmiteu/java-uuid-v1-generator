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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

import org.mapdb.DBMaker;

public class UUIDReader {
	public static void main(String[] args) throws Exception {
		new UUIDReader().run(args);
	}

	private void run(String[] args) throws Exception {
		File dir = new File("C:\\Project\\UUIDTest\\seeds");
		Set<String> set = DBMaker.newTempHashSet();
		long c = 0;
		for (File file : dir.listFiles()) {
			if (file.getName().startsWith("_")) {
				continue;
			}
			System.out.println("Reading " + file.getName());
			BufferedReader br = new BufferedReader(new FileReader(file));
			try {
				String ln = null;
				while ((ln = br.readLine()) != null) {
					c++;
					if (!set.add(ln)) {
						throw new Error("duplicate " + ln);
					}
				}
			} finally {
				br.close();
			}
		}
	}
}
