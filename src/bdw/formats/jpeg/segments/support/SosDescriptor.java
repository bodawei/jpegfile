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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author bodawei
 */
public class SosDescriptor {
	private int componentId;
	private int huffmannTableId;

	public void readData(DataInput source) throws IOException {
		componentId = source.readUnsignedByte();
		huffmannTableId = source.readUnsignedByte();
	}

	public void write(DataOutput output) throws IOException {
		output.writeByte(componentId);
		output.writeByte(huffmannTableId);
	}

	public int getComponentId() {
		return componentId;
	}

	public int getHuffmannTableId() {
		return huffmannTableId;
	}

	@Override
	public boolean equals(Object other) {
		if ((other == null) || (!(other instanceof SosDescriptor))) {
			return false;
		} else if (other != this) {
			SosDescriptor castOther = (SosDescriptor) other;

			if ((componentId != castOther.getComponentId()) ||
					(huffmannTableId != castOther.getHuffmannTableId())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + this.componentId;
		hash = 29 * hash + this.huffmannTableId;
		return hash;
	}
}
