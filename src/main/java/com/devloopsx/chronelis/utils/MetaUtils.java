package com.devloopsx.chronelis.utils;

import com.devloopsx.chronelis.dto.response.common.MetaInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.time.Instant;

public class MetaUtils {

	private MetaUtils() {
		// private constructor to prevent instantiation
	}

	public static MetaInfo buildMetaInfo(HttpServletRequest request) {
		return MetaInfo.builder().timestamp(Instant.now().toString()).instance(request.getRequestURI()).build();
	}

	public static MetaInfo buildWebSocketMetaInfo(SimpMessageHeaderAccessor headerAccessor) {
		String instance = headerAccessor.getDestination();
		return MetaInfo.builder()
				.timestamp(Instant.now().toString())
				.instance(instance != null ? instance : "Unknown-WebSocket-Destination")
				.build();
	}
}
