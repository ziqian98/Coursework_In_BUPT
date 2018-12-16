import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class ElecMenu extends JFrame implements ActionListener{

	private JButton jbc0, jbc1, jbc2, jbc3, jbcreturn;
	private JPanel jpcmenu;
	private CardLayout card1;
	private JPanel cpanel;
	private DisplayMeterUsage duelec;
	private String cid, cname = "";
	ConsumerFunction cf = new ConsumerFunction();
	TariffCheck tariffCheck = new TariffCheck();
	
	HistoryView hw ;
	Elec ec;
	
	/**
     * This is a constructor
     * @param consumerID id of consumer
     * @param consumerName name of consumer
     */
	
	public ElecMenu(String consumerID, String consumerName) {
		this.cid = consumerID;
		this.cname =consumerName;
		duelec = new DisplayMeterUsage(cid, "elec");
		Thread t0 = new Thread(duelec);
		t0.setDaemon(true);
		t0.start();
	}


	/**
     * This method sets the menu of the electricity meter
     */
	
	public void setelecmenu() {

		jpcmenu = new JPanel();
		jpcmenu.setLayout(null);
		jbc0 = new JButton("Current use today");          jbc0.setBounds(140,30,200,30);
		jbc1 = new JButton("History view");               jbc1.setBounds(140,90,200,30);
		jbc2 = new JButton("Tariff check");               jbc2.setBounds(140,150,200,30);
		jbc3 = new JButton("Budget setting");             jbc3.setBounds(140,210,200,30);
		jbcreturn = new JButton("Return");                jbcreturn.setBounds(350,300,100,20);
		jbc0.addActionListener(this);
		jbc1.addActionListener(this);
		jbc2.addActionListener(this);
		jbc3.addActionListener(this);
		jbcreturn.addActionListener(this);
		jpcmenu.add(jbc0);
		jpcmenu.add(jbc1);
		jpcmenu.add(jbc2);
		jpcmenu.add(jbc3);
		jpcmenu.add(jbcreturn);
		
		card1 = new CardLayout(); 
		cpanel = new JPanel(card1);
		cpanel.add(jpcmenu,"menu");
		
		this.add(cpanel);
		this.setTitle("SEMAMS");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		this.setSize(500, 400);
		this.setResizable(true);  
		this.setVisible(true);
	}

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
	
		if(e.getSource() == jbcreturn){
			dispose();
			ConsumerMenu.menuframe.setVisible(true);//Consumer's menu
		}
		
	
		else if(e.getSource() == jbc0) {
			duelec.displayframe.setVisible(true); 
		}
		

		else if(e.getSource() == jbc1) {
			hw = new HistoryView(cid, cname, 111);
			 ec=new Elec(cid, cname);
		}
		

		else if(e.getSource() == jbc2) {
			tariffCheck.electariffcheck();
		}


		else if(e.getSource() == jbc3) {
			BudgetManagement budgetManagement = new BudgetManagement(cid, cname, "elecbudget.txt");
			budgetManagement.budgetsetting();
		}
	}

}
