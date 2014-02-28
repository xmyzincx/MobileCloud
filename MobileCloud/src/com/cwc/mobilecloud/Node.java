package com.cwc.mobilecloud;

import org.json.simple.JSONArray;

public class Node {

	public String IPAddr;
	public int port;
	public String ID;
	public int age;
	public int param_metric;
	public 	JSONArray contents;



	Node(){
		IPAddr = "0.0.0.0";
		port = 0;
		ID = "";
		age = 0;
		param_metric = 0;
		contents = null;
	}

	Node(String ip, int ePort){
		IPAddr = ip;
		port = ePort;		
	}

	void setPort(int eport){
		port = eport;

	}

	void setIP(String ip){
		IPAddr = ip;		
	}

	void setBat_Info(int battery_level){

		param_metric = battery_level;
	}

	void setContents(JSONArray econtents){
		contents = econtents;
	}

	int getPort(){
		return port;
	}

	String getIP(){
		return IPAddr;		
	}
	
	String getID(){
		return ID;
	}

	int getParamValue(){
		return param_metric;
	}


	//TODO check this item
	JSONArray getContents(){
		return contents;
	}

	public Node clone() {
		Node clone = new Node();
		clone.setIP(this.getIP());
		clone.ID = this.ID;
		clone.age = this.age;
		clone.param_metric = this.param_metric;
		clone.contents = this.contents;

		return clone;
	}
}
