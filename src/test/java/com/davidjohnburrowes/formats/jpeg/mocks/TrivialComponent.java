/*
 *  Copyright 2014,2017 柏大衛
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
package com.davidjohnburrowes.formats.jpeg.mocks;

import com.davidjohnburrowes.format.jpeg.data.Component;
import java.io.DataInput;
import java.io.IOException;
import java.io.OutputStream;


public class TrivialComponent extends Component {
	public int data;

	@Override
	public int getSizeOnDisk() {
		return 1;
	}

	@Override
	public void readParameters(DataInput source) throws IOException {
		super.readParameters(source);
		data = source.readUnsignedByte();
	}

	@Override
	public void write(OutputStream stream) throws IOException {
		super.write(stream);

		stream.write((byte)data);
	}
}
