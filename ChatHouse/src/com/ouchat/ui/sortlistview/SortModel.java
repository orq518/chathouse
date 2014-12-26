package com.ouchat.ui.sortlistview;


public class SortModel {
	private String JID;   //显示的数据
	
	private String name;   //显示的数据
	
	private String sortLetters;  //显示数据拼音的首字母
	
	
	public String getJID() {
		return JID;
	}
	public void setJID(String name) {
		this.JID = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
