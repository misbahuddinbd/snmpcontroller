
/*
 * Searches for neighbours and IPS by contacting other classes like TableAgent and SNMPOperator
 * 
 */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.adventnet.snmp.snmp2.SnmpOID;
import com.adventnet.snmp.snmp2.SnmpPDU;
import com.adventnet.snmp.snmp2.SnmpSession;
import com.adventnet.snmp.snmp2.SnmpVar;

/*Searches for neighbours and 
 * Interface numbers as well as the according Descriptions
 * Returns Arrays
 */
public class SearchAgent {
	//References
	private TableAgent tAgent=null;
	private SnmpOperator operator=null;
//Lists
	private List <SnmpVar> localIPsList=new ArrayList <SnmpVar>();;
	private List <SnmpVar> atIPsList=new ArrayList <SnmpVar>();;
	private List <SnmpVar> neighList=new ArrayList <SnmpVar>();
	private List <SnmpVar> intList=new ArrayList <SnmpVar>();
	private List <SnmpVar> atIntList=new ArrayList <SnmpVar>();
	private List <SnmpVar> ipIntList=new ArrayList <SnmpVar>();
	private List <SnmpVar> ifInOctetsList=new ArrayList <SnmpVar>();
	private List<SnmpVar> varList= new ArrayList <SnmpVar>();
//OIDS
	private SnmpOID ipIPs= new SnmpOID(".1.3.6.1.2.1.4.20.1.1");
	private SnmpOID atIPs=new SnmpOID(".1.3.6.1.2.1.3.1.1.3");
	private SnmpOID atInt=new SnmpOID(".1.3.6.1.2.1.3.1.1.1");
	private SnmpOID allInt=new SnmpOID(".1.3.6.1.2.1.2.2.1.1");;
	private SnmpOID intDescr= new SnmpOID(".1.3.6.1.2.1.2.2.1.2");
	private SnmpOID ipInt= new SnmpOID(".1.3.6.1.2.1.4.20.1.2");
	private SnmpOID ifInOctets = new SnmpOID(".1.3.6.1.2.1.2.2.1.10");
	private SnmpOID sysObjectID= new SnmpOID(".1.3.6.1.2.1.1.2.0");
	private SnmpOID sysName= new SnmpOID(".1.3.6.1.2.1.1.5.0");
//Arrays
	private SnmpVar neighbours [][]=null;
	private SnmpVar localIPs[][]=null;
	private SnmpVar noIPsInt[][]=null;

	//Constructor
	public SearchAgent(TableAgent tAgent, SnmpOperator operator){
		this.tAgent=tAgent;
		this.operator=operator;
	}

	//Searches for neighbours not for the according interfaces, 
	//has to be executed before getNeighbours
	public void searchNeighbours(SnmpPDU pdu, SnmpSession session){
		//lets get atIPs and local IPs and then compare them
		//All IPs
		atIPsList.clear();
		atIPsList.addAll(tAgent.getColumn(pdu, session, atIPs));

		//Local IPS
		localIPsList.clear();
		localIPsList.addAll(tAgent.getColumn(pdu, session, ipIPs));	
		//NeighList cleared
		neighList.clear();
		//compare
		for(int i=0;i < atIPsList.size();i++){
			if(!localIPsList.contains(atIPsList.get(i))){
				neighList.add(atIPsList.get(i));

			}
		}
	}
	//Get the int number and int description for the neighbours. Returns an array
	public SnmpVar [][] getNeighbours(SnmpPDU pdu, SnmpSession session){
		//search for the interfaces in the ! AT Table! and put it together
		atIntList=tAgent.getColumn(pdu, session,atInt , atIPsList.size());
		//put it in one array
		neighbours=new SnmpVar [neighList.size()][3];
		for(int r=0;r<neighList.size();r++){
			//The ip
			neighbours[r][2]=neighList.get(r);
			//The interface number from the at Table
			neighbours[r][0]=atIntList.get((atIPsList.indexOf(neighList.get(r))));
			//The Interface description from the INT Table
			//get them with normal get. Take the OID and add the number of the Interface as Index
			neighbours[r][1]=operator.get(pdu, session, new SnmpOID(intDescr.toString()+"."+neighbours[r][0].toString())).getVariable(0);
		}
		return neighbours;	
	}
	//Returns an List of the neighbours (search and getNeighbours has to be executed first)
	public List<String> getNeighList(){
		List<String> neighStringList=new ArrayList<String>();
		for(int i=0;i<neighList.size();i++){
			neighStringList.add(neighList.get(i).toString());
		}
		return neighStringList;
	}

