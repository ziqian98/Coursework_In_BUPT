import javax.swing.JFrame;

	public class Consumer extends JFrame{
		String cid = "", cname = "";
		
		/**
	     * This is a constructor
	     * @param consumerID id of consumer
	     * @param consumerName name of consumer
	     */
		
		public Consumer(String consumerID, String consumerName) {
			this.cid = consumerID;
			this.cname = consumerName;
		}
	}
	
