package com.devloopsx.chronelis.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TokenType {
	ACCESS_TOKEN("access_token"), REFRESH_TOKEN("refresh_token");

	String key;
}
