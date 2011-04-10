/*
 *  Copyright 2011 æŸ�å¤§è¡›
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
package bdw.formats.jpeg.segments;

import bdw.formats.jpeg.segments.base.BlobSegmentBase;
import java.io.IOException;

/**
 *
 */
public class DataSegment extends BlobSegmentBase {

	public static int MARKER = 0x0100;

	public DataSegment() {
		super();
		this.interpretInitialByte = true;
	}

	@Override
	public int getMarker() {
		return DataSegment.MARKER;
	}

	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof DataSegment)) {
			return false;
		} else {
			DataSegment segment = (DataSegment) other;
			if (segment.getDataLength() != dataLength) {
				return false;
			}
			try {
				for (int index = 0; index < dataLength; index++) {
					if (data[index] != segment.getDataAt(index)) {
						return false;
					}
				}
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
}
