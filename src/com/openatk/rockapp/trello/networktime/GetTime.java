package com.openatk.rockapp.trello.networktime;

import java.util.Date;

public class GetTime {
	 public static void main(String[] args) {
	        SntpClient client = new SntpClient();
	        if (client.requestTime("pool.ntp.org", 30000)) {
	            long now = client.getNtpTime() + System.nanoTime() / 1000
	                    - client.getNtpTimeReference();
	            Date current = new Date(now);
	            System.out.println(current.toString());
	        }

	    }
}
