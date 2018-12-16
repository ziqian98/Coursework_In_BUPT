import java.io.*;

public class LastLineOperation {
	
	private String[] eachLine=new String[1000];  
	private int i=0;
	private String oneLine="";  
	private String lastLine="";
	
	 /**
     * This method is used to initialized all the variables in the class
     */

	public void initializer()
	{
		for(int j=0; j<1000; j++)
		{
			eachLine[j]="";
		}
		
		oneLine="";
		lastLine="";
		i=0;
	}
	
	 /**
     * The method provides the interface for customer to deposit money.
     * @param line used to replace the last line of specified file
     * @param filePath the path of related file
     */
	
    public void coverFileLastLine(String line, String filePath){  
 
    	try{
    		FileReader fileReader=new FileReader(filePath);
    		BufferedReader bufferedReader=new BufferedReader(fileReader);
    		
    		initializer();
    		
    		
    		while( (oneLine = bufferedReader.readLine()) != null) {
				
    			oneLine=oneLine.concat("\r\n");
    			eachLine[i]=oneLine;
    					i++;
			}

    		eachLine[i-1]=line; 
			FileWriter fileWriter=new FileWriter(filePath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

    		
    		for(int j=0; j<=i-1; j++)
    		{

    			bufferedWriter.write(eachLine[j]);  
    			
    		}
    		
    		bufferedWriter.close();
    		bufferedReader.close();
    	}
    	
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	
   
    } 
    
    /**
     * This method gets the last line of a specified file
     * @param filePath the path of related file
     * @return the last line of the specified file
     */
	    
    public String getFileLastLine(String filePath){ 
        RandomAccessFile raf; 
        lastLine = ""; 
        try { 
            raf = new RandomAccessFile(filePath, "r"); 
            long len = raf.length(); 
            if (len != 0L) { 
  
              long pos = len - 1; 
              raf.seek(pos);
  
              while (pos > 0) {  
                pos--; 
                raf.seek(pos);  
                
                if (raf.readByte() == '\n') {    
                  lastLine = raf.readLine(); 
                  break; 
                } 
                
                if(pos==0)
                {
               	lastLine=raf.readLine();
                }
              }
            } 
            raf.close(); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return lastLine; 
    }
    

}
