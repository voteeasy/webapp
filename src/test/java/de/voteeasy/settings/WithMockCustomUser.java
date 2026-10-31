package de.voteeasy.settings;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// inspired by https://docs.spring.io/spring-security/site/docs/4.2.x/reference/html/test-method.html
// https://docs.spring.io/spring-security/site/docs/current/reference/html5/#test-method-withuserdetails
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String username() default "admin13test";

    String name() default "Full admin user";
}
