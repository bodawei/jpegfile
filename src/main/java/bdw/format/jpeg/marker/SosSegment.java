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
package bdw.format.jpeg.marker;

import bdw.format.jpeg.component.SosComponentSpec;
import bdw.format.jpeg.data.MarkerSegment;
import bdw.format.jpeg.support.DataBounds;
import bdw.format.jpeg.support.DataMode;
import bdw.format.jpeg.support.FrameMode;
import bdw.format.jpeg.support.MarkerId;
import bdw.io.LimitingDataInput;
import bdw.util.Nibble;
import bdw.util.Size;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Start of Scan marker
 *
 * Note: Allow componentCount to be 0
 *
 * Note: This is defined on pages B-8 and B-10 of the standard.
 */
@MarkerId(SosSegment.MARKERID)
public class SosSegment extends MarkerSegment {
	private static final DataBounds numComponentsBounds =
			new DataBounds("numberComponentsBounds", Size.BYTE, 1, 4);
	private static final DataBounds spectralSelectionStartBounds =
			new DataBounds("spectralSelectionStart", Size.BYTE,
			0, 0,
			0, 0,
			0, 63,
			0, 7);
	private static final DataBounds spectralSelectionEndBounds =
		  new DataBounds("spectralSelectionEnd", Size.BYTE,
		  63, 63,
		  63, 63,
		  0, 63,
		  0, 0);
	private static final DataBounds successiveApproximationHighBounds =
		  new DataBounds("successiveApproximationHigh", Size.NIBBLE,
		  0, 0,
		  0, 0,
		  0, 13,
		  0, 0);
	private static final DataBounds successiveApproximationLowBounds =
		  new DataBounds("successiveApproximationLow", Size.NIBBLE,
		  0, 0,
		  0, 0,
		  0, 13,
		  0, 15);
	/**
	 * The marker that this type accepts
	 */
	public static final int MARKERID = 0xDA;

	/**
	 * Array of ScanDescriptors
	 */
	private List<SosComponentSpec> componentSpecs;

	/**
	 * Start of spectral selection
	 */
	private int spectralSelectionStart;

	/**
	 * End of spectral selection
	 */
	private int spectralSelectionEnd;

	/**
	 * Successive approximation
	 */
	private int successiveApproximationHigh;

	/**
	 * Successive approximation
	 */
	private int successiveApproximationLow;

	/**
	 * Constructs an instance with all properties empty
	 */
	public SosSegment() {
		super(MARKERID);
		componentSpecs = new ArrayList<SosComponentSpec>();
		spectralSelectionStart = 0;
		spectralSelectionEnd = 63;
		successiveApproximationHigh = 0;
		successiveApproximationLow = 0;
	}

	/**
	 * Sets the start spectral selection value
	 * @param start a value between 0 and 255 indicating the start
	 */
	public void setSpectralSelectionStart(int start) {
		spectralSelectionStartBounds.throwIfInvalid(start, getFrameMode(), getDataMode());
		throwIfSsStartInvalid(start);

		spectralSelectionStart = start;
	}

	/**
	 * @return the start of the spectral selection
	 */
	public int getSpectralSelectionStart() {
		return spectralSelectionStart;
	}

	/**
	 * Sets the end spectral selection value
	 * @param end a value between 0 and 255 indicating the end
	 */
	public void setSpectralSelectionEnd(int end) {
		spectralSelectionEndBounds.throwIfInvalid(end, getFrameMode(), getDataMode());
		throwIfSsEndInvalid(getSpectralSelectionStart(), end);

		spectralSelectionEnd = end;
	}

	/**
	 * @return the end of the spectral selection
	 */
	public int getSpectralSelectionEnd() {
		return spectralSelectionEnd;
	}

	/**
	 * Sets the end spectral selection value
	 * @param approx a value between 0 and 255 indicating the start
	 */
	public void setSuccessiveApproximationHigh(int approx) {
		successiveApproximationHighBounds.throwIfInvalid(approx, getFrameMode(), getDataMode());

		successiveApproximationHigh = approx;
	}

	/**
	 * @return the successive approximation value
	 */
	public int getSuccessiveApproximationHigh() {
		return successiveApproximationHigh;
	}

