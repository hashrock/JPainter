/*
 *  Copyright (c) 2011 Michael Zucchi
 *
 *  This file is part of ImageZ, a bitmap image editing appliction.
 *
 *  ImageZ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ImageZ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ImageZ.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.undo;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Base class for all undo records.
 * @author notzed
 */
public class CompoundRecord extends UndoRecord {

	LinkedList<UndoRecord> records = new LinkedList<UndoRecord>();

	/**
	 * Add a new undo item to the undo record.
	 * @param r
	 */
	public void addRecord(UndoRecord r) {
		records.add(r);
	}

	/**
	 * Re-do the compound action.
	 */
	public void redo() {
		for (UndoRecord r : records) {
			r.redo();
		}
	}

	/**
	 * Un-do the action in reverse.
	 */
	public void undo() {
		Iterator<UndoRecord> ir = records.descendingIterator();

		while (ir.hasNext()) {
			ir.next().undo();
		}
	}
}
