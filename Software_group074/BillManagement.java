import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

public class BillManagement implements ActionListener{
	private String oneline;
	private String id1 = "";
	private JFrame resultpage;
	private JPanel resultpanel;
	private DefaultTableModel bmodel;
	private JTable billlist;
	private String[] readbillinfo;
	private JScrollPane jspanbill;
	private JButton sendbill;
	Provider prov = new Provider();	
	
	public BillManagement(String id0) {
		this.id1 = id0;
		createframe();
	}


	/**
     * This method creates the result frame of bill management
     */
	
	public void createframe() {
		setresultpage();
		resultpage = new JFrame();
		resultpage.setTitle("Search result");
		resultpage.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		resultpage.setLayout(new BorderLayout()); 
		resultpage.add(resultpanel);
		resultpage.setVisible(true); 
		resultpage.setSize(500, 400);
        resultpage.setResizable(false);
	}

	/**
     * This method can displayed all the users wanted
     */
	
	public void setresultpage() {
		
		resultpanel = new JPanel();
		String[][] datab = {};
		String[] titleb = {"Username","User ID","Bill Status"};
		bmodel = new DefaultTableModel(datab,titleb);
		billlist = new JTable(bmodel);
		billlist.setPreferredScrollableViewportSize(new Dimension(480, 200));
		try {
			FileReader fileb = new FileReader("allbill.txt");
			BufferedReader bfrb = new BufferedReader(fileb);
			if(id1.equals("") ==true || id1.equals("all") == true) {
				for(oneline = bfrb.readLine();oneline != null;oneline = bfrb.readLine()) {
					readbillinfo = oneline.split(" ");
					if(!readbillinfo[0].equals("ID")) {
						bmodel.addRow(new String[] {readbillinfo[1], readbillinfo[0], readbillinfo[2]});
						}
				}
			}
			else {
				for(oneline = bfrb.readLine();oneline != null;oneline = bfrb.readLine()) {
					readbillinfo = oneline.split(" ");
					if(readbillinfo[0].equals(id1)==true)
						bmodel.addRow(new String[] {readbillinfo[1], readbillinfo[0], readbillinfo[2]});
				}
			}
			bfrb.close();
			fileb.close();
		}
		catch(IOException e) {
			System.out.print("Error occured in billlist()");
			System.exit(1);
		}
		jspanbill = new JScrollPane(billlist);
		
		sendbill = new JButton("Send email");
		sendbill.addActionListener(this);
		resultpanel.add(jspanbill);
		resultpanel.add(sendbill);
        
        if(billlist.getRowCount() ==0) {
        	JOptionPane.showMessageDialog(null , "User not found!","",JOptionPane.WARNING_MESSAGE);
        }
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == sendbill) {
			int row = billlist.getSelectedRow();
			String selectID = (String) billlist.getValueAt(row, 1);
			prov.sendemail(selectID, "allbill.txt");
			resultpage.dispose();
			createframe();
		}
	}
}
