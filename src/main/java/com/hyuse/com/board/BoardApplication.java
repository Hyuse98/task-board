package com.hyuse.com.board;

import com.hyuse.com.board.controller.ConsoleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BoardApplication {

	@Autowired
	private ConsoleController consoleController;

    public static void main(String[] args) {

		SpringApplication.run(BoardApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            consoleController.startApplication();
        };
    }
}
