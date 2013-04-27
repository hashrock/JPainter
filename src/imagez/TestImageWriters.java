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
package imagez;

import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

/**
 *
 * @author notzed
 */
public class TestImageWriters {

	public static void main(String [] args) {
		ImageWriter iw;

		String [] fmts= ImageIO.getReaderFormatNames();

		System.out.println("Supported read formats");
		for (String f : fmts) {
			System.out.println(" " + f);
		}


		fmts= ImageIO.getWriterFormatNames();
		System.out.println("Supported write formats");
		for (String f : fmts) {
			System.out.println(" " + f);
		}

		System.out.println("what about tif?");
		Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("TIF");
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		Iterator<ImageReader> ir = ImageIO.getImageReadersByFormatName("tif");
		while (ir.hasNext()) {
			System.out.println(ir.next());
		}
		System.out.println("no tiff?");
	}
}
