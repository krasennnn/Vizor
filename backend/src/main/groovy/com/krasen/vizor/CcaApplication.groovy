package com.krasen.vizor

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CcaApplication {

    static void main(String[] args) {
        SpringApplication.run(CcaApplication, args)
    }

}
