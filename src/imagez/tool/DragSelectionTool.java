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
package imagez.tool;

import imagez.ui.RectangleModel;
import imagez.image.ZSelectionModel;
import imagez.ui.CornerOverlay.BLHandle;
import imagez.ui.CornerOverlay.BRHandle;
import imagez.ui.CornerOverlay.TLHandle;
import imagez.ui.CornerOverlay.TRHandle;
import imagez.ui.ImageView;
import imagez.ui.ToolOverlay;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

/**
 *
 * Currently testbed for user interaction.
 *
 * How am i going to do this???
 *
 * Do I add an empty path overlay and use piccolo events to get the basic input.
 * or do i rely on the normal tool mechanism?
 *
 * FIXME: Work out input event processing properly, tool overlay(s) and whatnot.
 * 
 * @author notzed
 */
abstract public class DragSelectionTool extends SelectionTool {

	LinkedList<ToolOverlay> handles = new LinkedList<ToolOverlay>();
	RectangleModel model = new RectangleModel();
	Point start;
	Point finish;

	public DragSelectionTool() {
		model.addPropertyChangeListener(modelListener);
	}

	@Override
	public void setImageView(ImageView source) {
		if (source != this.source) {
			if (this.source != null) {
				this.source.removeOverlays(handles);
			}
		}
		super.setImageView(source);
	}
	PropertyChangeListener modelListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updateShape();
		}
	};

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isConsumed()) {
			return;
		}

		start = getImagePixel(e);

		source.getCurrentSelection().mergeEdit();
		super.mousePressed(e);
		
		clearHandles();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (e.isConsumed()) {
			return;
		}
		finish = getImagePixel(e);
		
		Shape s = getWorkingSelection(start, finish);
		ZSelectionModel selection = source.getCurrentSelection();

		selection.updateSelection(mode, s, true);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isConsumed()) {
			return;
		}

		Point p = getImagePixel(e);

		finish = p;
		Shape s = getWorkingSelection(start, finish);
		ZSelectionModel selection = source.getCurrentSelection();

		selection.updateSelection(mode, s, true); //false);

		model.setRange(start.x, start.y, finish.x, finish.y);

		clearHandles();
		handles.add(new TLHandle(source, model));
		handles.add(new TRHandle(source, model));
		handles.add(new BLHandle(source, model));
		handles.add(new BRHandle(source, model));
		source.addOverlays(handles);
	}

	void clearHandles() {
		this.source.removeOverlays(handles);
		handles.clear();
	}

	@Override
	public void clearSelection() {
		clearHandles();
	}
	
	void updateShape() {
		if (handles.size() > 0) {
			Point st = new Point(model.getX1(), model.getY1());
			Point fi = new Point(model.getX2(), model.getY2());
			Shape s = getWorkingSelection(st, fi);

			ZSelectionModel selection = source.getCurrentSelection();
			selection.updateSelection(mode, s, true);
		}
	}

	abstract Shape getWorkingSelection(Point start, Point finish);
}
