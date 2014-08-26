package disp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import compile.format.DefaultFormat;
import core.Audio;
import core.Segment;
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
			this.preEvaluate = preEvaluate;
		}
	}
	
	private static final Pattern rtime = Pattern.compile("((\\d{1,2}):)?((\\d{1,2}):)?(\\d{1,2})(.(\\d{1,3}))?");
	
	private JTextField interval;
	private JTextField format;
	private JCheckBox preEval;
	private JSpinner len;
	
	private Audio aud;
	
	public IntervalOperationChooser(Audio aud) {
		this.aud = aud;
		setTitle("Interval Operation");
		
		//TODO gui
	}
	
	//returns interval operation object, otherwise returns null if invalid time specified
	private IntervalOperation build() {
		String ivlt = interval.getText();
		Matcher m = rtime.matcher(ivlt);
		String hours = m.group(2), minutes = m.group(4), seconds = m.group(5), ms = m.group(7);
		
		double every = 0;
		if(m.find()) {
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
		
		if(io.preEvaluate) { 
			DefaultFormat df = new DefaultFormat(aud.typeManager());
			df.formatString(DefaultFormat.TEXT_EXPR);
			
			for(double s = aud.getSelectedRegionStart(); s <= aud.getSelectedRegionEnd(); s+=io.every) {
				Segment seg = new Segment(s, s+io.length);
				seg.transcript(io.text);
				seg.transcript(df.format(seg));
				aud.segments().add(seg);
			}
		}
		else {
			for(double s = aud.getSelectedRegionStart(); s <= aud.getSelectedRegionEnd(); s+=io.every) {
				Segment seg = new Segment(s, s+io.length);
				seg.transcript(io.text);
				aud.segments().add(seg);
			}
		}
	}
}
