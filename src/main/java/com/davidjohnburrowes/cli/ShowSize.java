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
package com.davidjohnburrowes.cli;

import com.davidjohnburrowes.format.jpeg.JpegData;
import com.davidjohnburrowes.format.jpeg.data.DataItem;
import com.davidjohnburrowes.format.jpeg.marker.JfifSegment;
import java.io.RandomAccessFile;

/**
 * Simple utility which prints out the size found in a JFIF segment, if any.
 */
public class ShowSize {
    public static void main(String[] args)  {
		if (args.length != 1) {
			System.out.println("This simple demo utility must be called with a JPEG file. For example:");
			System.out.println("    bin/jshowsize.sh myWonderfulJpegFile.jpg");
			return;
		}

        JpegData parser = new JpegData();
		try {
			RandomAccessFile file = new RandomAccessFile(args[0], "r");
			parser.read(file);
			if ( ! parser.isValid()) {
				System.out.println("INVALID FILE: " + args[0]);
			}
			for (DataItem sgmt : parser) {
				if (sgmt instanceof JfifSegment) {
					JfifSegment jfif = (JfifSegment) sgmt;
					String units;
					switch (jfif.getUnits()) {
						case 0:
							units = "px";
							break;
						case 1:
							units = "in";
							break;
						case 2:
							units = "cm";
							break;
						default:
							units = "(unknown units)";
							break;

					}
					System.out.println("Width: " + jfif.getXDensity() + units);
					System.out.println("Height: " + jfif.getYDensity() + units);
				}
			}
		} catch (Exception ex) {
			System.out.println("EXCEPTION: " + ex);
			ex.printStackTrace();
		}
    }

}
