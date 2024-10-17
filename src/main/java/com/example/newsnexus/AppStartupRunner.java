package com.example.newsnexus;

import com.example.newsnexus.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements CommandLineRunner {

    @Autowired
    private CityService cityService;

    @Override
    public void run(String... args) throws Exception {
        String filePath = getClass().getClassLoader().getResource("uscities.csv").getPath();

        cityService.importCitiesFromCsv(filePath);
    }
}
