package com.smarttrip.tripservice;

import java.net.InetAddress;

public class TestGeoapifyConnection {
	
	public static void main(String[] args)throws Exception {
		System.out.println("Resolving api.geoapify.com...");
		InetAddress address = InetAddress.getByName("api.geoapify.com");
		System.out.println("Resolved to: " + address.getHostAddress());
	}
}
