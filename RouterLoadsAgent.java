
/*
 * Calculates Router Loads and returns the values.
 */
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.adventnet.snmp.snmp2.SnmpException;
import com.adventnet.snmp.snmp2.SnmpPDU;
import com.adventnet.snmp.snmp2.SnmpSession;

//Gets the Router Loads from all Router in the supplied list
public class RouterLoadsAgent {
	//Refrences
	ConnectionManager conmanager=null;
	SearchAgent sagent=null;
	//Initialized in Constructor basic connection details
	String username=null;
	String password=null;
	//The variables we need
	SnmpSession session = null;
	SnmpPDU pdu=null;
	// Connection
	Object conDetails[]=null;
	//Time Values
	//long values (not used for calculations)
	long startTime=new Long("0"); //start of the Histogram part
	long firstSampleTime=new Long("0");//just for user information not used in calculations!
	//BigINt used for calculation
	BigDecimal startCollectTime=new BigDecimal("0");
	BigDecimal stopCollectTime=new BigDecimal("0");
	
	//Lists
	List <BigDecimal> firstOctetList=new ArrayList <BigDecimal>();//For summing up the Octets
	List <BigDecimal> secondOctetList=new ArrayList <BigDecimal>();//For summing up the Octets
	List <BigDecimal> collectTimeList=new ArrayList <BigDecimal>();//Storing the collecting times per router IN SECONDS
	List <BigDecimal> routerLoadsList=new ArrayList <BigDecimal>();//Collects the results for the loads from all routers
	List <BigDecimal> firstTimeList=new ArrayList <BigDecimal>();//Collects the results for the loads from all routers
	List <BigDecimal> secondTimeList=new ArrayList <BigDecimal>();//Collects the results for the loads from all routers
	
	//Array for getting the octets in combination with the sysuptime
	BigDecimal [] octetTimeArray=null;
	
	//Constructor
	public RouterLoadsAgent(SearchAgent sagent,ConnectionManager conmanager,String username, String password){
		this.sagent=sagent;
		this.conmanager=conmanager;
		this.username=username;
		this.password=password;
	}

	//Only method
	public List<BigDecimal> getRouterLoads(List <String> routerList,List<Integer> intNrList, long sampleDistance){
		System.out.print("---Start collecting: ");
		//clear the list
		routerLoadsList.clear();
		secondOctetList.clear();
		
		//the loop defining the time window of the whole measurement and printing (after that print finished)
		for(int i=0;i<2;i++){
			//clear the collectTimeList and firstOctetList to use it empty
			collectTimeList.clear();
			firstOctetList.clear();
			firstTimeList.clear();
			
			//Visit all router (Router List ) and get the IfinOctets
			startTime=System.currentTimeMillis();//Just info
			for(int j=0;j<routerList.size();j++){
				try {
					//New Connection
					conDetails = conmanager.establishConnection(routerList.get(j),username,password );
					session = (SnmpSession) conDetails[0];
					pdu=(SnmpPDU) conDetails[1];
					//get the octets and time
					octetTimeArray=sagent.getIfInOctets(pdu, session,intNrList.get(j));
					firstOctetList.add(octetTimeArray[0]);
					firstTimeList.add(octetTimeArray[1]);
					//close
					conmanager.terminateConnection(session);
				} catch (SnmpException e) {
					e.printStackTrace();
				}//end catch
			}// end for
			
			//To avoid calculates at the first measurement. If nothing to compare skip calculations
			if(!secondOctetList.isEmpty()){
				//start calculations
				for(int k=0;k<firstOctetList.size();k++){
					//(sample(firstList)-sample(secondList))/(colllection time for this router+sample Frequency) and multiply with 8 for Bits/s
					routerLoadsList.add(firstOctetList.get(k).subtract(secondOctetList.get(k))
										.divide(firstTimeList.get(k).subtract(secondTimeList.get(k)).divide(new BigDecimal("100"),3,RoundingMode.HALF_UP),3,RoundingMode.HALF_UP)//divide by 100 as this are timeticks
										.multiply(new BigDecimal("8")).setScale(0, RoundingMode.HALF_UP));//rounding
				}//end for
				System.out.print(" second sample collected in: "+new Long(System.currentTimeMillis()-firstSampleTime-sampleDistance)+"ms ");
				System.out.println("");
			}else{
				//Print out second measurement start
				firstSampleTime=System.currentTimeMillis();
				System.out.print(" first sample collected in: "+new Long(firstSampleTime-startTime)+"ms ");
			}
			//Delete
			secondOctetList.clear();
			secondTimeList.clear();
			//Fill the second octet list with the current samples 
			secondOctetList.addAll(firstOctetList);
			secondTimeList.addAll(firstTimeList);

			//Sleep for defined time distance between samples
			try {
				Thread.currentThread();
				Thread.sleep(sampleDistance);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}//end for

		return routerLoadsList;
	}


}
