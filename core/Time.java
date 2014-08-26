package core;

public class Time {
	public int hours;
	public int minutes;
	public int seconds;
	public double milliseconds;
	
	public Time(int hours, int minutes, int seconds, double milliseconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		this.milliseconds = milliseconds;
	}
	
	public Time(double ms) {
		double hours = Math.floor(ms/3600000);
		double minutes = Math.floor((ms-hours*3600000)/60000);
		double seconds = Math.floor((ms-minutes*60000-hours*3600000)/1000);
		ms = ms - hours*360000 - minutes*60000 - seconds*1000;

		hours = (int)hours;
		minutes = (int)minutes;
		seconds = (int)seconds;
		milliseconds = ms;
	}
	
	public Time() {
		hours = minutes = seconds = 0;
		milliseconds = 0;
	}
	
	public double totalMs() {
		return hours*3600000 + minutes*60000 + seconds*1000 + milliseconds;
	}
	
	public String hh() {
		String hrs = Integer.toString(hours);
		if(hrs.length() < 2) {
			hrs = "0"+hrs;
		}
		return hrs;
	}
	
	public String mm() {
		String mins = Integer.toString(minutes);
		if(mins.length() < 2) {
			mins = "0"+mins;
		}
		return mins;
	}
	
	public String ss() {
		String sec = Integer.toString(seconds);
		if(sec.length() < 2) { 
			sec = "0"+sec;
		}
		return sec;
	}
	
	public String ms3() {
		String strms = Integer.toString((int)milliseconds);
		if(strms.length() < 3) {
			int amount = 3 - strms.length();
			for(int i = 0; i < amount; i++) {
				strms="0"+strms;
			}
		}
		return strms;
	}
	
	
	public String h() {
		return Integer.toString(hours);
	}
	
	public String m() {
		return Integer.toString(minutes);
	}
	
	public String s() {
		return Integer.toString(seconds);
	}
	
	public String ms() {
		String str = Double.toString(milliseconds);
		if(str.length() > 3) {
			str = str.substring(0, 3);
		}
		return str;
	}
}
