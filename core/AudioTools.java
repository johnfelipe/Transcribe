package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//managed beat setting / unsetting, placing beats by interval (bpm+offs spec.)
//behaves like a virtual 'jukebox' with additional features.
public class AudioTools {
	public static interface RegionChangeCallback {
		//returns if keep, (false -> delete, true -> keep)
		boolean run();
	}
	
	private Audio aud;
	private AudioTimeDetector atd;
	private Set<RegionChangeCallback> selectChangeCallbacks;
	private static final double SNAP = 0.001;
	
	public AudioTools(Audio aud) {
		this.aud = aud;
		selectChangeCallbacks = new HashSet<RegionChangeCallback>();
		atd = null;
	}
	
	public Audio audio() {
		return aud;
	}
	
	//snaps to nearest .001
	private static double snap(double value) {
		return (int)(value/SNAP)*SNAP;
	}

	public void undo() {
		//TODO
	}
	
	public void regionChangeCallback(final Runnable cb) {
		selectChangeCallbacks.add(new RegionChangeCallback() {
			@Override
			public boolean run() {
				cb.run();
				return true;
			}
		});
	}
	
	public void regionChangeCallback(RegionChangeCallback cb) {
		selectChangeCallbacks.add(cb);
	}
	
	
	
	//returns if selected segment
	public boolean selectRegion(double start, double end) {
		Set<Segment> segments = aud.segments();
		
		double ds = snap(start), de = snap(end);
		boolean dsChanged = false, deChanged = false;
		boolean selectedSegment = false;
		
		for(Segment seg : segments) {
			if(start >= seg.start() && end <= seg.end()) {
				ds = seg.start();
				de = seg.end();
				selectedSegment = true;
				break;
			}
			else if(start < seg.end() && end > seg.end()) {
				ds = seg.end()+0.1;
				dsChanged = true;
			}
			else if(end >= seg.start() && start < seg.start()) {
				de = seg.start()-0.1;
				deChanged = true;
			}
			
			if(dsChanged && deChanged) {
				break;
			}
		}
		
		aud.setSelectedRegion(ds, de);
		HashSet<RegionChangeCallback> rem = new HashSet<RegionChangeCallback>();
		for(RegionChangeCallback cb : selectChangeCallbacks) {
			if(!cb.run()) {
				rem.add(cb);
			}
		}
		for(RegionChangeCallback r : rem) {
			selectChangeCallbacks.remove(r);
		}
		
		return selectedSegment;
	}
	
	//returns segments ordered from earliest to latest
	public List<Segment> orderedSegments() {
		return orderedSegments(aud.segments());
	}
	
	public List<Segment> orderedSegments(Set<Segment> ss) {
		ArrayList<Segment> segs = new ArrayList<Segment>();
		
		for(Segment seg : ss) {
			segs.add(seg);
		}
		
		Collections.sort(segs, new Comparator<Segment>() {
			@Override
			public int compare(Segment s1, Segment s2) {
				if(s1.start() < s2.start()) {
					return -1;
				}
				else {
					return 1;
				}
			}
		});
		
		return segs;
	}
	
	public Segment segmentContaining(double ms) {
		Set<Segment> segments = aud.segments();
		for(Segment seg : segments) {
			if(ms >= seg.start() && ms <= seg.end())
				return seg;
		}
		return null;
	}
	
	//returns the selected segment (null if none selected)
	public Segment selectedSegment() {
		Set<Segment> segments = aud.segments();
		double s = aud.getSelectedRegionStart();
		double e = aud.getSelectedRegionEnd();
		
		for(Segment seg : segments) {
			if(Math.abs(seg.start() - s) <= SNAP && Math.abs(seg.end() - e) <= SNAP) {
				return seg;
			}
		}
		
		return null;
	}
	
	//deletes selected segment, returns if successful
	public boolean deleteSegment() {
		Segment selected = selectedSegment();
		if(selected != null) {
			aud.segments().remove(selected);
			return true;
		}
		else {
			return false;
		}
	}
	
	//retypes the selected segment, returns if successful
	public boolean retypeSegment(String type) {
		Segment selected = selectedSegment();
		if(selected != null) {
			selected.segmentType(type);
			return true;
		}
		return false;
	}
	
	//selects all segments from (from) to (to)
	public Set<Segment> selectSegments(Segment from, Segment to) {
		HashSet<Segment> out = new HashSet<Segment>();
		
		List<Segment> segs = orderedSegments();
		int s1 = segs.indexOf(from);
		int s2 = segs.indexOf(to);
		
		if(s2 < s1) {
			int t = s2;
			s2 = s1;
			s1 = t;
		}
		
		for(int i = s1; i <= s2; i++) {
			out.add(segs.get(i));
		}
		
		return out;
	}
	
	public void unselect() {
		aud.unselect();
	}
	
	public boolean newSegmentAtSelection(String type) {
		Set<Segment> segments = aud.segments();
		
		double start = aud.getSelectedRegionStart();
		double end = aud.getSelectedRegionEnd();
		
		//segment too small
		if(end - start < 50.0) {
			return false;
		}
		
		//check if this segment selection overlaps
		for(Segment seg : segments) {
			if(seg.start() >= start && seg.end() <= end)
				return false;
		}
		
		segments.add(new Segment(start, end, type));
		return true;
	}
	
	public void playToggle(final Runnable tickCallback, final boolean endAtRegionEnd) {
		if(aud.playing()) {
			aud.stop();
			
			if(atd != null) {
				atd.kill();
				atd = null;
			}
		}
		else {
			atd = new AudioTimeDetector(aud) {
				@Override
				public void timeChanged(double ms, double lastms) {
					if(ms >= aud.length()-50 || (endAtRegionEnd && aud.getSelectedRegionEnd()-ms < 5)) {
						aud.stop();
						atd.kill();
						atd = null;
					}
					
					if(tickCallback != null) {
						tickCallback.run();
					}
				}
			};
			atd.start();
			aud.play();
		}
	}
}
