package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import computation.ComputationResult;
import computation.NodeRequest;

public class NodeLogger {

	public static String NodeID = "";
	public static String Directory = "";
	
	/**
	 * Logs the computation. nodeID and Directory must be set
	 * @param request The request that should be logged
	 * @param result The result from the request
	 */
	public static void log(NodeRequest request, ComputationResult result) {
		String logLine1 = String.format("%d %c %d", request.getOperand1(), request.getOperator(), request.getOperand2());
		String logLine2 = result.toLogString();
		
		String fileName = String.format("%s_%s.log", DateUtils.formatDate(new Date()), NodeID);
		String directoryPath = System.getProperty("user.dir") + File.separator + Directory;
		directoryPath = directoryPath.replace("/", File.separator);
		String filePath = directoryPath + File.separator + fileName;
		
		PrintWriter writer = null;
		try {
			File dir = new File(directoryPath);
			dir.mkdirs();
			File f = new File(filePath);
			writer = new PrintWriter(f);
			writer.println(logLine1);
			writer.println(logLine2);
		} catch (FileNotFoundException e) {
			// Nothing we can do about that
		} finally {
			if(writer != null)
				writer.close();
		}
	}
}
