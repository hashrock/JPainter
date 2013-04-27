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
package imagez.tool.ui;

import imagez.blend.BlendMode;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import imagez.tool.*;
import imagez.ui.ImageWindow;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class BrushIcon implements Icon {

	PaintTool tool;

	public BrushIcon(PaintTool tool) {
		this.tool = tool;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D gg = (Graphics2D) g.create();

		gg.translate(x + 16, y + 16);
		gg.setPaint(tool.getPaint());
		gg.fill(tool.getBrush());
		gg.dispose();
	}

	@Override
	public int getIconWidth() {
		return 32;
	}

	@Override
	public int getIconHeight() {
		return 32;
	}
}

class PenIcon implements Icon {

	PaintTool tool;

	public PenIcon(PaintTool tool) {
		this.tool = tool;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D gg = (Graphics2D) g.create();

		gg.translate(x, y);
		gg.setPaint(tool.getSource().getForegroundColour());
		gg.fillRect(0, 0, 24, 24);
		gg.dispose();
	}

	@Override
	public int getIconWidth() {
		return 24;
	}

	@Override
	public int getIconHeight() {
		return 24;
	}
}

class XMenuItem extends JMenuItem {

	public XMenuItem(Icon c) {
		super(c);
		setMargin(new Insets(0, 0, 0, 0));
		setBorder(null);
		setBorderPainted(false);
	}
	/*
	@Override
	public Dimension getPreferredSize() {
	return new Dimension(16, 16);
	}
	
	
	@Override
	public Dimension getMaximumSize() {
	return new Dimension(16, 16);
	}
	 * 
	 */
}

/**
 *
 * @author notzed
 */
public class PaintOptions extends javax.swing.JPanel {

	/** Creates new form PaintOptions */
	public PaintOptions() {
		initComponents();
		fadeOptions.setVisible(fadeOutCheckBox.isSelected());
	}
	public PaintTool tool;
	JColorChooser jcc;
	JInternalFrame jccWin;
	
	ComboBoxModel getModeModel() {
		return new DefaultComboBoxModel(BlendMode.getBlendModes());
	}

	public PaintOptions(PaintTool t) {
		this();
		this.tool = t;

		modeComboBox.setSelectedItem(tool.getMode());

		jButton1.setIcon(new BrushIcon(tool));
//		jButton1.setMargin(new Insets(0, 0, 0, 0));
		jButton1.setBorder(null);
		//jButton2.setIcon(new PenIcon(tool));

		//	JMenuBar mb = new JMenuBar();
		//	jPanel2.add(mb);

		//	JMenu menu;
		//	mb.add(menu = new JMenu());
		//	mb.setBackground(Color.red);
		//	menu.setBackground(Color.blue);
		//	menu.setBorder(null);
		//	menu.setBackground(null);
		//	menu.setIcon(new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY)));

		//menu.add(new XMenuItem(new PenIcon()));

		//menu.add(jcc = new JColorChooser());
		jcc = new JColorChooser();
		jcc.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		jcc.setOpaque(true);
		Dimension d = jcc.getPreferredSize();

		//jcc.setBounds(208, 8, d.width, d.height);
		final ImageWindow win = (ImageWindow) t.getSource().getTopLevelAncestor();
		jccWin = new JInternalFrame("Colour", false, true, false, true);
		jccWin.setLocation(158, 8);
		jccWin.add(jcc);
		jccWin.pack();
		
		jButton2.setAction(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JButton b = (JButton) e.getSource();

				win.setOverlay(jccWin);
				jccWin.setVisible(true);
				try {
					jccWin.setSelected(true);
				} catch (PropertyVetoException ex) {
					Logger.getLogger(PaintOptions.class.getName()).log(Level.SEVERE, null, ex);
				}
				jccWin.grabFocus();
//				win.setOverlay(jcc);
			}
		});
		jButton2.setIcon(new PenIcon(tool));
		jButton2.setBorder(null);

