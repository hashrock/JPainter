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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * A complete mess of code ...
 *
 * I couldn't find a way to load images asynchronously and cancel them properly if they're taking too long - ImageReader.abort() just
 * gets ignored/takes a very long time.
 *
 * So instead I just leave the thread running and noop once it's done.
 *
 * Given the above it's probably just as simple to use a simple iconimage and load the image without using all the low-level stuff.
 * Only issue is you don't get the meta-data until later on.
 */

class AsyncImageLoader implements Runnable, IIOReadProgressListener {

	File image;
	Thread worker;
	ImageReader reader;
	JLabel thumb;
	ImageInputStream iis;
	boolean cancelled;
	private final JLabel info;

	public AsyncImageLoader(File image, JLabel thumb, JLabel info) {
		this.image = image;
		this.thumb = thumb;
		this.info = info;

		worker = new Thread(this);
	}

	public void execute() {
		worker.start();
	}

	public void abort() {
		//try {
				cancelled = true;
			if (worker.isAlive()) {
				System.out.println("aborting thread");
				worker.interrupt();
				//worker.join();
				System.out.println(" .. done");
			}
		//} catch (InterruptedException ex) {
		//	Logger.getLogger(AsyncImageLoader.class.getName()).log(Level.SEVERE, null, ex);
		//}
	}

	@Override
	public void run() {
		try {
			thumb.setText("working");
			thumb.setIcon(null);
			info.setText(null);
			iis = ImageIO.createImageInputStream(image);
			Iterator<ImageReader> ir = ImageIO.getImageReaders(iis);
			if (!ir.hasNext()) {
				thumb.setText("not image/unknown format");
				System.out.println("Unable to read image");
				return;
			}
			reader = ir.next();
			System.out.println("Found image reader " + reader + " format " + reader.getFormatName());
			reader.setInput(iis);

			if (false && reader.readerSupportsThumbnails()) {
				System.out.println("reader supports thumbnails, checking ...");
				if (reader.hasThumbnails(0)) {
					System.out.println("image 0 has " + reader.getNumThumbnails(0) + " thumbnail(s)");
					//icon.setIcon(new ImageIcon(r.readThumbnail(0, 0)));
					//done = true;
				} else {
					System.out.println("no thumbnail");
				}
			}

			IIOMetadata meta = reader.getImageMetadata(0);
			//for (String s: meta.getMetadataFormatNames()) {
			//	System.out.println("metadata format " + s);
			//}
			Node n = meta.getAsTree("javax_imageio_1.0");

			StringBuilder sb = new StringBuilder("<html>");
			sb.append("<b>Size</b> ");
			sb.append(reader.getWidth(0));
			sb.append(" x ");
			sb.append(reader.getHeight(0));
			sb.append("<br>");

			NodeList nl = n.getChildNodes();
			int c = nl.getLength();
			for (int i =0;i<c;i++) {
				Node x = nl.item(i);
				String name = x.getNodeName();
				System.out.println(x.getNodeName());
				if (name.equals("Chroma")) {
					//System.out.println("scanning chroma node");
					NodeList cl = x.getChildNodes();
					int cc = cl.getLength();
					for (int j=0;j<cc;j++) {
						Node y = cl.item(j);
						String nn = y.getNodeName();

						if (nn.equals("ColorSpaceType")) {
							sb.append("<b>Colour Space</b> ");
							sb.append(y.getAttributes().getNamedItem("name").getNodeValue());
							sb.append("<br>");
						} else if (nn.equals("NumChannels")) {
							sb.append("<b>Channels</b> ");
							sb.append(y.getAttributes().getNamedItem("value").getNodeValue());
							sb.append("<br>");
						}
						//System.out.println(" " + y.getNodeName());
					}
				} else if (name.equals("Text")) {
					NodeList cl = x.getChildNodes();
					int cc = cl.getLength();
					for (int j=0;j<cc;j++) {
						Node y = cl.item(j);
						String nn = y.getNodeName();

						if (nn.equals("TextEntry")) {
							Node what = y.getAttributes().getNamedItem("keyword");
							String w = what == null ? null : what.getNodeValue();
							sb.append("<b>");
							sb.append(w == null ? "Comment" : w);
							sb.append("</b> ");
							sb.append(y.getAttributes().getNamedItem("value").getNodeValue());
							sb.append("<br>");
						}
					}
				}
			}

			info.setText(sb.toString());

			reader.addIIOReadProgressListener(this);

			ImageReadParam rp = reader.getDefaultReadParam();
			if (rp.canSetSourceRenderSize()) {
				rp.setSourceRenderSize(new Dimension(256, 256));
			}
			BufferedImage bi = reader.read(0, rp);
			if (cancelled)
				return;
			//			icon.setIcon(new ImageIcon(bi));
			Image ii = bi;
			if (bi.getWidth() > 256 || bi.getHeight() > 256) {
				if (bi.getWidth() > bi.getHeight()) {
					ii = bi.getScaledInstance(256, -1, Image.SCALE_FAST);
				} else {
					ii = bi.getScaledInstance(-1, 256, Image.SCALE_FAST);
				}
			}
			if (cancelled)
				return;
			thumb.setIcon(new ImageIcon(ii));
			thumb.setText(null);
		} catch (IOException x) {
			System.out.println("ex reading image" + x);
		}
	}

