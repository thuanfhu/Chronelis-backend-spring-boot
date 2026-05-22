package com.devloopsx.chronelis.service;

public interface TokenBlacklistService {
  void blacklistToken(String token, long expirationTimeInSeconds);

  boolean isTokenBlacklisted(String token);

  void removeFromBlacklist(String token);
}
