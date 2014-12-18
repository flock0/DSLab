package computation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;

import model.ComputationRequestInfo;

public class LogResult extends Result {
	
	private List<ComputationRequestInfo> logs;
	public LogResult(ResultStatus status, List<ComputationRequestInfo> logs) {
		super(status);
		this.logs = logs;
	}
	
	@Override
	public String toString() {
		try
		{
			try (ByteArrayOutputStream s = new ByteArrayOutputStream())
			{
				try (ObjectOutputStream os = new ObjectOutputStream(s)) 
				{					
					os.writeObject(logs);
					os.flush();
					s.flush();
					return "!logs " + new String(Base64.encode(s.toByteArray()));					
				}
			}
		}	
		catch(IOException e)
		{
			throw new RuntimeException("IOException during toString", e);
		}			
	}		
	
	public List<ComputationRequestInfo> getLogs() {
		return logs;
	}

	@SuppressWarnings("unchecked")
	public static LogResult fromString(String in) {
		if(in == null)
			return new LogResult(ResultStatus.Error, null);
		
		String[] split = in.split("\\s");
		if(split.length != 2) //Should be !logs <BASE64 encoded List<ComputationRequestInfo>>
			return new LogResult(ResultStatus.Error, null);
		
		List<ComputationRequestInfo> logs = null;
		try
		{
			
			try (ByteArrayInputStream s = new ByteArrayInputStream(Base64.decode(split[1].getBytes())))
			{
				try (ObjectInputStream is = new ObjectInputStream(s)) 
				{		
					try
					{
						logs = (List<ComputationRequestInfo>)is.readObject();
					}
				 	catch (Exception e) { 
				 		//To catch the Exception, if is.readObject is not of the type List<ComputationRequestInfo>
				 		//Would be better to check, but that is difficult due to generics
				 		return new LogResult(ResultStatus.Error, null);
				    }														
				}
			}
		}	
		catch(IOException e)
		{
			throw new RuntimeException("IOException during toString", e);
		}		
		
		ResultStatus status = ResultStatus.OK;					
		return new LogResult(status, logs);
	}	
}
