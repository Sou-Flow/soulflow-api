package com.poly.models.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
	
	private String token;
	
	private String pk;
	
	private String fullname;
	
	private String email;
	
	private String photo;
}
