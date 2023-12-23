package org.cubewhy.celestial.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ConfigFile {
    @Getter
    private JsonObject config;
    public final File file;

    public ConfigFile(File file) {
        this.file = file;
        this.load();
    }

    public ConfigFile setValue(String key, String value) {
        this.config.addProperty(key, value);
        return this.save();
    }

    public ConfigFile setValue(String key, char value) {
        this.config.addProperty(key, value);
        return this.save();
    }

    public ConfigFile setValue(String key, int value) {
        this.config.addProperty(key, value);
        return this.save();
    }

    public ConfigFile setValue(String key, boolean value) {
        this.config.addProperty(key, value);
        return this.save();
    }

    public ConfigFile setValue(String key, JsonObject value) {
        this.config.add(key, value);
        return this.save();
    }

    public ConfigFile initValue(String key, JsonElement value) {
        if (!this.config.has(key)) {
            log.info("Init value " + key + " -> " + value);
            this.config.add(key, value);
        }
        return this.save();
    }

    public ConfigFile initValue(String key, String value) {
        return this.initValue(key, new JsonPrimitive(value));
    }

    public ConfigFile initValue(String key, int value) {
        return this.initValue(key, new JsonPrimitive(value));
    }

    public JsonElement getValue(String key) {
        return this.config.get(key);
    }

    public ConfigFile save() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.file));
            bufferedWriter.write(config.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ConfigFile load() {
        Gson gson = new Gson();
        BufferedReader bufferedReader;
        boolean successful = false;

        while (!successful) {
            try {
                bufferedReader = new BufferedReader(new FileReader(this.file));
                config = gson.fromJson(bufferedReader, JsonObject.class);
                if (config == null) {
                    config = new JsonObject();
                }
                successful = true;
            } catch (FileNotFoundException e) {

                try {
                    if (!this.file.getParentFile().exists()) {
                        this.file.getParentFile().mkdirs();
                    }
                    this.file.createNewFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return this;
    }

}
