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
package bdw.formats.jpeg;

import bdw.formats.jpeg.segments.AppNSegment;
import bdw.formats.jpeg.segments.ComSegment;
import bdw.formats.jpeg.segments.DacSegment;
import bdw.formats.jpeg.segments.DataSegment;
import bdw.formats.jpeg.segments.DhpSegment;
import bdw.formats.jpeg.segments.DhtSegment;
import bdw.formats.jpeg.segments.DnlSegment;
import bdw.formats.jpeg.segments.DqtSegment;
import bdw.formats.jpeg.segments.DriSegment;
import bdw.formats.jpeg.segments.EoiSegment;
import bdw.formats.jpeg.segments.ExpSegment;
import bdw.formats.jpeg.segments.JpgNSegment;
import bdw.formats.jpeg.segments.JpgSegment;
import bdw.formats.jpeg.segments.UnknownSegment;
import bdw.formats.jpeg.segments.RstSegment;
import bdw.formats.jpeg.segments.base.SegmentBase;
import bdw.formats.jpeg.segments.SofSegment;
import bdw.formats.jpeg.segments.SoiSegment;
import bdw.formats.jpeg.segments.SosSegment;
import bdw.formats.jpeg.segments.TemSegment;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class JpegParser implements Iterable<SegmentBase> {

	protected List<Class<? extends SegmentBase>> segmentManagers;
	protected List<SegmentBase> segments;
	protected File diskVersion;

	/**
	 * Creates a new JpegFile with an empty list of segments.
	 */
	public JpegParser() {
		segments = new ArrayList<SegmentBase>();
		segmentManagers = new ArrayList();
	}

	public JpegParser(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
		this();
	}

	/**
	 * Adds all standard Jpeg segments to this instance.
	 * */
	public void addStandardSegments() {
		addSegmentHandler(SoiSegment.class);
		addSegmentHandler(EoiSegment.class);
		addSegmentHandler(DqtSegment.class);
		addSegmentHandler(SofSegment.class);
		addSegmentHandler(DhtSegment.class);
		addSegmentHandler(SosSegment.class);

		// test
		addSegmentHandler(TemSegment.class);
		addSegmentHandler(DacSegment.class);
		addSegmentHandler(DnlSegment.class);
		addSegmentHandler(JpgSegment.class);
		addSegmentHandler(RstSegment.class);
		addSegmentHandler(DriSegment.class);
		addSegmentHandler(DhpSegment.class);
		addSegmentHandler(ExpSegment.class);
		addSegmentHandler(ComSegment.class);
		addSegmentHandler(JpgNSegment.class);

		addSegmentHandler(AppNSegment.class);

		// reserved between 0x02 and BF
	}

	public void addSegmentHandler(Class<? extends SegmentBase> aClass) {
		segmentManagers.add(aClass);
	}

	public void readFromFile(File jpegFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, InvalidJpegFormat {
		RandomAccessFile file = new RandomAccessFile(jpegFile, "r");
		int aByte;
		int markerByte;
		boolean seenSOS = false;

		while (file.getFilePointer() < file.length()) {
			long pos = file.getFilePointer();
			aByte = file.readUnsignedByte();

			if (aByte != 0xFF) {
				file.seek(pos);
				if (seenSOS) {
					DataSegment data = new DataSegment(DataSegment.SUBTYPE, file, ParseMode.STRICT);
					this.segments.add(data);
				} else {
					UnknownSegment data = new UnknownSegment(UnknownSegment.SUBTYPE, file, ParseMode.STRICT);
					this.segments.add(data);
				}
			} else {
				markerByte = 0;
				if (file.getFilePointer() < file.length()) {
					markerByte = file.readUnsignedByte();
				}
				if (markerByte == 0) {
					file.seek(pos);
					if (seenSOS) {
						DataSegment data = new DataSegment(DataSegment.SUBTYPE, file, ParseMode.STRICT);
						this.segments.add(data);
					} else {
						UnknownSegment data = new UnknownSegment(UnknownSegment.SUBTYPE, file, ParseMode.STRICT);
						this.segments.add(data);
					}
				} else {
					Class<? extends SegmentBase> managerClass;
					Boolean canHandle = Boolean.FALSE;
					Method handler;
					Exception foo;
					Constructor constructor;
					SegmentBase manager = null;

					for (int index = 0; index < this.segmentManagers.size(); index++) {
						managerClass = this.segmentManagers.get(index);
						try {
							handler = managerClass.getDeclaredMethod("canHandleMarker", int.class);
							if (handler != null) {
								canHandle = (Boolean) handler.invoke(managerClass, markerByte);
							}
							if (canHandle) {
								constructor = managerClass.getDeclaredConstructor(int.class, RandomAccessFile.class, ParseMode.class);
								try {
									manager = (SegmentBase) constructor.newInstance(markerByte, file, ParseMode.STRICT);
									this.segments.add(manager);
									break;
								} catch (Exception e) {
									canHandle = Boolean.FALSE;
								}
							}
						} catch (IllegalArgumentException ex) {
							foo = ex;
							//ex.printStackTrace();
						} catch (InvocationTargetException ex) {
							foo = ex;
						} catch (NoSuchMethodException ex) {
							foo = ex;
						} catch (SecurityException ex) {
							foo = ex;
						}
					
					}
					if (manager == null) {
						UnknownSegment data = new UnknownSegment(UnknownSegment.SUBTYPE, file, ParseMode.STRICT);
						this.segments.add(data);
					}  else if (manager instanceof SosSegment) {
						seenSOS = true;
					}
				}
			}
		}
	}

	public void readFromStream(InputStream stream) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, InvalidJpegFormat {
		DataInputStream dataStream = new DataInputStream(stream);
		int aByte;
		int markerByte;

		try {
			while (true) {
				boolean seenSOS = false;
				dataStream.mark(2);
				aByte = dataStream.readUnsignedByte();

				if (aByte != 0xFF) {
					if (seenSOS) {
						DataSegment data = new DataSegment(DataSegment.SUBTYPE, dataStream, ParseMode.STRICT);
						this.segments.add(data);
					} else {
						UnknownSegment data = new UnknownSegment(UnknownSegment.SUBTYPE, dataStream, ParseMode.STRICT);
						this.segments.add(data);
					}
				} else {
					markerByte = 0;
					markerByte = dataStream.readUnsignedByte();
					if (markerByte == 0) {
						dataStream.reset();
						if (seenSOS) {
							DataSegment data = new DataSegment(DataSegment.SUBTYPE, dataStream, ParseMode.STRICT);
							this.segments.add(data);
						} else {
							UnknownSegment data = new UnknownSegment(UnknownSegment.SUBTYPE, dataStream, ParseMode.STRICT);
							this.segments.add(data);
						}
					} else {
						Class<? extends SegmentBase> managerClass;
						Boolean canHandle = Boolean.FALSE;
						Method handler;
						Exception foo;
						Constructor constructor;
						SegmentBase manager = null;

						for (int index = 0; index < this.segmentManagers.size(); index++) {
							managerClass = this.segmentManagers.get(index);
							try {
								handler = managerClass.getDeclaredMethod("canHandleMarker", int.class);
								if (handler != null) {
									canHandle = (Boolean) handler.invoke(managerClass, markerByte);
								}
								if (canHandle) {
									constructor = managerClass.getDeclaredConstructor(int.class, InputStream.class, ParseMode.class);
									try {
										manager = (SegmentBase) constructor.newInstance(markerByte, dataStream, ParseMode.STRICT);
										this.segments.add(manager);
										break;
									} catch (Exception e) {
										canHandle = Boolean.FALSE;
									}
								}
							} catch (IllegalArgumentException ex) {
								foo = ex;
								ex.printStackTrace();
							} catch (InvocationTargetException ex) {
								foo = ex;
							} catch (NoSuchMethodException ex) {
								foo = ex;
							} catch (SecurityException ex) {
								foo = ex;
							}

						}
						if (manager == null) {
							UnknownSegment data = new UnknownSegment(UnknownSegment.SUBTYPE, dataStream, ParseMode.STRICT);
							this.segments.add(data);
						}  else if (manager instanceof SosSegment) {
							seenSOS = true;
						}
					}
				}

			}
		} catch (EOFException exception) {
			// we're done.  so gracefully quit.
		}
	}

	/**
	 * Writes a copy of the contents of this file to the specified stream.
	 * Note that this is a copy of whatever the set of segments have, not necessarily what
	 * was in the file this was passed in the constructor.
	 * @param stream the stream to write to
	 */
	public void write(OutputStream stream) throws IOException {
		Iterator<SegmentBase> i = iterator();
		while (i.hasNext()) {
			i.next().write(stream);
		}
	}

	/**
	 * Returns whether the set of segments in the file describe a reasonable jpeg file
	 * for example, starts with a SOI segment, and ends with an EOI. Overlooks details which
	 * are generally OK even if not defined by the spec.
	 * @return true if the segments describe a legitimate Jpeg file
	 */
	public boolean isValid() {
		boolean foundBegin = false;
		boolean foundEnd = false;

		Iterator<SegmentBase> i = iterator();
		while (i.hasNext()) {
			SegmentBase segment = i.next();

			if (segment instanceof SoiSegment) {
				foundBegin = true;
			} else if (segment instanceof EoiSegment) {
				if (!foundBegin) {
					return false;
				} else {
					foundEnd = true;
				}
			}
		}

		if ((foundBegin == false) || (foundEnd == false)) {
			return false;
		}

		for (SegmentBase segment : this) {
			if ( ! segment.isValid()) {
				return false;
			}

			if (segment instanceof UnknownSegment) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @return true if the segments describe a jpeg file that exactly matches the jpeg spec.
	 */
	public boolean isStrictlyValid() {
		return false;
	}

	/*
	 * Adds the specified segment to the list, unless it is already there,
	 * in which case add nothing.
	 * @param segment The segment to be added (null is ignored)
	 */
	public void addSegment(SegmentBase segment) {
		if ((segment != null) && (!segments.contains(segment))) {
			segments.add(segment);
		}
	}

	/**
	 * Inserts the segment at the specified location. If the segment is already in the file,
	 * it will be moved and put at the location that (before the move) was index.
	 * @param segment The segment to be inserted (null is ignored)
	 * @param index The index to add the segment at. If out of range, will be put at the start or end of the list
	 */
	public void insertSegmentAt(SegmentBase segment, int index) {
		if (index >= segments.size()) {
			index = segments.size();
		} else if (index < 0) {
			index = 0;
		}

		if ((segment != null) && (!segments.contains(segment))) {
			segments.add(index, segment);
		}
	}

	/**
	 * Returns the segment at the specified index.
	 * @param index the index of the segment
	 * @return the segment at the specified index in the file, or null if no such segment.
	 */
	public SegmentBase getSegmentAt(int index) {
		if ((index >= 0) && (index <= segments.size())) {
			return segments.get(index);
		}

		return null;
	}

	/**
	 * Returns a list of all the segments in the file.
	 * @return a new (possibly empty) array containing all the segments in the file in order
	 */
	public List<SegmentBase> getSegments() {
		List<SegmentBase> copy = new ArrayList<SegmentBase>();

		copy.addAll(segments);

		return copy;
	}

	/**
	 * Removes the specified segment from the file.  If the segment is not in the file
	 * does nothing.
	 * @param segment The segment to remove from the file
	 */
	public void removeSegment(SegmentBase segment) {
		int index = segments.indexOf(segment);
		if (index != -1) {
			removeSegmentAt(index);
		}
	}

	/**
	 * Removes the segment from the specified index in the file
	 * If no segment at that position, nothing is changed
	 * @param index index of the segment to remove
	 */
	public void removeSegmentAt(int index) {
		if ((index >= 0) && (index <= segments.size())) {
			segments.remove(index);
		}
	}

	/**
	 * @return An iterator that will iterate over all the segments in the file
	 */
	@Override
	public Iterator<SegmentBase> iterator() {
		return segments.listIterator();
	}
}
