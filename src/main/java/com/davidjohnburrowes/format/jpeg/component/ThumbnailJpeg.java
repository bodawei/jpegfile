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
package com.davidjohnburrowes.format.jpeg.component;

import com.davidjohnburrowes.format.jpeg.JpegData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * A thumbnail which is a jpeg image
 */
public class ThumbnailJpeg extends Thumbnail  {
	private JpegData jpegImage;

	/**
	 * Constructs the thumbnail with a simple jpeg image
	 */
	public ThumbnailJpeg() {
		jpegImage = new JpegData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + jpegImage.getSizeOnDisk();
	}

	/**
	 * @param image The jpeg image to use
	 */
	public void setJpegImage(JpegData image) {
		if (image == null) {
			throw new IllegalArgumentException("JpegData may not be null");
		}

		this.jpegImage = image;
	}

	public JpegData getJpegImage() {
		return this.jpegImage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(RandomAccessFile file) throws IOException {
		jpegImage.read(file);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(InputStream stream) throws IOException {
		jpegImage.setDataMode(getDataMode());
		jpegImage.read(stream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		jpegImage.write(stream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof ThumbnailJpeg)) {
			return false;
		}

		return (getJpegImage().equals(((ThumbnailJpeg) other).getJpegImage()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash * jpegImage.hashCode();
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void	changeChildrenModes() {
		jpegImage.setDataMode(getDataMode());
		jpegImage.setFrameMode(getFrameMode());
		jpegImage.setHierarchicalMode(getHierarchicalMode());
	}
}
