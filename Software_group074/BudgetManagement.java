import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BudgetManagement extends JFrame implements ActionListener{

	private JPanel budgetsetpanel;
	private String readnum;
	private JLabel showbudget;
	private JButton changebudget;
	private JButton backbutton;
	private float currentbudget;
	private JFrame changebudgetframe;
	private JPanel changebudgetpanel;
	private JLabel changebudgetinfo;
	private JTextField changebudgetnumber;
	private JButton changebudgetbutton;
	private String fname;
	private String cid;
	private String cname;
	private String budgettype;
	ConsumerFunction cf = new ConsumerFunction();
	
	/**
     * This is a constructor
     * @param consumerID id of consumer
     * @param consumerName name of consumer
     * @param budgettype0 type of budget
     */

	public BudgetManagement(String consumerID, String consumerName, String budgettype0) {
		this.cid = consumerID;
		this.cname = consumerName;
		this.budgettype = budgettype0;
	}

	/**
     * This method creates the GUI of Consumer's function "change budget"
     */

	public void budgetsetting() {
		fname = "consumerinfo/" + cid +"/" + cid + budgettype;
		budgetsetpanel = new JPanel();
		budgetsetpanel.setLayout(null);


		cf.readbudget(cid, budgettype);
		showbudget = new JLabel("Budget: " + cf.readbudget(cid, budgettype));
		budgetsetpanel.add(showbudget);           
		showbudget.setBounds(150,150,500,100);
		changebudget = new JButton("Change Budget");
		budgetsetpanel.add(changebudget);         
		changebudget.addActionListener(this); 
		changebudget.setBounds(150,250,200,30);
		backbutton = new JButton("Back");    
		backbutton.addActionListener(this);
		budgetsetpanel.add(backbutton);
		backbutton.setBounds(200,300,100,30);
		
		this.add(budgetsetpanel);
		this.setTitle("Budget Management");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  
		this.setSize(500, 400);
		this.setResizable(false);  
		this.setVisible(true);
	}


	/**
     * This method creates the window which can change the budget
     */
	
	
	public void changebudgetwindow() {
		changebudgetframe = new JFrame();
		changebudgetpanel = new JPanel();
		
		changebudgetinfo = new JLabel("Please enter the new budget:");
		changebudgetnumber  = new JTextField(10);
		changebudgetnumber.addActionListener(this);
		changebudgetbutton = new JButton("Change");
		changebudgetbutton.addActionListener(this);
		
		changebudgetpanel.add(changebudgetinfo);
		changebudgetpanel.add(changebudgetnumber);
		changebudgetpanel.add(changebudgetbutton);
		changebudgetframe.setTitle("Search result");
		changebudgetframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		changebudgetframe.setLayout(new BorderLayout()); 
		changebudgetframe.add(changebudgetpanel);
		changebudgetframe.setVisible(true); 
		changebudgetframe.setSize(300, 150);
		changebudgetframe.setResizable(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == changebudget) {
			changebudgetwindow();
		}
		else if(e.getSource() == changebudgetbutton) {
			String newbudget = changebudgetnumber.getText();

			if(newbudget.equals("")) {
				
			}
			else{
				cf.changeuserbudget(cid, newbudget,budgettype); //写入新的电价
	    		changebudgetnumber.setText("");
				cf.readbudget(cid, budgettype);
				showbudget.setText("Budget: " + cf.readbudget(cid, budgettype));
			}
		}
		else if(e.getSource() == backbutton) {
			dispose();
		}
	}
	
	
}
