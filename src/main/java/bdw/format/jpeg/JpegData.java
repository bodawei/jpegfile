/*
 *  Copyright 2014 柏大衛
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
package bdw.format.jpeg;

import bdw.format.jpeg.data.DataItem;
import bdw.format.jpeg.data.EntropyData;
import bdw.format.jpeg.data.ExtraFf;
import bdw.format.jpeg.data.Marker;
import bdw.format.jpeg.marker.AppNSegment;
import bdw.format.jpeg.marker.ComSegment;
import bdw.format.jpeg.marker.DacSegment;
import bdw.format.jpeg.marker.DhpSegment;
import bdw.format.jpeg.marker.DhtSegment;
import bdw.format.jpeg.marker.DnlSegment;
import bdw.format.jpeg.marker.DqtSegment;
import bdw.format.jpeg.marker.DriSegment;
import bdw.format.jpeg.marker.EoiMarker;
import bdw.format.jpeg.marker.ExpSegment;
import bdw.format.jpeg.marker.JfifSegment;
import bdw.format.jpeg.marker.JfxxSegment;
import bdw.format.jpeg.marker.JpgNSegment;
import bdw.format.jpeg.marker.JpgSegment;
import bdw.format.jpeg.marker.ResNSegment;
import bdw.format.jpeg.marker.RstMMarker;
import bdw.format.jpeg.marker.SofSegment;
import bdw.format.jpeg.marker.SoiMarker;
import bdw.format.jpeg.marker.SosSegment;
import bdw.format.jpeg.marker.TemMarker;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.support.MarkerId;
import bdw.format.jpeg.support.MarkerIdRange;
import bdw.format.jpeg.support.MarkerIdSet;
import bdw.format.jpeg.validate.NonHierarchicalValidator;
import bdw.format.jpeg.validate.Validator;
import bdw.util.Util;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents an entire Jpeg file. It can read and write a jpeg file
 * from disk or from an input stream, as well have a set of JPEG markers and
 * segments "hand added".
 *
 * Note that this has a special relationship with the FrameMode and HierarchicalMode
 * properties.  If there are no DHP or SOF segments in the JPEG file, then the
 * specified FrameMode and HierarchicalMode is applied to all markers. However,
 * if there are DHP and FrameModes, then their own properties will affect the
 * respective modes of the markers in the file.  Specifically, if DHP is present,
 * all markers will have HierarchicalMode set to true.  All markers after one
 * frame (or the start of the file) and the next SOF segment as well as markers
 * within the frame defined by that SOF segment get the FrameMode of that segment.
 */
public class JpegData extends DataItem implements Iterable<DataItem> {
	/**
	 * The list of DataItems this is managing
	 */
	private List<DataItem> dataItems = new ArrayList<DataItem>();

	/**
	 * The set of marker classes that this will use when read()'ing
	 */
	private List<Class<? extends Marker>> markerTypes =
		new ArrayList<Class<? extends Marker>>();

	/**
	 * The validator that this will use when reading
	 */
	private Validator validator = new NonHierarchicalValidator();

	/**
	 * Create a JpegData instance, with the set of all marker types
	 * defined in the JPEG and JFIF standards.
	 */
	public JpegData() {
		markerTypes.add(SoiMarker.class);
		markerTypes.add(EoiMarker.class);
		markerTypes.add(SofSegment.class);
		markerTypes.add(SosSegment.class);
		markerTypes.add(RstMMarker.class);
		markerTypes.add(ComSegment.class);
		markerTypes.add(JfifSegment.class);
		markerTypes.add(JfxxSegment.class);
		markerTypes.add(DqtSegment.class);
		markerTypes.add(DhtSegment.class);
		markerTypes.add(DacSegment.class);
		markerTypes.add(DnlSegment.class);
		markerTypes.add(DriSegment.class);
		markerTypes.add(DhpSegment.class);
		markerTypes.add(ExpSegment.class);
		markerTypes.add(TemMarker.class);
		markerTypes.add(AppNSegment.class);
		markerTypes.add(JpgNSegment.class);
		markerTypes.add(JpgSegment.class);
		markerTypes.add(ResNSegment.class);
	}