	@Override
	public void sequenceStarted(ImageReader source, int minIndex) {
	}

	@Override
	public void sequenceComplete(ImageReader source) {
	}

	@Override
	public void imageStarted(ImageReader source, int imageIndex) {
	}

	@Override
	public void imageProgress(ImageReader source, float percentageDone) {
		if (Thread.interrupted()) {
			System.out.println("aborting reader");
			source.abort();
		}
		//System.out.println("image progress" + percentageDone);
	}

	@Override
	public void imageComplete(ImageReader source) {
	}

	@Override
	public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
	}

	@Override
	public void thumbnailProgress(ImageReader source, float percentageDone) {
	}

	@Override
	public void thumbnailComplete(ImageReader source) {
	}

	@Override
	public void readAborted(ImageReader source) {
	}
}

/**
 * Image loading file chooser.
 *
 * Shows an image preview.
 *
 * Will eventually include image loading options - like memory format.
 * @author notzed
 */
public class ImageFileChooser extends JFileChooser {

	class ImageInfo extends JPanel implements PropertyChangeListener {

		File file;
		JLabel icon;
		JLabel info;
		ImageIcon iicon;

		public ImageInfo(JFileChooser fc) {
			super(new BorderLayout());
			//setPreferredSize(new Dimension(256, 256));
			fc.addPropertyChangeListener(this);

			setBorder(new EmptyBorder(0, 8, 0, 0));

			iicon = new ImageIcon();
			icon = new JLabel(iicon);
			//icon.setHorizontalAlignment(icon.CENTER);
			//icon.setVerticalAlignment(icon.CENTER);
			icon.setPreferredSize(new Dimension(256, 256));
			icon.setMinimumSize(new Dimension(256, 256));
			icon.setMaximumSize(new Dimension(256, 256));
			icon.setHorizontalTextPosition(SwingConstants.CENTER);
			icon.setVerticalTextPosition(SwingConstants.CENTER);
			add(icon, BorderLayout.CENTER);

			info = new JLabel();
			JScrollPane sp = new JScrollPane(info);
			//sp.setPreferredSize(new Dimension(256, 180));
			//sp.setMaximumSize(new Dimension(256, -1));
			info.setPreferredSize(new Dimension(256, 180));
			info.setMaximumSize(new Dimension(256, -1));
			info.setVerticalAlignment(SwingConstants.TOP);
			add(sp, BorderLayout.SOUTH);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();

			if (name.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
				file = (File) evt.getNewValue();
				update();
			}
		}
		SwingWorker loadIcon;
		AsyncImageLoader ail;

