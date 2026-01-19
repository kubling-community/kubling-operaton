package com.kubling.samples.operaton.orders;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {"com.kubling"})
@EnableAutoConfiguration(exclude = {
        ErrorMvcAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@EnableTransactionManagement
@EnableJdbcRepositories(
        basePackages = "com.kubling.samples.operaton",
        jdbcOperationsRef = "ordersJdbcTemplate"
)
public class StartOrdersCoordinator {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(StartOrdersCoordinator.class);
        app.setBannerMode(Banner.Mode.OFF);

        app.run(args);
    }

}
