import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ConsumerManagement implements ActionListener{
	
	private String oneline = "";
	private DefaultTableModel vmodel;
	private JTable vlist;
	private String[] readconsumerinfo;
	private JScrollPane jspanview;
	private JPanel viewpage;
	private JLabel viewLabel;
	private JTextField viewid;
	private JButton vsearch;
	private JButton vremove;
	private JButton vreturn;
	private JPanel vpsearch;
	private JPanel vbutton;
	private JFrame viewframe;
	Provider prov = new Provider();
	
	/**
     * This is a constructor 
     */
	
	public ConsumerManagement() {
		createframe("");
	}

//	

	/**
     * The method shows the result of the searched consumer
     * @param cid id of consumer
     */
	public void createframe(String cid) {
		viewframe = new JFrame();
		viewconsumer(cid);
		viewframe.setTitle("Search result");
		viewframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		viewframe.setLayout(new BorderLayout()); 
		viewframe.add(viewpage);
		viewframe.setVisible(true); 
		viewframe.setSize(500, 400);
		viewframe.setResizable(true);
	}

	/**
     * The method creates the GUI of view consumer frame	
     * @param cid id of consumer
     */
	public void viewconsumer(String cid) {
    	viewpage = new JPanel(new FlowLayout());
    	viewLabel = new JLabel("Please enter the user ID:");
    	viewid = new JTextField(20);
    	vsearch = new JButton("Search");  vsearch.addActionListener(this);
    	vremove = new JButton("Remove");  vremove.addActionListener(this);
    	vreturn = new JButton("Return");  vreturn.addActionListener(this);
    	vpsearch = new JPanel();
    	vbutton = new JPanel();
    	vpsearch.add(viewLabel);
    	vpsearch.add(viewid);
    	vpsearch.add(vsearch);
    	vlist(cid);
    	vbutton.add(vremove);
    	vbutton.add(vreturn);
    	viewpage.add(vpsearch);
    	viewpage.add(jspanview);                     
    	viewpage.add(vbutton);
	}


	/**
     * The method searches the consumer ID in the file and show the result in a JTable, 
     * if enter "" or "all", it will show all the consumers
     * @param cid id of consumer
     */
	
	public void vlist(String cid) {                          
		String[][] datas = {};
		String[] titles = {"Username","User ID","Status"};
		vmodel = new DefaultTableModel(datas,titles);
		vlist = new JTable(vmodel);
		vlist.setPreferredScrollableViewportSize(new Dimension(400, 200));
		try {
			
			FileReader file = new FileReader("all.txt");
			BufferedReader bfr = new BufferedReader(file);
			if(cid.equals("")) {
				for(oneline = bfr.readLine();oneline != null;oneline = bfr.readLine()) {
					readconsumerinfo = oneline.split(" ");
					if(readconsumerinfo[0].equals("p") == false) {
						vmodel.addRow(new String[] {readconsumerinfo[2],readconsumerinfo[0]," "});
					}
				}
			}
			else {
				for(oneline = bfr.readLine();oneline != null;oneline = bfr.readLine()) {
					readconsumerinfo = oneline.split(" ");
					if(readconsumerinfo[0].equals(cid) == true) {
						vmodel.addRow(new String[] {readconsumerinfo[2],readconsumerinfo[0]," "});
					}
				}
			}
			bfr.close();
			file.close();
			
		}
		catch(IOException|ArrayIndexOutOfBoundsException e) {
			System.out.print("Error occured in vlist()");
			System.exit(1);
		}
		jspanview = new JScrollPane(vlist);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == vremove) {
			int row = vlist.getSelectedRow();
			if(row != 0) {
				String selectID = (String) vlist.getValueAt(row, 1);
				prov.removeconsumer(selectID, "all.txt");
				prov.removeconsumer(selectID, "allbill.txt");
				prov.removeconsumerfile(selectID);
				viewframe.dispose();
				createframe("");
			}	
		}
		
		else if(e.getSource() == vsearch) {
			String inputid = viewid.getText();
			viewframe.dispose();
			createframe(inputid);
		}
	}
	
}
