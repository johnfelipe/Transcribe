package core.state;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class StateManager {
	private static ArrayList<State> states;
	private static int position;
	
	private static class State {
		private JSONObject stateObject;
		
		public static State capture() {
			//TODO
			return null;
		}
		
		public void load() {
			//TODO
		}
	}
	
	public static void push() {
		//TODO
	}
	
	public static void back() {
		//TODO
	}
	
	public static void forward() {
		//TODO
	}
	
	public static void end() {
		//TODO
	}
	
	public static void load() {
		//TODO
	}
}
