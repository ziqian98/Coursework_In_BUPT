import java.io.*;

	public class GetConsumer {
		
		private String oneLine;
		private String [] userInfo=new String[100];  
		private int i=1;   
		
		 /**
	     * This method is used to initialize all the variables
	     */
	  
		public void initializer()
		{
			oneLine="";
			for (int j=0; j<100; j++)
			{
				userInfo[j]="";
			}
			i=1;
		}
		
		 /**
	     * This method is used to get the id number of all consumers
	     * @return an array of String type that stores the id number of each consumer
	     */
	 
		
		public String[] getconsumer()
		{
			
			initializer();  
			try{
				FileReader fileReader=new FileReader("all.txt");
				BufferedReader bufferedReader=new BufferedReader(fileReader);
				while((oneLine = bufferedReader.readLine())!=null)
				{
					if(oneLine.split(" ").length==3) 
					{
						userInfo[i]=oneLine.split(" ")[0];  
						i++;
					}	
				}
				userInfo[0]=i-1+"";
				bufferedReader.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			return userInfo ;
		}
}
