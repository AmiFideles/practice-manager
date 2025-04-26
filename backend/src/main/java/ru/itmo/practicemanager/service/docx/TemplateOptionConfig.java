package ru.itmo.practicemanager.service.docx;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class TemplateOptionConfig {

    private static final String CONFIG_PATH = "/app/config/template_option";
    private static final String PARAM_KEY = "date";

    private String practiceDateRange;

    @PostConstruct
    public void init() {
//        load();
    }

    public synchronized String getPracticeDateRange() {
        return practiceDateRange;
    }

    public synchronized void setPracticeDateRange(String value) {
        save();
        this.practiceDateRange = value;
    }

    private void load() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            props.load(in);
            practiceDateRange = props.getProperty(PARAM_KEY, "");
        } catch (IOException e) {
            practiceDateRange = "";
        }
    }

    private void save() {
        Properties props = new Properties();
        props.setProperty(PARAM_KEY, practiceDateRange);
        try (FileOutputStream out = new FileOutputStream(CONFIG_PATH)) {
            props.store(out, "Template options");
        } catch (IOException e) {
            // логировать при необходимости
        }
    }
}