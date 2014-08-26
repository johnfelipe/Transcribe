package core;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;

public class Audio {
	private String audioFile;
	
	private AudioContext ac;
	private SamplePlayer sp;
	private Sample samp;
	private Gain gain;
	
	private double lastPlayStart;
	
	//necessary abstraction to avoid bugs with audio library
	//timeCurrentTimeSet -> time the (double currentTime) was set
	//currentTime -> the current 'time' set by client
	//timeLastPlayed -> time the audio was last played
	private double startPoint;
	
	private double selectedRegionStart;
	private double selectedRegionEnd;
	
	private HashSet<Segment> segments;
	private SegmentTypeManager typeManager;
	
	public Audio(String file) {
		lastPlayStart = 0;
		audioFile = file;
		
		ac = new AudioContext(); 
		samp = SampleManager.sample(file);
		sp = new SamplePlayer(ac, samp);
		gain = new Gain(ac, 1, 0.1f);
		
		gain.addInput(sp);
		ac.out.addInput(gain);
		stop();
		
		typeManager = new SegmentTypeManager();
		segments = new HashSet<Segment>();
		
		selectedRegionStart = -1;
		selectedRegionEnd = -1;
	}
	
	public void close() {
		ac.stop();
	}
	
	public double startPoint() {
		return startPoint;
	}
	
	public void startPoint(double v) {
		startPoint = v;
	}
	
	public double getSelectedRegionStart() {
		return selectedRegionStart;
	}
	
	public double getSelectedRegionEnd() {
		return selectedRegionEnd;
	}
	
	public void setSelectedRegion(double start, double end) {
		this.selectedRegionStart = start;
		this.selectedRegionEnd = end;
	}
	
	public void unselect() {
		this.selectedRegionStart = -1;
		this.selectedRegionEnd = -1;
	}
	
	public boolean hasSelection() {
		return this.selectedRegionStart > -1;
	}
	
	public void getFrame(double ms, float[] out) {
		samp.getFrameNoInterp(ms, out);
	}
	
	public double msToSamples(double ms) {
		return samp.msToSamples(ms);
	}
	
	//length in ms
	public double length() {
		return samp.getLength()-bufferTime();
	}
	
	public String file() {
		return audioFile;
	}
	
	public Set<Segment> segments() {
		return segments;
	}
	
	public SegmentTypeManager typeManager() {
		return typeManager;
	}
	
	public void stop() {
		try {
			sp.pause(true);
			ac.stop();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	public void play(double start) {
		if(start >= length()-2 || start < 0)
			return;
		
		stop();
		lastPlayStart = start;
		sp.start((float)start);
		ac.start();
	}
	
	public void play() {
		play(startPoint);
	}
	
	public boolean playing() {
		return !sp.isPaused();
	}
	
	//returns the current time of playback
	private double bufferTime() {
		return 150;
	}
	
	public double time() {
		double point = sp.getPosition()-bufferTime();
		if(point < lastPlayStart) {
			return lastPlayStart;
		}
		else
			return point;
	}
	
	public static String msString(double ms) {
		double hours = Math.floor(ms/3600000);
		double minutes = Math.floor((ms-hours*3600000)/60000);
		double seconds = Math.floor((ms-minutes*60000-hours*3600000)/1000);
		ms = ms - hours*360000 - minutes*60000 - seconds*1000;
		
		String tstr = "";
		if(hours > 0) {
			String hr = Integer.toString((int)hours);
			if(hr.length() < 2)
				hr = "0"+hr;
			tstr += hr+":";
		}
		
		if(minutes > 0) {
			String min = Integer.toString((int)minutes);
			if(min.length() < 2)
				min = "0"+min;
			tstr += min+":";
		}
		
		String sec = Integer.toString((int)seconds);
		if(sec.length() < 2)
			sec = "0"+sec;
		
		String strms = Integer.toString((int)ms);
		if(strms.length() < 3) {
			int amount = 3 - strms.length();
			for(int i = 0; i < amount; i++) {
				strms="0"+strms;
			}
		}
		
		tstr += (sec + "." + strms);
		
		return tstr;
	}
}
