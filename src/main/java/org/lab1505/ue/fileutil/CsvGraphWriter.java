package org.lab1505.ue.fileutil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVWriter;

import org.jgrapht.Graph;

/**
 * CsvGraphWriter is a class that can write any object of {@link Graph} into a
 * local csv file.
 */
public class CsvGraphWriter {
    /**
     * Write a {@link Graph} to a local file. The method utilizes every field of
     * edgeType.
     * 
     * @param <V>      type of vertex
     * @param <E>      type of edge
     * @param graph    the graph to write
     * @param edgeType Type of edge, should be the same with E
     * @param url      the local file directory to write the graph
     */
    public static <V, E> void writeTo(Graph<V, E> graph, Class<? extends E> edgeType, String filename) {
        File file = FileDirectoryGenerator.createDefaultFile(filename);

        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, Integer> keymap = null;
        for (E edge : graph.edgeSet()) {
            Field[] originalFields = edgeType.getDeclaredFields();
            ArrayList<Field> fields = new ArrayList<>();
            for(Field f:originalFields){
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                Ignore ignore = f.getDeclaredAnnotation(Ignore.class);
                if(ignore==null){
                    fields.add(f);
                }
            }
            
            /**
             * write header
             */
            if (keymap == null) {
                keymap = new HashMap<String, Integer>();
                int i = 2;

                for (Field f : fields) {
                    keymap.put(f.getName(), i++);
                }

                keymap.put("source_vertex", 0);
                keymap.put("target_vertex", 1);

                String[] headarr = new String[keymap.size()];
                for (Map.Entry<String, Integer> entry : keymap.entrySet()) {
                    headarr[entry.getValue()] = entry.getKey();
                }

                writer.writeNext(headarr);
            }

            /**
             * write content
             */
            String[] contentarr = new String[keymap.size()];
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            contentarr[0] = source.toString();
            contentarr[1] = target.toString();

            for (Field f : fields) {
                String fieldName = f.getName();
                String fieldValue = "N/A";
                try {
                    fieldValue = f.get(edge).toString();
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                int index = keymap.get(fieldName);
                contentarr[index] = fieldValue;
            }

            writer.writeNext(contentarr);
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}