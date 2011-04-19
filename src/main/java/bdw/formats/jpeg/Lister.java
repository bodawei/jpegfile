/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.formats.jpeg;

import bdw.formats.jpeg.segments.AppNSegment;
import bdw.formats.jpeg.segments.DataSegment;
import bdw.formats.jpeg.segments.JunkSegment;
import bdw.formats.jpeg.segments.base.SegmentBase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bodawei
 */
public class Lister {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        JpegParser parser = new JpegParser();
		parser.addStandardSegments();
		try {
			parser.readFromFile(new File(args[0]));
			for (SegmentBase base : parser) {
				System.out.println("Segment: " + base.getClass().getName() + " - " + base.getMarker());
				if (base instanceof DataSegment) {
					DataSegment segment = (DataSegment) base;
					System.out.println("\t\tLength: " + segment.getDataLength());
				} else if (base instanceof JunkSegment) {
					JunkSegment segment = (JunkSegment) base;
					System.out.println("\t\tLength: " + segment.getDataLength());
					System.out.println("\t\tBytes 0-1: " + segment.getDataAt(0) + " " + segment.getDataAt(1));
				} else if (base instanceof AppNSegment) {
					AppNSegment segment = (AppNSegment) base;
					System.out.println("\t\tLength: " + segment.getBytes().length);
					System.out.println("\t\tBytes 0-1: " + segment.getBytes()[0] + " " + segment.getBytes()[1]);
					System.out.println("\t\tBytes last two: " + segment.getBytes()[segment.getBytes().length-2] + " " + segment.getBytes()[segment.getBytes().length-1]);
				}
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Lister.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Lister.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(Lister.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(Lister.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvalidJpegFormat ex) {
			Logger.getLogger(Lister.class.getName()).log(Level.SEVERE, null, ex);
		}


    }

}
