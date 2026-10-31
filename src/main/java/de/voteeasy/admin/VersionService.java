package de.voteeasy.admin;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class VersionService {
    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    // TODO #26: why is bean buildProperties not properly wired in?
    public VersionService(@Autowired(required = false) BuildProperties buildProperties, @Autowired(required = false) GitProperties gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    public Map<String, String> getBuildProperties() {
        Map<String, String> resultingProperties = new TreeMap<>(Comparator.comparing(String::toString));

        if (buildProperties != null && buildProperties.getTime() != null && Strings.isNotEmpty(buildProperties.getVersion()) && Strings.isNotEmpty(buildProperties.getName())) {
            resultingProperties.put("buildTime", "" + LocalDateTime.ofInstant(buildProperties.getTime(), ZoneOffset.UTC));
            resultingProperties.put("version", buildProperties.getVersion());
            resultingProperties.put("name", buildProperties.getName());
        }

        return resultingProperties;
    }

    public Map<String, String> getGitProperties() {
        Map<String, String> resultingProperties = new TreeMap<>(Comparator.comparing(String::toString));

        if (gitProperties != null) {
            resultingProperties.put("commitId", gitProperties.getShortCommitId() == null? "\uD83E\uDEB9" : gitProperties.getShortCommitId());
            if (gitProperties.getCommitTime() != null) {
                resultingProperties.put("lastCommitTime", "" + LocalDateTime.ofInstant(gitProperties.getCommitTime(), ZoneOffset.UTC));
            }
            resultingProperties.put("branch", gitProperties.getBranch() == null? "\uD83E\uDEB9" : gitProperties.getBranch());
        }

        return resultingProperties;
    }

}
