package org.lab1505.ue.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.ue.exception.EdgeNotFoundException;
import org.lab1505.ue.exception.GraphIoException;

/**
 * An implementation of {@link Graph}. Since every element of this graph is
 * String(type of Vertex) or composition of String(type of Edge is
 * HashMap<String,String>), it cannot directly be used in most graph
 * algormthms(shortest-path etc.). A transformation from TranspNet to other
 * graph is essential when you need to do some calculation. The main purpose of
 * this class if to store graph information from files without specify the types.
 * 
 */
public class StringMapGraph extends SimpleDirectedGraph<String, StringMapEdge> {

    private static final long serialVersionUID = 1L;

    public StringMapGraph(){
        super(StringMapEdge.class);
    }
    
    /**
     * Add a specific attribute to the graph in the (key,value) form. Throw
     * EdgeNotFoundException if the edge does not exist.
     * 
     * @param initNode  begin node
     * @param endNode   end node
     * @param attrName  key of the attribute
     * @param attrValue value of the attribute
     * 
     * @exception EdgeNotFoundException
     */
    public void addAttr(String initNode, String endNode, String attrName, String attrValue)
            throws EdgeNotFoundException {
        if (this.containsEdge(initNode, endNode)) {
            StringMapEdge sme = this.getEdge(initNode, endNode);
            if (sme == null) {
                sme = new StringMapEdge();
            }
            sme.put(attrName, attrValue);
        } else {
            throw new EdgeNotFoundException("The edge " + initNode + "->" + endNode + "cannot be found in this graph.");
        }
    }

    /**
     * Add a specific attribute to the graph in the (key,value) form. Different from
     * {@link #addAttr} if the edge does not exist, Create the edge and add it to
     * the map.
     * 
     * @param initNode  begin node
     * @param endNode   end node
     * @param attrName  key of the attribute
     * @param attrValue value of the attribute
     * 
     * @exception EdgeNotFoundException
     */
    public void addAttrAnyway(String initNode, String endNode, String attrName, String attrValue) {
        StringMapEdge edge = null;
        if(!containsEdge(initNode, endNode)){
            edge = new StringMapEdge();
            addEdge(initNode, endNode);
        }
        edge = getEdge(initNode, endNode);
        edge.put(attrName, attrValue);
    }

    /**
     * Init the TranspNet with data from a csv file. Notice that the first two
     * columns of the file should be initNodes and endNodes.
     * 
     * @param url
     * @param attrs          array of attribute names
     * @param omitLineNumber
     * @throws GraphIOException
     */
    public void initFromCsv(String url, String[] attrs, int omitLineNumber) throws GraphIoException {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(new File(url)));
            int n = 0;
            while (n++ < omitLineNumber) {
                reader.readNext();
            }
            String[] strs = null;
            while ((strs = reader.readNext()) != null) {
                if (attrs.length != strs.length - 2) {
                    reader.close();
                    throw new GraphIoException("Number of attributes is not compatible. \n"
                            + "Check argument attrs and the corresponding parts in the csv file.");
                }
                StringMapEdge edge = new StringMapEdge();
                for (int i = 0; i < attrs.length; i++) {
                    edge.put(attrs[i], strs[i + 2]);
                }
                addEdge(strs[0], strs[1],edge);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add several attributes from a csv file. Each line of the csv file (except the
     * first line if you set omitLineNumber = 1) should be composed of (initNodeId,
     * endNodeId, attrToAdd_1,attrToAdd_2,...).
     * 
     * @param url
     * @param attrs
     * @param omitLineNumber
     * @throws GraphIOException
     */
    public void addAttrsFromCsv(String url, String[] attrs, int omitLineNumber) throws GraphIoException {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(new File(url)));
            int n = 0;
            while (n++ < omitLineNumber) {
                reader.readNext();
            }
            String[] strs = null;
            while ((strs = reader.readNext()) != null) {
                if (attrs.length != strs.length - 2) {
                    reader.close();
                    throw new GraphIoException("Number of attributes is not compatible. \n"
                            + "Check argument attrs and the corresponding parts in the csv file.");
                }
                StringMapEdge edge = null;
                if (!this.containsEdge(strs[0], strs[1])) {
                    edge = new StringMapEdge();
                } else {
                    edge = this.getEdge(strs[0], strs[1]);
                    if (edge == null) {
                        edge = new StringMapEdge();
                    }
                    for (int i = 0; i < attrs.length; i++) {
                        edge.put(attrs[i], strs[i + 2]);
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a single attribute from a csv file. Each line of the csv file (except the
     * first line if you set omitLineNumber = 1) should be composed of (initNodeId,
     * endNodeId, attrToAdd).
     * 
     * @param url
     * @param attr
     * @param omitLineNumber
     * @throws GraphIOException
     */
    public void addAttrFromCsv(String url, String attr, int omitLineNumber) throws GraphIoException {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(new File(url)));
            int n = 0;
            while (n++ < omitLineNumber) {
                reader.readNext();
            }
            String[] strs = null;
            while ((strs = reader.readNext()) != null) {
                if (strs.length != 3) {
                    reader.close();
                    throw new GraphIoException("Every row of the csv file has to contain three items, "
                            + "greater or less than three is not allowed");
                }
                StringMapEdge edge = null;
                if (!this.containsEdge(strs[0], strs[1])) {
                    edge = new StringMapEdge();
                    addEdge(strs[0],strs[1],edge);
                } else {
                    edge = this.getEdge(strs[0], strs[1]);
                }
                edge.put(attr, strs[2]);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the StringMapGraph to a csv file.
     * 
     * @param url the file to write the graph.
     * 
     * @throws GraphIoException When the graph contains no edge.
     */
    public void writeToCsv(String url) throws GraphIoException {
        File file = new File(url);
        File parent = new File(file.getParent());
        if (!parent.exists()) {
            parent.mkdirs();
        }

        CSVWriter writer = null;
        HashMap<String,Integer> keymap = null;
        
        try {
            writer = new CSVWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (edgeSet().size() < 1) {
            throw new GraphIoException("The graph is empty thus cannot be written into a csv file.");
        }

        // write header
        for (StringMapEdge edge:edgeSet()){
            if(keymap == null){
                keymap = new HashMap<String,Integer>();
                int index = 2;
                for(String key:edge.keys()){
                    keymap.put(key, index ++);
                }
                keymap.put("begin", 0);
                keymap.put("end",1);
                
                String[] headarr = new String[keymap.size()];
                for(Map.Entry<String,Integer> entry:keymap.entrySet()){
                    headarr[entry.getValue()] = entry.getKey();
                }
                writer.writeNext(headarr);
            }
            //write content
            String source = getEdgeSource(edge);
            String target = getEdgeTarget(edge);
            String[] valuearr =new String[keymap.size()];
            for(String attrKey:edge.keys()){
                String attrValue = edge.get(attrKey);
                int index = keymap.get(attrKey);
                valuearr[index] = attrValue;
            }
            valuearr[0] = source;
            valuearr[1] = target;
            writer.writeNext(valuearr);
            
        }

        try {
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}