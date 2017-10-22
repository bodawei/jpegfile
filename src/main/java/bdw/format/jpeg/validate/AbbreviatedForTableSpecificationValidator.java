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
import java.util.List;

/**
 * Validator to validate the "Abbreviated format for table-specification data"
 * defined on page B-21 of the standard.
 */
public class AbbreviatedForTableSpecificationValidator extends Validator {

	/**
	 * States that the validation process goes through
	 */
	private enum State {
		WANT_SOI,
		WANT_TABLESMISC_OR_EOI,
		DONE
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate(List<DataItem> elements) {
		List<Exception> problems = super.validate(elements);
		State state = State.WANT_SOI;

		for (int index = 0; index < elements.size(); index++) {
			if (isIgnored(elements.get(index))) {
				continue;
			}

			DataItem element = elements.get(index);
			switch (state) {
				case WANT_SOI:
					if (isSOI(element)) {
						state = State.WANT_TABLESMISC_OR_EOI;
					} else {
						addUnexpectedProblem(problems, "SOI", element);
					}
					break;
				case WANT_TABLESMISC_OR_EOI:
					if (isTablesMisc(element)) {
						state = State.WANT_TABLESMISC_OR_EOI;
					} else if (isEOI(element)) {
						state = State.DONE;
					} else {
						addUnexpectedProblem(problems, "tables/misc or EOI", element);
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
			case WANT_TABLESMISC_OR_EOI:
				addUnexpectedProblem(problems, "Tables or EOI", null);
				break;
		}

		return problems;
	}
}
