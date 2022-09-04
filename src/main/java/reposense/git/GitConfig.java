package reposense.git;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import reposense.system.CommandRunner;
import reposense.system.LogsManager;

/**
 * Contains git config related functionalities.
 * Git config is used to set Git configuration values on a global or local
 * project level.
 */
public class GitConfig {
    public static final String FILTER_LFS_SMUDGE_KEY = "filter.lfs.smudge";
    public static final String FILTER_LFS_SMUDGE_VALUE = "git-lfs smudge --skip -- %f";
    public static final String FILTER_LFS_PROCESS_KEY = "filter.lfs.process";
    public static final String FILTER_LFS_PROCESS_VALUE = "git-lfs filter-process --skip";

    public static final String LIST_GLOBAL_CONFIG_COMMAND = "git config --global --list";

    public static final List<String[]> SKIP_SMUDGE_CONFIG_SETTINGS = Arrays.asList(
            new String[] {FILTER_LFS_SMUDGE_KEY, FILTER_LFS_SMUDGE_VALUE},
            new String[] {FILTER_LFS_PROCESS_KEY, FILTER_LFS_PROCESS_VALUE});

    private static final Logger logger = LogsManager.getLogger(GitConfig.class);

    /**
     * Returns the configuration values for lfs smudge and process within global git config.
     *
     * @return a list of string arrays where 0-index is key and 1-index is value.
     */
    public static List<String[]> getGlobalGitLfsConfig() {
        try {
            String gitConfig = getGitGlobalConfig();
            return Arrays.stream(gitConfig.split("\n"))
                    .map(line -> line.split("="))
                    .filter(line -> line[0].equals(FILTER_LFS_SMUDGE_KEY) || line[0].equals((FILTER_LFS_PROCESS_KEY)))
                    .collect(Collectors.toList());
        } catch (RuntimeException re) {
            logger.log(Level.WARNING, "Could not get global git lfs config", re);
            return new ArrayList<>();
        }
    }

    /**
     * Set the global git lfs configuration values. Does not run command if the configuration
     * values are already set to skip.
     *
     * @param lfsConfigs a list of string arrays where 0-index is key and 1-index is value.
     */
    public static void setGlobalGitLfsConfig(List<String[]> lfsConfigs) {
        String command = setGitLfsConfigCommand(lfsConfigs);
        CommandRunner.runCommand(Paths.get("."), command);
    }

    private static String setGitLfsConfigCommand(List<String[]> lfsConfigs) {
        String command = "";
        for (String[] config : lfsConfigs) {
            command += "git config --global " + config[0] + " " + "\"" + config[1] + "\"" + " && ";
        }
        return command.substring(0, Math.max(0, command.length() - 4));
    }

    private static String getGitGlobalConfig() {
        return CommandRunner.runCommand(Paths.get("."), LIST_GLOBAL_CONFIG_COMMAND);
    }
}