	/**
	 * Replace the set of marker types that this instance will use
	 * when parsing a Jpeg file.  This is mainly useful if you have your own
	 * types that you want this to use. Note that the read() routines will
	 * try to find a marker type based on the order of this list. Thus, if you
	 * have two marker types that both could be used (e.g. JfifSegment,
	 * JfxxSegment and AppNSegment all could match a 0xE0 marker, so in the
	 * default list they are provided in that order, so Jfif and Jfxx will be
	 * matched before the more generic AppNSegment)
	 *
	 * @param markerTypes The list of marker types to use.
	 */
	public void setMarkerTypes(List<Class<? extends Marker>> markerTypes) {
		this.markerTypes = markerTypes;
	}

	/*
	 * @return the set of marker types this is using
	 */
	public List<Class<? extends Marker>> getMarkerTypes() {
		return markerTypes;
	}

	/**
	 * @param validator The validator to use with this this JpegData instance
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	/**
	 * @return The validator being used by this JpegData instance
	 */
	public Validator getValidator() {
		return this.validator;
	}

	/*
	 * @return the number of segments.
	 */
	public int getItemCount() {
		return dataItems.size();
	}

	/**
	 * @param index The index to retrieve an item from
	 * @return the DataItem at the specified index
	 */
	public DataItem getItem(int index) {
		return dataItems.get(index);
	}

	/**
	 * @param item The Dataitem to add to the end of the list of data items
	 */
	public void addItem(DataItem item) {
		this.insertItem(dataItems.size(), item);
	}

	/**
	 * Inserts the specified DataItem into this. note that the modes of the
	 * DataItem will be updated
	 * @param index The index to add the DataItem at
	 * @param item The DataItem to be added
	 */
	public void insertItem(int index, DataItem item) {
		dataItems.add(index, item);
		item.setDataMode(getDataMode());
		setModes();
	}

	/**
	 * @param index The index of the DataItem to remove
	 * @return the removed DataItem
	 */
	public DataItem deleteItem(int index) {
		DataItem item =  dataItems.remove(index);
		setModes();
		return item;
	}

	/**
	 * @return An iterator that will iterate over all the segments in the file
	 */
	@Override
	public Iterator<DataItem> iterator() {
		return dataItems.listIterator();
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int getSizeOnDisk() {
		int size = super.getSizeOnDisk();
		for (DataItem item : this) {
			size += item.getSizeOnDisk();
		}
		return size;
	}

	/**
	 * @inheritdoc
	 *
	 * Note: In STRICT mode, if a marker segment has a problem, this will not
	 * read it in. After reading all marker segments in, a syntax check across
	 * segments will be run, and this may throw an InvalidJpegFormat exception,
	 * leaving all segments in this instance.
	 */
	@Override
	public void read(RandomAccessFile file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("Input file may not be null");
		}
		int extraFFCount = 0;

		while (file.getFilePointer() < file.length()) {
			DataItem result = null;
			long pos = file.getFilePointer();
			int aByte = file.readUnsignedByte();

			if (aByte != 0xFF) {
				file.seek(pos);
				result = new EntropyData();
				result.read(file);
			} else {
				int markerIdentifier;
				try {
					markerIdentifier = file.readUnsignedByte();
				} catch (IOException e) {
					if (extraFFCount != 0) {
						ExtraFf ff = new ExtraFf();
						ff.setFfCount(extraFFCount + 1);
						addItem(ff);
					}
					break;
				}

				switch (markerIdentifier) {
					case 0x00:
						file.seek(pos);
						result = new EntropyData();
						result.read(file);
						break;
					case 0xFF:
						extraFFCount ++;
						pos ++;
						file.seek(pos);
						break;
					default:
						List<Marker> types = findCandidateSegmentTypes(markerIdentifier);
						Exception last = null;

						for (Marker segment : types) {
							try {
								segment.setDataMode(getDataMode());
								segment.read(file);
								result = segment;
								break;
							} catch (Exception e) {
								last = e;
								file.seek(pos + 2); // rewind to just after the 0xFF## markerid
							}
						}

						if (result == null) {
							throw new InvalidJpegFormat("Could not handle segment", last);
						}
						break;
				}
			}

			if (result != null) {
				if (extraFFCount != 0) {
					ExtraFf ff = new ExtraFf();
					ff.setFfCount(extraFFCount);
					addItem(ff);
					extraFFCount = 0;
				}
				addItem(result);
			}
		}

		setModes();

		if (getDataMode() == DataMode.STRICT && !validate().isEmpty()) {
			throw new InvalidJpegFormat("The JPEG file is invalid.", validate().get(0));
		}
	}


