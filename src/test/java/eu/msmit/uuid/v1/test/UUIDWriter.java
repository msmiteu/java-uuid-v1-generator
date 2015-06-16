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

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.msmit.uuid.v1.UUIDv1;

public class UUIDWriter {
	public static void main(String[] args) throws Exception {
		new UUIDWriter().run(args);
	}

	private void run(String[] args) throws Exception {
		for (String arg : args) {
			if ("child".equals(arg)) {
				runUUID(args);
				return;
			}
		}

		File javaBin = new File(System.getProperty("java.home"), "bin");
		File javaExec = null;
		for (File file : javaBin.listFiles()) {
			String name = file.getName();
			if (name.contains(".")) {
				name = name.substring(0, name.indexOf("."));
			}
			if (!name.equals("java")) {
				continue;
			}
			javaExec = file;
		}
		if (javaExec == null) {
			throw new Error("Java not found");
		}

		final List<String> cmd = new ArrayList<String>();
		cmd.add(javaExec.getAbsolutePath());
		cmd.add("-cp");
		cmd.add(System.getProperty("java.class.path", ""));
		cmd.add(UUIDWriter.class.getName());
		cmd.add("child");

		File dir = new File("C:\\Project\\UUIDTest\\out.log");
		final Redirect redir = Redirect.to(dir);

		ExecutorService executorService = Executors.newFixedThreadPool(3);
		for (int i = 0; i < 1024; i++) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						System.out.println("Starting process");
						ProcessBuilder builder = new ProcessBuilder(cmd);
						builder.redirectError(redir);
						builder.redirectOutput(redir);
						Process proc = builder.start();
						int exit = proc.waitFor();
						if (exit != 0) {
							System.out.println("process exited with code "
									+ exit);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		executorService.shutdown();
	}

	private void runUUID(String[] args) throws Exception {
		String fileName = "_" + UUID.randomUUID().toString();
		File dir = new File("C:\\Project\\UUIDTest\\seeds");
		File out = new File(dir, fileName);

		int uuidlen = UUIDv1.next().toString().length();
		int count = 100000;

		RandomAccessFile rnd = new RandomAccessFile(out, "rw");
		rnd.setLength(count * (uuidlen + 1));

		List<String> uuids = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			UUID next = UUIDv1.next();
			uuids.add(next.toString());
		}

		// Write sorterd
		for (String uuid : uuids) {
			rnd.write(uuid.getBytes());
			rnd.write('\n');
		}

		rnd.close();

		out.renameTo(new File(out.getParentFile(), fileName.substring(1)));
	}
}
