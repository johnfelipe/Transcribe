package disp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import core.Audio;
import core.AudioTools;
import core.Segment;

public class TranscriptPanel extends JPanel {
	public static interface TranscriptChangeListener {
		void transcriptChanged(String ts);
	}
	
	private static class SegmentInfo extends JPanel {
		private static final long serialVersionUID = 883969380328956436L;
		
		private static final String PREPEND_SEGTYPE = "Segment Type: ";

		private JLabel segmentTypeLabel;
		private JLabel selectedTimeLabel;
		
		private AudioTools at;
		
		public SegmentInfo(final AudioTools at) {
			this.at = at;
			
			FlowLayout lo = new FlowLayout();
			lo.setAlignment(FlowLayout.LEFT);
			
			setLayout(lo);
			segmentTypeLabel = new JLabel("No segment selected.");
			selectedTimeLabel = new JLabel("");
			selectedTimeLabel.setPreferredSize(new Dimension(230, 15));
			
			add(selectedTimeLabel);
			add(new JPanel());
			add(segmentTypeLabel);
			
			at.regionChangeCallback(new Runnable() {
				@Override
				public void run() {
					Audio aud = at.audio();
					
					double start = aud.getSelectedRegionStart();
					double end = aud.getSelectedRegionEnd();
					
					if(end - start < 1) {
						selectedTimeLabel.setText("Selection: " + Audio.msString(start));
					}
					else {
						selectedTimeLabel.setText("Selection: " + Audio.msString(start) + " => " + Audio.msString(end));
					}
				}
			});
		}
		
		public void setSegment(Segment seg) {
			if(seg != null) {
				segmentTypeLabel.setText(PREPEND_SEGTYPE + at.audio().typeManager().get(seg.segmentType()).name());
			}
			else {
				segmentTypeLabel.setText("No segment selected.");
			}
		}
	};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6779419084036672724L;

	private static final Font TRANSCRIPT_FONT = new Font("Lucida Console", 15, 15);
	private JTextArea text;
	
	private SegmentInfo si;
	private Segment currentSeg;
	private HashSet<TranscriptChangeListener> changeListeners;
	
	public TranscriptPanel(AudioTools at) {
		changeListeners = new HashSet<TranscriptChangeListener>();
		
		text = new JTextArea();
		text.setFont(TRANSCRIPT_FONT);
		currentSeg = null;
		
		text.setWrapStyleWord(true);
		text.setMargin(new Insets(5, 5, 5, 5));
		text.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent k) {
				//invoke later, after type is processed
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						writeTranscript();
					}
					
				});
			}
		});
		
		setLayout(new BorderLayout());
		
		final JScrollPane pane = new JScrollPane(text);
		si = new SegmentInfo(at);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				pane.setPreferredSize(new Dimension(getWidth(), getHeight()-70));
				si.setPreferredSize(new Dimension(getWidth(), 25));
			}
		});
		
		add(si, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		setBorder(new EmptyBorder(7, 7, 7, 7));
	}
	
	public void addTranscriptChangeListener(TranscriptChangeListener tcl) {
		changeListeners.add(tcl);
	}
	
	public void textAreaFocus() {
		text.requestFocus();
	}
	
	public boolean textAreaHasFocus() {
		return text.hasFocus();
	}
	
	private void writeTranscript() {
		if(currentSeg != null) {
			String ts = text.getText();
			currentSeg.transcript(ts);
			for(TranscriptChangeListener tcl : changeListeners) {
				tcl.transcriptChanged(ts);
			}
		}
	}
	
	public void readSegment(Segment seg) {
		if(currentSeg == seg)
			return;
		
		si.setSegment(seg);
		
		if(seg != null) {
			text.setText(seg.transcript());
		}
		else {
			text.setText("");
		}
		
		currentSeg = seg;
	}
}
