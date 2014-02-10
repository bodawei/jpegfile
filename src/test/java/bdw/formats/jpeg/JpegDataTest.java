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

package bdw.formats.jpeg;

import bdw.format.jpeg.JpegData;
import bdw.format.jpeg.component.DqtQuantizationTable;
import bdw.format.jpeg.component.FrameComponent;
import bdw.format.jpeg.component.SosComponentSpec;
import bdw.format.jpeg.data.DataItem;
import bdw.format.jpeg.data.EntropyData;
import bdw.format.jpeg.data.ExtraFf;
import bdw.format.jpeg.data.Marker;
import bdw.format.jpeg.marker.AppNSegment;
import bdw.format.jpeg.marker.ComSegment;
import bdw.format.jpeg.marker.DnlSegment;
import bdw.format.jpeg.marker.DqtSegment;
import bdw.format.jpeg.marker.EoiMarker;
import bdw.format.jpeg.marker.RstMMarker;
import bdw.format.jpeg.marker.SofSegment;
import bdw.format.jpeg.marker.SoiMarker;
import bdw.format.jpeg.marker.SosSegment;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.InvalidJpegFormat;
import bdw.format.jpeg.validate.HierarchicalValidator;
import bdw.format.jpeg.validate.Validator;
import bdw.formats.jpeg.mocks.B2;
import bdw.formats.jpeg.mocks.BadB1;
import bdw.formats.jpeg.mocks.GoodB1;
import bdw.formats.jpeg.test.TestUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JpegDataTest {
	private TestUtils utils;
	private JpegData jpeg;
	private String FULL_JPEG = "FFD8" +					// SOI
				  "FFE6 0008 01 02 03 04 05 06" +		// APPN
				  "FFFE 0004 FF 00" +						// COM
				  "FFC0 000B 08 0000 0001 01 01 11 00" +									// SOF
				  "FFDB 0043 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F" +	// DQT
				  "             10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F" +
				  "             20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F" +
				  "             30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F 40" +
				  "FFDA 0008 01 00 11 00 3F 00" +		// SOS
				  "FF 00 FE 00" +								// Entropy
				  "FFDC 0004 0005" +							// DNL
				  "FFDA 0008 01 00 11 00 3F 00" +		// SOS
				  "01 02 03 04 05" +							// Entropy
				  "FF" +											// Extra FF
				  "FFD0" +										// RST
				  "01 02 03 04 05" +							// Entropy
				  "FFD9";										// EOI
	private String MULTI_FRAME = "FFD8" +					// SOI
				  "FFFE 0004 FF 00" +						// COM
				  "FFDE 000B 08 0000 0001 01 01 11 00" +	// DHP
				  "FFFE 0004 FF 00" +						// COM
				  "FFC7 000B 08 0000 0001 01 01 11 00" +	// SOF (lossless diff)
				  "FFFE 0004 FF 00" +						// COM
				  "FFDA 0008 01 00 00 00 00 00" +		// SOS
				  "FF 00 FE 00" +								// Entropy
				  "FFFE 0004 FF 00" +						// COM
				  "FFC1 000B 08 0000 0001 01 01 11 00" +	// SOF (extended)
				  "FFFE 0004 FF 00" +						// COM
				  "FFDA 0008 01 00 11 00 3F 00" +		// SOS
				  "FF 00 FE 00" +								// Entropy
				  "FFD9";										// EOI
	private JpegData full_jpeg;

	@Before
	public void setUp() throws IOException {
		utils = new TestUtils();
		jpeg = new JpegData();

		full_jpeg = new JpegData();
		full_jpeg.addItem(new SoiMarker());

		AppNSegment appn = new AppNSegment(0xE6);
		appn.setBytes(utils.makeByteArray("01 02 03 04 05 06"));
		full_jpeg.addItem(appn);

		ComSegment com = new ComSegment();
		com.setComment(utils.makeByteArray("FF 00"));
		full_jpeg.addItem(com);

		SofSegment sof = new SofSegment(0xC0);
		sof.setSamplePrecision(8);
		sof.setImageHeight(0);
		sof.setImageWidth(1);
		FrameComponent c = new FrameComponent();
		c.setComponentId(1);
		c.setHorizontalScaling(1);
		c.setVerticalScaling(1);
		c.setQuantizationSelector(0);
		sof.addComponent(c);
		full_jpeg.addItem(sof);

		DqtSegment dqt = new DqtSegment();
		DqtQuantizationTable t = new DqtQuantizationTable();
		for (int index = 0; index < 64; index++) {
			t.setElement(index, index+1);
		}
		dqt.addTable(t);
		full_jpeg.addItem(dqt);

		SosSegment sos = new SosSegment();
		sos.setSpectralSelectionStart(0);
		sos.setSpectralSelectionEnd(63);
		sos.setSuccessiveApproximationHigh(0);
		sos.setSuccessiveApproximationLow(0);
		SosComponentSpec s = new SosComponentSpec();
		s.setComponentSelector(0);
		s.setDcTableSelector(1);
		s.setAcTableSelector(1);
		sos.addComponentSpec(s);
		full_jpeg.addItem(sos);

		EntropyData entropy = new EntropyData();
		entropy.setData(utils.makeByteArray("FFFE00"));
		full_jpeg.addItem(entropy);

		DnlSegment dnl = new DnlSegment();
		dnl.setNumberOfLines(5);
		full_jpeg.addItem(dnl);

		sos = new SosSegment();
		sos.setSpectralSelectionStart(0);
		sos.setSpectralSelectionEnd(63);
		sos.setSuccessiveApproximationHigh(0);
		sos.setSuccessiveApproximationLow(0);
		s = new SosComponentSpec();
		s.setComponentSelector(0);
		s.setDcTableSelector(1);
		s.setAcTableSelector(1);
		sos.addComponentSpec(s);
		full_jpeg.addItem(sos);

		entropy = new EntropyData();
		entropy.setData(utils.makeByteArray("0102030405"));
		full_jpeg.addItem(entropy);

		ExtraFf extra = new ExtraFf();
		extra.setFfCount(1);
		full_jpeg.addItem(extra);

		full_jpeg.addItem(new RstMMarker(0xD0));

		entropy = new EntropyData();
		entropy.setData(utils.makeByteArray("0102030405"));
		full_jpeg.addItem(entropy);

		full_jpeg.addItem(new EoiMarker());
	}

	@Test
	public void setMarkerTypes_changesMarkersUsedForReading() throws IOException {
		InputStream stream = utils.makeInputStream("FFB1 0003 01 FFB2 0002");
		List<Class<? extends Marker>> types = new ArrayList<Class<? extends Marker>>();
		types.add(BadB1.class);
		types.add(GoodB1.class);
		types.add(B2.class);

		jpeg.setValidator(new Validator());
		jpeg.setMarkerTypes(types);
		jpeg.read(stream);

		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(GoodB1.class, i.next().getClass());
		assertEquals(B2.class, i.next().getClass());
		assertFalse(i.hasNext());
	}

	@Test
	public void getItemCount_byDefault_isZero() {
		assertEquals(0, jpeg.getItemCount());
	}

	@Test
	public void getItemCount_afterMarkersAdded_isCorrect() {
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(new EoiMarker());
		assertEquals(2, jpeg.getItemCount());
	}

	@Test
	public void getItem_returnsExpectedItem() {
		DataItem expected = new EoiMarker();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(expected);
		assertEquals(expected, jpeg.getItem(1));
	}

	@Test
	public void addItem_addsItemToTheEnd() {
		DataItem expected = new EoiMarker();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(expected);
		assertEquals(expected, jpeg.getItem(1));
	}

	@Test
	public void insertItem_addsItemToTheSpecifiedPlace() {
		DataItem expected = new EoiMarker();
		jpeg.addItem(new SoiMarker());
		jpeg.insertItem(0, expected);
		assertEquals(expected, jpeg.getItem(0));
	}

	@Test
	public void deleteItem_removesTheitem() {
		DataItem expected = new EoiMarker();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(expected);

		jpeg.deleteItem(0);

		assertEquals(expected, jpeg.getItem(0));
	}

	@Test
	public void iterator_returnsAnIterator() {
		DataItem expected = new EoiMarker();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(expected);

		Iterator i = jpeg.iterator();

		i.next();
		assertEquals(expected, i.next());
	}

	@Test
	public void getSizeOnDisk_byDefault_returnsZero() {
		assertEquals(0, jpeg.getSizeOnDisk());
	}

	@Test
	public void getSizeOnDisk_withMarkers_returnsExpectedSize() {
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(new EoiMarker());
		assertEquals(4, jpeg.getSizeOnDisk());
	}

	@Test(expected=IllegalArgumentException.class)
	public void readStream_givenNull_throwsException() throws IOException {
		jpeg.read((InputStream) null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void read_givenNull_throwsException() throws IOException {
		jpeg.read((RandomAccessFile) null);
	}

	@Test
	public void read_givenAnEmptyFile_readsNoMarkers() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		assertEquals(0, jpeg.getItemCount());
	}

	@Test
	public void read_givenAnEmptyStream_readsNoMarkers() throws IOException {
		InputStream stream = utils.makeInputStream("");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		assertEquals(0, jpeg.getItemCount());
	}

	@Test
	public void read_givenMarkerlessFile_readsOneEntropyData() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("00 AE FF 00 00 22 11");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(1, jpeg.getItemCount());
		EntropyData item = (EntropyData) i.next();
		assertArrayEquals(utils.makeByteArray("00 AE FF 00 22 11"), item.getData());
	}

	@Test
	public void read_givenMarkerlessStream_readsOneEntropyData() throws IOException {
		InputStream stream = utils.makeInputStream("00 AE FF 00 00 22 11");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(1, jpeg.getItemCount());
		EntropyData item = (EntropyData) i.next();
		assertArrayEquals(utils.makeByteArray("00 AE FF 00 22 11"), item.getData());
	}

	@Test
	public void readFile_withFFInEntropy_readsEntropyCorrectly() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF D8 11 22 FF 00 44 55 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		Iterator<DataItem> i = jpeg.iterator();
		i.next();
		EntropyData s = (EntropyData) i.next();
		assertEquals(5, s.getData().length);
	}

	@Test
	public void read_withFFInEntropy_readsEntropyCorrectly() throws IOException {
		InputStream stream = utils.makeInputStream("FF D8 11 22 FF 00 44 55 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		Iterator<DataItem> i = jpeg.iterator();
		i.next();
		EntropyData s = (EntropyData) i.next();
		assertEquals(5, s.getData().length);
	}

	@Test
	public void readFile_withFF00InEntropy_readsEntropyCorrectly() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF D8 FF 00 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		Iterator<DataItem> i = jpeg.iterator();
		i.next();
		EntropyData s = (EntropyData) i.next();
		byte[] d = s.getData();
		assertEquals(1, d.length);
		assertEquals(-1, d[0]);
	}

	@Test
	public void read_withFF00InEntropy_readsEntropyCorrectly() throws IOException {
		InputStream stream = utils.makeInputStream("FF D8 FF 00 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		Iterator<DataItem> i = jpeg.iterator();
		i.next();
		EntropyData s = (EntropyData) i.next();
		byte[] d = s.getData();
		assertEquals(1, d.length);
		assertEquals(-1, d[0]);
	}

	@Test
	public void readFile_givenExtraFFsBetweenMarkers_readsCorrectNumberOfItems() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF FF FF D8 FF FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		assertEquals(4, jpeg.getItemCount());
	}

	@Test
	public void read_givenExtraFFsBetweenMarkers_readsCorrectNumberOfItems() throws IOException {
		InputStream stream = utils.makeInputStream("FF FF FF D8 FF FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		assertEquals(4, jpeg.getItemCount());
	}

	@Test
	public void readFile_givenExtraFFsBetweenMarkers_createsExtraFfWithTheRightCounts() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF FF FF D8 FF FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		Iterator<DataItem> i = jpeg.iterator();

		assertEquals(2, ((ExtraFf)i.next()).getFfCount());
		i.next();
		assertEquals(1, ((ExtraFf)i.next()).getFfCount());
	}

	@Test
	public void read_givenExtraFFsBetweenMarkers_createsExtraFfWithTheRightCounts() throws IOException {
		InputStream stream = utils.makeInputStream("FF FF FF D8 FF FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		Iterator<DataItem> i = jpeg.iterator();

		assertEquals(2, ((ExtraFf)i.next()).getFfCount());
		i.next();
		assertEquals(1, ((ExtraFf)i.next()).getFfCount());
	}

	@Test
	public void readFile_givenAStreamOfFFs_createsOneExtraFfItem() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF FF FF FF FF");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		assertEquals(1, jpeg.getItemCount());
		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(5, ((ExtraFf)i.next()).getFfCount());
	}

	@Test
	public void read_givenAStreamOfFFs_createsOneExtraFfItem() throws IOException {
		InputStream stream = utils.makeInputStream("FF FF FF FF FF");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		assertEquals(1, jpeg.getItemCount());
		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(5, ((ExtraFf)i.next()).getFfCount());
	}

	@Test
	public void read_givenTrivialFile_readsCorrectNumberOfMarkers() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF D8 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		assertEquals(2, jpeg.getItemCount());
	}

	@Test
	public void read_givenTrivialStream_readsCorrectNumberOfMarkers() throws IOException {
		InputStream stream = utils.makeInputStream("FF D8 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		assertEquals(2, jpeg.getItemCount());
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_givenFailingMarkerType_givesUp() throws IOException {
		InputStream stream = utils.makeInputStream("FFB1 0003 01 FFB2 0002");
		List<Class<? extends Marker>> types = new ArrayList<Class<? extends Marker>>();
		types.add(BadB1.class);	// This will fail to be read
		types.add(B2.class);

		jpeg.setDataMode(DataMode.LAX);
		jpeg.setMarkerTypes(types);

		jpeg.read(stream);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void readFile_givenFailingMarkerType_givesUp() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FFB1 0003 01 FFB2 0002");
		List<Class<? extends Marker>> types = new ArrayList<Class<? extends Marker>>();
		types.add(BadB1.class);	// This will fail to be read
		types.add(B2.class);

		jpeg.setDataMode(DataMode.LAX);
		jpeg.setMarkerTypes(types);

		jpeg.read(file);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void read_givenNoMatchingMarkerTypes_givesUp() throws IOException {
		InputStream stream = utils.makeInputStream("FFB1 0003 01 FFB2 0002");
		List<Class<? extends Marker>> types = new ArrayList<Class<? extends Marker>>();
		types.add(B2.class);

		jpeg.setDataMode(DataMode.LAX);
		jpeg.setMarkerTypes(types);

		jpeg.read(stream);
	}

	@Test(expected=InvalidJpegFormat.class)
	public void readFile_givenNoMatchingMarkerTypes_givesUp() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FFB1 0003 01 FFB2 0002");
		List<Class<? extends Marker>> types = new ArrayList<Class<? extends Marker>>();
		types.add(B2.class);

		jpeg.setDataMode(DataMode.LAX);
		jpeg.setMarkerTypes(types);

		jpeg.read(file);
	}

	@Test
	public void read_withMarkersAndEntropy_readsThreeItems() throws IOException {
		InputStream stream = utils.makeInputStream("FF D8 00 AE 22 11 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(3, jpeg.getItemCount());
		assertTrue(i.next() instanceof SoiMarker);
		assertTrue(i.next() instanceof EntropyData);
		assertTrue(i.next() instanceof EoiMarker);
	}

	@Test
	public void readFile_withMarkersAndEntropy_readsThreeItems() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile("FF D8 00 AE 22 11 FF D9");

		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(file);

		Iterator<DataItem> i = jpeg.iterator();
		assertEquals(3, jpeg.getItemCount());
		assertTrue(i.next() instanceof SoiMarker);
		assertTrue(i.next() instanceof EntropyData);
		assertTrue(i.next() instanceof EoiMarker);
	}

	@Test
	public void read_completeStandardJpegData() throws IOException {
		InputStream stream = utils.makeInputStream(FULL_JPEG);

		jpeg.read(stream);

		assertEquals(full_jpeg, jpeg);
	}

	@Test
	public void readFile_completeStandardJpegData() throws IOException {
		RandomAccessFile file = utils.makeRandomAccessFile(FULL_JPEG);

		jpeg.read(file);

		assertEquals(full_jpeg, jpeg);
	}

	@Test
	public void write_writesExpectedOutput() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		jpeg.addItem(new SoiMarker());
		jpeg.addItem(new EoiMarker());

		jpeg.write(output);

		assertArrayEquals(utils.makeByteArray("FFD8 FFD9"), output.toByteArray());
	}

	@Test
	public void read_write_roundtrip() throws IOException {
		InputStream stream = utils.makeInputStream(FULL_JPEG);
		jpeg.read(stream);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		jpeg.write(output);

		assertArrayEquals(utils.makeByteArray(FULL_JPEG), output.toByteArray());
	}

	@Test
	public void validate_reportsNoProblemsOnValidJpeg() throws IOException {
		assertEquals(0, full_jpeg.validate().size());
	}

	@Test
	public void validate_reportsErrors_onEmptyJpeg() throws IOException {
		assertEquals(1, jpeg.validate().size());
	}

	@Test
	public void clearPassthrough_removesExtraFfAndCruftInMarkers() throws IOException {
		InputStream stream = utils.makeInputStream("FFFF FFD8 FFDC 0005 0045 00 FF FFD9");
		jpeg.setDataMode(DataMode.LAX);
		jpeg.read(stream);

		jpeg.clearPassthrough();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		jpeg.write(output);
		assertArrayEquals(utils.makeByteArray("FFD8 FFDC 0004 0045 FFD9"), output.toByteArray());
	}

	@Test
	public void testModeChanges() throws IOException {
		InputStream stream = utils.makeInputStream(MULTI_FRAME);
		jpeg.setValidator(new HierarchicalValidator());
		jpeg.read(stream);

		assertEquals(14, jpeg.getItemCount());

		for (int index = 0; index < jpeg.getItemCount(); index++) {
			assertTrue(jpeg.getItem(index).getHierarchicalMode());
		}

		for (int index = 0; index < 8; index++) {
			assertEquals(FrameMode.DIFF_HUFF_SPATIAL, jpeg.getItem(index).getFrameMode());
		}

		for (int index = 8; index < 14; index++) {
			assertEquals(FrameMode.HUFF_EXTENDED_SEQUENTIAL_DCT, jpeg.getItem(index).getFrameMode());
		}
	}
}
