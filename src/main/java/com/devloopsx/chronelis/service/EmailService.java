package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.domain.User;

public interface EmailService {
  void sendVerifyActiveAccountEmail(User user);

  void sendForgotPasswordEmail(User user);

  void sendVerifyChangeEmail(User user);
}
