package core;

public abstract class AudioTimeDetector extends Thread {
	private static class State {
		boolean active;
		public State(boolean active) {this.active = active;}
	};
	
	private volatile State state;
	private Audio aud;
	
	public AudioTimeDetector(Audio aud) {
		this.aud = aud;
		state = new State(true);
	}

	public abstract void timeChanged(double ms, double lastms);
	
	public void kill() {
		synchronized(state) {
			state.active = false;
		}
	}
	
	@Override
	public void run() {
		double lastTime = aud.time();
		while(true) {
			synchronized(state) {
				if(!state.active) {
					return;
				}
			}
			
			double time = aud.time();
			if(time != lastTime) {
				timeChanged(time, lastTime);
				lastTime = time;
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
	}
}
