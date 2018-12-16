import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class TariffCheck implements ActionListener{

	private JPanel showpanel;
	private JButton backbutton;
	private JLabel elecinfo1,gasinfo1;
	private JFrame electariframe,gastariframe;
	ConsumerFunction cf = new ConsumerFunction();
	

	/**
     * This is a constructor
     */
	public TariffCheck() {}


	/**
     * This method reads the history record of the electricity meter	
     */


	public void electariffcheck() {
		electariframe = new JFrame();
		showpanel = new JPanel();
		showpanel.setLayout(null);
    	backbutton = new JButton("Close");    backbutton.addActionListener(this);
    	showpanel.add(backbutton);            backbutton.setBounds(200,200,100,30);
    	elecinfo1 = new JLabel("Electricity traiff: " + cf.getelectariff() + " pounds");
    	showpanel.add(elecinfo1);          
    	elecinfo1.setBounds(100,50,200,100);
    	
    	electariframe.add(showpanel);
    	electariframe.setTitle("Electricity Tariff");
    	electariframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
    	electariframe.setSize(400, 300);
    	electariframe.setResizable(false);  
    	electariframe.setVisible(true);
	}

	/**
     * This method reads the history record of the gas meter
     */
	
	public void gastariffcheck() {
		gastariframe = new JFrame();
		showpanel = new JPanel();
		showpanel.setLayout(null);
    	backbutton = new JButton("Back");    backbutton.addActionListener(this);
    	showpanel.add(backbutton);            backbutton.setBounds(200,200,100,30);
    	
    	gasinfo1 = new JLabel("Gas traiff: " + cf.getgastariff() + " pounds");
    	showpanel.add(gasinfo1);          
    	gasinfo1.setBounds(100,50,200,100);
    	
    	gastariframe.add(showpanel);
    	gastariframe.setTitle("Gas Tariff");
    	gastariframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
    	gastariframe.setSize(400, 300);
    	gastariframe.setResizable(false);  
    	gastariframe.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() ==backbutton) {
			try{
				electariframe.dispose();
				gastariframe.dispose();
			}
			catch (NullPointerException e1) {

			}
		}
	}
	
}
