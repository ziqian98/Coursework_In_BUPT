import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ProviderUI extends JFrame implements ActionListener {
	private JButton jb0, jb1, jb2, jb3, jbreturn, jbpreturn;  
	private JPanel pmenu, cpanel1, c2, c3;                    
	private CardLayout card2;							
	private JButton jbadd, jbview, asave, aclear, areturn, vreturn;
	private JPanel c0, c01, auserid, ausername, abutton;	 
	private JTextField userid, username;								
	private JLabel newid, newname, newpasswd;;										
	private JPanel c1, cgas, celec;												
	private JButton gaschange, elecchange, treturn;									
	private JTextField gasnum, elecnum;												 
	private JLabel gasinfo, elecinfo;																										 
	private JTextField billuserid;
	private JButton billcheck;
	private JPanel c31;
	private JButton  billhistory, jbm32;
	private JButton jbm31;
	private JTextField meteruserid;
	private JButton metercheck, jbm2;
	private JButton jbm3;
	private JTextField userpasswd;
	private JPanel apasswd;
	private String addusername, adduserid, addpasswd;
	private String gastariff, electariff;
	private JLabel billnote;
	Provider prov = new Provider();
	
	/**
     * This is a constructor
     */
	
	public ProviderUI() {}

	/**
     * This method sets the GUI of the main menu of the provider frame
     */

	public void setmainGUI() {
		pmenu = new JPanel();
    	pmenu.setLayout(null);
    	jb0 = new JButton("Consumer information management"); jb0.setBounds(140,30,200,30);
    	jb1 = new JButton("Tariff management");               jb1.setBounds(140,90,200,30);
    	jb2 = new JButton("Meter management");                jb2.setBounds(140,150,200,30);
    	jb3 = new JButton("Bill management");                 jb3.setBounds(140,210,200,30);
    	jbreturn = new JButton("Exit");                     jbreturn.setBounds(350,300,100,20);
    	jb0.addActionListener(this);
    	jb1.addActionListener(this);
    	jb2.addActionListener(this);
    	jb3.addActionListener(this);
    	jbreturn.addActionListener(this);
    	
    	pmenu.add(jb0);
    	pmenu.add(jb1);
    	pmenu.add(jb2);
    	pmenu.add(jb3);
    	pmenu.add(jbreturn);
    	
    	consumermanagement();
    	
    	tariffmanagement();
    	
    	metermanagement();
    	
    	billmanagement();
    	
    	card2 = new CardLayout(); 
		cpanel1 = new JPanel(card2);
		cpanel1.add(pmenu,"menu");
		cpanel1.add(c0,"p0");
		cpanel1.add(c01,"p0-1");
	
		cpanel1.add(c1,"p1");
		cpanel1.add(c2,"p2");
		cpanel1.add(c3,"p3");
		cpanel1.add(c31,"p3-1");
    	
    	this.add(cpanel1);
    	this.setTitle("SEMAMS");
        this.setSize(500, 400);
        this.setResizable(false);  
        this.setVisible(true);
	}


	/**
     * This method sets the GUI of the Consumer Management frame
     */
	
	public void consumermanagement() {
		c0 = new JPanel();
    	c0.setLayout(null);
    	jbadd = new JButton("Add consumers");
    	jbview = new JButton("View / Remove consumers");
    	jbpreturn = new JButton("Return");
    	c0.add(jbadd);                  jbadd.setBounds(150,50,200,50);     jbadd.addActionListener(this);
    	c0.add(jbview);                 jbview.setBounds(150,200,200,50);   jbview.addActionListener(this);
    	c0.add(jbpreturn);              jbpreturn.setBounds(200,300,100,30);   jbpreturn.addActionListener(this);
    	
    	addconsumer();
	}


	/**
     * This method sets the GUI of the Add Consumer in Consumer Management frame		
     */

	public void addconsumer() {

    	c01 = new JPanel(null);
    	auserid = new JPanel();
    	newid = new JLabel("ID:       ");
    	userid = new JTextField(25);
    	userid.addActionListener(this);
    	auserid.add(newid);
    	auserid.add(userid);

    	ausername = new JPanel();          
    	newname = new JLabel("Name:");
    	username = new JTextField(25);
    	username.addActionListener(this);
    	ausername.add(newname);
    	ausername.add(username);

    	newpasswd = new JLabel("Password:");   
    	userpasswd = new JTextField(25);
    	apasswd = new JPanel();
    	apasswd.add(newpasswd);
    	apasswd.add(userpasswd);
    	userpasswd.addActionListener(this);
    	
    	abutton = new JPanel();
    	asave = new JButton("Save");
    	aclear = new JButton("Clear");
    	areturn = new JButton("Return"); 
   
    	abutton.add(asave);   
    	asave.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
    		if(prov.checkidinput(userid.getText())) {
	    		addusername = username.getText();
	    		adduserid = userid.getText();
	    		addpasswd = userpasswd.getText();
	    		prov.newconsumer(adduserid, addpasswd, addusername);    
	    		username.setText("");                             
	    		userid.setText("");
	    		userpasswd.setText("");
	    	}
    		else{
    			JOptionPane.showMessageDialog(null , "ID already exists, please try again!","",JOptionPane.WARNING_MESSAGE);
    		}
    	}
    	});
    	
 
    	abutton.add(aclear);   
    	aclear.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
    		username.setText("");
    		userid.setText("");
    		userpasswd.setText("");}});
    	
    	abutton.add(areturn);                   
    	areturn.addActionListener(this);
    	c01.add(auserid);      auserid.setBounds(0,20,500,80);
    	c01.add(ausername);    ausername.setBounds(0,110,500,80);
    	c01.add(apasswd);	   apasswd.setBounds(0,200,500,80);
    	c01.add(abutton);      abutton.setBounds(0,300,500,80);
	}	

	/**
     * This method sets the GUI of the Tariff Management frame			
     */

	public void tariffmanagement() {

    	c1 = new JPanel();
    	c1.setLayout(null);
    	cgas = new JPanel();
    	celec = new JPanel();
    	gasnum = new JTextField(10);         gasnum.addActionListener(this);
    	elecnum = new JTextField(10);        elecnum.addActionListener(this);
    	gasinfo = new JLabel("Current gas tariff is: "+prov.currenttariffgas()+" pounds");             
    	elecinfo = new JLabel("Current electricity tariff is: "+prov.currenttariffelec()+" pounds");  
    	
    	gaschange = new JButton("Change Gas Tariff"); 
    	gaschange.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
    		String newgastariff = gasnum.getText();      
    		prov.changetariff(newgastariff,"gas_tariff.txt"); 
    		gasinfo.setText("Current gas tariff is: "+prov.currenttariffgas()+" pounds");}});  
    	elecchange = new JButton("Change Elec Tariff");  
    	elecchange.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
    		String newelectariff = elecnum.getText();
    		prov.changetariff(newelectariff,"elec_tariff.txt");
    		elecinfo.setText("Current electricity tariff is: "+prov.currenttariffelec()+"pounds");}}); 
    	treturn = new JButton("Return");      
    	treturn.addActionListener(this);

    	cgas.add(gaschange);
    	cgas.add(gasnum);
    	
    	celec.add(elecchange);
    	celec.add(elecnum);
    	
    	c1.add(gasinfo);		gasinfo.setBounds(50,50,400,50);
    	c1.add(elecinfo);		elecinfo.setBounds(50,150,400,50);
    	c1.add(cgas);           cgas.setBounds(0,100,400,100);
    	c1.add(celec);          celec.setBounds(0,200,400,100);
    	c1.add(treturn);        treturn.setBounds(200,300,100,30);
	}
	/**
     * This method sets the GUI of the Meter Management frame			
     */


	public void metermanagement() {
		c2 = new JPanel();
		JLabel noticel = new JLabel("Please enter the ID you want to check:");
    	meteruserid = new JTextField(20); 
    	meteruserid.addActionListener(this);
    	metercheck = new JButton("Search");
    	c2.add(noticel);
    	c2.add(meteruserid);          meteruserid.addActionListener(this);
    	c2.add(metercheck);           metercheck.addActionListener(this);
    	jbm2 = new JButton("Back");   jbm2.addActionListener(this);
    	c2.add(jbm2);
	}

	
	/**
     * This method sets the GUI of the Bill Management frame			
     */


	public void billmanagement() {
		c3 = new JPanel();
		c3.setLayout(null);
    	billhistory = new JButton("View bill");
    	billhistory.setBounds(140,30,200,30);
    	
    	jbm3 = new JButton("Back"); 
    	jbm3.setBounds(140,90,200,30);
    	jbm3.addActionListener(this);
    	
    	c3.add(billhistory);        billhistory.addActionListener(this);
    	c3.add(jbm3);
    	
    	c31 = new JPanel();
    	billnote = new JLabel("Please enter the user ID you want to check:                     ");
    	billuserid = new JTextField(15);
    	billcheck = new JButton("Search");
  
    	c31.add(billnote);
    	c31.add(billuserid);          billuserid.addActionListener(this);
    	c31.add(billcheck);           billcheck.addActionListener(this);

    	jbm31 = new JButton("Back");  jbm31.addActionListener(this);
    	c31.add(jbm31);	    
	}

	/**
     * This method return to the main menu			
     */

	public void mainmenu(){
		dispose();
		EnergyMAM emam = new EnergyMAM();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
						if(e.getSource() == jbreturn){
			
							dispose();
						}
						else if(e.getSource() == jbpreturn||e.getSource() == treturn||e.getSource() == jbm2||e.getSource() == jbm3){
							card2.show(cpanel1,"menu");
						}
						else if(e.getSource() == jb0){
							card2.show(cpanel1, "p0"); 
						}
						else if(e.getSource() == jb1){
							card2.show(cpanel1, "p1"); 
						}
						else if(e.getSource() == jb2){
							card2.show(cpanel1, "p2");
						}
						else if(e.getSource() == jb3){
							card2.show(cpanel1, "p3"); 
						}
						else if(e.getSource() == jbadd){
							card2.show(cpanel1, "p0-1");
						}
						else if(e.getSource() == jbview){
							ConsumerManagement cm = new ConsumerManagement();
						}
						else if(e.getSource() == areturn||e.getSource() == vreturn){
							card2.show(cpanel1, "p0");
						}
						else if(e.getSource() == jbm32||e.getSource() == jbm31){
							card2.show(cpanel1, "p3");
						}
						else if(e.getSource() == billhistory) {
				
							card2.show(cpanel1, "p3-1");
						}
						else if(e.getSource() == billcheck) {
							BillManagement bm = new BillManagement(billuserid.getText());
							billuserid.setText("");
						}
						else if(e.getSource() == metercheck) {
							String inputid = meteruserid.getText();
							if(prov.checkid(inputid) == true && inputid.equals("") == false) {
								MeterManagement mm = new MeterManagement(inputid);
							}
							else {
								JOptionPane.showMessageDialog(null , "ID not exists, please try again!","",JOptionPane.WARNING_MESSAGE);
							}
						}
		}


}
