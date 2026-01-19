package com.kubling.samples.operaton;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {"com.kubling"})
@EnableAutoConfiguration(exclude = {
        ErrorMvcAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@EnableTransactionManagement
public class StartEmbeddedOperaton {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(StartEmbeddedOperaton.class);
        app.setBannerMode(Banner.Mode.OFF);

        app.run(args);
    }

}
