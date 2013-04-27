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
package imagez.brush;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;

/**
 *
 * @author notzed
 */
public class BrushListModel extends AbstractListModel {

	ArrayList<BrushContext> brushes = new ArrayList<BrushContext>();

	@Override
	public int getSize() {
		return brushes.size();
	}

	@Override
	public Object getElementAt(int index) {
		return brushes.get(index);
	}

	public void addBrush(BrushContext bc) {
		int bi = brushes.size();
		brushes.add(bc);

		fireIntervalAdded(this, bi, bi);
	}

	public void initBrushes() {
		for (int i = 1; i < 10; i++) {
			BrushContext bc = new BrushContext();
			float r = i / 2.0f;

			bc.setRadiusX(r);
			bc.setRadiusY(r);
			bc.setDraw(BrushDraw.Solid);
			bc.setFill(BrushFill.Solid);
			bc.setShape(BrushShape.Ellipse);
			addBrush(bc);
		}
		for (int i = 1; i < 10; i++) {
			BrushContext bc = new BrushContext();
			float r = i / 2.0f;

			bc.setRadiusX(r);
			bc.setRadiusY(r);
			bc.setDraw(BrushDraw.Solid);
			bc.setFill(BrushFill.Radial);
			bc.setShape(BrushShape.Ellipse);
			addBrush(bc);
		}
	}

	public void loadBrush(File b) throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(b));
			BrushContext bc = (BrushContext) ois.readObject();

		} catch (ClassNotFoundException ex) {
			Logger.getLogger(BrushListModel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
