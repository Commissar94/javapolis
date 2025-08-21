package javapolis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan("javapolis.model")
@ComponentScan("javapolis")
@EnableTransactionManagement
public class JavapolisApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavapolisApplication.class, args);
    }
}
