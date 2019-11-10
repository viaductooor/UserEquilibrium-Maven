package org.lab1505.ue.alg.chongqing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EmergencyStations {
    private List<FireAlarmCenter> fireAlarmCenters;
    private List<Hospital> hospitals;
    private List<PoliceStation> policeStations;

    public EmergencyStations(List<FireAlarmCenter> fireAlarmCenters,
                             List<Hospital> hospitals, List<PoliceStation> policeStations) {
        this.fireAlarmCenters = fireAlarmCenters;
        this.hospitals = hospitals;
        this.policeStations = policeStations;
    }

    /**
     * Every line should be composed of (node_id,name,type,resource), where node_id is an Integer,
     * type is one of {"hospital", "fire","police"}, and resource is an Integer which stands for
     * the number of the available cars.
     * First line is header, therefore you should read from the second line.
     *
     * @param inputStream
     * @return
     */
    public static EmergencyStations readFromInputStream(InputStream inputStream) {
        List<FireAlarmCenter> fireAlarmCenters = new ArrayList<>();
        List<Hospital> hospitals = new ArrayList<>();
        List<PoliceStation> policeStations = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.readLine();
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] items = line.split(",");
                int id = Integer.parseInt(items[0].trim());
                String name = items[1].trim();
                String type = items[2].trim();
                int resource = Integer.parseInt(items[3].trim());
                switch (type) {
                    case "hospital":
                        hospitals.add(new Hospital(id, name, resource));
                        break;
                    case "fire":
                        fireAlarmCenters.add(new FireAlarmCenter(id, name, resource));
                        break;
                    case "police":
                        policeStations.add(new PoliceStation(id, name, resource));
                        break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EmergencyStations(fireAlarmCenters, hospitals, policeStations);
    }

    public List<FireAlarmCenter> getFireAlarmCenters() {
        return fireAlarmCenters;
    }

    public List<Hospital> getHospitals() {
        return hospitals;
    }

    public List<PoliceStation> getPoliceStations() {
        return policeStations;
    }
}
