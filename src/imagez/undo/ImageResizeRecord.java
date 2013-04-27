/*
 * Copyright (C) 2011 notzed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.undo;

import imagez.image.ZImage;
import java.awt.Dimension;

/**
 *
 * @author notzed
 */
public class ImageResizeRecord extends CompoundRecord {
	private final ZImage image;

	Dimension osize;
	Dimension nsize;

	public ImageResizeRecord(ZImage image, Dimension osize, Dimension nsize) {
		super();
		this.image = image;
		this.osize = osize;
		this.nsize = nsize;
	}

	@Override
	public void undo() {
		image.setSize(osize);
		super.undo();
	}

	@Override
	public void redo() {
		image.setSize(nsize);
		super.redo();
	}
}
