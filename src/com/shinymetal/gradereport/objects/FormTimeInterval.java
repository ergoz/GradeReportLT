package com.shinymetal.gradereport.objects;

import java.util.Date;

public class FormTimeInterval extends FormSelectableField {
	
	private Date start;
	private Date stop;
	
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getStop() {
		return stop;
	}
	public void setStop(Date stop) {
		this.stop = stop;
	}
	
}
