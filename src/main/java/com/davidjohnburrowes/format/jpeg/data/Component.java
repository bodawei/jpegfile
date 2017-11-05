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
package com.davidjohnburrowes.format.jpeg.data;

import com.davidjohnburrowes.util.Util;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Common parent class for all types that are parts of a MarkerSegment.
 * The main purpose of this class is to provide code-sharing and common interface
 * across all components.
 */
public class Component extends DataItem {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(RandomAccessFile file) throws IOException {
		readParameters(file);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(InputStream stream) throws IOException {
		readParameters(Util.wrapAsDataInput(stream));
	}

	/**
	 * Convenience routine called by both read() routines to parse their data.
	 * Subclasses will generally want to override this, rather than the two
	 * read() routines.
	 *
	 * @param dataSource The input source to read from
	 *
	 * @throws IOException on errors when reading from the dataSource
	 */
	public void readParameters(DataInput dataSource) throws IOException {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stream The stream to write to
	 * @throws IOException If an io problem happens.
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		DataOutputStream dataOut = Util.wrapAsDataOutput(stream);

		this.writeParameters(dataOut);
	}

	/**
	 * Write the contents of this component out to the specified stream.
	 * This is called by write(), and allows subclasses to override this when
	 * they want to use a DataOutputStream.
	 *
	 * @param output The stream to write to.
    * @throws IOException if an IO Exception happens
	 */
	public void writeParameters(DataOutputStream output) throws IOException {
	}

}