	/**
	 * Sets the end spectral selection value
	 * @param approx a value between 0 and 255 indicating the start
	 */
	public void setSuccessiveApproximationLow(int approx) {
		successiveApproximationLowBounds.throwIfInvalid(approx, getFrameMode(), getDataMode());

		successiveApproximationLow = approx;
	}

	/**
	 * @return the successive approximation value
	 */
	public int getSuccessiveApproximationLow() {
		return successiveApproximationLow;
	}

	/**
	 * @return The number of componentSpecs in this segment
	 */
	public int getComponentSpecCount() {
		return componentSpecs.size();
	}

	/**
	 * @param index Index of the componentSpec to be returned
	 * @return The componentSpec at the specified index
	 */
	public SosComponentSpec getComponentSpec(int index) {
		return componentSpecs.get(index);
	}

	/**
	 * Add a componentSpec to the end of the list of componentSpecs
	 * @param componentSpec the componentSpec to add
	 */
	public void addComponentSpec(SosComponentSpec componentSpec) {
		insertComponentSpec(componentSpecs.size(), componentSpec);
	}

	/**
	 * @param index The index to put the new componentSpec (others are moved "right"
	 * @param componentSpec The componentSpec to add
	 */
	public void insertComponentSpec(int index, SosComponentSpec componentSpec) throws
			IndexOutOfBoundsException {
		if (componentSpec == null) {
			throw new IllegalArgumentException("Entry may not be null");
		}

		numComponentsBounds.throwIfInvalid(index+1, getFrameMode(), getDataMode());

		componentSpecs.add(index, componentSpec);
	}

