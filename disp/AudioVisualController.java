package disp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import compile.IntervalMerger;
import core.Audio;
import core.AudioTools;
import core.Main;
import core.Segment;
import core.SegmentType;
import core.SegmentTypeManager;
import disp.SegmentTypeManagerFrame;
import disp.TranscriptPanel.TranscriptChangeListener;

public class AudioVisualController extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8126596226035874476L;
	private AudioVisual av;
	private AudioTools at;
	private ShadedAudioVisual sav;
	private TranscriptPanel transcriptPanel;
	private SegmentTypeManagerFrame typeManagerFrame;
	private JLabel selectedTypeLabel;
	
	//used by scroll
	private JSpinner scrollMultiplier;
	private JScrollBar moveBar;
	private double pStartVisu = -1;
	private double pEndVisu = -1;
	public CompileSettings settings;
	private IntervalOperationChooser ioc;
	
	//TODO state saving for undo / redo
	private String selectedSegmentType = "$default";
	
	
	public String selectedSegmentType() {
		return selectedSegmentType;
	}
	
	public void selectedSegmentType(String type) {
		selectedSegmentType = type;
		updateSelectedType();
	}
	
	public void updateSelectedType() {
		SegmentType type = av.getAudio().typeManager().get(selectedSegmentType);
		if(type == null) {
			selectedTypeLabel.setText("Using: (None)");
		}
		else {
			Color c = type.color();
			selectedTypeLabel.setText("<html>Using: <font style='background-color:rgb("
					+ c.getRed() + "," 
					+ c.getGreen() + "," 
					+ c.getBlue() + ");'>"+type.name()+"</font></html>");
		}
	}
	
	public void selectType(int idx) {
		List<String> types = typeManagerFrame.segmentOrder();
		if(idx >= types.size()) {
			return;
		}
		selectedSegmentType(types.get(idx));
	}
	
	private void saveAs() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileNameExtensionFilter("JSON State Export", "json"));
		if(jfc.showSaveDialog(AudioVisualController.this) == JFileChooser.APPROVE_OPTION) {
			performSaveTo(jfc.getSelectedFile());
		}
	}
	
	
	public AudioVisualController(AudioVisual avi, AudioTools ato) {
		this.av = avi;
		this.at = ato;
		this.ioc = new IntervalOperationChooser(avi.getAudio());
		this.ioc.setSize(300, 200);
		
		this.settings = new CompileSettings(avi.getAudio());
		this.settings.setSize(300, 400);
		
		//called on segment types update
		avi.getAudio().typeManager().updateCallback(new Runnable() {
			@Override
			public void run() {
				SegmentTypeManager stm = av.getAudio().typeManager();
				Set<String> types = stm.types();
				Set<Segment> segments = av.getAudio().segments();
				
				Iterator<Segment> iter = segments.iterator();
				while(iter.hasNext()) {
					Segment seg = iter.next();
					if(!types.contains(seg.segmentType())) {
						iter.remove();
					}
				}
				
				AudioVisualController.this.repaint();
				updateSelectedType();
			}
		});
		
		{
			typeManagerFrame = new SegmentTypeManagerFrame(av.getAudio().typeManager());
			typeManagerFrame.setTitle("Type Definitions");
			typeManagerFrame.setBounds(0,0,250,200);
			typeManagerFrame.setResizable(false);
		}
		
		final AudioVisualController avc = this;
		
		sav = new ShadedAudioVisual(av.getAudio());
		
		sav.setRangeChangeCallback(new Runnable() {
			public void run() { 
				av.visuStart(sav.getRangeStart());
				av.visuEnd(sav.getRangeEnd());
				av.repaint();

				avc.repaint();
			}
		});
		
		av.visuStart(sav.getRangeStart());
		av.visuEnd(sav.getRangeEnd());
		
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double dir = e.getPreciseWheelRotation();
				double vs = av.visuStart();
				double ve = av.visuEnd();
				double c = av.unmapX(e.getX());
				
				double ns = vs+(c-vs)*-dir/10.0;
				double ne = ve+(c-ve)*-dir/10.0;
				
				if(ns < 0)
					ns = 0;
				
				if(ne > av.getAudio().length())
					ne = av.getAudio().length();
				
				//av will be updated by this change
				sav.setRange(ns, ne);
			}
		});
		
		scrollMultiplier = new JSpinner();
		scrollMultiplier.getModel().setValue(1);
		scrollMultiplier.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if((Integer)scrollMultiplier.getModel().getValue() < 1) {
					scrollMultiplier.getModel().setValue(1);
				}
			}
		});
		

		moveBar = new JScrollBar(JScrollBar.HORIZONTAL);
		moveBar.setValue(45);
		moveBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == 1) {					
					final double pxoffs = (1000*(Integer)(scrollMultiplier.getValue())*(moveBar.getValue()-45)/45.0);
					final double msOffs = Math.signum(pxoffs)*Math.abs(av.unmapX(Math.abs(pxoffs))-av.unmapX(0));
					
					new Thread() {
						public void run() {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {}
							
							double sd = msOffs+pStartVisu;
							double ed = msOffs+pEndVisu;
							pStartVisu = -1;
							pEndVisu = -1;
							
							av.visuStart(sd);
							av.visuEnd(ed);
							sav.setRange(sd, ed);
							av.repaint();
							sav.repaint();
						}
					}.start();
					
					
					moveBar.setValue(45);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == 1) {
					pStartVisu = av.visuStart();
					pEndVisu = av.visuEnd();
				}
			}
		});
		moveBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if(pStartVisu == -1.0 || pEndVisu == -1.0)
					return;
				
				double pxoffs = (1000*(Integer)(scrollMultiplier.getValue())*(moveBar.getValue()-45)/45.0);
				double msOffs = Math.signum(pxoffs)*Math.abs(av.unmapX(Math.abs(pxoffs))-av.unmapX(0));

				av.visuStart(msOffs+pStartVisu);
				av.visuEnd(msOffs+pEndVisu);
				sav.setRange(msOffs+pStartVisu, msOffs+pEndVisu);
				av.repaint();
				sav.repaint();
			}
		});
		
		
		JPanel top = new JPanel();
		{
			BorderLayout lo = new BorderLayout();
			sav.setPreferredSize(new Dimension(0, 45));
			top.setLayout(lo);
			
			//navigator above
			top.add(sav, BorderLayout.NORTH);
			
			//primary waveform visual center
			top.add(av);
			
			//controls below
			top.add(createControlsPanel(), BorderLayout.SOUTH);
		}
		
		JPanel bottom = new JPanel();
		{
			bottom.setLayout(new BorderLayout());
			transcriptPanel = createTranscriptPanel();
	
			bottom.add(moveBar, BorderLayout.NORTH);
			bottom.add(transcriptPanel, BorderLayout.CENTER);
		}
		
		GridLayout lo = new GridLayout();
		lo.setColumns(1);
		lo.setRows(2);
		setLayout(lo);

		add(top);
		add(bottom);
	}
	
	private void performSaveTo(File output) {
		String abs = output.getAbsolutePath();
		if(!abs.endsWith(".json")) {
			abs += ".json";
			output = new File(abs);
		}

		try {
			output.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write(Main.save());
			bw.close();
			Main.activeFile(output);
		} 
		catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(AudioVisualController.this, "Error saving: " + e1.getMessage());
		}
	}
	
	//rewind start point to the beginning of the segment, useful shortcut.
	private void startPointToSegmentStart() {
		Segment sel = at.selectedSegment();
		if(sel != null) {
			av.getAudio().startPoint(sel.start());
			av.repaint();
			sav.repaint();
		}
	}
	
	private JPanel createControlsPanel() {
		return new JPanel() {
			private static final long serialVersionUID = 2054596320493442120L;
			
			private int selectStartX = -1;

			//reusable method for changing the region based on mouse movement
			private void performRegionChange(MouseEvent e) {
				if(selectStartX > -1) {
					int startX = selectStartX;
					int endX = e.getPoint().x;
					if(endX < startX) {
						int t = startX;
						startX = endX;
						endX = t;
					}
					
					at.selectRegion(av.unmapX(startX), av.unmapX(endX));
					
					//OPT perhaps allow timer movement during play somehow. Have to test how intuitive this is
					av.getAudio().startPoint(av.unmapX(startX));
					
					sav.repaint();
					av.repaint();
				}
			}
			
			//anonymous constructor for controls panel
			{
				selectedTypeLabel = new JLabel();
				updateSelectedType();
				
				//code related to selecting regions (mouse movement listener, and click listener)
				{
					//selection listener
					av.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							selectStartX = e.getPoint().x;
							AudioVisualController.this.requestFocus();
						}
						
						@Override
						public void mouseReleased(MouseEvent e) {
							performRegionChange(e);
							selectStartX = -1;
							AudioVisualController.this.requestFocus();
						}
					});
					
					//movement listener
					av.addMouseMotionListener(new MouseMotionAdapter() {
						@Override
						public void mouseDragged(MouseEvent e) {
							performRegionChange(e);
							AudioVisualController.this.requestFocus();
						}
					});
				}
				
				//button for adding beat at selection start
				JButton addSegment = new JButton("<html><a style='text-decoration: underline;'>C</a>reate Segment</html>");
				addSegment.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//DEBUG, should be selected SegmentType
						if(!at.newSegmentAtSelection(selectedSegmentType)) {
							JOptionPane.showMessageDialog(null, "A segment cannot be created in this selection.");
						}
						av.repaint();
						sav.repaint();
					}
				});
				
				
				//button for merging segments
				final JButton mergeSegments = new JButton("<html><a style='text-decoration: underline;'>M</a>erge</html>");
				mergeSegments.setPreferredSize(new Dimension(105, 26));
				mergeSegments.addActionListener(
						new MergeSegmentActionListener(mergeSegments,AudioVisualController.this,at,av,sav,transcriptPanel));
				
				
				
				JButton delSegment = new JButton("<html><a style='text-decoration: underline;'>D</a>elete</html>");
				delSegment.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(!at.deleteSegment()) {
							JOptionPane.showMessageDialog(null, "There is nothing to delete.");
						}
						av.repaint();
						sav.repaint();
					}
				});
				
				JButton defineTypes = new JButton("<html><font style='text-decoration: underline;'>T</font>ype Definitions</html>");
				defineTypes.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(!typeManagerFrame.isVisible()) {
							typeManagerFrame.setVisible(true);
						}
					}
				});
				
				JButton save = new JButton("<html><font style='text-decoration: underline;'>S</font>ave</html>");
				JButton saveas = new JButton("Save As..");
				{	
					saveas.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							saveAs();
						}
					});
					
					save.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(Main.activeFile() != null) {
								performSaveTo(Main.activeFile());
							}
							else {
								saveAs();
							}
						}
					});
				}
				
				JButton compile = new JButton("Compile");
				compile.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(!settings.isVisible()) {
							settings.setVisible(true);
						}
					}
				});
	
				
				//control layout
				{
					FlowLayout lo = new FlowLayout();
					lo.setAlignment(FlowLayout.LEFT);
					setLayout(lo);
					
					scrollMultiplier.setPreferredSize(new Dimension(30, 20));
					add(scrollMultiplier);
					add(addSegment);
					add(mergeSegments);
					add(delSegment);
					add(defineTypes);
					add(save);
					add(saveas);
					add(compile);
					add(selectedTypeLabel);
				}
				
				//hotkeys
				AudioVisualController.this.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						switch(e.getKeyCode()) {
						case KeyEvent.VK_LEFT:
							{
								Audio aud = av.getAudio();
								at.selectRegion(
										av.unmapX(av.mapX(aud.getSelectedRegionStart())-4), 
										av.unmapX(av.mapX(aud.getSelectedRegionEnd())-4));
								av.repaint();
								sav.repaint();
							}
							break;
						case KeyEvent.VK_RIGHT:
							{
								Audio aud = av.getAudio();
								at.selectRegion(
										av.unmapX(av.mapX(aud.getSelectedRegionStart())+4), 
										av.unmapX(av.mapX(aud.getSelectedRegionEnd())+4));
								av.repaint();
								sav.repaint();
							}
							break;
						case KeyEvent.VK_UP:
							at.newSegmentAtSelection(selectedSegmentType);
							av.repaint();
							sav.repaint();
							break;
						case KeyEvent.VK_DOWN:
						case KeyEvent.VK_DELETE:
							at.deleteSegment();
							av.repaint();
							sav.repaint();
							break;
						case KeyEvent.VK_SPACE:
							{
								final Audio aud = av.getAudio();
								final Segment selected = at.selectedSegment();
								
								at.playToggle(new Runnable() {
									public void run() {
										av.repaint();
										sav.repaint();
										
										double time = aud.time();
										Segment seg = at.segmentContaining(aud.time());
										if(seg != null && seg != selected) {
											at.selectRegion(time, time+0.1);
										}
									}
								}, av.mapX(aud.getSelectedRegionEnd()) - av.mapX(aud.getSelectedRegionStart()) > 3);
							}
							break;
						case KeyEvent.VK_T:
							if(!typeManagerFrame.isVisible()) {
								typeManagerFrame.setVisible(true);
							}
							break;
						case KeyEvent.VK_1:
						case KeyEvent.VK_2:
						case KeyEvent.VK_3:
						case KeyEvent.VK_4:
						case KeyEvent.VK_5:
						case KeyEvent.VK_6:
						case KeyEvent.VK_7:
						case KeyEvent.VK_8:
						case KeyEvent.VK_9:
							selectType(Integer.parseInt(String.valueOf(e.getKeyChar()))-1);
							break;
						case KeyEvent.VK_0:
							selectType(9);
							break;
						case KeyEvent.VK_S:
							if(Main.activeFile() != null) {
								performSaveTo(Main.activeFile());
							}
							else {
								saveAs();
							}
							break;
							
						//reserved (for convenience, etc.)
						case KeyEvent.VK_C:
						case KeyEvent.VK_U:
						case KeyEvent.VK_V:
						case KeyEvent.VK_B:
							break;
						case KeyEvent.VK_M:
							(new MergeSegmentActionListener(mergeSegments,AudioVisualController.this,at,av,sav,transcriptPanel))
								.actionPerformed(null);
							break;
						case KeyEvent.VK_F:
							startPointToSegmentStart();
							break;
						case KeyEvent.VK_I:
							ioc.setVisible(true);
							break;
						default:
							if(!transcriptPanel.textAreaHasFocus()) {
								transcriptPanel.textAreaFocus();
							}
							break;
						}
					}
					
					@Override
					public void keyTyped(KeyEvent e) {
						switch(e.getKeyChar()) {
						case 'u':
							at.unselect();
							av.repaint();
							sav.repaint();
							break;
						case 'c':
							at.newSegmentAtSelection(selectedSegmentType);
							av.repaint();
							sav.repaint();
							break;
						}
					}
				});
				AudioVisualController.this.setFocusable(true);
				AudioVisualController.this.requestFocus();
			}
		};
	}
	
	private TranscriptPanel createTranscriptPanel() {
		final TranscriptPanel tp = new TranscriptPanel(at);
		
		at.regionChangeCallback(new Runnable() {
			@Override
			public void run() {
				tp.readSegment(at.selectedSegment());
			}
		});
		
		tp.addTranscriptChangeListener(new TranscriptChangeListener() {
			@Override
			public void transcriptChanged(String ts) {
				av.repaint();
			}
		});

		return tp;
	}
}
