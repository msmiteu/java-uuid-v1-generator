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

import java.util.Random;

import eu.msmit.uuid.v1.DefaultGenerator;

public class SkewingGenerator extends DefaultGenerator {
	public static void main(String[] args) {
		new SkewingGenerator().next();
	}

	private Random random_;
	private long skew_;

	@Override
	protected long currentTimeMs() {
		if (random_ == null) {
			random_ = new Random();
		}
		if (random_.nextInt(1000) < 1) {
			skew_ += random_.nextInt(1000);
		}

		return super.currentTimeMs() - skew_;
	}
}
