import java.awt.*;
import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class ConsumerFunction {

	/**
     * The method reads the current usage from buffer and history record, then combines them and returns the current total usage
     * @param cid id of consumer
     * @param buffertype type of electricity or gas
     * @param recordpos refers to the position in a array and used to judge if it is electricity of gas
     * @return double current total usage
     */
	
	public double readmeter(String cid, String buffertype, int recordpos) {
		String filename = "consumerinfo/" + cid +"/" + cid + buffertype;
		double currentusage = 0.0;
		try {
			FileReader fileReader=new FileReader(filename);	
			BufferedReader bufferedReader=new BufferedReader(fileReader);
			String readnum = bufferedReader.readLine();
			
			bufferedReader.close();
			fileReader.close();
			
			LastLineOperation llo = new LastLineOperation();
			String recordline = llo.getFileLastLine("consumerinfo/" + cid +"/" + cid + ".txt");
			String[] parser = recordline.split(" ");
			float f = Float.parseFloat(readnum) + Float.parseFloat(parser[recordpos]);
			BigDecimal bg = new BigDecimal(f);
			currentusage = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	
		}
		catch(IOException e){
			System.out.println("error occured in readmeter");
			System.exit(1);
		}
        return currentusage;
	}


	/**
     * The method reads the tariff of the meter
     * @param type gas or electricity
     * @return float the current budget
     */

	public float readtariff(String type) {
		float thetariff = 0.0f;
		try {
			FileReader fr1=new FileReader(type);
			BufferedReader bfr1=new BufferedReader(fr1);
			
			thetariff = Float.parseFloat(bfr1.readLine());
			bfr1.close();
			fr1.close();
		}
		catch(IOException e){
			System.out.println("error occured in readtariff");
			System.exit(1);
		}
		return thetariff;
	}


	/**
     * The method reads consumer's budget
     * @param cid id of consumer
     * @param budgettype type of budget
     * @return int the current budget
     */

	
	public int readbudget(String cid, String budgettype) {
		String fname = "consumerinfo/" + cid +"/" + cid + budgettype;
		int currentbudget = 0;
		try {
			FileReader fr1=new FileReader(fname);
			BufferedReader bfr1=new BufferedReader(fr1);
			
			currentbudget = (int)(Float.parseFloat(bfr1.readLine()));
			bfr1.close();
			fr1.close();
		}
		catch(IOException e){
			System.out.println("error occured in readbudget");
			System.exit(1);
		}
		return currentbudget;
	}
	
	/**
     * The method searches the history record of electricity usage
     * @param id1 id of consumer
     * @param date0 start date of the range
     * @param date1 end date of the range
     */

	public void setresultpageelec(String id1, String date0, String date1) {
		JFrame resultpage = new JFrame();
		JPanel resultpanel = new JPanel();
		String oneline = "";
		String[] hrecord;
		String[][] datas = {};
		String[] titles = {"Year","Month","Date", "Daily Usage", "Total Usage"};
		DefaultTableModel model = new DefaultTableModel(datas,titles);
		JTable htable = new JTable(model);
		htable.setPreferredScrollableViewportSize(new Dimension(400, 250));
		
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
						if(linedate.before(enddate)==true && linedate.after(begindate)==true || linedate.equals(begindate)==true || linedate.equals(enddate)==true) {
							model.addRow(new String[] {hrecord[0],hrecord[1],hrecord[2],hrecord[3],hrecord[5]});
						}
					}
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
		resultpage.setSize(500, 400);
        resultpage.setResizable(false);
	}
	
	/**
     * The method searches the history record of gas usage
     * @param id1 id of consumer
     * @param date0 start date of the range
     * @param date1 end date of the range
     */
	
	public void setresultpagegas(String id1, String date0, String date1) {
		JFrame resultpage = new JFrame();
		JPanel resultpanel = new JPanel();
		String oneline = "";
		String[] hrecord;
		String[][] datas = {};
		String[] titles = {"Year","Month","Date", "Daily Usage", "Total Usage"};
		DefaultTableModel model = new DefaultTableModel(datas,titles);
		JTable htable = new JTable(model);
		htable.setPreferredScrollableViewportSize(new Dimension(400, 250));
		
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
						if(linedate.before(enddate)==true && linedate.after(begindate)==true || linedate.equals(begindate)==true || linedate.equals(enddate)==true) {
							model.addRow(new String[] {hrecord[0],hrecord[1],hrecord[2],hrecord[4],hrecord[6]});
	
						}
					}
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
		resultpage.setSize(500, 400);
        resultpage.setResizable(true);
	}
	/**
     * The method gets the electricity tariff
     * @return electricity tariff
     */
	
	public String getelectariff() {
		String electariff = "";
		try {
    		FileReader fReader = new FileReader("elec_tariff.txt");
    		BufferedReader bfr = new BufferedReader(fReader);
    		electariff = bfr.readLine();
    		bfr.close();
    		fReader.close();
    	}
    	catch(IOException e) {
    		System.out.println("Tariff check error");
    	}
		return electariff;
	}
	/**
     * The method gets the gas tariff
     * @return gas tariff
     */
	
	public String getgastariff() {
		String gastariff = "";
		try {
    		FileReader fReader = new FileReader("gas_tariff.txt");
    		BufferedReader bfr = new BufferedReader(fReader);
    		gastariff = bfr.readLine();
    		bfr.close();
    		fReader.close();
    	}
    	catch(IOException e) {
    		System.out.println("Tariff check error");
    	}
		return gastariff;
	}

	/**
     * The method changes the budget in the file system
     * @param cid id of consumer
     * @param budget type of budget
     * @param filename name of file related to store the budget
     */
	
		public void changeuserbudget(String cid, String budget, String filename) {
			String path1 = "consumerinfo/" + cid + "/" + cid + filename;
			try {
				FileWriter fw = new FileWriter(path1);
				BufferedWriter bfw = new BufferedWriter(fw);
				bfw.write(budget);
				bfw.close();
				fw.close();
			}catch(IOException e) {
				System.out.print("Error occured in changebudget");
				System.exit(1);
			}
		}
	
}
