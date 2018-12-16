import java.awt.*;  
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class EnergyMAM extends JFrame implements ActionListener, Runnable{
	
	
	JPanel welcomeJpanel, jpusern, jppasswd, jp1, jp2; 
    JLabel jlwel, jlusern, jlpasswd;
    JButton jblogin, jbexit, jbreg;
    JRadioButton cSys, pSys; 
    ButtonGroup bg1;
    JTextField usern; 
    JPasswordField passwd;
    private boolean checkresult = false;
    private boolean isprovider = false;
    private String oneLine = "", inputusern = "", inputpasswd = "";
    String[] checkinfo = new String[40];
    StringBuilder checkbuf = new StringBuilder();
    ConsumerMenu cmenu ;
    ProviderUI pui;
	private String consumerID = "";
	private String consumerName = "";
	
	/**
     * This method is required when there is a thread
     */
	
	public void run()
	{
		
		Refresh ref = new Refresh();
		ref.fresh();
		}
	
	
	/**
     * This is a constructor
     */
    public EnergyMAM(){}
    

	/**
     * This method defines the start login menu
     */
    public void begin(){
    	welcomeJpanel = new JPanel();
    	jpusern = new JPanel();
    	jppasswd = new JPanel();
    	jp1 = new JPanel();
    	jp2 = new JPanel();
    	
    	jlusern = new JLabel("User ID:");
    	jlpasswd = new JLabel("Password:");
    	jlwel = new JLabel("Please select your identity");
        
        cSys = new JRadioButton("Consumer",true);  
        pSys = new JRadioButton("Provider");
        bg1=new ButtonGroup();  
        bg1.add(cSys);  
        bg1.add(pSys);
    	
    	jblogin = new JButton("Login");
    	jbexit = new JButton("Exit");
    	jbreg = new JButton("Register");
    	jblogin.addActionListener(this);
    	jbexit.addActionListener(this);
    	jbreg.addActionListener(this);
    	
        usern=new JTextField(20);  
        usern.addActionListener(this);
        jpusern.add(jlusern);
        jpusern.add(usern);
        
        passwd=new JPasswordField(20);
        passwd.addActionListener(this);
        jppasswd.add(jlpasswd);
        jppasswd.add(passwd);
        
        jp1.add(jlwel);
        jp1.add(cSys);  
        jp1.add(pSys);
        
        jp2.setLayout(new FlowLayout());
        jp2.add(jblogin);
        jp2.add(jbexit);
        
        welcomeJpanel.add(jpusern);
        welcomeJpanel.add(jppasswd);
        welcomeJpanel.add(jp1);
        welcomeJpanel.add(jp2);
        
        this.add(welcomeJpanel);


        this.setTitle("SEMAMS");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setSize(400, 200);
        this.setResizable(false);  
        this.setVisible(true);  
    }
    
    /**
     * This method checks if the input ID and password is right
     * @return if consumer logins successfully
     */
    
    public boolean LoginCheck(){
    	try{
    		FileReader fileReader=new FileReader("all.txt");
			BufferedReader bufferedReader=new BufferedReader(fileReader);
			while( (oneLine = bufferedReader.readLine()) != null) {
				checkbuf.append(oneLine + " ");
			}
			checkinfo = (checkbuf.toString()).split(" ");
			for(int x = 0; x < checkinfo.length; x++) { 
				if(checkinfo[x].equals(inputusern) == true){
					if(checkinfo[x+1].equals(inputpasswd) == true){
				    	checkresult = true;
				    	consumerName = checkinfo[x+2];
						consumerID = checkinfo[x];
						if(consumerID.equals("p") == true) {
							isprovider = true;
						}
						break;
					}
				}
			}
    	}
    	catch(IOException e){
			System.out.println("error occured in login");
			System.exit(1);
		}
    	
    	if(checkresult == true) {
			usern.setText("");
	    	passwd.setText("");
			}
		else if(checkresult == false) {
			usern.setText("");
		   	passwd.setText("");
		   	JOptionPane.showMessageDialog(null , "Wrong username or password! Please try again.","",JOptionPane.WARNING_MESSAGE);
		}
		return checkresult;
    }    
    
    /**
     * This method is an overrided action listener
     */
    
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == jblogin){                         
			inputusern = usern.getText();
			inputpasswd = String.valueOf(passwd.getPassword());
		
			usern.setText("");
			passwd.setText("");
			if(cSys.isSelected()){
				
				LoginCheck();
				if(checkresult == true && isprovider ==false) {
					cmenu = new ConsumerMenu(consumerID, consumerName);
					JOptionPane.showMessageDialog(null , "Login successful!","",JOptionPane.WARNING_MESSAGE);
					checkresult=false;
				}
			}
			else if(pSys.isSelected()){
				LoginCheck();
				if(checkresult == true && isprovider == true) {
				    pui = new ProviderUI();
				    pui.setmainGUI();
				    JOptionPane.showMessageDialog(null , "Login successful!","",JOptionPane.WARNING_MESSAGE);
				    checkresult=false;
				}
			}
		}
		else if(e.getSource() == jbreg){
		}
		else{	
			System.exit(0);
		}
	}
	
	/**
     * This method checks if the input ID and password is right
     * @param args An array of command line input
     */
	
	public static void main(String[] args) {  
		EnergyMAM o = new EnergyMAM();
		o.begin();
		
		Thread t = new Thread(o);
		
		t.setDaemon(true);
		
		t.start();
    }
     
} 