/*
 * Name: Ravitej Urikiti

 * Ref: http://www.programcreek.com/2014/02/k-means-clustering-in-java/
 */


import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.clusterers.SimpleKMeans;

import java.io.BufferedReader;
import java.io.FileReader;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



public class wekacluster extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public wekacluster() {
		super();

	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String input_file = null;
		PrintWriter disp=response.getWriter();
		try {
			List<FileItem> fields = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			String clusters = null;
			// Fetching the values from the jsp file
			for (FileItem field : fields) {
				if (field.isFormField()) {
					String fieldName = field.getFieldName();
					String fieldValue = field.getString();
					clusters=fieldValue;
					if(fieldName.equals("clusters"))
					{
						clusters=fieldValue;
					}
				} else {
					input_file = FilenameUtils.getName(field.getName());
					InputStream file_content = field.getInputStream();
					File get_file = new File(input_file);
					FileUtils.copyInputStreamToFile(file_content,get_file);
				}
			}
			//Getting the data from CSV file 
			CSVLoader load_file = new CSVLoader();
			load_file.setSource(new File(input_file));
			Instances instan = load_file.getDataSet();
			String[] records=input_file.split("\\.");

			ArffSaver arff_saver = new ArffSaver();
			arff_saver.setInstances(instan);
			arff_saver.setFile(new File(input_file));
			arff_saver.setDestination(new File(records[0]+".arff"));
			arff_saver.writeBatch();

			//Start Kmeans clustering
			SimpleKMeans simplekmeans = new SimpleKMeans();
			//Assigning attributes
			simplekmeans.setSeed(10);
			simplekmeans.setPreserveInstancesOrder(true);
			simplekmeans.setNumClusters(Integer.parseInt(clusters));

			BufferedReader buff_file = readDataFile(records[0]+".arff"); 
			Instances data_instance = new Instances(buff_file);
			ArrayList<Double> avgtemp=new ArrayList<Double>();
			ArrayList<Double> avgprecp=new ArrayList<Double>();

			//Fetching the data
			for(int i=0;i<data_instance.numInstances();i++)
			{
				System.out.println(data_instance.instance(i).value(1));
				avgtemp.add(data_instance.instance(i).value(1));
				avgprecp.add(data_instance.instance(i).value(2));
			}

			//Build the cluster
			int i=0;
			simplekmeans.buildClusterer(data_instance);
			
			// This array returns the cluster number (starting with 0) for each instance. The array has as many elements as the number of instances
			int[] values = simplekmeans.getAssignments();
			JsonArray json_arr=new JsonArray();
			
			for(int totclusters : values) {
				JsonObject cluster_data=new JsonObject();
				cluster_data.addProperty("temp", avgtemp.get(i));
				cluster_data.addProperty("precp", avgprecp.get(i));
				cluster_data.addProperty("totclusters", totclusters);
				json_arr.add(cluster_data);	   	
				i++;
			}

			System.out.println(json_arr);
			request.setAttribute("jcluster", json_arr);
			request.setAttribute("clusters", clusters);

			//Redirect to showcluster jsp page and show visualisation of clusters
			RequestDispatcher dispatch=request.getRequestDispatcher("display.jsp");
			dispatch.forward(request, response);

		}
		catch(Exception e)
		{
			disp.println(input_file);
			disp.println(e);
			disp.print(e.getStackTrace());
		}

	}	

	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;

		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}

		return inputReader;
	}

}