		void update() {
			if (file == null) {
				icon.setIcon(null);
			} else {
				if (true) {
					if (ail != null)
						ail.abort();
					ail = new AsyncImageLoader(file, icon, info);
					ail.execute();
					return;
				} else {
				}

				try {
					boolean done = false;
					ImageInputStream iis = ImageIO.createImageInputStream(file);
					Iterator<ImageReader> ir = ImageIO.getImageReaders(iis);
					if (!ir.hasNext()) {
						System.out.println("Unable to read image");
						return;
					}
					ImageReader r = ir.next();
					System.out.println("Found image reader " + r + " format " + r.getFormatName());
					r.setInput(iis);
					System.out.println("  tiled? " + r.isImageTiled(0));
					if (r.readerSupportsThumbnails()) {
						System.out.println("reader supports thumbnails, checking ...");
						if (r.hasThumbnails(0)) {
							System.out.println("image 0 has " + r.getNumThumbnails(0) + " thumbnail(s)");
							icon.setIcon(new ImageIcon(r.readThumbnail(0, 0)));
							done = true;
						} else {
							System.out.println("no thumbnail");
						}
					}
					r.addIIOReadProgressListener(new IIOReadProgressListener() {

						@Override
						public void sequenceStarted(ImageReader source, int minIndex) {
						}

						@Override
						public void sequenceComplete(ImageReader source) {
						}

						@Override
						public void imageStarted(ImageReader source, int imageIndex) {
						}

						@Override
						public void imageProgress(ImageReader source, float percentageDone) {
							System.out.println("image progress" + percentageDone);
						}

						@Override
						public void imageComplete(ImageReader source) {
						}

						@Override
						public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
						}

						@Override
						public void thumbnailProgress(ImageReader source, float percentageDone) {
						}

						@Override
						public void thumbnailComplete(ImageReader source) {
						}

						@Override
						public void readAborted(ImageReader source) {
						}
					});
					if (!done) {
						ImageReadParam rp = r.getDefaultReadParam();
						if (rp.canSetSourceRenderSize()) {
							rp.setSourceRenderSize(new Dimension(256, 256));
						}
						BufferedImage bi = r.read(0, rp);
						icon.setIcon(new ImageIcon(bi));
						done = true;
					}
				} catch (IOException x) {
				}
				if (true) {
					return;
				}
				//if (loadIcon != null) {
				//	loadIcon.cancel(true);
				//}
				loadIcon = new SwingWorker() {

					@Override
					protected Object doInBackground() throws Exception {

						Image ii = ImageIO.read(file);
						if (ii != null) {
							if (ii.getWidth(null) > 256 || ii.getHeight(null) > 256) {
								if (ii.getWidth(null) > ii.getHeight(null)) {
									ii = ii.getScaledInstance(256, -1, Image.SCALE_FAST);
								} else {
									ii = ii.getScaledInstance(-1, 256, Image.SCALE_FAST);
								}
							}
						}
						publish(ii);
						return ii;
					}

					@Override
					protected void process(List chunks) {
						Image ii = (Image) chunks.get(0);

						if (ii != null) {
							icon.setIcon(new ImageIcon(ii));
						} else {
							icon.setIcon(null);
						}
						loadIcon = null;
					}
				};
				loadIcon.execute();
			}
		}
	}

	public ImageFileChooser() {
		super();

		setAccessory(new ImageInfo(this));

		// HACK: change mode to detail mode to make it nicer to use
		try {
			JPanel rootPanel = (JPanel) getComponent(0);
			JPanel buttonPanel = (JPanel) rootPanel.getComponent(0);
			int len = buttonPanel.getComponentCount();
			int seen = 0;

			for (int i = 0; i < len; i++) {
				Component c = buttonPanel.getComponent(i);

				if (c instanceof JToggleButton) {
					seen++;
					if (seen == 2) {
						//((JToggleButton)c).setSelected(true);
						((JToggleButton) c).doClick();
					}
				}
			}
		} catch (Exception x) {
			System.out.println("filechooser hacks are dangerous: " + x.getMessage());
		}
		setPreferredSize(new Dimension(500 + 256, 600));
	}
}
