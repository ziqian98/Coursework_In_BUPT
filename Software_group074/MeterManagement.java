import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MeterManagement implements ActionListener{
	
	private JTextField year1;
	private JTextField year2;
	private JTextField month1;
	private JPanel searchpanel;
	private JTextField month2;
	private JTextField day1;
	private JTextField day2;
	private JLabel from1;
	private JLabel to1;
	private JButton searchhis;
	private JPanel searchp1;
	private JPanel searchp2;
	private String cid;
	private JFrame searchframe;
	Provider prov = new Provider();
	
	 /**
     * This is a constructor
     * @param inputid id of the consumer the manager wants to search
     */ 
	

	public MeterManagement(String inputid) {
		this.cid = inputid;
		setsearchframe();
	}

	 /**
     * The method sets the GUI of the search frame, 
     * provider can search consumers' history record by the input date range
     */
	
	public void setsearchframe() {
		setsearchpanel();
		searchframe = new JFrame();
		searchframe.setTitle("Search result");
		searchframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		searchframe.setLayout(new BorderLayout()); 
		searchframe.add(searchpanel);
		searchframe.setVisible(true); 
		searchframe.setSize(500, 250);
		searchframe.setResizable(false);
	}
	
	 /**
     * The method sets the GUI of the search panel, 
     * provider can search consumers' history record by the input date range
     */

	public void setsearchpanel() {
		searchpanel = new JPanel();
		
		year1= new JTextField(10);
		year2= new JTextField(10);
		month1= new JTextField(5);
		month2= new JTextField(5);
		day1= new JTextField(5);
		day2= new JTextField(5);
		from1 = new JLabel("From:");
		to1 = new JLabel("To:");
		searchhis = new JButton("Search");
		searchp1 = new JPanel();
		searchp2 = new JPanel();
		
		searchp1.add(from1);
		searchp1.add(year1);   
		year1.addActionListener(this);
		searchp1.add(month1);  
		month1.addActionListener(this);
		searchp1.add(day1);    
		day1.addActionListener(this);
		
		searchp2.add(to1);
		searchp2.add(year2);   
		year2.addActionListener(this);
		searchp2.add(month2);  
		month2.addActionListener(this);
		searchp2.add(day2);    
		day2.addActionListener(this);
		searchp2.add(searchhis);  
		searchhis.addActionListener(this);
		
		searchpanel.add(searchp1);
		searchpanel.add(searchp2);
		searchpanel.add(searchhis);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == searchhis) {
			String begindate = year1.getText() + "-" + month1.getText() + "-" + day1.getText();
			String enddate = year2.getText() + "-" + month2.getText() + "-" + day2.getText();
			year1.setText("");
			month1.setText("");
			day1.setText("");
			year2.setText("");
			month2.setText("");
			day2.setText("");
			prov.readhistory(cid, begindate, enddate);
		}

	}
}