	//Get Ips returns IPlist Array
	public SnmpVar [][] getIPs(SnmpPDU pdu, SnmpSession session){
		localIPsList.clear();
		ipIntList.clear();
		//Local IPs
		localIPsList.addAll(tAgent.getColumn(pdu, session, ipIPs));
		//Interfaces
		ipIntList.addAll(tAgent.getColumn(pdu, session, ipInt, localIPsList.size()));
		//Put it together
		localIPs=new SnmpVar [localIPsList.size()][3];
		for(int r=0;r<localIPsList.size();r++){
			//The ip
			localIPs[r][2]=localIPsList.get(r);
			//The interface number from the ip Table
			localIPs[r][0]=ipIntList.get(r);
			//The Interface description from the INT Table
			//get them with normal get. Take the OID and add the number of the Interface as Index NOT SURE YET ???? (IP as String)
			localIPs[r][1]=operator.get(pdu, session, new SnmpOID(intDescr.toString()+"."+localIPs[r][0].getNumericValueAsString())).getVariable(0);
		}
		return localIPs;
	}
	//Returns IPList
	public List<String> getIPList(){
		List<String> ipStringList=new ArrayList<String>();
		for(int i=0;i<localIPsList.size();i++){
			ipStringList.add(localIPsList.get(i).toString());
		}
		return ipStringList;
	}
	//Get Interfaces without IPs (loopbakcs interfaces in down state)
	public SnmpVar [][] getNoIPInt(SnmpPDU pdu, SnmpSession session,List<Integer> intNrList){
		//from the Interface List 
		intList.clear();
		intList.addAll(tAgent.getColumn(pdu, session, allInt));
		//add number to nrofintefaces list
		intNrList.add(intList.size());
		//from the IP List 
		ipIntList.clear();
		ipIntList.addAll(tAgent.getColumn(pdu, session, ipInt));
		//check sizes 
		if(intList.size()>ipIntList.size()){
			//How big shall the list for this interfaces be
			noIPsInt=new SnmpVar[intList.size()-ipIntList.size()][2];
			//Get the descriptions as we only need 
			for(int r=0; r<(intList.size()-ipIntList.size());r++){	
				//The Number of the Interface through comparing
				for(int i=0;i <intList.size();i++){
					if(!ipIntList.contains(intList.get(i))){
						noIPsInt[r][0]=intList.get(i);
					}
				}

				//The Interface description from the INT Table
				//get them with normal get as int number index. Take the OID and add the number of the Interface as Index 
				noIPsInt[r][1]=operator.get(pdu, session, new SnmpOID(intDescr.toString()+"."+Integer.valueOf(noIPsInt[r][0].getNumericValueAsString()))).getVariable(0);
			}		
		}else{
			noIPsInt=new SnmpVar[0][0];
		}
		return noIPsInt;
	}
	//Get the IFInOctets and the time of request on the router
	public BigDecimal[] getIfInOctets(SnmpPDU pdu, SnmpSession session,int repetitions){
		BigDecimal sumIfInOctets=new BigDecimal("0");
		BigDecimal sysUpTime=new BigDecimal("0");
		//clear all used Lists
		ifInOctetsList.clear();
		varList.clear();
		//Add all in octests to the list
		//!!we give the sys object ID cause the implementation does no allow a nonscalar parameter for getBulk
		varList.addAll(tAgent.getColumnAndTime(pdu, session, sysObjectID,ifInOctets, repetitions));
		//We get a List with the octets and the time at the very end
		sysUpTime=new BigDecimal(varList.get(varList.size()-1).getNumericValueAsString());
		varList.remove(varList.size()-1);
		ifInOctetsList.addAll(varList);
		
		//Sum them up 
		for(int i=0;i<ifInOctetsList.size();i++){
			sumIfInOctets=sumIfInOctets.add(new BigDecimal(ifInOctetsList.get(i).toString()));
		}
		BigDecimal[] octetsTimeArray={sumIfInOctets,sysUpTime};
		
		return octetsTimeArray;
		
	}
	//Get the Hostname of an SNMP device
	public String getHostName(SnmpPDU pdu, SnmpSession session){
		String hostName=null;
		hostName=operator.get(pdu, session, sysName).getVariable(0).toString();
		return hostName;
	}
	
	
	
}



