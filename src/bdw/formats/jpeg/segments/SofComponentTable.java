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


package bdw.formats.jpeg.segments;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author bodawei
 */
public class SofComponentTable {
	private int componentId;
	private int samplingValues;
	private int quantizationTableId;

	public void readFromFile(RandomAccessFile file) throws IOException {
		// actually, read an array of these.
		componentId = file.readUnsignedByte(); //  (1 = Y, 2 = Cb, 3 = Cr, 4 = I, 5 = Q)
		samplingValues = file.readUnsignedByte(); //(bit 0-3 vert., 4-7 hor.)
		quantizationTableId = file.readUnsignedByte();

		//- JFIF uses either 1 component (Y, greyscaled) or 3 components (YCbCr, sometimes called YUV, colour).
	}

}
