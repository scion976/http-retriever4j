/*
 * Copyright 2022 HTTPRetriever4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fluffyluffs.httpretriever4j;

/** Header */
public class Header {

	private final String type;
	private final String header;

	public Header(String type, String header) {
		this.type = type;
		this.header = header;
	}

	public String getType() {
		return type;
	}

	public String getHeader() {
		return header;
	}
	
	
}
