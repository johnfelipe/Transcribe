package disp;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import core.Audio;
import core.AudioTools;
import core.Segment;

public class MergeSegmentActionListener implements ActionListener {
	private AudioTools at;
	private AudioVisual av;
	private AudioVisualController avc;
	private ShadedAudioVisual sav;
	private TranscriptPanel transcriptPanel;
	private JButton mergeSegments;
	
	public MergeSegmentActionListener(JButton mergeSegments, 
			AudioVisualController avc, 
			AudioTools at, 
			AudioVisual av, 
			ShadedAudioVisual sav, 
			TranscriptPanel transcriptPanel) 
	{
		
		this.mergeSegments = mergeSegments;
		this.av = av;
		this.at = at;
		this.avc = avc;
		this.sav = sav;
		this.transcriptPanel = transcriptPanel;
	}
	
	private boolean isActive() {
		return mergeSegments.getText().contains("with");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Segment base = at.selectedSegment();
		final boolean active = isActive();
		
		if(!active) {
			if(base == null) {
				JOptionPane.showMessageDialog(avc, "No base segment is selected.");
				return;
			}
			
			mergeSegments.setText("Merge with..");
			
			at.regionChangeCallback(new AudioTools.RegionChangeCallback() {
				@Override
				public boolean run() {
					if(!isActive()) {
						return false;
					}
					
					Segment sel = at.selectedSegment();
		
					if(sel == base || sel == null) {
						return true;
					}
					else {
						final String with = JOptionPane.showInputDialog("Merge with..", " ");
						final List<Segment> segs = at.orderedSegments(at.selectSegments(base, sel));
						final Audio aud = av.getAudio();
					
						String transcript = "";
						for(int i = 0; i < segs.size(); i++) {
							Segment s = segs.get(i);
							
							if(i < segs.size() - 1) {
								transcript += s.transcript().trim() + with;
							}
							else {
								transcript += s.transcript().trim();
							}
							
							if(s != base) {
								aud.segments().remove(s);
								continue;
							}
						}
						base.transcript(transcript);
						base.setRange(segs.get(0).start(), segs.get(segs.size()-1).end());
						
						if(transcriptPanel != null) {
							transcriptPanel.repaint();
						}
						
						mergeSegments.setText("<html><a style='text-decoration: underline;'>M</a>erge</html>");
						
						av.repaint();
						sav.repaint();
						
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								aud.setSelectedRegion(base.start(), base.end());
								av.repaint();
								sav.repaint();
							}
						});
						return false;
					}
				}
			});
		}
		else {
			mergeSegments.setText("<html><a style='text-decoration: underline;'>M</a>erge</html>");
		}
	}
}
