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
 * Validator to validate the standard non-hierarchical syntax of a JPEG file.
 * This is defined on page B-5 of the standard. Note that this includes the
 * abbreviated compressed data format (page B-21) as that is the same overall
 * format, but may be missing some tables.
 *
 * This does no validation of the parameters across markers (e.g. it doesn't
 * make sure the component counts in one marker match the data in another)
 */
public class NonHierarchicalValidator extends HasFrameValidator {

	/**
	 * States that the validation process goes through
	 */
	private enum State {
		WANT_SOI,
		WANT_TABLES_OR_SOF,
		WANT_EOI,
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
						state = State.WANT_TABLES_OR_SOF;
					} else {
						addUnexpectedProblem(problems, "SOI", element);
					}
					break;
				case WANT_TABLES_OR_SOF:
					if (isSOF(element) || isTablesMisc(element)) {
						index = validateFrame(elements, index, false, problems);
						state = State.WANT_EOI;
					} else {
						addUnexpectedProblem(problems, "Tables or SOF", element);
					}
					break;
				case WANT_EOI:
					if (isEOI(element)) {
						state = State.DONE;
					} else {
						addUnexpectedProblem(problems, "EOI", element);
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
			case WANT_TABLES_OR_SOF:
				addUnexpectedProblem(problems, "Tables or SOF", null);
				break;
			case WANT_EOI:
				addUnexpectedProblem(problems, "EOI", null);
				break;
		}

		return problems;
	}
}
