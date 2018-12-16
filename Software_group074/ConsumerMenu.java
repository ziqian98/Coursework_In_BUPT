import java.awt.event.*;
import javax.swing.*;

public class ConsumerMenu implements ActionListener{
	private JButton jbcGas, jbcElec, jbcexit;
	private JPanel jpc0, jpc1, jpcmain;
	private JLabel jlc0, jlc1, jlc2, jlc3;
	static JFrame menuframe;
	private String cid, cname;
	GasMenu cgas;
	ElecMenu celec ;
	
	/**
     * This is a constructor
     * @param consumerID id of consumer
     * @param consumerName name of consumer
     */
	
	
	public ConsumerMenu(String consumerID, String consumerName) {
		this.cid = consumerID;
		this.cname = consumerName;
		setmainGUI();

	}



	/**
     * The method sets the GUI of the consumer, 
     * lets the consumer choose to use the function of the electricity meter or the gas meter
     */
	
	public void setmainGUI(){
		menuframe = new JFrame();
		jpc0 = new JPanel();
		jpc1 = new JPanel();
		jpcmain = new JPanel();
		
		jlc0 = new JLabel("No.");
		jlc1 = new JLabel(cid);
		jlc2 = new JLabel("Name:");
		jlc3 = new JLabel(cname);
		jpc0.add(jlc0);
		jpc0.add(jlc1);
		jpc0.add(jlc2);
		jpc0.add(jlc3);

		jbcGas = new JButton("Gas"); 
		jbcElec = new JButton("Electricity");
		jbcexit = new JButton("Exit");
		jbcGas.addActionListener(this);
		jbcElec.addActionListener(this);
		jbcexit.addActionListener(this);
		jpc1.add(jbcGas);
		jpc1.add(jbcElec);
		jpcmain.setLayout(null);
		jpcmain.add(jpc0); jpc0.setBounds(0,50,500,100);
		jpcmain.add(jpc1); jpc1.setBounds(0,200,500,100);
		jpcmain.add(jbcexit); jbcexit.setBounds(200,320,100,30);
		
		menuframe.add(jpcmain);
		menuframe.setTitle("SEMAMS");  
		menuframe.setSize(500, 400);
		menuframe.setResizable(false);  
		menuframe.setVisible(true);
	}


	/**
     * The method listens the action of the user and 
     * sets the menu which the consumer choose or return to the menu 
     * @param e a click event
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jbcGas){
			menuframe.setVisible(false);
			 cgas = new GasMenu(cid, cname);
			cgas.setgasmenu();
		}
		else if(e.getSource() == jbcElec){
			menuframe.setVisible(false);
			celec = new ElecMenu(cid, cname);
			celec.setelecmenu();
		}
		else if(e.getSource() == jbcexit){
			menuframe.dispose();
		}
	}
}
