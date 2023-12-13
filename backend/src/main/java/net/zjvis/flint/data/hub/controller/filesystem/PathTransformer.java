package net.zjvis.flint.data.hub.controller.filesystem;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode
@ToString
public class PathTransformer {

    private final String userHome;

    @Builder
    @Jacksonized
    public PathTransformer(String userHome) {
        this.userHome = removeFirstSlash(userHome);
    }

    public String userRelativePath(Long userId, String userIndependentPath) {
        return StringUtils.removeStart(removeFirstSlash(userIndependentPath), userHomePath(userId));
    }

    public String userIndependentPath(Long userId, String userRelativePath) {
        return String.format("%s/%s", userHomePath(userId), removeFirstSlash(userRelativePath));
    }

    public String userHomePath(Long userId) {
        return String.format("%s/%s", userHome, userId);
    }

    public String graphOutputTempPath(Long userId) {
        return String.format("%s%s/", "temp_output_", userId);
    }

    private String removeFirstSlash(String path) {
        return StringUtils.removeStart(path, "/");
    }
}
