
public abstract class meter {
	private String cid;
	private String cname;
	
	 /**
     * This is a constructor
     * @param consumerID id of consumer
     * @param consumerName name of consumer
     */ 
	
	public meter(String consumerID, String consumerName) {
		this.cid = consumerID;
		this.cname = consumerName;
		
	}

}
