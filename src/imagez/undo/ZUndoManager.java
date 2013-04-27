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

import imagez.image.ZImage;
import imagez.image.ZLayer;
import java.awt.Rectangle;

/**
 * This seems like i'm entering a world of pain.  A world of pain.
 * @author notzed
 */
public class ZUndoManager {
	/*
	 * So the big question is, how to implement undo?
	 *
	 * 1. Drawing operations only occur on temporary layers and are only commited once they are complete.
	 * 2. Provides a very obvious checkpoint for undo granularity.
	 *
	 * That's that bit sorted.
	 *
	 * 3. Layer commit has a region, so that can be used as a guide for what information needs saving.
	 *
	 * 4. The compositor and the rest of the system wants to get busy on the data, so it has
	 *    to be copied immediately or at least as part of the pipeline.
	 * 5. Immediate is easier.  The pipeline is running on the graphics context at the moment (bad) but
	 *    wont be forever.  Now and in the future it may not run synchronous with the undo mechanism - then
	 *    again that would make the screen unusuable ... so perhaps it is reasonable to force it always
	 *    to run synchronously.
	 *
	 * 6. Could the compositor do pixel-level diffing?  Yes.  But might be unwise due to added complexity.
	 * 6a.Then again, it is only the tool layer compositor that needs to have it and it only exists for
	 *    this ask.
	 *
	 *
	 * o It might make sense to have the undo manager be the main driver for the editing process.
	 *
	 *   An image edit could be the following steps:
	 * o create a tool layer to generate the editing data
	 * o tool draws data
	 * o tool releases the tool layer with commit
	 * o an undo record is created of the target layer data
	 * o a redo record is created representing the tool layer application
	 *   - data + mode
	 *   - should mask be included or just as part of the current state maintained by undo records?
	 * o redo is invoked on the redo record to apply the change
	 *
	 * ! Could possibly be used as a basis of a macro facility too, which simply doesn't store the undo information.
	 */
	UndoRecord tail;
	UndoRecord head;
	UndoRecord last;
	ZLayer layer;

	public void addLayerCreate(ZLayer src, int index) {
		done(new LayerCreateRecord(src, index));
	}
	public void addLayerDelete(ZLayer src, int index) {
		done(new LayerDeleteRecord(src, index));
	}
	
	public void addLayerChange(ZLayer src, Rectangle bounds, ZLayer delta) {
		done(new LayerChangeRecord(src, bounds, delta));
	}

	public void addLayerTranslate(ZLayer src, int dx, int dy) {
		done(new LayerTranslateRecord(src, dx, dy));
	}

	public void addLayerReplace(ZLayer src, ZLayer replaced) {
		done(new LayerReplaceRecord(src, replaced));
	}

	public void addLayerReorder(ZImage src, int oindex, int nindex) {
		done(new LayerReorderRecord(src, oindex, nindex));
	}
	
	public void addImageReplace(ZImage src, ZImage replaced) {
		done(new ImageReplaceRecord(src, replaced));
	}

	public void addCompound(CompoundRecord cr) {
		done(cr);
	}

	void done(UndoRecord rec) {
		rec.prev = last;
		if (last != null) {
			last.next = rec;
		} else {
			head = rec;
		}
		last = tail = rec;
		if (head == null) {
			head = rec;
		}
	}

	public void undo() {
		if (last != null) {
			last.undo();
			last = last.prev;
		}
	}

	public void redo() {
		if (last == null) {
			last = head;
			if (last != null) {
				last.redo();
			}
		} else if (last != null && last.next != null) {
			last = last.next;
			last.redo();
		}
	}

	public boolean canRedo() {
		return last != null && last.next != null;
	}

	public boolean canUndo() {
		return last != null;
	}

	/*
	 * Do I want some sort of save object from the layer itself?
	 * Might be handy for alternative buffer types/efficiency.
	 *
	 * Object save(Rectangle rect);
	 * void restore(Object saved, Rectangle rect);
	 *//*
	float[] copyRGBA(ZLayerRGBAFloat layer, Rectangle bounds) {
	//??
	float[] data = new float[bounds.width * bounds.height * 4];
	int dpos = 0;
	for (int y = 0; y < bounds.height; y++) {
	layer.getLine(data, dpos, bounds.x, bounds.y + y, bounds.width);
	dpos += bounds.width * 4;
	}
	return data;
	}
	 *
	 */
	/*
	ImageChangeRecord createImageChanged(Rectangle bounds) {
	float[] data = new float[bounds.width * bounds.height * 4];
	int dpos = 0;
	for (int y = 0; y < bounds.height; y++) {
	layer.getLine(data, dpos, bounds.x, bounds.y + y, bounds.width);
	dpos += bounds.width * 4;
	}

	return new ImageChangeRecord(data, bounds);
	}
	 */
}
