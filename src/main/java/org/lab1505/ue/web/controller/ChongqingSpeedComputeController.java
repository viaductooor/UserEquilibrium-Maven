package org.lab1505.ue.web.controller;

import org.lab1505.ue.alg.chongqing.ChongqingTaxiGpsProcessor;
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

@Controller
public class ChongqingSpeedComputeController {
    @PostMapping("/compute_speed/run")
    @ResponseBody
    public Result<String> computeSpeed(@RequestParam("gps_files") MultipartFile[] gpsFiles,
                                       @RequestParam("nodes_file") MultipartFile nodesFile,
                                       HttpServletResponse response) {
        InputStream[] gpsStreams = new InputStream[gpsFiles.length];
        InputStream nodesStream = null;
        OutputStream outputStream = null;
        try {
            for (int i = 0; i < gpsFiles.length; i++) {
                gpsStreams[i] = gpsFiles[i].getInputStream();
            }
            outputStream = response.getOutputStream();
            nodesStream = nodesFile.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=result.csv");
        ChongqingTaxiGpsProcessor.computeAndWriteNodeAvgSpeed(gpsStreams, nodesStream, outputStream);
        return Result.success("success");
    }
}