	/**
	 * @inheritdoc
	 *
	 * Note: In STRICT mode, if a marker segment has a problem, this will not
	 * read it in. After reading all marker segments in, a syntax check across
	 * segments will be run, and this may throw an InvalidJpegFormat exception,
	 * leaving all segments in this instance.
	 */
	@Override
	public void read(InputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Input stream may not be null");
		}

		DataInputStream diStream = Util.wrapAsDataInput(stream);
		int extraFFCount = 0;

		while (true) {
			DataItem result = null;
			int aByte;

			diStream.mark(2);
			try {
				aByte = diStream.readUnsignedByte();
			} catch (EOFException e) {
				// Assume we're done now with the file.
				break;
			}

			if (aByte != 0xFF) {
				diStream.reset();
				result = new EntropyData();
				result.read(stream);
			} else {
				int markerIdentifier;

				try {
					markerIdentifier = diStream.readUnsignedByte();
				} catch (EOFException e) {
					if (extraFFCount != 0) {
						ExtraFf ff = new ExtraFf();
						ff.setFfCount(extraFFCount + 1);
						addItem(ff);
					}
					break;
				}

				switch (markerIdentifier) {
					case 0x00:
						diStream.reset();
						result = new EntropyData();
						result.read(stream);
						break;
					case 0xFF:
						extraFFCount ++;
						diStream.reset();
						diStream.readUnsignedByte(); // just read this. if we get another exception, we're very unahppy, so let it pass through
						break;
					default:
						Exception last = null;
						List<Marker> types = findCandidateSegmentTypes(markerIdentifier);
						diStream.mark(65536);
						for (Marker segment : types) {
							try {
								segment.setDataMode(getDataMode());
								segment.read(diStream);
								result = segment;
								break;
							} catch (Exception e) {
								last = e;
								diStream.reset();
							}
						}

						if (result == null) {
							throw new InvalidJpegFormat("Could not handle segment", last);
						}
						break;
				}
			}

			if (result != null) {
				if (extraFFCount != 0) {
					ExtraFf ff = new ExtraFf();
					ff.setFfCount(extraFFCount);
					addItem(ff);
					extraFFCount = 0;
				}
				addItem(result);
			}
		}

		setModes();

