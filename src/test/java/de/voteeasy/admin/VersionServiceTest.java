package de.voteeasy.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private BuildProperties buildProperties;
    @Mock
    private GitProperties gitProperties;

    private static Properties prepareGitProperties() {
        Properties p = new Properties();
        p.setProperty("commit.id.abbrev", "0cdac352-36e2-48bf-99de-bca7b12c68ce");
        p.setProperty("branch", "master");
        p.setProperty("commit.time", String.valueOf(0L));
        return p;
    }

    private static Properties prepareBuildProperties() {
        Properties p = new Properties();
        p.setProperty("name", "myTestName");
        p.setProperty("version", "4711");
        p.setProperty("time", String.valueOf(0L));
        return p;
    }

    @Test
    void emptyPropertiesDoNotCauseAnyExceptions() {
        VersionService versionService = new VersionService(buildProperties, gitProperties);
        assertThat(versionService).isNotNull();
        assertThat(versionService.getBuildProperties()).isEmpty();
        assertThat(versionService.getGitProperties()).hasSize(2); //3rd one is filtered out
    }

    @Test
    void parsesStuffOutOfInjectedGitProperties() {
        VersionService versionService = new VersionService(buildProperties, new GitProperties(prepareGitProperties()));

        final Map<String, String> properties = versionService.getGitProperties();
        assertThat(properties).isNotNull().containsEntry("commitId", "0cdac352-36e2-48bf-99de-bca7b12c68ce");
        assertThat(properties).containsEntry("branch", "master");
        assertThat(properties).containsEntry("lastCommitTime", "1970-01-01T00:00");
    }

    @Test
    void parsesStuffOutOfInjectedBuildProperties() {
        VersionService versionService = new VersionService(new BuildProperties(prepareBuildProperties()), gitProperties);

        final Map<String, String> properties = versionService.getBuildProperties();
        assertThat(properties).isNotNull().containsEntry("name", "myTestName");
        assertThat(properties).containsEntry("version", "4711");
        assertThat(properties).containsEntry("buildTime", "1970-01-01T00:00");
    }
}
