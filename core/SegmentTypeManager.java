package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SegmentTypeManager {
	private HashMap<String, SegmentType> types;
	private Set<Runnable> updateCallbacks;
	
	public SegmentTypeManager() {
		updateCallbacks = new HashSet<Runnable>();
		types = new HashMap<String, SegmentType>();
		define("$default", SegmentType.DEFAULT);
	}
	
	public void define(String type, SegmentType def) {
		types.put(type, def);
	}
	
	public void undefine(String type) {
		if(types.containsKey(type))
			types.remove(type);
	}
	
	public SegmentType get(String type) {
		return types.get(type);
	}
	
	public void clear() {
		types.clear();
	}
	
	public void update() {
		for(Runnable r : updateCallbacks) {
			r.run();
		}
	}
	
	public void updateCallback(Runnable r) {
		updateCallbacks.add(r);
	}
	
	public Set<String> types() {
		return types.keySet();
	}
}
