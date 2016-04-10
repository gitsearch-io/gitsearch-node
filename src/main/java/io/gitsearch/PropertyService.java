package io.gitsearch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String FILE_NAME = "config.properties";
    private Properties properties;

    public PropertyService() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME)) {
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
