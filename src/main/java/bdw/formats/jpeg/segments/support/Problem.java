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
package bdw.formats.jpeg.segments.support;

/**
 *
 */
public class Problem {
	
	public static enum ProblemType {
		ERROR,
		WARNING,
		INFO;
	}
	
	private int code;
	private ProblemType type;
	
	public Problem(ProblemType type, int code) {
		this.type = type;
		this.code = code;
	}
	
	public ProblemType getType() {
		return type;
	}
	
	public int getCode() {
		return code;
	}
	
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof Problem)) {
			return false;
		}
		
		Problem pOther = (Problem) other;
		
		if ((getCode() == pOther.getCode()) &&
			(getType() == pOther.getType())) {
			return true;
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + this.code;
		hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
		return hash;
	}
		
}
