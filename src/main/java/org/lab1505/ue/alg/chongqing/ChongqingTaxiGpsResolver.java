package org.lab1505.ue.alg.chongqing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ChongqingTaxiGpsResolver {
    /**
     * Simply import original taxi gps data
     *
     * @param inputStreams A series of files from which to read data
     * @return taxi gps record entries
     */
    public static List<TaxiGpsRecord> readTaxiRecords(InputStream[] inputStreams) {
        List<TaxiGpsRecord> list = new ArrayList<>();
        for (InputStream is : inputStreams) {
            try (InputStreamReader reader = new InputStreamReader(is, "GB2312");
                 BufferedReader bf = new BufferedReader(reader)) {
                String line = null;
                while ((line = bf.readLine()) != null) {
                    TaxiGpsRecord entry = resolveAnEntry(line);
                    if (isTaxiEntryValid(entry)) {
                        // process
                        list.add(entry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static TaxiGpsRecord resolveAnEntry(String entry) {
        String[] items = entry.split(",");
        String date = items[0];
        String time = items[1];
        String useless = items[2];
        String car_id = items[3];
        double lon = Double.parseDouble(items[4]);
        double lat = Double.parseDouble(items[5]);
        double speed = Double.parseDouble(items[6]);
        double angle = Double.parseDouble(items[7]);
        int state = Integer.parseInt(items[8]);
        boolean valid = !Boolean.parseBoolean(items[9]);
        return new TaxiGpsRecord(date, time, useless, car_id, lon, lat, speed, angle, state, valid);
    }

    private static boolean isTaxiEntryValid(TaxiGpsRecord entry) {
        return !(entry.lat < 20) && !(entry.lon < 100) && entry.valid != false;
    }

    public static class TaxiGpsRecord {
        public String date;
        public String time;
        public String useless;
        public String car_id;
        public double lon;
        public double lat;
        public double speed;
        public double angle;
        public int state;
        public boolean valid;

        public TaxiGpsRecord(String date, String time, String useless, String car_id, double lon, double lat, double speed, double angle, int state, boolean valid) {
            this.date = date;
            this.time = time;
            this.useless = useless;
            this.car_id = car_id;
            this.lon = lon;
            this.lat = lat;
            this.speed = speed;
            this.angle = angle;
            this.state = state;
            this.valid = valid;
        }

        @Override
        public String toString() {
            return "TaxiEntry{" +
                    "date='" + date + '\'' +
                    ", time='" + time + '\'' +
                    ", useless='" + useless + '\'' +
                    ", car_id='" + car_id + '\'' +
                    ", lon=" + lon +
                    ", lat=" + lat +
                    ", speed=" + speed +
                    ", angle=" + angle +
                    ", state=" + state +
                    ", valid=" + valid +
                    '}';
        }
    }

//    public static void main(String[] args) throws FileNotFoundException {
//        String[] urls = new String[]{
//          "20170328_097.TXT",
//          "20170328_098.TXT",
//          "20170328_099.TXT",
//          "20170328_100.TXT",
//          "20170328_101.TXT",
//          "20170328_102.TXT",
//          "20170328_103.TXT",
//          "20170328_104.TXT",
//          "20170328_105.TXT",
//          "20170328_106.TXT",
//          "20170328_107.TXT",
//          "20170328_108.TXT",
//        };
//        InputStream[] inputStreams = new InputStream[urls.length];
//        for(int i=0;i<urls.length;i++){
//            inputStreams[i] = new FileInputStream("files/chongqing/gps/"+urls[i]);
//        }
//        List<TaxiEntry> list = readTaxiEntries(inputStreams);
//        for(TaxiEntry e:list){
//            System.out.println(e);
//        }
//    }
}
