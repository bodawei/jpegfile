/*
 *  Copyright 2017 柏大衛
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
import bdw.format.jpeg.marker.ComSegment;
import bdw.format.jpeg.marker.SoiMarker;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Demonstration of how you might insert a comment into a JPEG file. The idea for this
 * fine example goes to Alexander Zagniotov https://github.com/azagniotov/stubby4j
 */
public class AddComment {

    public static void main(String[] args)  {
		if (args.length != 3) {
			System.out.println("Demonstration utility of how to add a comment to a JPEG file");
			System.out.println("Call as: bin/addComment.sh source.jpg newDest.jpg 'this is a cool comment'");
			return;
		}

		try {
         // read in the JPEG file
         JpegData parser = new JpegData();
			RandomAccessFile file = new RandomAccessFile(args[0], "r");
         parser.read(file);

			if (!parser.isValid()) {
				System.out.println("INVALID FILE: " + args[0]);
				for (Exception e : parser.validate()) {
					System.out.println("PROBLEM: " + e.getMessage());
				}
            return;
			}

         if (!(parser.getItem(0) instanceof SoiMarker)) {
				System.out.println("Did not find an Start Of Image (SOI) marker at start of file.  This is strange.");
            return;
         }

         // Create a comment segment to add to the file
         ComSegment comment = new ComSegment();
         comment.setStringComment(args[2]);

         // Put it into the parsed results after position 0 (which is the SOI marker)
         parser.insertItem(1, comment);

         // Write out the image
         FileOutputStream out = new FileOutputStream(args[1]);
         parser.write(out);
         out.close();

		} catch (Exception ex) {
			System.out.println("EXCEPTION: " + ex);
			ex.printStackTrace();
		}
    }

}
