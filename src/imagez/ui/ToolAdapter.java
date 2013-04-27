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
package imagez.ui;

import imagez.image.ZImage;
import imagez.tool.ZTool;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Manages drawing tool events for each drawing surface.
 * 
 * FIXME: tthis only needs to go onto the imageview?
 * @author notzed
 */
public class ToolAdapter implements MouseMotionListener, MouseListener {

	ImageView imageView;
	ZTool tool;

	public ToolAdapter(ImageView imageView) {
		this.imageView = imageView;
		imageView.addMouseListener(this);
		// if tool.wantsmotionevents? or do they all?
		imageView.addMouseMotionListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		ZImage image = imageView.getImage();
//		tool = ToolModel.getInstance().getTool();
		tool = imageView.getTool();
		if (image != null && tool != null) {
//			ToolModel.getInstance().setImageView(imageView);
//			tool.setImageView(imageView);
			//	imageView.addMouseMotionListener(this);
			tool.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (tool != null) {
			tool.mouseReleased(e);
			//	imageView.removeMouseMotionListener(this);
			tool = null;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (tool != null) {
			tool.mouseDragged(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (tool != null) {
			System.out.println("mouse moved during drag ignored");
			return;
		}
		
		ZImage image = imageView.getImage();
		ZTool ltool = imageView.getTool();

	//	ZTool tool = ToolModel.getInstance().getTool();
		if (image != null && ltool != null) {
	//		ToolModel.getInstance().setImageView(imageView);
	//		tool.setImageView(imageView);
			//imageView.addMouseMotionListener(this);
			ltool.mouseMoved(e);
		}
	}
}
