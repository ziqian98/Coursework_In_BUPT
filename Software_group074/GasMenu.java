import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class GasMenu extends JFrame implements ActionListener{

	private JButton jbc0, jbc1, jbc2, jbc3, jbcreturn;
	private JPanel jpcmenu;
	private CardLayout card1;
	private JPanel cpanel;
	private DisplayMeterUsage dugas;
	private String cid, cname = "";
	
	TariffCheck tariffCheck = new TariffCheck();
	ConsumerFunction cf = new ConsumerFunction();
	HistoryView hw;
	Gas gas;
	
	 /**
     * This is a constructor
     * @param consumerID id of consumer
     * @param consumerName name of consumer
     */ 
	
	public GasMenu(String consumerID, String consumerName) {
		
		this.cid = consumerID;
		this.cname =consumerName;
		dugas = new DisplayMeterUsage(cid, "gas");
		Thread t1 = new Thread(dugas);
		t1.setDaemon(true);
		t1.start();
	}
	
	 /**
     * This method sets the menu of the gas meter
     */
  

	public void setgasmenu() {
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
			ConsumerMenu.menuframe.setVisible(true);
		}
		
	
		else if(e.getSource() == jbc0) {
			dugas.displayframe.setVisible(true); 
		}
		
		else if(e.getSource() == jbc1) {
			 hw = new HistoryView(cid, cname, 222);
			 gas= new Gas(cid, cname);
		}
		
		else if(e.getSource() == jbc2) {
			
			tariffCheck.gastariffcheck();
		}

		else if(e.getSource() == jbc3) {
			BudgetManagement budgetManagement = new BudgetManagement(cid, cname, "gasbudget.txt");
			budgetManagement.budgetsetting();
		}
	}

}