		if (getDataMode() == DataMode.STRICT && !validate().isEmpty()) {
			throw new InvalidJpegFormat("The JPEG file is invalid.", validate().get(0));
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void write(OutputStream stream) throws IOException {
		for (DataItem item : this) {
			item.write(stream);
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		for (DataItem item : dataItems) {
			results.addAll(item.validate());
		}
		results.addAll(validator.validate(dataItems));
		return results;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public void clearPassthrough() {
		super.clearPassthrough();
		for (int index = getItemCount()-1; index >= 0; index--) {
			DataItem item = getItem(index);
			if (item instanceof ExtraFf) {
				deleteItem(index);
			} else {
				item.clearPassthrough();
			}
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof JpegData)) {
			return false;
		}

		JpegData jOther = (JpegData) other;
		if (getItemCount() != jOther.getItemCount()) {
			return false;
		}

		Iterator thisIterator = this.iterator();
		Iterator otherIterator = jOther.iterator();

		while (thisIterator.hasNext()) {
			if (!thisIterator.next().equals(otherIterator.next())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + (this.dataItems != null ? this.dataItems.hashCode() : 0);
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void	changeChildrenModes() {
		super.changeChildrenModes();
		for (DataItem item : this) {
			item.setDataMode(getDataMode());
		}
		setModes();
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void checkModeChange() {
		setModes();
	}

	/**
	 * Update the Frame and Hierarchical modes on all items.  Start off using the
	 * modes specified on this JpegData instance. however, if we find a DHP
	 * segment, set all hierarchical modes to true. If we find a SOF segment,
	 * apply its FrameMode to all marker segments governed by it (everything
	 * after the last frame (or start of file) to the end of this frame (or end
	 * of file)
	 */
	protected void setModes() {
		int lastEntropyindex = -1;
		FrameMode currentFrameMode = getFrameMode();
		boolean currentHierMode = getHierarchicalMode();

		for (int currentIndex = 0; currentIndex < getItemCount(); currentIndex++) {
			DataItem item = getItem(currentIndex);

			if (item instanceof Marker) {
				int markerId = ((Marker)item).getMarkerId();

				// Found DHP.  Revise the hiearchical mode specified for all items before this
				if (markerId == DhpSegment.MARKERID) {
					currentHierMode = true;
					for (int subIndex = 0; subIndex <= currentIndex; subIndex++) {
						getItem(subIndex).setHierarchicalMode(currentHierMode);
					}
				}

				// Found an SOF. Update everything before the frame
				if ((markerId >= SofSegment.FIRST1_MARKERID && markerId <= SofSegment.LAST1_MARKERID) ||
					(markerId >= SofSegment.FIRST2_MARKERID && markerId <= SofSegment.LAST2_MARKERID) ||
					(markerId >= SofSegment.FIRST3_MARKERID && markerId <= SofSegment.LAST3_MARKERID) ||
					(markerId >= SofSegment.FIRST4_MARKERID && markerId <= SofSegment.LAST4_MARKERID)) {
					currentFrameMode = FrameMode.fromValue(markerId);
					for (int subIndex = lastEntropyindex+1; subIndex <= currentIndex; subIndex++) {
						getItem(subIndex).setFrameMode(currentFrameMode);
					}
				}
			}

			if (item instanceof EntropyData) {
				lastEntropyindex = currentIndex;
			}

			item.setHierarchicalMode(currentHierMode);
			item.setFrameMode(currentFrameMode);
		}
	}

	/**
	 * Examines the set of markerTypes this JpegData can make use of, and compares
	 * the provided markerId against them, returning the subset of marker types
	 * that may be able to parse that kind of data.  The items are returned in
	 * the same order that they are specified in the markerTypes array.
	 */
	private List<Marker> findCandidateSegmentTypes(int markerId) {
		List<Marker> matches = new ArrayList<Marker>();

		for (Class<? extends Marker> segmentType : markerTypes) {
			MarkerId simpleAntn = segmentType.getAnnotation(MarkerId.class);

			if (simpleAntn != null && simpleAntn.value() == markerId) {
				try {
					Constructor constructor = segmentType.getDeclaredConstructor();
					matches.add((Marker) constructor.newInstance());
				} catch (Exception e) {
					throw new InvalidJpegFormat("No constructor found for " +
							  segmentType.getName());
				}
			} else {
				MarkerIdRange rangeAntn = segmentType.getAnnotation(MarkerIdRange.class);
				MarkerIdSet setAntn = segmentType.getAnnotation(MarkerIdSet.class);

				if (rangeAntn != null && inRange(rangeAntn, markerId) ||
					setAntn != null && inSet(setAntn, markerId)) {
					try {
						Constructor intConstructor = segmentType.getDeclaredConstructor(int.class);

						matches.add((Marker) intConstructor.newInstance(markerId));
					} catch (Exception e) {
						throw new InvalidJpegFormat("No constructor(int) found for " +
							  segmentType.getName());
					}
				}
			}
		}

		return matches;
	}

	/**
	 * Returns true if the specified markerID is in the range specifiedby the
	 * MarkerIdRange annotation
	 */
	private boolean inRange(MarkerIdRange annotation, int markerId) {
		return markerId >= annotation.first() && markerId <= annotation.last();
	}

	/**
	 * Returns true if the specified markerID value is among the values defined
	 * in the MarkerIdSet annotation.
	 */
	private boolean inSet(MarkerIdSet annotation, int markerId) {
		int[] values = annotation.value();

		for (int value : values) {
			if (value == markerId) {
				return true;
			}
		}

		return false;
	}
}
