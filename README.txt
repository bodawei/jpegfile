JPEGFile : The README

PURPOSE
=======
This project provides classes to manipulate the data structures that make up a "JPEG" file.  It does not provide any kind of JPEG encoder or decoder.

The original motivation for the project was my desire to be able to programmatically make changes to the "EXIF" information in a JPEG file.  Since I could find no tools already existing to do this in Java, I set out to write my own, and the project broadened a little bit beyond just doing EXIF.


CREDITS
=======
This is constructed without the benefit of the formal specs, as they're a bit expensive for the hobbyist (e.g. http://www.iso.org/iso/iso_catalogue/catalogue_tc/catalogue_detail.htm?csnumber=18902).

I've used references from various sources:
Very usefully has been Alex Katovsky's jpegdump project:
	http://www.a-kat.com/programming/cpp/jpeg/JPEGDump.html
	http://www.facebook.com/alexander.katovsky
Others:
* http://www.bsdg.org/swag/GRAPHICS/0143.PAS.html
* http://www.cs.clemson.edu/~westall/481/notes/jpegimage.pdf
* http://www.imperialviolet.org/binary/jpeg/
* http://www.impulseadventure.com/
* http://www.w3.org/Graphics/JPEG/jfif3.pdf

