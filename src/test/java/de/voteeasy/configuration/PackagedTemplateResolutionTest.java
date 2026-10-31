package de.voteeasy.configuration;

import de.voteeasy.admin.LoginController;
import de.voteeasy.login.LoginLogoutController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PackagedTemplateResolutionTest {
    @TempDir
    Path directory;

    @Test
    void welcomeViewsResolveFromAJarRatherThanAnExplodedSourceDirectory() throws Exception {
        Path jar = directory.resolve("templates.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("templates/app/index.html"));
            output.write("<html>VoteEasy</html>".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        // An isolated JAR classpath reproduces the Docker resource lookup. An exploded
        // directory can hide the extra slash produced by an absolute view name.
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, null);
             GenericApplicationContext context = new GenericApplicationContext()) {
            context.setClassLoader(loader);
            SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
            resolver.setApplicationContext(context);
            resolver.setPrefix("classpath:/templates/");
            resolver.setSuffix(".html");
            resolver.setCheckExistence(true);
            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(resolver);

            String welcome = new LoginLogoutController().showAppWelcomePage().getViewName();
            String flashMessage = new LoginController(null).testFlashMessageTemplate(
                    new ExtendedModelMap(), new RedirectAttributesModelMap()).getViewName();

            for (String view : new String[]{welcome, flashMessage}) {
                assertThat(resolver.resolveTemplate(engine.getConfiguration(), null, view, null))
                        .as("Packaged template for view %s", view).isNotNull();
            }
            assertThat(resolver.resolveTemplate(engine.getConfiguration(), null, "/app/index.html", null))
                    .as("The former absolute view name must reproduce the JAR lookup failure").isNull();
        }
    }
}
