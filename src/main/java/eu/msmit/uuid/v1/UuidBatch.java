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

/**
 * @author Marijn Smit (info@msmit.eu)
 * @since Feb 25, 2015
 */
public class UuidBatch {

	public static final UuidBatch ONE = new UuidBatch(1);

	public static enum Strategy {
		DISTRIBUTE, FOLLOW_TIME;
	}

	private final int amount_;
	private final Strategy strategy_;

	public UuidBatch(int amount) {
		this(amount, Strategy.DISTRIBUTE);
	}

	public UuidBatch(int amount, Strategy strategy) {
		amount_ = amount;
		strategy_ = strategy;
	}

	public int getAmount() {
		return amount_;
	}

	public Strategy getStrategy() {
		return strategy_;
	}
}
