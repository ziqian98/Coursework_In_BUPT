import java.awt.*;
import java.io.*;
import java.math.BigDecimal;

import javax.swing.*;

public class DisplayMeterUsage implements Runnable {
	private String cid, metertype;
	public JFrame displayframe;
	private JPanel displaypanel;
	private int currentbudget;
	private String readnum;
	private JLabel displayusage;
	private String buffertype, budgettype, tarifftype;
	private int recordpos;
	private JButton warningbox;
	private JLabel displaycost;
	ConsumerFunction cf = new  ConsumerFunction();
	
	
	/**
     * This is a constructor
     * @param cid id of the consumer
     * @param metertype type of meter to display(ele or gas)
     */
	
	
	public DisplayMeterUsage(String cid, String metertype) {
		this.cid = cid;
		this.metertype = metertype;

		if(metertype.equals("elec") == true) {
			buffertype = "electricity.txt";
			budgettype = "elecbudget.txt";
			recordpos = 5;
			tarifftype = "elec_tariff.txt";
		}
		else {								
			buffertype = "gas.txt";
			budgettype = "gasbudget.txt";
			recordpos = 6;
			tarifftype = "gas_tariff.txt";
		}
		createframe();
	
	}
	
	/**
     * Method required when there is another thread
     */
	

	public void run()						
	{
		while(true)     
		{
			if(metertype.equals("elec")==true)
			{
				try{	
						Thread.sleep(3000);
						String readnumberelec = String.valueOf(cf.readmeter(cid, buffertype, recordpos));
						double costresult = cf.readmeter(cid, buffertype, recordpos)*cf.readtariff(tarifftype);
						displayusage.setText("Usage: " + readnumberelec+ " kWh");
						displaycost.setText("Cost: " + twofloat(costresult)+ " pounds");
						if(cf.readmeter(cid, buffertype, recordpos) >= cf.readbudget(cid, budgettype)) {
							warningbox.setBackground(Color.RED);
						}
						else {
							warningbox.setBackground(Color.GREEN);
						}
						
					}
				catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			
			else
			{
				try{	
					Thread.sleep(10000);
					
					String readnumbergas = String.valueOf(cf.readmeter(cid, buffertype, recordpos));
					double costresult = cf.readmeter(cid, buffertype, recordpos)*cf.readtariff(tarifftype);
					displayusage.setText("Usage: " + readnumbergas+ "m3");
					displaycost.setText("Cost: " + twofloat(costresult)+ " pounds");
					if(cf.readmeter(cid, buffertype, recordpos) >= cf.readbudget(cid, budgettype)) {
						warningbox.setForeground(Color.RED);
					}
					else {
						warningbox.setForeground(Color.GREEN);
					}
				}
			catch(InterruptedException e){
				e.printStackTrace();
				}
			}
		}
		
		
	}
	
	/**
     * This method creates the GUI of the currentusage frame
     */
	

	public void createframe() {
		displayframe = new JFrame();
		
		displaypanel = new JPanel();
		displaypanel.setLayout(null);     
	
		
		displayusage = new JLabel("Usage: " + String.valueOf(cf.readmeter(cid, buffertype, recordpos)) + " m3");//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		displaypanel.add(displayusage);
		displayusage.setBounds(50,20,300,50);
		displayusage.setFont(new Font("Dialog",1,30));

		displaycost = new JLabel("Cost: " + twofloat(cf.readmeter(cid, buffertype, recordpos)*cf.readtariff(tarifftype))+ " pounds");
		displaypanel.add(displaycost);
		displaycost.setBounds(50,70,300,50);
		displaycost.setFont(new Font("Dialog",1,30));
		
		warningbox = new JButton();
		warningbox.setBackground(Color.GREEN);
		warningbox.setBounds(150,150,100,100);
		displaypanel.add(warningbox);
		
		displayframe.setTitle("Display Usage");
		displayframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		displayframe.setLayout(new BorderLayout()); 
		displayframe.add(displaypanel);
		displayframe.setVisible(false); 
		displayframe.setSize(400, 320);
		displayframe.setResizable(false);
	}
	
	public double twofloat(double f) {
		BigDecimal bg = new BigDecimal(f);
		double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return f1;
	}
}