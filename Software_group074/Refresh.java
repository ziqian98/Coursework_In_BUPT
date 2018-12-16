import java.io.*;
import java.math.BigDecimal;
import java.text.*;
import java.util.*;

public class Refresh {
	
	private float dailyEle=0.0f;      
	private float dailyGas=0.0f;
	private String oneLine = "";
	private String[] parser=new String[10];
	private String[]  userInformation = new String[100];
	private String infofile = "", ebufferfile = "", gbufferfile = "";  
	private boolean flag=false;
	
    /**
     * The method initializes all the variables in the class
     */
	
	public void initializer()
	{
		dailyEle=0.0f; 
		dailyGas=0.0f;;
		oneLine = "";
		infofile = "";
		ebufferfile = "";
		gbufferfile = "";
		
		for(int k=0; k<100; k++)
		{
			userInformation[k]="";
		}
		
		for(int i=0; i<10; i++)
		{
			parser[i]="";
		}
		
	}
    /**
     * The method is responsible for refreshing the use of electricity and gas of each consumer 
     * and storing the daily usage into disk at the end of a day
     */
	
	public void fresh()
	{	
		LastLineOperation llo = new LastLineOperation();

		initializer();
		
		GetConsumer GC=new GetConsumer();

		while(true)     
		{
			for(int i=0;;i++)  
			{
				try{	
						Thread.sleep(3000);
					}
				catch(InterruptedException e){
					e.printStackTrace();
				}
				

				try{
					if(i==0&&flag==false){  

						for(int k=0; k<100; k++)
						{
							userInformation[k]="";
						}
						userInformation=GC.getconsumer();  
						for(int j=1; j<=Integer.parseInt(userInformation[0]); j++)
						{
							if(userInformation[j].equals("p") == false) {								
							ebufferfile = "consumerinfo/"+ userInformation[j] + "/" + userInformation[j]+"electricity.txt";  //将读取到的用户ID生成对应的用户文件路径
							gbufferfile = "consumerinfo/"+ userInformation[j] + "/" + userInformation[j]+"gas.txt";
							infofile = "consumerinfo/"+ userInformation[j] + "/" + userInformation[j]+".txt";
							
							FileReader fileReader=new FileReader(ebufferfile);
							BufferedReader bufferedReader=new BufferedReader(fileReader);
							oneLine = bufferedReader.readLine();
							dailyEle=Float.parseFloat(oneLine); 
							bufferedReader.close();
							
							FileReader fileReader0=new FileReader(gbufferfile);
							BufferedReader bufferedReader0=new BufferedReader(fileReader0);
							oneLine = bufferedReader0.readLine();
							dailyGas=Float.parseFloat(oneLine); 
							bufferedReader0.close();	
							}
						}
						
					}

				}
				
		    	catch(IOException e){
					e.printStackTrace();
				}			
				
				dailyEle=dailyEle+0.6f;

				dailyGas=dailyGas+0.9f;
				
				try{
					for(int k=0; k<100; k++)
					{
						userInformation[k]="";
					}
					
					userInformation=GC.getconsumer();  
					
					for(int j=1; j<=Integer.parseInt(userInformation[0]); j++)
					{
						if(userInformation[j].equals("p") == false) {
							ebufferfile = "consumerinfo/"+ userInformation[j] + "/" + userInformation[j]+"electricity.txt";  //将读取到的用户ID生成对应的用户文件路径
							FileWriter fileWriter=new FileWriter(ebufferfile);          
							BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);
							bufferedWriter.write(dailyEle+"");		
							bufferedWriter.close();
						}
					}
				}
				catch(IOException e){
		    		e.printStackTrace();
				}
				
				
				if((i!=0)&&(i%10==0))
				{
					
					for(int k=0; k<100; k++)
					{
						userInformation[k]="";
					}
					
					userInformation=GC.getconsumer(); 
					
					try{
						for(int j=1; j<=Integer.parseInt(userInformation[0]); j++)
						{
							if(userInformation[j].equals("p") == false) {
								gbufferfile = "consumerinfo/"+ userInformation[j] + "/" + userInformation[j]+"gas.txt";
								FileWriter fileWriter=new FileWriter(gbufferfile);
								BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);
								bufferedWriter.write(dailyGas+"");			
								bufferedWriter.close();
							}
						}
					}
			    	catch(IOException e){
			    		e.printStackTrace();
					}
					
				}
				
				int time=Integer.parseInt((new SimpleDateFormat("HHmmss")).format(Calendar.getInstance().getTime())+"");
				
				if(235955<time&&time<235959&&i>5) 
				{			
					
					for(int k=0; k<100; k++)
					{
						userInformation[k]="";
					}
					
					userInformation=GC.getconsumer(); 
					
					
					try{
						for(int j=1; j<=Integer.parseInt(userInformation[0]); j++)
						{
							if(userInformation[j].equals("p") == false) {
						
								infofile = "consumerinfo/"+ userInformation[j] + "/" + userInformation[j]+".txt";
								oneLine=llo.getFileLastLine(infofile);  
								parser=oneLine.split(" ");

								float totalelec=dailyEle+Float.parseFloat(parser[5]);
								float totalgas=dailyGas+Float.parseFloat(parser[6]);
								
								parser[0]=(new SimpleDateFormat("yyyy")).format(Calendar.getInstance().getTime())+"";
								parser[1]=(new SimpleDateFormat("MM")).format(Calendar.getInstance().getTime())+"";
								parser[2]=(new SimpleDateFormat("dd")).format(Calendar.getInstance().getTime())+"";
								oneLine="\r\n"+parser[0]+" "+parser[1]+" "+parser[2]+" "+twofloat(dailyEle)+" "+twofloat(dailyGas)
										+" "+twofloat(totalelec)+" "+twofloat(totalgas)+" "+parser[7];
								
								BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
										new FileOutputStream(infofile, true)));

								bufferedWriter.write(oneLine);
						
								bufferedWriter.close();
	
							}
						}
						
						
			    	}
			    	catch(IOException e){
			    		e.printStackTrace();
					}
					
					dailyEle=0.0f;
					dailyGas=0.0f;  
					flag=true;  
					break;  
				}
			}
		}
	}
	
    /**
     * The method receives a float variable and returns a double variable
     * @param f the input float variable
     * @return a double format
     */
	public double twofloat(float f) {
		BigDecimal bg = new BigDecimal(f);
		double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return f1;
	}

}
