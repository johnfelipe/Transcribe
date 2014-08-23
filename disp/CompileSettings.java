package disp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import compile.format.DefaultFormat;
import compile.Collapse;
import compile.Compiler;
import compile.IntervalMerger;
import compile.RemoveEmpty;
import compile.RemoveSymbols;
import core.Audio;
import core.Segment;
import disp.compile.MergeTemplate;

public class CompileSettings extends JFrame {
	private static final long serialVersionUID = -1113296162504003194L;
	
	private static final Font WRITE_FORMAT_FONT = new Font("Consolas", 13, 13);
	
	//TODO add remaining settings, test.
	private Audio aud;
	
	private DefaultListModel<MergeTemplate> mtListModel;
	private JList<MergeTemplate> mergeTemplates;
	
	//[$Time] $Type : $Text for example.
	private JTextField writeFormat;
	private JCheckBox removeEmptySegments;
	
	
	//for serialization purposes.//
	public DefaultListModel<MergeTemplate> mtListModel() {
		return mtListModel;
	}
	
	public String writeFormat() {
		return writeFormat.getText();
	}
	
	public void writeFormat(String fmt) {
		writeFormat.setText(fmt);
	}
	
	public boolean removeEmptySegments() {
		return removeEmptySegments.isSelected();
	}
	
	public void removeEmptySegments(boolean value) {
		removeEmptySegments.setSelected(value);
	}
	//.//
	
	
	
