package com.devloopsx.chronelis.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
public class AfterCommitExecutor {

  public void runAfterCommit(Runnable action) {
    if (action == null) {
      return;
    }

    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      runSafely(action);
      return;
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            runSafely(action);
          }
        });
  }

  private void runSafely(Runnable action) {
    try {
      action.run();
    } catch (Exception ex) {
      log.warn("After-commit action failed", ex);
    }
  }
}
