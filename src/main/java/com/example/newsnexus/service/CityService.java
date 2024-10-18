package com.example.newsnexus.service;

import com.example.newsnexus.model.City;
import com.example.newsnexus.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    public void importCitiesFromCsv(BufferedReader br) {
        try {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length < 2) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }

                City city = new City();
                city.setName(data[0].substring(1, data[0].length() - 1).trim());
                city.setState(data[1].substring(1, data[1].length() - 1).trim());
                cityRepository.save(city);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
