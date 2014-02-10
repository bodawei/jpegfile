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
package bdw.cli;

import bdw.format.jpeg.JpegData;
import bdw.format.jpeg.data.DataItem;
import bdw.format.jpeg.data.EntropyData;
import bdw.format.jpeg.data.Marker;
import bdw.format.jpeg.marker.ComSegment;
import java.io.RandomAccessFile;

/**
 * Simple hacky routine which will read a JPEG file and write out what markers
 * are in it.
 */
public class Lister {
    public static void main(String[] args)  {
        JpegData parser = new JpegData();
		try {
			RandomAccessFile file = new RandomAccessFile(args[0], "r");
			try {
				parser.read(file);
			} catch (Exception e) {
				System.out.println("EXCEPTION: " + e);
				e.printStackTrace();
			}
			if ( ! parser.isValid()) {
				System.out.println("INVALID FILE: " + args[0]);
				for (Exception e : parser.validate()) {
					System.out.println("PROBLEM: " + e.getMessage());
				}
			}
			for (DataItem item : parser) {
				if (item instanceof Marker) {
					Marker base = (Marker) item;
					System.out.println("Marker: " + base.getClass().getSimpleName() + " - " + base.getMarkerId());
					if (base instanceof ComSegment) {
						ComSegment segment = (ComSegment) base;
						String s = segment.getStringComment();
						System.out.println("\t\tComment: " + s);
					}
				} else if (item instanceof EntropyData) {
				//	System.out.println("        Entropy data (" + ((EntropyData)item).getData().length + " bytes)");
				}
			}
		} catch (Exception ex) {
			System.out.println("EXCEPTION: " + ex);
			ex.printStackTrace();
		}
    }

}