//		jPanel2.add(b);

		jcc.getSelectionModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				tool.getSource().setForegroundColour(jcc.getColor());
				jButton1.repaint();
				jButton2.repaint();
				System.out.println("colour changed");
			}
		});

		opacitySpinner.setValue(t.getOpacity());

		t.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(PaintTool.PROP_OPACITY)) {
					opacitySpinner.setValue((Float) evt.getNewValue());
				} else if (evt.getPropertyName().equals(PaintTool.PROP_MODE)) {
					System.out.println("set combobox mode");
					modeComboBox.setSelectedItem(tool.getMode());
				}
			}
		});

		opacitySpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("opacity changed?");
				tool.setOpacity((Float) opacitySpinner.getValue());
			}
		});

		modeComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					System.out.println(" new mode selected " + e.getItem());
					tool.setMode((BlendMode) e.getItem());
				}
			}
		});
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        modeComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        opacitySpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        fadeOutCheckBox = new javax.swing.JCheckBox();
        fadeOptions = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();

        setFont(new java.awt.Font("DejaVu Sans Mono", 0, 10)); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel1.getFont().getSize()-2));
        jLabel1.setText("Mode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel1, gridBagConstraints);

        modeComboBox.setFont(modeComboBox.getFont().deriveFont(modeComboBox.getFont().getStyle() & ~java.awt.Font.BOLD, modeComboBox.getFont().getSize()-2));
        modeComboBox.setMaximumRowCount(24);
        modeComboBox.setModel(getModeModel());
        modeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(modeComboBox, gridBagConstraints);

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel2.getFont().getSize()-2));
        jLabel2.setText("Opacity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel2, gridBagConstraints);

        opacitySpinner.setFont(opacitySpinner.getFont().deriveFont(opacitySpinner.getFont().getStyle() & ~java.awt.Font.BOLD, opacitySpinner.getFont().getSize()-2));
        opacitySpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.01f)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(opacitySpinner, gridBagConstraints);

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel3.getFont().getSize()-2));
        jLabel3.setText("Brush");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel3, gridBagConstraints);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jButton1.setFont(jButton1.getFont().deriveFont(jButton1.getFont().getStyle() & ~java.awt.Font.BOLD, jButton1.getFont().getSize()-2));
        jButton1.setIcon(new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY)));
        jButton1.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jButton2.setFont(jButton2.getFont().deriveFont(jButton2.getFont().getStyle() & ~java.awt.Font.BOLD, jButton2.getFont().getSize()-2));
        jButton2.setIcon(new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_GRAY)));
        jButton2.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel2, gridBagConstraints);

        fadeOutCheckBox.setFont(fadeOutCheckBox.getFont().deriveFont(fadeOutCheckBox.getFont().getStyle() & ~java.awt.Font.BOLD, fadeOutCheckBox.getFont().getSize()-2));
        fadeOutCheckBox.setText("Fade Out");
        fadeOutCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fadeOutCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(fadeOutCheckBox, gridBagConstraints);

        fadeOptions.setLayout(new javax.swing.BoxLayout(fadeOptions, javax.swing.BoxLayout.LINE_AXIS));

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getStyle() & ~java.awt.Font.BOLD, jLabel5.getFont().getSize()-2));
        jLabel5.setText("Length");
        fadeOptions.add(jLabel5);

        jSpinner2.setFont(jSpinner2.getFont().deriveFont(jSpinner2.getFont().getStyle() & ~java.awt.Font.BOLD, jSpinner2.getFont().getSize()-2));
        fadeOptions.add(jSpinner2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(fadeOptions, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_jButton1ActionPerformed

	private void fadeOutCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fadeOutCheckBoxActionPerformed
		fadeOptions.setVisible(fadeOutCheckBox.isSelected());
	}//GEN-LAST:event_fadeOutCheckBoxActionPerformed

	private void modeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeComboBoxActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_modeComboBoxActionPerformed

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel fadeOptions;
    private javax.swing.JCheckBox fadeOutCheckBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JComboBox modeComboBox;
    private javax.swing.JSpinner opacitySpinner;
    // End of variables declaration//GEN-END:variables
}
