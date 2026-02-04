package com.innowise.authservice.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseConfig {

  @Bean
  public SpringLiquibase liquibase(DataSource dataSource,
      @Value("${spring.liquibase.change-log}") String changeLog,
      @Value("${spring.liquibase.enabled:true}") boolean enabled) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(changeLog);
    liquibase.setShouldRun(enabled);
    return liquibase;
  }

  @Bean
  public static BeanFactoryPostProcessor liquibaseDependencyPostProcessor() {
    return beanFactory -> {
      if (beanFactory.containsBeanDefinition("entityManagerFactory")
          && beanFactory.containsBeanDefinition("liquibase")) {
        beanFactory.getBeanDefinition("entityManagerFactory").setDependsOn("liquibase");
      }
    };
  }
}
