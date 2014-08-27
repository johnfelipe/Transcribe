package disp;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import compile.format.DefaultFormat;
import core.Audio;
import core.Segment;
import core.SegmentType;
import core.Time;

public class IntervalOperationChooser extends JFrame {
	public static class IntervalOperation {
		double every;
		double length;
		String text;
		boolean preEvaluate;
	
		public IntervalOperation(double every, double len, String text, boolean preEvaluate) {
			this.every = every;
			this.text = text;
			this.length = len;
			this.preEvaluate = preEvaluate;
		}
	}
	
	private static final Pattern rtime = Pattern.compile("((\\d{1,2}):)?((\\d{1,2}):)?(\\d{1,2})(.(\\d{1,3}))?");
	
	private JTextField interval;
	private JTextField format;
	private JCheckBox preEval;
	private JSpinner len;
	private AudioVisualController avc;

	private static final Font formatFont = new Font("Consolas", 14, 14);
	
	private String segType = "$default";
	
	private Audio aud;
	
	public IntervalOperationChooser(Audio aud, AudioVisualController avc) {
		this.aud = aud;
		this.avc = avc;
		
		setTitle("Interval Operation");
		
		final JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				go();
			}
		});
		
		interval = new JTextField("00:00:05.00");
		preEval = new JCheckBox("Pre-evaluate segments");
		format = new JTextField("[\\hh:\\mm:\\ss]");
		len = new JSpinner();
		len.setValue(1000.0);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(3,3,3,3);
		
		format.setFont(formatFont);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0.02;
		add(new JLabel("Text: "), c);
		c.weightx = 1.0;
		c.gridx = 1;
		add(format, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0.04;
		add(new JLabel("Interval: "), c);
		c.weightx = 1.0;
		c.gridx = 1;
		add(interval, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.weightx = 0.04;
		add(new JLabel("Length: "), c);
		c.weightx = 1.0;
		c.gridx = 1;
		add(len, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		add(preEval, c);
		
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		add(ok, c);
	}
	
	public void setSegmentType(String t) {
		this.segType = t;
		setTitle("Interval Operation: " + aud.typeManager().get(t).name());
	}
	
	//returns interval operation object, otherwise returns null if invalid time specified
	private IntervalOperation build() {
		String ivlt = interval.getText();
		Matcher m = rtime.matcher(ivlt);
		
		double every = 0;
		if(m.find()) {
			String hours = m.group(2), minutes = m.group(4), seconds = m.group(5), ms = m.group(7);
			
			Time t = new Time();
			t.hours = hours.isEmpty() ? 0 : Integer.parseInt(hours);
			t.minutes = minutes.isEmpty() ? 0 : Integer.parseInt(minutes);
			t.seconds = Integer.parseInt(seconds);
			t.milliseconds = ms.isEmpty() ? 0 : Double.parseDouble(ms);

			every = t.totalMs();
		}
		else {
			return null;
		}

		return new IntervalOperation(every, (double)len.getValue(), format.getText(), preEval.isSelected());
	}
	
	private void go() {
		IntervalOperation io = build();
		if(io == null) {
			JOptionPane.showMessageDialog(this, "Error: Time format specified invalid. Expecting (hh-)(mm-)ss(.ms)");
		}
		
		SegmentType st = aud.typeManager().get(segType);
		if(st == null) {
			JOptionPane.showMessageDialog(this, "Error: Invalid segment type");
			return;
		}
		
		double start = aud.getSelectedRegionStart() - (aud.getSelectedRegionStart() % io.every);
		if(start < aud.getSelectedRegionStart()) {
			start += io.every;
		}
		
		if(io.preEvaluate) { 
			DefaultFormat df = new DefaultFormat(aud.typeManager());
			df.formatString(DefaultFormat.TEXT_EXPR);
			
			for(double s = start; s <= aud.getSelectedRegionEnd(); s+=io.every) {
				Segment seg = new Segment(s, s+io.length);
				seg.transcript(io.text);
				seg.transcript(df.format(seg));
				seg.segmentType(segType);
				aud.segments().add(seg);
			}
		}
		else {
			for(double s = start; s <= aud.getSelectedRegionEnd(); s+=io.every) {
				Segment seg = new Segment(s, s+io.length);
				seg.segmentType(segType);
				seg.transcript(io.text);
				aud.segments().add(seg);
			}
		}
		
		avc.repaint();
		setVisible(false);
	}
}
