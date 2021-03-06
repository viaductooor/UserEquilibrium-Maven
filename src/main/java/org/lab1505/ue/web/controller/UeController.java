package org.lab1505.ue.web.controller;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.alg.UserEquilibrium;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.entity.UeLinkEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.TntpReader;
import org.lab1505.ue.web.result.CodeMsg;
import org.lab1505.ue.web.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
public class UeController {
    @PostMapping("/ue/run")
    @ResponseBody
    public Result<String> uploadLinksFile(@RequestParam(value = "linksFile", required = false) MultipartFile linksFile,
                                          @RequestParam(value = "tripsFile", required = false) MultipartFile tripsFile,
                                          HttpServletResponse response,
                                          HttpServletRequest request) {


        System.out.println("Receiving a request!");
        if (linksFile == null || tripsFile == null) {
            return Result.error(CodeMsg.ERROR);
        }

        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=result.csv");

        try (InputStream linksStream = linksFile.getInputStream();
             InputStream tripsStream = tripsFile.getInputStream();
             OutputStream outputStream = response.getOutputStream()) {

            // Run Algorithm
            SimpleDirectedGraph<Integer, LinkEdge> originalNet = TntpReader.readNet(linksStream);
            SimpleDirectedWeightedGraph<Integer, UeLinkEdge> net = UserEquilibrium.fromLinkEdgeGraph(originalNet);
            SimpleDirectedGraph<Integer, DemandEdge> trips = TntpReader.readTrips(tripsStream);
            UserEquilibrium<Integer, DemandEdge, UeLinkEdge> ueq = new UserEquilibrium<>(net, trips);
            ueq.assign(50);
            CsvGraphWriter.writeTo(net, UeLinkEdge.class, outputStream);

        } catch (IOException e) {

            return Result.error(CodeMsg.ERROR);
        }
        return Result.success("resources/tmp/result.csv");
    }
}
