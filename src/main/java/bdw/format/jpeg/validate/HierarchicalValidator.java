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
package bdw.format.jpeg.validate;

import bdw.format.jpeg.data.DataItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator to validate the standard hierarchical syntax of a JPEG file.
 * This is defined on page B-19 of the standard.
 *
 * This does no validation of the parameters across markers. For example,
 * this does not validate that if a non differential frame uses DCT, then
 * all but final differential frames should use DCT.
 */
public class HierarchicalValidator extends HasFrameValidator {

	/**
	 * States that the validation process goes through
	 */
	private enum State {
		WANT_SOI,
		WANT_TABLES_OR_DHP,
		WANT_FRAME,
		WANT_FRAME_OR_EOI,
		DONE
	};

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate(List<DataItem> elements) {
		List<Exception> problems = new ArrayList<Exception>();
		State state = State.WANT_SOI;

		for (int index = 0; index < elements.size(); index++) {
			DataItem element = elements.get(index);
			if (isIgnored(element)) {
				continue;
			}
			switch (state) {
				case WANT_SOI:
					if (isSOI(element)) {
						state = State.WANT_TABLES_OR_DHP;
					} else {
						addUnexpectedProblem(problems, "SOI", element);
					}
					break;
				case WANT_TABLES_OR_DHP:
					if (isDHP(element)) {
						state = State.WANT_FRAME;
					} else if (isTablesMisc(elements.get(index))) {
						state = State.WANT_TABLES_OR_DHP;
					} else {
						addUnexpectedProblem(problems, "Tables or DHP", element);
					}
					break;
				case WANT_FRAME:
					if (isSOF(element) || isEXP(element) || isTablesMisc(element)) {
						index = validateFrame(elements, index, true, problems);
						state = State.WANT_FRAME_OR_EOI;
					} else {
						addUnexpectedProblem(problems, "Tables or SOF", element);
					}
					break;
				case WANT_FRAME_OR_EOI:
					if (isSOF(element) || isEXP(element) || isTablesMisc(element)) {
						index = validateFrame(elements, index, true, problems);
						state = State.WANT_FRAME_OR_EOI;
					} else if (isEOI(element)) {
						state = State.DONE;
					} else {
						addUnexpectedProblem(problems, "Tables, SOF or EOI", element);
					}
					break;
				case DONE:
					addUnexpectedProblem(problems, null, element);
					break;
			}
		}

		switch (state) {
			case WANT_SOI:
				addUnexpectedProblem(problems, "SOI", null);
				break;
			case WANT_TABLES_OR_DHP:
				addUnexpectedProblem(problems, "Tables or DHP", null);
				break;
			case WANT_FRAME:
				addUnexpectedProblem(problems, "Tables or SOF", null);
				break;
			case WANT_FRAME_OR_EOI:
				addUnexpectedProblem(problems, "Tables, SOF or EOI", null);
				break;
		}

		return problems;
	}
}
