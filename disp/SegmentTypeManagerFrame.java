package disp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringEscapeUtils;

import core.SegmentType;
import core.SegmentTypeManager;
//!TODO
public class SegmentTypeManagerFrame extends JFrame {
	private static final long serialVersionUID = 2900985952790537512L;
	
	//specialized segment type for storing in JList, 
	//easier to synchronize list with type manager if definition name is held in object
	private static class SegmentTypeEntry {
		public String definedName;
		public SegmentType st;
		
		public SegmentTypeEntry(String definedName, SegmentType in) {
			st = in;
			this.definedName = definedName;
		}
		
		@Override
		public String toString() {
			Color c = st.color();
			return "<html>"
					+ "<div style='display:inline;height:13px;background-color:rgb("
						+ c.getRed()+","+c.getGreen()+","+c.getBlue()+");'>" 
					+ StringEscapeUtils.escapeHtml4(st.name())
					+ "</div>"
					+ "</html>";
		}
	}
	
	
	
	private SegmentTypeManager stm;
	private JList<SegmentTypeEntry> types;
	private DefaultListModel<SegmentTypeEntry> model;
	
	public SegmentTypeManagerFrame(SegmentTypeManager stm) {
		this.stm = stm;
		
		model = new DefaultListModel<SegmentTypeEntry>();
		types = new JList<SegmentTypeEntry>(model);
		
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		
		pane.add(types, BorderLayout.CENTER);
		pane.add(createControls(), BorderLayout.SOUTH);
	}
	
	public List<String> segmentOrder() {
		synchronize();
		List<String> l = new ArrayList<String>();
		for(int i = 0; i < model.size(); i++) {
			l.add(model.get(i).definedName);
		}
		return l;
	}
	
	private JPanel createControls() {
		return new JPanel() {
			private static final long serialVersionUID = 8549105982513433473L;

			private JButton define;
			private JButton undefine;
			private JButton redefine;
			private JButton pickColor;
			private JColorChooser color;
			private Color selectedColor;
			
			private JTextField name;
			private JFrame colorChooser;
			
			private void setSelectedColor(Color sel) {
				selectedColor = sel;
				pickColor.setText("<html><div style='background-color:rgb("+sel.getRed()+","+sel.getGreen()+","+sel.getBlue()+")'>Color</div></html>");
			}
			
			{	
				types.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						SegmentTypeEntry ste = types.getSelectedValue();
						if(ste != null) {
							name.setText(ste.st.name());
							setSelectedColor(ste.st.color());
						}
					}
				});
				
				define = new JButton("Define");
				undefine = new JButton("Undefine");
				redefine = new JButton("Redefine");
				pickColor = new JButton("Color");
				
				define.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						stm.define(String.valueOf((name.getName()+stm.types().size()).hashCode()), 
								new SegmentType(name.getText(), selectedColor));
						stm.update();
						SegmentTypeManagerFrame.this.repaint();
					}
				});
				
				undefine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						List<SegmentTypeEntry> sel = types.getSelectedValuesList();
						if(sel.size() == 0)
							JOptionPane.showMessageDialog(SegmentTypeManagerFrame.this, "Nothing to undefine.");
						else {
							for(SegmentTypeEntry s : sel) {
								stm.undefine(s.definedName);
							}
							stm.update();
							SegmentTypeManagerFrame.this.repaint();
						}
					}
				});
				
				redefine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SegmentTypeEntry sel = types.getSelectedValue();
						if(sel != null) {
							SegmentType ref = stm.get(sel.definedName);
							ref.color(selectedColor);
							ref.name(name.getText());
							SegmentTypeManagerFrame.this.repaint();
							stm.update();
						}
						else {
							JOptionPane.showMessageDialog(SegmentTypeManagerFrame.this, "Nothing to redefine.");
						}
					}
				});
				
				setSelectedColor(SegmentType.DEFAULT.color());
				
				pickColor.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(!colorChooser.isVisible()) {
							colorChooser.setVisible(true);
						}
					}
				});
				
				name = new JTextField();
				
				//color picker frame
				{
					colorChooser = new JFrame("Color Picker");
					colorChooser.setSize(new Dimension(450, 450));
					colorChooser.setResizable(false);
					
					JButton ok = new JButton("OK");
					ok.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							setSelectedColor(color.getColor());
							colorChooser.setVisible(false);
						}
					});
					
					JButton cancel = new JButton("Cancel");
					cancel.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							colorChooser.setVisible(false);
						}
					});
					
					JPanel okcancel = new JPanel();
					okcancel.setLayout(new FlowLayout());
					okcancel.add(ok);
					okcancel.add(cancel);
					
					Container pane = colorChooser.getContentPane();
					pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
					pane.add(color = new JColorChooser(), Component.CENTER_ALIGNMENT);	
					pane.add(okcancel, Component.CENTER_ALIGNMENT);
				}
				
				setLayout(new GridBagLayout());
				
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridwidth = 1;
				c.gridheight = 1;
				
				c.gridx=0;
				c.gridy=0;
				add(define, c);
				
				c.gridx=1;
				add(undefine,c);
				
				c.gridx=2;
				add(redefine, c);
				
				
				c.gridx=0;
				c.gridy=1;
				add(pickColor, c);
				
				c.gridwidth=2;
				c.gridx=1;
				add(name, c);
			}
		};
	}

	private void synchronize() {
		Set<String> defined = stm.types();
		Set<String> listed = new HashSet<String>();
		
		for(int i = 0; i < model.size(); i++) {
			SegmentTypeEntry st = model.get(i);
			
			//? not defined -> delete
			if(!defined.contains(st.definedName)) {
				model.remove(i);
				i--;
			}
			else {
				listed.add(st.definedName);
			}
		}
		
		for(String def : defined) {
			//not listed -> crate
			if(!listed.contains(def)) {
				model.add(model.size(), new SegmentTypeEntry(def, stm.get(def)));
			}
		}
	}
	
	@Override
	public void paint(Graphics g) {
		synchronize();
		super.paint(g);
	}
}
