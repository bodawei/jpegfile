/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bdw.cli;

import bdw.format.jpeg.JpegParser;
import bdw.format.jpeg.data.Segment;
import bdw.format.jpeg.segment.ComSegment;
import bdw.format.jpeg.segment.base.SegmentBase;
import java.io.File;

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
			if ( ! parser.isValid()) {
				System.out.println("INVALID FILE: " + args[0]);
			}
			for (Object sgmt : parser) {
				if (sgmt instanceof SegmentBase) {
					SegmentBase base = (SegmentBase) sgmt;
					System.out.println("Segment: " + base.getClass().getSimpleName() + " - " + base.getMarker());
					if (base instanceof ComSegment) {
						ComSegment segment = (ComSegment) base;
						System.out.println("\t\tComment: " + segment.getComment());
					}
				}
				if (sgmt instanceof Segment) {
					Segment base = (Segment) sgmt;
					System.out.println("Segment: " + base.getClass().getSimpleName() + " - " + base.getMarker());
//					if (base instanceof ComSegment) {
//						ComSegment segment = (ComSegment) base;
//						System.out.println("\t\tComment: " + segment.getComment());
//					}
				}
//				if (base instanceof DataSegment) {
//					DataSegment segment = (DataSegment) base;
//					System.out.println("\t\tLength: " + segment.getDataLength());
//				} else if (base instanceof JunkSegment) {
//					JunkSegment segment = (JunkSegment) base;
//					System.out.println("\t\tLength: " + segment.getDataLength());
//					System.out.println("\t\tBytes 0-1: " + segment.getDataAt(0) + " " + segment.getDataAt(1));
//				} else if (base instanceof AppNSegment) {
//					AppNSegment segment = (AppNSegment) base;
//					System.out.println("\t\tLength: " + segment.getBytes().length);
//					System.out.println("\t\tBytes 0-1: " + segment.getBytes()[0] + " " + segment.getBytes()[1]);
//					System.out.println("\t\tBytes last two: " + segment.getBytes()[segment.getBytes().length-2] + " " + segment.getBytes()[segment.getBytes().length-1]);
//				}
			}
		} catch (Exception ex) {
			System.out.println("EXCEPTION: " + ex);
			ex.printStackTrace();
		}


    }

}