	/**
	 * @param index Index of the componentSpec to delete
	 */
	public void deleteComponentSpec(int index) throws IndexOutOfBoundsException {
		componentSpecs.remove(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSizeOnDisk() {
		return super.getSizeOnDisk() + 4 + (2 * getComponentSpecCount());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		spectralSelectionStartBounds.accumulateOnViolation(getSpectralSelectionStart(), this.getFrameMode(), results);
		spectralSelectionEndBounds.accumulateOnViolation(getSpectralSelectionEnd(), this.getFrameMode(), results);
		accumulateOnViolation(results);
		successiveApproximationHighBounds.accumulateOnViolation(getSuccessiveApproximationHigh(), this.getFrameMode(), results);
		successiveApproximationLowBounds.accumulateOnViolation(getSuccessiveApproximationLow(), this.getFrameMode(), results);
		numComponentsBounds.accumulateOnViolation(componentSpecs.size(), this.getFrameMode(), results);

		for (SosComponentSpec spec: componentSpecs) {
			results.addAll(spec.validate());
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}

		SosSegment castOther = (SosSegment) other;
		if ((getSpectralSelectionStart() == castOther.getSpectralSelectionStart())
				&& (getSpectralSelectionEnd() == castOther.getSpectralSelectionEnd())
				&& (getSuccessiveApproximationHigh() == castOther.getSuccessiveApproximationHigh())
				&& (getSuccessiveApproximationLow() == castOther.getSuccessiveApproximationLow())) {
			if (getComponentSpecCount() != castOther.getComponentSpecCount()) {
				return false;
			}
			for (int index = 0; index < getComponentSpecCount(); index++) {
				SosComponentSpec myEntry = getComponentSpec(index);
				SosComponentSpec otherEntry = castOther.getComponentSpec(index);
				if ( ! myEntry.equals(otherEntry)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.componentSpecs != null ? this.componentSpecs.hashCode() : 0);
		hash = 37 * hash + this.spectralSelectionStart;
		hash = 37 * hash + this.spectralSelectionEnd;
		hash = 37 * hash + this.successiveApproximationHigh;
		hash = 37 * hash + this.successiveApproximationLow;
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readParameters(LimitingDataInput dataSource) throws IOException {
		int componentCount = dataSource.readUnsignedByte();

		componentSpecs.clear();

		for (int index = 0; index < componentCount; index++) {
			SosComponentSpec entry = new SosComponentSpec();
			entry.setFrameMode(getFrameMode());
			entry.setDataMode(getDataMode());
			entry.readParameters(dataSource);
			componentSpecs.add(entry);
		}

		setSpectralSelectionStart(dataSource.readUnsignedByte());
		setSpectralSelectionEnd(dataSource.readUnsignedByte());
		int approximation = dataSource.readUnsignedByte();

		setSuccessiveApproximationHigh(Nibble.getUpper(approximation));
		setSuccessiveApproximationLow(Nibble.getLower(approximation));
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		stream.writeByte(getComponentSpecCount());

		for (SosComponentSpec spec : componentSpecs) {
			spec.writeParameters(stream);
		}

		stream.writeByte(getSpectralSelectionStart());
		stream.writeByte(getSpectralSelectionEnd());
		stream.writeByte(Nibble.makeByte(getSuccessiveApproximationHigh(),
				  getSuccessiveApproximationLow()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkModeChange() {
		super.checkModeChange();
		setSpectralSelectionStart(getSpectralSelectionStart());
		setSpectralSelectionEnd(getSpectralSelectionEnd());
		setSuccessiveApproximationHigh(getSuccessiveApproximationHigh());
		setSuccessiveApproximationLow(getSuccessiveApproximationLow());
		if (getComponentSpecCount() != 0) {
			numComponentsBounds.throwIfInvalid(componentSpecs.size(), this.getFrameMode(), getDataMode());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void	changeChildrenModes() {
		super.changeChildrenModes();
		for (SosComponentSpec spec: componentSpecs) {
			spec.setDataMode(getDataMode());
			spec.setFrameMode(getFrameMode());
			spec.setHierarchicalMode(getHierarchicalMode());
		}
	}

	/**
	 * Check special conditions, and add any exceptions to the provided results
	 */
	private void accumulateOnViolation(List<Exception> results) {
		RuntimeException result = checkSsStartConditions(getSpectralSelectionStart());
		if (result != null) {
			results.add(result);
		}

		result = checkSsEndConditions(getSpectralSelectionStart(), getSpectralSelectionEnd());
		if (result != null) {
			results.add(result);
		}
	}

	/**
	 * Throw an exception if ssEnd conditions aren't met
	 */
	private void throwIfSsEndInvalid(int ssStart, int ssEnd) {
		RuntimeException result = checkSsEndConditions(ssStart, ssEnd);
		if (result != null && getDataMode() == DataMode.STRICT) {
			throw result;
		}
	}


	/**
	 * Throw an exception if ssEnd conditions aren't met
	 */
	private void throwIfSsStartInvalid(int ssStart) {
		RuntimeException result = checkSsStartConditions(ssStart);
		if (result != null && getDataMode() == DataMode.STRICT) {
			throw result;
		}
	}

	/**
	 * Check that spectralSelectionStart is within proper ranges, based the
	 * frame mode
	 */
	private RuntimeException checkSsStartConditions(int ssStart) {
		if (ssStart != 0 &&
				(getFrameMode() == FrameMode.DIFF_HUFF_SPATIAL ||
				 getFrameMode() == FrameMode.DIFF_AC_SPATIAL) &&
				getHierarchicalMode()) {
			return new IllegalArgumentException("spectralSelectionStart must be 0 in lossless, differential. hierarchical mode");
		}

		if (ssStart == 0 &&
			(getFrameMode() == FrameMode.AC_LOSSLESS ||
			getFrameMode() == FrameMode.HUFF_LOSSLESS)) {
			return new IllegalArgumentException("spectralSelectionStart must not be 0 in non-lossless, differential, hierarchical mode");
		}

		return null;
	}

	/**
	 * Check that spectralSelectionEnd is within proper ranges, based on the
	 * provided spectralSelectionStart value
	 */
	private RuntimeException checkSsEndConditions(int ssStart, int ssEnd) {
		if (ssEnd < ssStart && getFrameMode() != null && getFrameMode().isProgressive()) {
			return new IllegalArgumentException("spectralSelectionEnd must be at least the same as spectralSelectionStart in progressive mode");
		}

		if (ssStart == 0 && ssEnd != 0 && getFrameMode() != null && getFrameMode().isProgressive()) {
			return new IllegalArgumentException("spectralSelectionEnd must be 0 when spectralSelectionStart is 0");
		}

		return null;
	}
}
