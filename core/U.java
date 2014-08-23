package core;

import java.util.Base64;

public class U {
	public static String b64e(String plain) {
		return new String(Base64.getEncoder().encode(plain.getBytes()));
	}
	
	public static String b64d(String b64) {
		return new String(Base64.getDecoder().decode(b64.getBytes()));
	}
}
