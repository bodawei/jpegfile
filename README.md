JPEGFile
========

JPEGFile is a library for reading and writing JPEG files at a fairly low level.

Some examples of when you would use it would make it clear when and why you would use it:

* You have an existing JPEG file that you want to open, and add a textual comment into it: This library is great for that!
* You want to dig through a JPEG file and understand what is in it: This library is also great for yo!
* You have an raw bitmapped image that you want to turn into a JPEG file: This library won't help you.

Version and Legal Stuff
-----------------------

This is version 0.9 of this library.  What that version is meant to convey is: This is beta quality.  It works, there are not likely to be any drastic changes to the API or functionality before 1.0.  However, it hasn't been used soooo extensively that it can be considered unquestionably rock-solid.  Think of this as a Google beta.  It won't reach 1.0 for quite a while, and it should be quite  safe to use for the time being.

This is being distributed under the Apache 2.0 license. See the relevant license file in the directory alongside this read me.

Naturally, I welcome all feedback, contributions, questions, and notes if you use this.

Known Issues
------------

* Some JPEG files are padded with a series of 0's after the EOI marker.  Technically, these are valid JPEG files because once you see the EOI marker, we have no concern for what else is there.  Yet, I've also seen JPEG files with clearly flawed junk after EOI.  I haven't determined how to handle this yet.
* Coping with an EOF in the middle of reading a segment is not as graceful as it needs to be. (the bytes making up that segment are effectively lost, thus defeating the goal of providing full passthrough of data)
* Validation isn't as comprehensive as it could be.

History
-------

Some time ago, I thought "Wouldn't it be great to add some metadata to all my JPEG files? I know there's some way to do that. I thought it would be easy to find a Java library which let me open my files and add the relevant metadata. However, I didn't find anything.  "Gosh, the world really needs this!" I figured.  So, I started reverse engineering the JPEG format. I got progressively more interested, found the book *JPEG Still Image Data Compression Standard*, and over time build this library which should read in any JPEG file and allow you access to all the internal data structures

Of course, during this process, I realized that much of the info I wanted was in EXIF or related data formats which aren't part of JPEG itself. So, ironically, this doesn't actually solve my original problem fully.  But, this is a great foundation and it should be relatively easy to add those others on top of this.

Example Uses
------------

In the bin directory are a couple shell scripts that use this library to do some simple work with JPEG files.  They might be a good place to start to see what this library can do.  None of them will alter your original file.


Design Architecture
-------------------

This project has evolved to being as much a general-purpose toolkit for fiddling with JPEG files as a library to accomplish the original goal.

### A brief overview of the JPEG format ###

As you may or may not know, a JPEG file is made up of a series of bytes (of course), where some of those bytes are special "markers" that identify particularly interesting data.  Markers are of the form 0xFF##, where ## is a pair of hex digits.  So, a JPEG file must start with 0xFFD8, which indicates (surprise) the start of a JPEG file (this is the SOI, Start of Image, marker).  Other markers are followed by parameters, and these are called marker segments. Marker segments may have various metadata (e.g. COM segments, or APPN segments), may have data crucially important to decompressing the data (e.g. quantization tables).

An ordinary JPEG file contains a SOF (Start of Frame) segment which defines various parameters about how to interpret the JPEG data, and it is followed by a one or more SOS (Start of Scan) segments, each of which is followed by one or more chunks of binary data. This binary data (called "entropy coded" data) is the actual compressed image. If there are multiple chunks of entropy coded data, they are separated by RST (restart) markers.  Thus a simple JPEG file might have this structure

~~~
SOI
COM
SOF
DQT  // Define Quantization Table
SOS
Entropy data
RST
Entropy data
SOS
Entropy data
EOI  // End of Image
~~~

There's also the ability to define a JPEG file so that it involves a "hierarchical" process.  in this case, there are multiple frames. The first may define a low resolution version of the image, and subsequent ones deliver higher resolution versions. The idea here is that you can deliver an approximation of the image first, and if you want ultimately deliver a lossless version of the image by the time you've accessed and processed all the bytes.  The structure of this is similar to the more common format, above:

~~~
SOI
COM
DHP  // Define Hierarchical Progression
SOF
SOS
Entropy data
SOF
SOS
Entropy data
EOI
~~~

You may have heard of JFIF.  This is defined by a separate standard, which more or less says: Take the JPEG standard, and reserve the first (or first and second) marker segments after the SOI marker to convey a bit more meta information about the image (e.g. the size, perhaps a thumbnail).  In this case, the image might be like this:

~~~
SOI
JFIF  // An APP0 segment with an identifying string as the first 5 bytes of data
JFXX  // An APP0 segment with another identifying string
SOF
SOS
Entropy data
EOI
~~~

### About the JPEGFile architecture ###

All the interesting classes in this library descend from DataItem. This defines a common interface and common behavior for all chunks of data that make up a JPEG file (I use the word "file" loosely here. This can deal with an in-memory stream as well as a file on disk).  There's an extensive comment at the start of DataItem which not only discusses it, but the various modes of behavior it supports (you may begin to want to read the JPEG standard to understand all of that, I'm afraid)

At the lower level, the JPEGFile library has a set of classes that represent the individual markers and marker segments. Thus, there is a class that represent the SOI (Start of Image) marker, a class that represents an SOF (Start of Frame) marker segment, a class that represents a JFIF Segment. These are ignorant of any greater context.  You can instantiate one and hand-set all the properties of the instance, or you can read a series of bytes (arranged according to the JPEG standard) from a file or a stream to populate it.

There are also a set of classes that descend from "Component" (not awt.Component). These are just pieces of the MarkerSegments.  For instance, a DAC (Define Arithmetic Coding) segment is made up of a set of tables, and each table is represented as an instance of a DacConditioningTable instance.

Alongside these is a class, EntropyData, which represents the actual entropy-coded data that makes up the JPEG image.

Probably most interestingly here, though, is the JpegData class. This represents an entire JPEG file. It contains routines which walk through a JPEG file, instantiate the relevant Marker and MarkerSegment classes, and ask them to parse the next part of the file. If you want "one stop shopping" use of this library, just instantiate one of these, point it at a file, and away you go.

As it is written, the JpegData class can be reconfigured so you can give it a different set of marker and segment classes (if you should have some specialized need where you want to swap out one of the existing segment classes, JpegData will work fine with this). JpegData also has a replaceable validator, so if you want to check for different validity conditions in your JPEG files, you can accomplish this too.

However, it is conceivable that JpegData may not meet your exact needs.  For example, you might want to have something which scans quickly through a JPEG file and just identifies one kind of segment.  It would be easy to write your own parser that would do this, and then just make use of the individual marker and marker segment classes as needed.

It is worth noting that one of the design goals here is to allow the library to cope with some kinds of malformed JPEG files.  If you set the DataMode to LAX on any DataItem, it will ignore a lot of aspects of the data that violate the JPEG standard, allowing you to read in some kinds of malformed files and identify where and why they are malformed.

Finally, another one of the goals for this project has been to allow you to read a JPEG file in and write it back out and get byte-for-byte fidelity.  That is, if you JPEG file is technically valid, but makes use of one of the corners of the JPEG format where there are multiple ways to do that thing, this library will not alter your data.  It will try to "pass through" all your bytes.  If you have LAX mode set, you'll even be able to read in and write out invalid JPEG files.

I hope this is enough to get you oriented with the code