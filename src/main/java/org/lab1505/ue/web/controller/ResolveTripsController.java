package org.lab1505.ue.web.controller;

import org.lab1505.ue.alg.chongqing.CMapTrip;
import org.lab1505.ue.alg.chongqing.ChongqingMapResolver;
import org.lab1505.ue.alg.chongqing.ChongqingTaxiGpsProcessor;
import org.lab1505.ue.alg.chongqing.ChongqingTaxiGpsResolver;
import org.lab1505.ue.web.result.CodeMsg;
import org.lab1505.ue.web.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Controller
public class ResolveTripsController {
    @PostMapping("resolvetrips/resolve")
    @ResponseBody
    public Result<String> resolveTrips(@RequestParam(value = "gpsFiles[]", required = false) MultipartFile[] gpsRecordsFiles,
                                       @RequestParam(value = "nodeFile", required = false) MultipartFile nodeFile,
                                       HttpServletResponse response) {
        if (gpsRecordsFiles == null || nodeFile == null) {
            return Result.error(CodeMsg.ERROR);
        }
        if (gpsRecordsFiles.length < 1) {
            return Result.error(CodeMsg.ERROR);
        }

        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=result.csv");

        InputStream nodeInputStream = null;
        InputStream[] gpsRecordInputStreams = new InputStream[gpsRecordsFiles.length];
        OutputStream outputStream = null;

        try {

            for (int i = 0; i < gpsRecordsFiles.length; i++) {
                gpsRecordInputStreams[i] = gpsRecordsFiles[i].getInputStream();
            }

            nodeInputStream = nodeFile.getInputStream();
            outputStream = response.getOutputStream();

            List<ChongqingTaxiGpsResolver.TaxiGpsRecord> records = ChongqingTaxiGpsResolver.readTaxiRecords(gpsRecordInputStreams);
            List<ChongqingMapResolver.ChongqingMapNode> nodes = ChongqingMapResolver.readNodes(nodeInputStream);

            List<CMapTrip> taxiTrips = ChongqingTaxiGpsProcessor.getTaxiTrips(records, nodes);
            ChongqingTaxiGpsProcessor.writeTaxiTrips(outputStream, taxiTrips);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.ERROR);
        } finally {

            try {

                for (InputStream is : gpsRecordInputStreams) {
                    is.close();
                }

                nodeInputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Result.success("success");
    }
}
