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

import bdw.format.jpeg.JpegData;
import bdw.format.jpeg.component.ThumbnailJpeg;
import bdw.format.jpeg.data.DataItem;
import bdw.format.jpeg.marker.JfifSegment;
import bdw.format.jpeg.marker.JfxxSegment;
import bdw.format.jpeg.marker.SofSegment;
import bdw.format.jpeg.support.InvalidJpegFormat;
import java.util.List;

/**
 * HFIF validator.  The same as the NonHierarchical validator, except that
 * it also verifies that:
 * <ul>
 * <li>The JFIF segment appears right after the SOI marker
 * <li>The JFXX segment, if it appears, is right after the JFIF segment.
 * <li>The SOF has certain parameter values
 * </ul>
 *
 * Note that this depends explicitly on the JfifSegment and JfxxSegment classes.
 * This is unlike other validators that are class independent.
 */
public class JfifValidator extends NonHierarchicalValidator {

	/**
	 * States that the validation process goes through
	 */
	private enum State {
		STARTED,
		FOUND_SOI,
		FOUND_JFIF,
		WANT_SOF
	};

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate(List<DataItem> elements) {
		List<Exception> problems = super.validate(elements);
		State state = State.STARTED;

		for (int index = 0; index < elements.size(); index++) {
			DataItem element = elements.get(index);
			if (isIgnored(element)) {
				continue;
			}

			switch (state) {
				case STARTED:
					if (isSOI(element)) {
						state = State.FOUND_SOI;
					}
					break;
				case FOUND_SOI:
					if (element instanceof JfifSegment) {
						JfifSegment segment = (JfifSegment)element;
						if (segment.getVersion() == 0x0102) {
							state = State.FOUND_JFIF;	// Can look for a JFXX segment
						} else {
							state = State.WANT_SOF;
						}
					} else {
						problems.add(new InvalidJpegFormat("Did not find JFIF segment immediately after SOI"));
						return problems;
					}
					break;
				case FOUND_JFIF:
					if (element instanceof JfxxSegment) {
						JfxxSegment jfxx = (JfxxSegment) element;
						if (jfxx.getExtensionCode() == JfxxSegment.JPEG &&
								  jfxx.getThumbnail() instanceof ThumbnailJpeg) {
							JpegData jpegImage = ((ThumbnailJpeg)jfxx.getThumbnail()).getJpegImage();
							for (DataItem item : jpegImage) {
								if (item instanceof JfifSegment) {
									problems.add(new InvalidJpegFormat("Thumbnail should not hava a Jfif segment"));
								} else if (item instanceof SofSegment) {
									checkComponents((SofSegment) item, problems, true);
								}
							}
						}
					}
					index--; // try again.
					state = State.WANT_SOF;
					break;
				case WANT_SOF:
					if (element instanceof SofSegment) {
						checkComponents((SofSegment) element, problems, false);
						return problems;
					}
					break;
			}
		}

		return problems;
	}

	/**
	 * Check that the SOF component matches the spec requirements for parameter values
	 */
	protected void checkComponents(SofSegment sof, List<Exception> problems, boolean thumbnail) {
		String prefix = "JFIF " + (thumbnail ? "thumbnail" : "images");
		if (sof.getComponentCount() != 1 && sof.getComponentCount() != 3) {
			problems.add(new InvalidJpegFormat(prefix + " must have 1 or 3 components"));
		}
		if (sof.getComponent(0).getComponentId() != 1) {
			problems.add(new InvalidJpegFormat(prefix + " must have component #1 being 1"));
		}
		if (sof.getComponentCount() >= 2) {
			if (sof.getComponent(1).getComponentId() != 2) {
				problems.add(new InvalidJpegFormat(prefix + " must have component #2 being 2"));
			}
		}
		if (sof.getComponentCount() >= 3) {
			if (sof.getComponent(2).getComponentId() != 3) {
				problems.add(new InvalidJpegFormat(prefix + " must have component #3 being 3"));
			}
		}
	}
}