	@SuppressWarnings("serial")
	public CompileSettings(Audio aud) { 
		this.aud = aud;
		
		setTitle("Compiler Settings");
		
		removeEmptySegments = new JCheckBox("Remove Empty Segments");
		
		mtListModel = new DefaultListModel<MergeTemplate>();
		mergeTemplates = new JList<MergeTemplate>();
		mergeTemplates.setModel(mtListModel);
		
		writeFormat = new JTextField();
		writeFormat.setText(DefaultFormat.DEFAULT_FMT_STRING);
		writeFormat.setFont(WRITE_FORMAT_FONT);
		
		final JButton compile = new JButton("Compile");
		compile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performCompile();
			}
		});
		
		final JButton addMerger = new JButton("Add Segment Merger");
		final JButton removeMerger = new JButton("Delete");
		final JButton editMerger = new JButton("Edit");
		final JButton sort = new JButton("Sort");
		final JSpinner from = new JSpinner();
		final JSpinner to = new JSpinner();
		final JCheckBox alphaNumEndsOnly = new JCheckBox("A# Ends Only");
		final JCheckBox removeAlphaNumEnds = new JCheckBox("Remove Non-A# Ends");
		final JTextField mergeText = new JTextField();
		
		from.setValue(0);
		to.setValue(3000);
		
		sort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sortMergers();
			}
		});
		
		mergeTemplates.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				MergeTemplate mt = mergeTemplates.getSelectedValue();
				if(mt != null) {
					alphaNumEndsOnly.setSelected(mt.alphaNumEndsOnly);
					removeAlphaNumEnds.setSelected(mt.removeNonAlphanumAtJoin);
					from.setValue((int)mt.minDist);
					to.setValue((int)mt.maxDist);
					mergeText.setText(mt.mergeText);
				}
			}
		});
		
		addMerger.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MergeTemplate mt = new MergeTemplate();
				mt.alphaNumEndsOnly = alphaNumEndsOnly.isSelected();
				mt.removeNonAlphanumAtJoin = removeAlphaNumEnds.isSelected();
				mt.minDist = (Integer)from.getValue();
				mt.maxDist = (Integer)to.getValue();
				mt.mergeText = mergeText.getText();
				mtListModel.add(mtListModel.size(), mt);
				mergeTemplates.repaint();
			}
		});
		
		removeMerger.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = mergeTemplates.getSelectedIndex();
				if(i >= 0) {
					mtListModel.remove(i);
				}
				mergeTemplates.repaint();
			}
		});
		
		from.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if((Integer)from.getValue() < 0) {
					from.setValue(0);
				}
			}	
		});
		
		to.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if((Integer)to.getValue() < 0) {
					to.setValue(0);
				}
			}
		});
		
		
		Container pane = getContentPane();
		
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5,5,5,5));
		
		BorderLayout lo = new BorderLayout();
		lo.setVgap(10);
		p.setLayout(lo);
		
		p.add(new JPanel() {
			{
				setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				
				c.gridwidth = 1;
				c.gridheight = 1;
				
				c.weightx = 0.05;
				c.gridx = 0;
				c.gridy = 0;
				add(new JLabel("Format: "), c);
				
				c.weightx = 1.0;
				c.gridx = 1;
				add(writeFormat, c);
			}
		}, BorderLayout.NORTH);
		
		p.add(new JPanel() {
			{
				setLayout(new BorderLayout());
				add(mergeTemplates, BorderLayout.CENTER);
				add(new JPanel() {
					{
						setLayout(new GridBagLayout());
						final Insets insets = new Insets(0,0,4,4);
						final Insets zero = new Insets(0,0,0,0);
						
						GridBagConstraints c = new GridBagConstraints();
						c.fill = GridBagConstraints.HORIZONTAL;
						c.insets = zero;
						
						c.gridwidth = 1;
						c.gridheight = 1;
						
						c.weightx = 1.0;
						c.weighty = 1.0;
						
						//first row (from -> to)
						{
							c.insets = new Insets(4,0,0,0);
							c.gridx = 0;
							c.gridy = 0;
							add(from, c);
							
							c.gridx = 1;
							add(to, c);
						}
						
						//second row (A#-ends options)
						{
							c.insets = zero;
							
							c.gridx = 0;
							c.gridy = 1;
							add(alphaNumEndsOnly, c);
							
							c.gridx = 1;
							add(removeAlphaNumEnds, c);
						}
						
						//third row
						{
							c.gridwidth = 2;
							c.gridx = 0;
							c.gridy = 2;
							c.insets = insets;
							
							add(mergeText, c);
						}
						
						//fourth row (Delete/Create)
						{
							c.weightx = 1.0;
							c.gridwidth = 1;
							c.gridx = 0;
							c.gridy = 3;
							
							add(removeMerger, c);
							
							c.gridx = 1;
							add(addMerger, c);
						}
						
						//fifth row (Sort/Edit)
						{
							c.gridwidth = 1;
							c.gridx = 0;
							c.gridy = 4;
							add(sort, c);
							
							c.gridx = 1;
							add(editMerger, c);
						}
						
						//sixth row (Remove Empty Segments)
						{
							c.gridwidth = 2;
							c.gridx = 0;
							c.gridy = 5;
							add(removeEmptySegments, c);
						}
					}
				}, BorderLayout.SOUTH);
			}
		}, BorderLayout.CENTER);
		
		p.add(compile, BorderLayout.SOUTH);
		
		pane.add(p);
		
		setResizable(false);
		pack();
	}
	
	private void sortMergers() {
		List<MergeTemplate> templates = new ArrayList<MergeTemplate>();
		for(int i = 0; i < mtListModel.size(); i++) {
			templates.add(mtListModel.get(i));
		}
		
		Collections.sort(templates, new Comparator<MergeTemplate>() {
			@Override
			public int compare(MergeTemplate m1, MergeTemplate m2) {
				if(m1.maxDist > m2.maxDist) {
					return 1;
				}
				else if(m1.maxDist == m2.maxDist) {
					return 0;
				}
				else {
					return -1;
				}
			}
		});
		
		mtListModel.clear();
		for(MergeTemplate mt : templates) {
			mtListModel.add(mtListModel.size(), mt);
		}
		
		mergeTemplates.repaint();
	}
	
	private Compiler buildCompiler() {
		DefaultFormat df = new DefaultFormat(aud.typeManager());
		df.formatString(writeFormat.getText());
		
		Compiler compiler = new Compiler(aud, df);
		
		if(removeEmptySegments.isSelected()) {
			compiler.addProcessor(new RemoveEmpty());
		}
		
		for(int i = 0; i < mtListModel.size(); i++) {
			compiler.addProcessor(mtListModel.get(i).createMerger());
		}
		
		compiler.addProcessor(new RemoveSymbols());
		
		return compiler;
		/*
		compiler.addProcessor(new IntervalMerger(0, 999, new IntervalMerger.MergeOperation() {
			@Override
			public Segment merge(Segment s1, Segment s2) {
				Segment m = s1.clone();
				String s1t = s1.transcript().trim();
				String s2t = " " + s2.transcript().trim();
				m.transcript(s1t+s2t);
				return m;
			}
		}));
		compiler.addProcessor(new IntervalMerger(1000, 2999, new IntervalMerger.MergeOperation() {
			@Override
			public Segment merge(Segment s1, Segment s2) {
				Segment m = s1.clone();
				String s1t = s1.transcript().trim();
				String s2t = s2.transcript().trim();
				if(s1t.matches("^.+?[a-zA-Z]+$") && s2t.matches("^[a-zA-Z]+.+?$")) {
					m.transcript(s1t+", "+s2t);
				}
				else {
					m.transcript(s1t + " " + s2t);
				}
				return m;
			}
		}));
		compiler.addProcessor(new IntervalMerger(3000, 9999, new IntervalMerger.MergeOperation() {
			@Override
			public Segment merge(Segment s1, Segment s2) {
				Segment m = s1.clone();
				
				String s1t = s1.transcript().trim();
				String s2t = s2.transcript().trim();
				
				while(s1t.matches("^.+?\\p{Punct}+$")) {
					s1t = s1t.substring(0, s1t.length()-1);
				}
				while(s2t.matches("^\\p{Punct}+.+?$")) {
					s2t = s2t.substring(1, s2t.length());
				}
				
				m.transcript(s1t + " . . . " + s2t);
				return m;
			}
		}));
		compiler.addProcessor(new IntervalMerger(10000, 20000, new IntervalMerger.MergeOperation() {
			@Override
			public Segment merge(Segment s1, Segment s2) {
				Segment m = s1.clone();
				String s1t = s1.transcript();
				String s2t = " [pause] " + s2.transcript().trim();
				m.transcript(s1t+s2t);
				return m;
			}
		}));
		*/
	}
	
	private File target() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Select export location");
		jfc.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));
		if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			return jfc.getSelectedFile();
		}
		else {
			return null;
		}
	}
	
	private void performCompile() {
		File targ = target();
		if(targ == null) {
			return;
		}
		
		Compiler compiler = buildCompiler();
		try {
			if(!targ.exists()) {
				targ.createNewFile();
			}
			else {
				//TODO warn about overwriting
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(targ));
			bw.write(compiler.compile());
			bw.close();
		} 
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
}
