
/* Establishes Connection to SNMP Agents
 * and kills them if requested
 */
import java.net.InetAddress;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.USMUserEntry;
import com.adventnet.snmp.snmp2.usm.USMUtils;

public class ConnectionManager {
	//Variables needed
	private SnmpSession session = null;
	private SnmpAPI api =new SnmpAPI();
	private InetAddress rhost = null;
	private String userName = null;
	private String password = null;
	private SnmpPDU pdu = null;
	private int port=161; //Hard coded Port number !
	//ENgineID to String
	HexBinaryAdapter hbAdapter=new HexBinaryAdapter();
	//For returning multiple parameters
	private Object connectionDetails[]= new Object [3];;
	//For V3
	private byte [] snmpID = null;
	
	//Constructor for Port number
	public ConnectionManager(int port){
		this.port=port;
	}
	
	//The main connection class
	public Object[] establishConnection(String ip, String userName, String password) throws SnmpException{
		String engineIDString="";
		try{
			//Set the variables for this case userName==communityString (for v1/v2c)
			rhost=InetAddress.getByName(ip);
			this.userName=userName;
			this.password=password;
			
			//Open the Connection
			session = new SnmpSession(api);
			session.setVersion(SnmpAPI.SNMP_VERSION_3);
			session.setRetries(5);
			//Open the session
			session.open();
			
		    //Create the PDU
		    pdu = new SnmpPDU();
			//V3 stuff
			engineIDString=initv3Parameter();
		
			// set remote Host 
			UDPProtocolOptions opt = (UDPProtocolOptions)session.getProtocolOptions(); 
			opt.setRemoteAddress(rhost);
			opt.setRemotePort(port);
			pdu.setProtocolOptions(opt);
		//throw exception to calling class
		}catch(Exception e){
			throw new SnmpException("Agent not answering");
		}
		//return session PDu and EngineID
		connectionDetails[0]=session;
		connectionDetails[1]=pdu;
		connectionDetails[2]=engineIDString;
		return connectionDetails;
	}
	//Kill a connection
	public boolean terminateConnection(SnmpSession session){
		try{
		session.close();
		}catch(Exception e){
			System.out.println("Connection Manager terminateConnection");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	//only internally used 
	private String initv3Parameter() throws SnmpException{
		String engineIDString="";
		try{
			//DELTE
			
			//Get the ID
			SnmpEngineEntry engineID=new SnmpEngineEntry(rhost.getHostAddress(),161);
			//session, timeout retries
			snmpID=engineID.discoverSnmpEngineID(session,1,2);
			//get the engine ID
				engineIDString=hbAdapter.marshal(snmpID);
			//SNMPv3 stuff
			pdu.setUserName(userName.getBytes());
			USMUtils.init_v3_parameters(
				userName,
				USMUserEntry.MD5_AUTH,
				password,
				null,
				rhost.getHostAddress(),
				port,
				session);
			pdu.setContextID(snmpID);
			//throw exception to calling class
		}catch(Exception e){
			throw new SnmpException("Device does not answer");
		}
		return engineIDString;
		
		
	}
	

	

}
