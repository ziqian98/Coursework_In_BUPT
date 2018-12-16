import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;  
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

	public class Provider extends JFrame {
		
		 /**
	     * The method changes the tariff in the file system
	     * @param str1 the tariff in the String format
	     * @param path1 path of related tariff file
	     */

		public void changetariff(String str1,String path1) {
			try {
				FileWriter fw = new FileWriter(path1);
				BufferedWriter bfw = new BufferedWriter(fw);
				bfw.write(str1);
				bfw.close();
				fw.close();
			}catch(IOException e) {
				System.out.print("Error occured in changetariff");
				System.exit(1);
			}
		}
		
		 /**
	     * The method changes the tariff in the file system
	     * @param str id number in the String format
	     * @return the check result in a boolean form
	     */
		
		
		public boolean checkidinput(String str) {
			boolean checkresult = true;
			String oneline111 = "";
			try {
				FileReader fReader = new FileReader("all.txt");
				BufferedReader bufferedReader = new BufferedReader(fReader);
				for(oneline111 = bufferedReader.readLine();oneline111!=null;oneline111 = bufferedReader.readLine()) {
					String[] userinfo = oneline111.split(" ");
					if(str.equals(userinfo[0])) {
						checkresult = false;
					}
				}
				bufferedReader.close();
				fReader.close();
			}
			catch (Exception e) {
				System.out.println("error in prov.checkidinput!");
			}
			return checkresult;
		}

		 /**
	     * The method reads the current electricity tariff number
	     * @return the electricity tariff
	     */
		public String currenttariffelec(){
			String electariff = "";
			try {
				FileReader fr = new FileReader("elec_tariff.txt");
				BufferedReader bfr = new BufferedReader(fr);
				electariff  = bfr.readLine();
				bfr.close();
				fr.close();
			}catch(IOException e) {
				System.out.print("Error occured in currenttariffelec");
				System.exit(1);
			}
			return electariff;
		}
		
		 /**
	     * The method reads the current gas tariff number
	     * @return the gas tariff
	     */

		public String currenttariffgas() {
			String gastariff = "";
			try{
				FileReader fr1 = new FileReader("gas_tariff.txt");
				BufferedReader bfr1 = new BufferedReader(fr1);
				gastariff = bfr1.readLine();
				bfr1.close();
				fr1.close();
			}catch(IOException e) {
				System.out.print("Error occured in vilst()");
				System.exit(1);
			}
			return gastariff;
		}


		 /**
	     * The method adds a new consumer's information in the file and creates his own folder and files
	     * @param arg0 id of the consumer
	     * @param arg1 password of the consumer
	     * @param arg2 name of the consumer
	     */
		public void newconsumer(String arg0,String arg1,String arg2) { 
			String inputargs = arg0 + " " +arg1+ " "+ arg2;
			String inputbillargs = arg0 + " " +arg2+ " "+ "Unsent";


			if(checkid(arg0))
			{
				System.out.println("Duplicated user name");

				JOptionPane.showMessageDialog(null , "ID already exists, please try again!","",JOptionPane.WARNING_MESSAGE);
				return;
			}

			Calendar now = Calendar.getInstance();
			try{
		
				FileWriter fw = new FileWriter("all.txt",true);
				BufferedWriter bfw = new BufferedWriter(fw);
				bfw.newLine();
				bfw.write(inputargs);
				bfw.close();
				fw.close();
	
				FileWriter fwb = new FileWriter("allbill.txt",true);
				BufferedWriter bfwb = new BufferedWriter(fwb);
				bfwb.newLine();
				bfwb.write(inputbillargs);
				bfwb.close();
				fwb.close();

				File file1 = new File("consumerinfo/"+arg0);
				if(!file1.exists()) {file1.mkdirs();}
				FileWriter filehistory = new FileWriter("consumerinfo/"+arg0+"/"+arg0+".txt");
				BufferedWriter bfw1 = new BufferedWriter(filehistory);
				bfw1.write(Integer.toString(now.get(Calendar.YEAR))+" "+Integer.toString(now.get(Calendar.MONTH)+1)+" "+Integer.toString(now.get(Calendar.DATE)-1)+" "+"0.0 0.0 0.0 0.0 13");
				bfw1.newLine();
				bfw1.write(Integer.toString(now.get(Calendar.YEAR))+" "+Integer.toString(now.get(Calendar.MONTH)+1)+" "+Integer.toString(now.get(Calendar.DATE))+" "+"0.0 0.0 0.0 0.0 13");
			
				bfw1.close();
				filehistory.close();
				
				FileWriter ebuf = new FileWriter("consumerinfo/"+arg0+ "/" + arg0 + "electricity.txt");
				BufferedWriter bfw2 = new BufferedWriter(ebuf);
				bfw2.write("0.0");
				bfw2.close();
				ebuf.close();
	
				FileWriter gbuf = new FileWriter("consumerinfo/"+arg0+ "/" + arg0 + "gas.txt");
				BufferedWriter bfw3 = new BufferedWriter(gbuf);
				bfw3.write("0.0");
				bfw3.close();
				gbuf.close();
	
				FileWriter ebudg = new FileWriter("consumerinfo/"+arg0+ "/" + arg0 + "elecbudget.txt");
				BufferedWriter bfw4 = new BufferedWriter(ebudg);
				bfw4.write("500.0");
				bfw4.close();
				ebudg.close();

				FileWriter gbudg = new FileWriter("consumerinfo/"+arg0+ "/" + arg0 + "gasbudget.txt");
				BufferedWriter bfw5 = new BufferedWriter(gbudg);
				bfw5.write("500.0");
				bfw5.close();
				gbudg.close();
				JOptionPane.showMessageDialog(null , "Information added successfully","",JOptionPane.WARNING_MESSAGE);
			}
			catch(IOException e) {
				System.out.print("error occured in newconsumer");
				System.exit(1);
			}
			
		}
		
		
		 /**
	     * The method deletes the consumer information in the file 
	     * @param cid id of consumer
	     * @param filepath path of related file
	     */
		public void removeconsumer(String cid, String filepath) {
				String oneline = "";
				FileReader fr;
				ArrayList<String> allconsumer = new ArrayList<String>();
				try {
					fr = new FileReader(filepath);
					BufferedReader bfr = new BufferedReader(fr);
					for(oneline = bfr.readLine(); oneline != null; oneline = bfr.readLine()) {
						String[] lineinfo = oneline.split(" ");
				
						if(cid.equals(lineinfo[0]) ==false) {
							allconsumer.add(lineinfo[0]);
							allconsumer.add(lineinfo[1]);
							allconsumer.add(lineinfo[2]);
						}
					}
					bfr.close();
					fr.close();

					FileWriter fw = new FileWriter(filepath);
					BufferedWriter bfw = new BufferedWriter(fw);
					String writeinfo = allconsumer.get(0) +" "+allconsumer.get(1) +" "+allconsumer.get(2);
					bfw.write(writeinfo);
					for(int x = 3; x< allconsumer.size(); x+=3) {
						writeinfo = allconsumer.get(x) +" "+allconsumer.get(x+1) +" "+allconsumer.get(x+2);
						bfw.newLine();
						bfw.write(writeinfo);
					}
					bfw.close();
					fw.close();
					
				} catch (IOException|ArrayIndexOutOfBoundsException e) {
					System.out.println("error occured in removeconsumer");
					System.exit(1);
				}
				
			}
	
		 /**
	     * The method removes consumer's folder in the file system
	     * @param selectID id of the selected consumer
	     */
			
		public void removeconsumerfile(String selectID) {
				String dir = "consumerinfo/"+ selectID;
				doDeleteEmptyDir(dir);
		        String newDir2 = dir;
		        boolean success = deleteDir(new File(newDir2));
		        if (success) {
		            System.out.println("Successfully deleted populated directory: " + newDir2);
		        }
		        else {
		            System.out.println("Failed to delete populated directory: " + newDir2);
		        }
			}
		 /**
	     * The method judges if you have delete a file successfully
	     * @param dir path of the deleted file
	     */
			
		
		public void doDeleteEmptyDir(String dir) {
	        boolean success = (new File(dir)).delete();
	        if (success) {
	            System.out.println("Successfully deleted empty directory: " + dir);
	        } else {
	            System.out.println("Failed to delete empty directory: " + dir);
	        }
	    }
		 /**
	     * The method judges if you have delete a file successfully
	     * @param dir path of the deleted file
	     * @return if delete successfully
	     */
			
		public boolean deleteDir(File dir) {
	        if (dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i=0; i<children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
	        }
	    
	        return dir.delete();
	    }
		/**
	     * The method simulates the function to send a email
	     * @param selectedcid id of the consumer selected
	     * @param filepath path of file that stores the consumers' information
	     */

		public void sendemail(String selectedcid, String filepath) {
	  		String oneline = "";
	  		ArrayList<String> allconsumer = new ArrayList<String>();
	  			try {
	  				FileReader fr = new FileReader(filepath);
	  				BufferedReader bfr = new BufferedReader(fr);
	  				for(oneline = bfr.readLine(); oneline != null; oneline = bfr.readLine()) {
	  					String[] lineinfo = oneline.split(" ");
	  				
	  					allconsumer.add(lineinfo[0]);
	  					allconsumer.add(lineinfo[1]);
	  					if(selectedcid.equals(lineinfo[0]) ==true) {
	  						allconsumer.add("Sent");
	  					}
	  					else {
	  						allconsumer.add(lineinfo[2]);
	  					}
	  				}
	  				
	  				bfr.close();
	  				fr.close();

	  				FileWriter fw = new FileWriter(filepath);
	  				BufferedWriter bfw = new BufferedWriter(fw);
	  				String writeinfo = allconsumer.get(0) +" "+allconsumer.get(1) +" "+allconsumer.get(2);
	  				bfw.write(writeinfo);
	  				for(int x = 3; x< allconsumer.size(); x+=3) {
	  					writeinfo = allconsumer.get(x) +" "+allconsumer.get(x+1) +" "+allconsumer.get(x+2);
	  					bfw.newLine();
	  					bfw.write(writeinfo);
	  				}
	  				bfw.close();
	  				fw.close();
	  			}
	  			catch (IOException e) {
	  				System.out.println("error in sendemail!");
	  			}
	  	}
		
		/**
	     * This method is used to get the id number of all consumers
	     * @return an array of String type that stores the id number of each consumer
	     */

	  	
		public String[] getconsumer()
		{
			String oneLine;
			String [] userInfo=new String[100];  
			int i=1;   
				
			oneLine="";
			for (int j=0; j<100; j++)
			{
				userInfo[j]="";
			}
			i=1;
			
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
		
		/**
	     * This method checks if the input id is valid
	     * @param id id of the consumer
	     * @return a boolean variable that marks if the input id is right
	     */

		
		public boolean checkid(String id)
		{
			boolean flag=false;
			String[] userInfo=new String[100];
			userInfo=getconsumer();
			for(int i=1; i<=Integer.parseInt(userInfo[0]);i++)
			{
				if(userInfo[i].equals(id))
				{
					flag=true;  
				}
			}
			
			return flag;
		}
		
		/**
	     * This method reads the history of the selected consumer
	     * @param id1 id of desired consumer
	     * @param date0 start date
	     * @param date1 end date
	     */

		
		public void readhistory(String id1, String date0, String date1) {
			JFrame resultpage = new JFrame();
			JPanel resultpanel = new JPanel();
			String oneline = "";
			String[] hrecord;
			String[][] datas = {};
			String[] titles = {"Year","Month","Date", "Daily Elec Usage", "Daily Gas Usage", "Total Elec Usage", "Total Gas Usage"};
			DefaultTableModel model = new DefaultTableModel(datas,titles);
			JTable htable = new JTable(model);
			htable.setPreferredScrollableViewportSize(new Dimension(720, 480));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			String filename = "consumerinfo/" + id1 + "/" + id1 + ".txt";
			try {
				Date enddate = sdf.parse(date1);
				Date begindate = sdf.parse(date0);
				if(enddate.before(begindate) == false) {
					try{
			    		FileReader fileReader=new FileReader(filename);
						BufferedReader bufferedReader=new BufferedReader(fileReader);
						for(oneline = bufferedReader.readLine();oneline != null;oneline = bufferedReader.readLine()) {
							hrecord = oneline.split(" ");
							Date linedate = sdf.parse(hrecord[0] +"-"+ hrecord[1] +"-"+hrecord[2]);
							if(linedate.before(enddate) && linedate.after(begindate) || linedate.equals(begindate) || linedate.equals(enddate)) {
								model.addRow(new String[] {hrecord[0],hrecord[1],hrecord[2],hrecord[3],hrecord[4],hrecord[5],hrecord[6]});
								
							}
						}
						bufferedReader.close();
					}
					catch(IOException e){
						System.out.println("error occured in rh");
						resultpage.dispose();
					}
				}
				else {
					JOptionPane.showMessageDialog(null , "Invalid input, please try again!","",JOptionPane.WARNING_MESSAGE);
				}
			}
			catch(ParseException e) {
				System.out.println("error occured in parse");
				JOptionPane.showMessageDialog(null , "Invalid input, please try again!","",JOptionPane.WARNING_MESSAGE);
				resultpage.dispose();

			}
			JScrollPane jspan = new JScrollPane(htable);
			resultpanel.add(jspan);
			resultpage.setTitle("Search result");
			resultpage.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			resultpage.setLayout(new BorderLayout()); 
			resultpage.add(resultpanel);
			resultpage.setVisible(true); 
			resultpage.setSize(800, 600);
	        resultpage.setResizable(false);
		}

}

