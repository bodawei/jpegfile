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
package bdw.format.jpeg.data;

import bdw.io.LimitExceeded;
import bdw.io.LimitingDataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Several segments just consist of a series of tables, where the
 * number of table is defined by the size of the segment. This class
 * wraps up the common reading functionality, along with common routines for
 * adding and removing tables.
 */
abstract public class TableSegment<E extends Component> extends MarkerSegment implements Iterable<E> {
	private static final int MAX_SEGMENT_SIZE = 65536;
	/**
	 * The list of table entries
	 */
	protected List<E> tables = new ArrayList<E>();

	/**
	 * Constructor.  What a surprise.
	 */
	public TableSegment(int markerId) {
		super(markerId);
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public int getSizeOnDisk() {
		int length = 0;

		for (E table : this) {
			length += table.getSizeOnDisk();
		}

		return super.getSizeOnDisk() + length;
	}

	/**
	 * @return the number of tables in this segment
	 */
	public int getTableCount() {
		return tables.size();
	}

	/**
	 * @param index The index of the table to retrieve
	 * @return The table at the index'th position
	 */
	public E getTable(int index) {
		return tables.get(index);
	}

	/**
	 * @param table The entry to be added at the end of the list of entries this
	 * manages.
	 */
	public void addTable(E table) {
		insertTable(tables.size(), table);
	}

	/**
	 * @param index The index to insert the specified table
	 * @param table The entry to be inserted at the index'th position
	 */
	public void insertTable(int index, E table) {
		if (index < 0 || index > getTableCount()) {
			throw new IndexOutOfBoundsException();
		}

		if (getSizeOnDisk() + table.getSizeOnDisk() > MAX_SEGMENT_SIZE) {
			throw new IllegalArgumentException("Too many entries. Can not add another");
		}

		table.setDataMode(this.getDataMode());
		table.setFrameMode(this.getFrameMode());

		tables.add(index, table);
	}

	@Override
	public Iterator<E> iterator() {
		return tables.iterator();
	}

	/**
	 * @inheritdoc
	 */
	@Override
	public List<Exception> validate() {
		List<Exception> results = super.validate();

		for (E table : tables) {
			results.addAll(table.validate());
		}

		return results;
	}

	/**
	 * Two DriSegments are equal if they have the same number of lines
	 * @param other The other object to test
	 * @return If other and this are equal
	 */
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}

		TableSegment castOther = (TableSegment) other;

		if (getTableCount() != castOther.getTableCount()) {
			return false;
		}

		for (int index = 0; index < getTableCount(); index++) {
			E thisTable = getTable(index);
			E otherTable = (E) castOther.getTable(index);
			if (!thisTable.equals(otherTable)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + (this.tables != null ? this.tables.hashCode() : 0);
		return hash;
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void readParameters(LimitingDataInput input) throws IOException {
		super.readParameters(input);

		tables = new ArrayList<E>();

		while (input.getRemainingLimit() != 0) {
			E table = createTable();

			try {
				input.mark(input.getRemainingLimit());
				table.readParameters(input);
				addTable(table);
			} catch (LimitExceeded e) {
				input.reset();
				return;
			}
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void writeParameters(DataOutputStream stream) throws IOException {
		super.writeParameters(stream);

		for (E table : tables) {
			table.write(stream);
		}
	}

	/**
	 * @inheritdoc
	 */
	@Override
	protected void	changeChildrenModes() {
		super.changeChildrenModes();
		for (E table: tables) {
			table.setDataMode(getDataMode());
			table.setFrameMode(getFrameMode());
		}
	}

	/**
	 * Subclasses must override this to create a new entry instance.
	 */
	abstract protected E createTable();
}
