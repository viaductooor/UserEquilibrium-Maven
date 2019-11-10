package org.lab1505.ue.fileutil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Json Format of trips: {origin:[{destination:demand},...]}
 */

public class TntpJsonConvertor {
    private static final Logger logger = Logger.getLogger(TntpJsonConvertor.class.getName());

    public static String tripsTntp2Json(String tripFile, String outputFile) {
        String jsonStr = tripsTntp2Json(tripFile);
        try (FileWriter fw = new FileWriter(outputFile)) {
            fw.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String tripsTntp2Json(String tripFile) {
        JSONObject mainObject = new JSONObject();
        FileInputStream fis = null;
        BufferedReader reader = null;
        String line = "";
        Matcher m = null;

        logger.info("Start converting Tntp file to Json file...");

        try {
            fis = new FileInputStream(tripFile);
            reader = new BufferedReader(new InputStreamReader(fis));
            boolean isTrip = false;
            int origin = -1;
            int destination;
            double demand;
            JSONArray dests = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Origin")) {
                    isTrip = true;
                    Pattern pOrigin = Pattern.compile("Origin\\s+(\\d+)");
                    if ((m = pOrigin.matcher(line)).find()) {
                        if (origin > 0) {
                            // Write the previous origin down
                            mainObject.put(origin, dests);
                        }
                        // Another origin is recognized
                        origin = Integer.parseInt(m.group(1));
                        dests = new JSONArray();
                    }
                } else if (isTrip) {
                    Pattern pItem = Pattern.compile("\\s*(\\d+)\\s*:\\s+(\\S+);");
                    m = pItem.matcher(line);
                    while (m.find()) {
                        destination = Integer.parseInt(m.group(1));
                        demand = Double.parseDouble(m.group(2));
                        if (origin != destination) {
                            JSONObject tripObject = new JSONObject();
                            tripObject.put(destination, demand);
                            dests.add(tripObject);
                        }
                    }
                }
            }
            // Write the last origin down
            mainObject.put(origin, dests);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String res = mainObject.toJSONString();

        logger.info("The Conversion is down:");
        logger.info(res);
        return res;
    }
}
