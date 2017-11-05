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
import java.util.List;

/**
 * Simple code-sharing class, which provides a protected method to do validation
 * on a frame.  This is used by other validators to get common behavior for
 * validating frames.
 *
 * Note, when used in hierarchical mode, the standard seems to imply on page
 * B-22 that EXP must come after other tables, while the description of EXP
 * on page B-20 that it can appear intermixed among tables.  This assumes the
 * latter, and puts no restrictions on the number of times it can occur there.
 * This also allows an EXP before the first frame, which may not be right.
 *
 * This also doesn't check some other details, such as whether RST values are
 * sequential, whether DRI has been defined when RST are used, whether a DQT
 * is present between progressive DCT's of one component, or the DQT in a DHP.
 */
public class HasFrameValidator extends Validator {

	/**
	 * States that the validation process goes through
	 */
	private enum State {
		WANT_TABLES_OR_SOF,
		WANT_TABLES_OR_SOS,
		WANT_ENTROPY,
		WANT_TABLES_OR_SOS_OR_ENDOFFRAME,
		WANT_RST_OR_DNL_OR_SOS_OR_ENDOFFRAME,
		WANT_RST_OR_OR_SOS_ENDOFFRAME
	};

	/**
	 * Validates a frame
    * @param elements The DataItems to use
    * @param startIndex The index to start at
    * @param hierarchical True if this is hierarchical
    * @param problems Problems that are found
    * @return The index
	 */
	protected int validateFrame(List<DataItem> elements, int startIndex, boolean hierarchical, List<Exception> problems) {
		State state = State.WANT_TABLES_OR_SOF;
		int sosCount = 0;
		int index = startIndex;
		int tableStartIndex = 0;
		boolean done = false;

		while (!done && index < elements.size()) {
			DataItem element = elements.get(index);
			if (isIgnored(element)) {
				index++;
				continue;
			}

			switch (state) {
				case WANT_TABLES_OR_SOF:
					if (isSOF(element)) {
						state = State.WANT_TABLES_OR_SOS;
						sosCount = 0;
					} else if (isEXP(element)) {
						if (hierarchical) {
							state = State.WANT_TABLES_OR_SOF;
						} else {
							addUnexpectedProblem(problems, "Tables or SOF", element);
						}
					} else if (isTablesMisc(element)) {
						state = State.WANT_TABLES_OR_SOF;
					} else {
						addUnexpectedProblem(problems, "Tables or SOF", element);
					}
					break;
				case WANT_TABLES_OR_SOS:
					if (isSOS(element)) {
						state = State.WANT_ENTROPY;
						tableStartIndex = 0;
						sosCount++;
					} else if (isTablesMisc(element)) {
						state = State.WANT_TABLES_OR_SOS;
					} else {
						addUnexpectedProblem(problems, "Tables or SOS", element);
					}
					break;
				case WANT_ENTROPY:
					if (isEntropy(element)) {
						if (sosCount == 1) {
							state = State.WANT_RST_OR_DNL_OR_SOS_OR_ENDOFFRAME;
						} else {
							state = State.WANT_RST_OR_OR_SOS_ENDOFFRAME;
						}
					} else {
						addUnexpectedProblem(problems, "Entropy data", element);
					}
					break;
				case WANT_TABLES_OR_SOS_OR_ENDOFFRAME:
					if (isSOS(element)) {
						state = State.WANT_ENTROPY;
						tableStartIndex = 0;
						sosCount++;
					} else if (isTablesMisc(element)) {
						state = State.WANT_TABLES_OR_SOS;
					} else {
						return (tableStartIndex == 0) ? index -1 : tableStartIndex -1;
					}
					break;
				case WANT_RST_OR_DNL_OR_SOS_OR_ENDOFFRAME:
					if (isRST(element)) {
						state = State.WANT_ENTROPY;
					} else if (isTablesMisc(element)) {
						state = State.WANT_TABLES_OR_SOS_OR_ENDOFFRAME;
						tableStartIndex = index;
					} else if (isSOS(element)) {
						state = State.WANT_ENTROPY;
						tableStartIndex = 0;
					} else if (isDNL(element)) {
						state = State.WANT_TABLES_OR_SOS;
					} else {
						return index -1;
					}
					break;
				case WANT_RST_OR_OR_SOS_ENDOFFRAME:
					if (isRST(element)) {
						state = State.WANT_ENTROPY;
					} else if (isTablesMisc(element)) {
						state = State.WANT_TABLES_OR_SOS_OR_ENDOFFRAME;
						tableStartIndex = index;
					} else if (isSOS(element)) {
						state = State.WANT_ENTROPY;
						tableStartIndex = 0;
					} else {
						return index -1;
					}
					break;
			}

			index++;
		}

		return index;
	}
}
