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
package com.davidjohnburrowes.format.jpeg.validate;

import com.davidjohnburrowes.format.jpeg.data.DataItem;
import com.davidjohnburrowes.format.jpeg.data.EntropyData;
import com.davidjohnburrowes.format.jpeg.data.ExtraFf;
import com.davidjohnburrowes.format.jpeg.data.Marker;
import com.davidjohnburrowes.format.jpeg.marker.AppNSegment;
import com.davidjohnburrowes.format.jpeg.marker.ComSegment;
import com.davidjohnburrowes.format.jpeg.marker.DacSegment;
import com.davidjohnburrowes.format.jpeg.marker.DhpSegment;
import com.davidjohnburrowes.format.jpeg.marker.DhtSegment;
import com.davidjohnburrowes.format.jpeg.marker.DnlSegment;
import com.davidjohnburrowes.format.jpeg.marker.DqtSegment;
import com.davidjohnburrowes.format.jpeg.marker.DriSegment;
import com.davidjohnburrowes.format.jpeg.marker.EoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.ExpSegment;
import com.davidjohnburrowes.format.jpeg.marker.RstMMarker;
import com.davidjohnburrowes.format.jpeg.marker.SofSegment;
import com.davidjohnburrowes.format.jpeg.marker.SoiMarker;
import com.davidjohnburrowes.format.jpeg.marker.SosSegment;
import com.davidjohnburrowes.format.jpeg.support.DataMode;
import com.davidjohnburrowes.format.jpeg.support.InvalidJpegFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all validators.  The role of a Validator is to examine a list
 * of DataItems and determine if it meets some level of syntactic or semantic
 * validity.  For example, a JPEG file must start with an SOI marker and end
 * eith an EOI marker.  Thus, a validator might report that a JPEG file that
 * started with an EOI marker and ends with an SOI marker is invalid.
 *
 * While designed for use by the JpegData class, it should be able to be used
 * by any client that has a list of DataItems.
 */
public class Validator {

	/**
	 * @param elements A list of DataItems
	 * @return a list of Exceptions. This will be empty if there are no problems,
	 *			and it will have one Exception for each validity problem found in
	 *			the items
	 */
	public List<Exception> validate(List<DataItem> elements) {
		return new ArrayList<Exception>();
	}

	/**
	 * Adds an exception to the list of problems indicating the caller expected
	 * to find expectedName, but instead found something else.
    * @param problems A List to store any problems found
    * @param expectedName The name that is expected
    * @param found The item found
	 */
	protected void addUnexpectedProblem(List<Exception> problems, String expectedName, DataItem found) {
		InvalidJpegFormat problem;
		String message;

		if (expectedName == null) {
			message = "Expected to find nothing, but found ";
		} else {
			message = "Expected to find a " + expectedName + " marker, but found ";
		}

		if (found == null) {
			message += "end of the list";
		} else {
			message += found.getClass().getName();
		}

		problem = new InvalidJpegFormat(message);

		problems.add(problem);

	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has the marker id of SOI
	 */
	protected boolean isSOI(DataItem item) {
		return (item instanceof Marker) &&
				  ((Marker)item).getMarkerId() == SoiMarker.MARKERID;
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem is one of the segment types that the standard
	 *				calls Tables/Misc
	 */
	protected boolean isTablesMisc(DataItem item) {
		if ( ! (item instanceof Marker)) {
			return false;
		}

		Marker marker = (Marker) item;
		int id = marker.getMarkerId();

		return (id == DqtSegment.MARKERID ||
				  id == DhtSegment.MARKERID ||
				  id == DacSegment.MARKERID ||
				  id == DriSegment.MARKERID ||
				  id == ComSegment.MARKERID ||
				  (id >= AppNSegment.FIRST_MARKERID &&
				  id <= AppNSegment.LAST_MARKERID));
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has the marker id of DHP
	 */
	protected boolean isDHP(DataItem item) {
		return (item instanceof Marker) &&
				  ((Marker)item).getMarkerId() == DhpSegment.MARKERID;
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has the marker id of EXP
	 */
	protected boolean isEXP(DataItem item) {
		return (item instanceof Marker) &&
				  ((Marker)item).getMarkerId() == ExpSegment.MARKERID;
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has one of the SOF marker id's
	 */
	protected boolean isSOF(DataItem item) {
		if ( ! (item instanceof Marker)) {
			return false;
		}

		Marker marker = (Marker) item;
		int id = marker.getMarkerId();

		return ((id >= SofSegment.FIRST1_MARKERID && id <= SofSegment.LAST1_MARKERID) ||
			(id >= SofSegment.FIRST2_MARKERID && id <= SofSegment.LAST2_MARKERID) ||
			(id >= SofSegment.FIRST3_MARKERID && id <= SofSegment.LAST3_MARKERID) ||
			(id >= SofSegment.FIRST4_MARKERID && id <= SofSegment.LAST4_MARKERID));
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has the marker id of SOS
	 */
	protected boolean isSOS(DataItem item) {
		return (item instanceof Marker) &&
				  ((Marker)item).getMarkerId() == SosSegment.MARKERID;
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem is an EntropyData instance
	 */
	protected boolean isEntropy(DataItem item) {
		return (item instanceof EntropyData);
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has the marker id of DNL
	 */
	protected boolean isDNL(DataItem item) {
		return (item instanceof Marker) &&
				  ((Marker)item).getMarkerId() == DnlSegment.MARKERID;
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has one of the RST marker id's
	 */
	protected boolean isRST(DataItem item) {
		return (item instanceof Marker) &&
				  (((Marker)item).getMarkerId() >= RstMMarker.FIRST_MARKERID &&
				   ((Marker)item).getMarkerId() <= RstMMarker.LAST_MARKERID);
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem has the marker id of EOI
	 */
	protected boolean isEOI(DataItem item) {
		return (item instanceof Marker) &&
				  ((Marker)item).getMarkerId() == EoiMarker.MARKERID;
	}

	/**
    * @param item The DataItem to examine
	 * @return true if the DataItem is one which should be ignored for validation
	 *					purposes.
	 */
	protected boolean isIgnored(DataItem item) {
		return (item instanceof ExtraFf);
	}
}
