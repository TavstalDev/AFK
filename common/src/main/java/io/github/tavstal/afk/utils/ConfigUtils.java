package io.github.tavstal.afk.utils;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import io.github.tavstal.afk.CommonClass;
import io.github.tavstal.afk.CommonConfig;
import io.github.tavstal.afk.models.ConfigField;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ConfigUtils {
    private  static final String minecraftRootPath = System.getProperty("user.dir");
    private static final Path configFilePath = Paths.get(minecraftRootPath, "config", "tafk.toml");

    public static CommonConfig LoadConfig() {
        File configFile = configFilePath.toFile();
        CommonConfig config = null;

        if (!configFile.exists()) {
            try {
                File parentDir = configFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                // Create a new instance of the config class with default values
                config = CommonConfig.class.getDeclaredConstructor().newInstance();
                SaveConfig(config);  // Save the new config file with default values
                return config;
            } catch (Exception e) {
                CommonClass.LOG.error("Failed to load default configs.");
                CommonClass.LOG.error(e.getLocalizedMessage());
                return null;
            }
        }

        CommentedFileConfig fileConfig = CommentedFileConfig.builder(configFile).sync().build();
        fileConfig.load();

        try {
            config = CommonConfig.class.getDeclaredConstructor().newInstance();  // Create an instance of the config class
            // Use reflection to load all fields automatically
            for (Field field : CommonConfig.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = fileConfig.getOrElse(field.getName(), field.get(config));
                field.set(config, value);  // Assign the value from the file to the field
            }
        } catch (Exception e) {
            CommonClass.LOG.error("Failed to load configs.");
            CommonClass.LOG.error(e.getLocalizedMessage());
        }

        return config;
    }

    public static void SaveConfig(CommonConfig config)
    {
        File configFile = configFilePath.toFile();
        StringBuilder tomlContent = new StringBuilder();
        try (FileWriter fileWriter = new FileWriter(configFile))
        {
            CommentedFileConfig commentedFileConfig = CommentedFileConfig.builder(configFile)
                    .sync()
                    .build();

            // Get fields and sort them by the ConfigField annotation
            List<Field> sortedFields = Arrays.stream(config.getClass().getDeclaredFields())
                    .peek(field -> field.setAccessible(true)) // Make private fields accessible
                    .sorted(Comparator.comparingInt(field -> {
                        ConfigField annotation = field.getAnnotation(ConfigField.class);
                        return annotation != null ? annotation.order() : Integer.MAX_VALUE;
                    }))
                    .toList();

            // Use reflection to save all fields automatically
            for (Field field : sortedFields) {
                try {
                    ConfigField annotation = field.getAnnotation(ConfigField.class);
                    if (annotation != null) {
                        String comment = annotation.comment();

                        // Add comment to the TOML content
                        if (!comment.isBlank())
                            tomlContent.append("# ").append(comment).append("\n");
                    }
                    tomlContent.append(field.getName()).append(" = ");
                    Object value = field.get(config);
                    tomlContent.append(value instanceof String ? "\"" + value + "\"" : value);
                    tomlContent.append("\n\n");
                } catch (IllegalAccessException e) {
                    CommonClass.LOG.error("Failed to load config variable values while saving.");
                    CommonClass.LOG.error(e.getLocalizedMessage());
                }
            }

            // Write the content to the file
            fileWriter.write(tomlContent.toString());
        }
        catch (Exception e) {
            CommonClass.LOG.error("Failed to save configs.");
            CommonClass.LOG.error(e.getLocalizedMessage());
        }
    }
}
