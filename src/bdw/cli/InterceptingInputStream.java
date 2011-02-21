/*
 *  Copyright 2011 柏大衛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bdw.cli;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A buffered input stream which keeps track of the bytes read, allowing the caller
 * to, after the fact, get a copy of those bytes.
 */
public class InterceptingInputStream extends BufferedInputStream {

	private List<Integer> interception;
	private int limit;
	private int limitUsed;
	
	public InterceptingInputStream(InputStream in) {
		super(in);
		interception = new ArrayList<Integer>();
		limit = 0;
		limitUsed = 0;
	}
	
	@Override
	public int read() throws IOException {
		int result = super.read();
		
		limitUsed ++;
		if (limitUsed > limit) {
			limit = 0;
			limitUsed = 0;
		}
		
		interception.add(new Integer(result));
		return result;
	}

	@Override
	public void mark(int readlimit) {
		super.mark(readlimit);
		limit = readlimit;
		limitUsed = 0;
	}
	
	@Override
	public void reset() throws IOException {
		super.reset();
		if (limit != 0) {
			for (int index = 0; index < limitUsed; index++) {
				interception.remove(interception.size() -1);
			}
			limit = 0;
			limitUsed = 0;
		}
	}
	
	public int getInterceptedLength() {
		return interception.size();
	}
	
	public int readIntereception() {
		return interception.remove(0);
	}
	
	public void flushInterception() {
		interception = new ArrayList<Integer>();
	}
}
