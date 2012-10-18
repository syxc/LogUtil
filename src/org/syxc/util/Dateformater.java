package org.syxc.util;

enum Dateformater {
	
	NORMAL("yyyy-MM-dd HH:mm"), 
	DD("yyyy-MM-dd"), 
	SS("yyyy-MM-dd HH:mm:ss");

	private String value;

	private Dateformater(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
