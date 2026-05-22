package com.devloopsx.chronelis;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChronelisApplication {

  public static void main(String[] args) {
    loadDotenvFrom(".");
    loadDotenvFrom("src/main/resources");

    SpringApplication.run(ChronelisApplication.class, args);
  }

  private static void loadDotenvFrom(String directory) {
    Dotenv dotenv =
        Dotenv.configure().directory(directory).ignoreIfMalformed().ignoreIfMissing().load();

    dotenv
        .entries()
        .forEach(
            entry -> {
              if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
              }
            });
  }
}
