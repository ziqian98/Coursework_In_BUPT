import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class HistoryView implements ActionListener{

	private JPanel historyviewpanel,searchp1,searchp2;
	private JTextField year1,year2,month1,month2,day1,day2;
	private JLabel from1,to1;
	private JButton searchhis;
	private JButton backbutton;
	private JFrame historyviewframe;
	private int viewtarget;
	ConsumerFunction cf = new ConsumerFunction();
	private String cid;
	private String cname;

	
	 /**
     * This method is used to view history information
     * @param consumerID id of consumer
     * @param consumerName name of consumer
     * @param type0 decides which type(ele or gas) to view
     */
 
	public HistoryView(String consumerID, String consumerName, int type0) {
		this.cid = consumerID;
		this.cname = consumerName;
		this.viewtarget = type0;
		createframe();
	}

	 /**
     * This method sets the GUI of the history view frame
     */
 
	public void createframe() {
		historyviewframe = new JFrame();
		historyviewpanel = new JPanel();
		
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
		
		backbutton = new JButton("Back");    
		backbutton.addActionListener(this);
		
		historyviewpanel.add(searchp1);
		historyviewpanel.add(searchp2);
		historyviewpanel.add(searchhis);
		historyviewpanel.add(backbutton);
		
		historyviewframe.add(historyviewpanel);
		historyviewframe.setTitle("History view");
		historyviewframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
		historyviewframe.setSize(500, 400);
		historyviewframe.setResizable(false);  
		historyviewframe.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == searchhis) {
			String startdate = year1.getText() +"-"+ month1.getText() +"-"+ day1.getText();
			String enddate =  year2.getText() +"-"+ month2.getText() +"-"+ day2.getText();
			year1.setText("");
			month1.setText("");
			day1.setText("");
			year2.setText("");
			month2.setText("");
			day2.setText("");
			
			if(viewtarget == 111)
				cf.setresultpageelec(cid, startdate, enddate);
			else if(viewtarget == 222)
				cf.setresultpagegas(cid, startdate, enddate);
		}
		else if(e.getSource() == backbutton) {
			historyviewframe.dispose();
		}
	}

}
