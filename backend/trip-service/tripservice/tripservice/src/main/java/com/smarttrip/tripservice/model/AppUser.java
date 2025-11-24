package com.smarttrip.tripservice.model;

import lombok.Data;

@Data
public class AppUser {
	private Long id;
	private String name;
	private String email;
	private String password;
}
