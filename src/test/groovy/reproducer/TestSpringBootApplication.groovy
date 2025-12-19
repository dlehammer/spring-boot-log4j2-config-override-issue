package reproducer

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@CompileStatic
@EnableWebMvc
@SpringBootApplication
class TestSpringBootApplication {

    static void main(String[] args) {
        SpringApplication.run(TestSpringBootApplication)
    }
}
