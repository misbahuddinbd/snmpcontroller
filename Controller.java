
/*MAIN CLASS
 * Creates all Objects, gives print commands and manages the operations.
 */

import java.io.Console;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;




import com.adventnet.snmp.snmp2.SnmpException;
import com.adventnet.snmp.snmp2.SnmpPDU;
import com.adventnet.snmp.snmp2.SnmpSession;

public class Controller {
	@SuppressWarnings("unchecked")
	public static void main (String args[]){
		//SNMP Port
		int port = 161;
		//Parameters for connection provided by console
		String firstHost=null;
		String username = null;
		String password=null;
		try {
			firstHost = args[0];
			username = args[1];
			password = args[2];
		} catch (Exception e1) {
			System.out.println("Wrong Input!");
			System.out.println("Input as follows: IP username password");
			System.exit(0);
		}

		//Intitialize the classes
		Printer printer = new Printer();
		ConnectionManager conmanager= new ConnectionManager(port);
		SnmpOperator operator = new SnmpOperator();
		TableAgent tagent=new TableAgent(operator);
		SearchAgent sagent = new SearchAgent(tagent,operator);
		RouterLoadsAgent rloadagent = new RouterLoadsAgent(sagent,conmanager,username,password);
		VariableBinsAgent binsAgent=new VariableBinsAgent();

		//The console
		Console console=System.console();

		//The variables we need
		SnmpSession session = null;
		SnmpPDU pdu=null;
		String engineIDString=null;
		String currentIP=null; //the current IP which the connection is established to
		String ip=""; //used for comparisons of IPs
		
		//Lists
		List <String> visitedList=new ArrayList <String>();//all visited Routers (all IPS of these routers)
		List <String> stillToVisitList=new ArrayList <String>(); //Routers that have to be visited
		List <String> noSNMPList=new ArrayList <String>();//for none SNMP devices
		List <String> routerList=new ArrayList <String>();//router IPS, one IP per router
		List <BigDecimal> routerLoadsList=new ArrayList <BigDecimal>();//Collects the results for the loads from all routers
		List <Integer> intNrList=new ArrayList <Integer>();//Numbers of the Interfaces on a defined router
		//Error calculation
		List <BigDecimal> []binArray;	
		List <BigDecimal> centroidsList=new ArrayList <BigDecimal>();
		
		//sets (no duplicates)
		Set<String> allNeighSet = new LinkedHashSet<String>();  //contains all neighbours in the network so all nodes and IPS in the nework
		
		// Connection
		Object conDetails[]=null;
		String hostName=null;
		
		//Measurement options
		String histArray[][]=null;
		String nrOfMeasurements=null;
		String measureFrequency=null;
		String sampleDistance=null;
		String nrOfBins="5";//standard implemention
		long sampleFrequency=new Long("0");
		long startMeasurementTime=new Long("0");//for checking the time between measurements 
		long stopMeasurementTime=new Long("0");//measuremnet took longer then distance start next one immediately
		
		//Octet comparison variables
		BigDecimal lowestOctet;
		BigDecimal biggestOctet;
		
		//Graph parameter
		BigDecimal binWidth=new BigDecimal("0");
		BigDecimal [] xAxesArray=null;//values of x axes
		int[] counterArray=null;//counter for the xaxes
		int biggestCounter=0;//coints matches for the bins
		
		
		//Initialize the ipList with an IP to start
		stillToVisitList.add(firstHost);
		
		//Inititalize visitCounter counter to jump non SNMP devices
		int visitCounter=0;
		
		//Print Header
		printer.printSeperator("DISCOVERY STARTED");
		
		
		//-------------------------------------Network Discovery-----------------------------
		// As long as there are addresses in the stilltoVisitList go on
		while(stillToVisitList.size()!=0){
			//try{
			boolean established=false;
			//as long as there is no established connection try it
			while(established==false){
				//go through the List till a working IP is found
				if(stillToVisitList.size()!=1){
					visitCounter++;
				}
				try {
					currentIP=stillToVisitList.get(visitCounter);
					conDetails = conmanager.establishConnection(currentIP,username,password );
					session = (SnmpSession) conDetails[0];
					pdu=(SnmpPDU) conDetails[1];
					//EngineID 
					engineIDString=(String)conDetails[2];
					//If no Exception occured till now then it worked so established
					established=true;
				} catch (SnmpException e) {
					//Its not accessable therefore delete from List
					noSNMPList.add(currentIP);
					stillToVisitList.remove(currentIP);
					//Check whever end is reached
					if(stillToVisitList.size()==0){
						break;
					}
					//if a field is deleted go back one with the counter but only if the counter is not 0 (-1 Exception)
					if(visitCounter!=0){
						visitCounter--;
					}
				}//end catch

			}//connection established 

			//only do if List is not 0 yet
			if(stillToVisitList.size()!=0){
				//Add ip to Router List which shows the IPS of accessable routers
				routerList.add(currentIP);
				//Get  details 
				hostName=sagent.getHostName(pdu, session);
				//reset visit counter
				visitCounter=0;
				
				//PRINT
				//Device
				printer.printTitel(hostName, currentIP,engineIDString);
				//IPS
				printer.printHead("LOCAL IPS");
				printer.printDescr("Int Nr.", "Inte Desc.", "IP");
				printer.print2DArray(sagent.getIPs(pdu, session));
				//No IP Int
				printer.printHead("NO IP INT");
				printer.printDescr("Int Nr.", "Inte Desc.","");
				printer.print2DArray(sagent.getNoIPInt(pdu, session,intNrList));
				//Neighbours
				printer.printHead("NEIGHBOURS");
				printer.printDescr("Int Nr.", "Inte Desc.", "IP");
				//Search for neighbours
				sagent.searchNeighbours(pdu, session);
				//Print the Neighbours Array
				printer.print2DArray(sagent.getNeighbours(pdu, session));
				//End of Printing

				//Get the neighbour and IP list
				allNeighSet.addAll(sagent.getNeighList());
				//all the local IPS to the visited list to avoid duplicate visit
				visitedList.addAll(sagent.getIPList());
				//Clear the still to visitlist
				stillToVisitList.clear(); 
				//Fill still to visit list new
				//As the allNeighList is a set we have to use an iteraotr to traverse it. 
				for(Iterator<String> iterator=allNeighSet.iterator();iterator.hasNext();ip=iterator.next()){
					if(!visitedList.contains(ip)&& !stillToVisitList.contains(ip)&& !noSNMPList.contains(ip)){
						stillToVisitList.add(ip);
					}
				}
			}//end if

			//Close 
			if(conmanager.terminateConnection(session)==true){
			}else{
				System.out.println("Session can not be terminated");
			}	
		}//end for, end of router search
	
		//Print information 
		printer.printSeperator("DISCOVERY FINISHED");
		//sort list
		Collections.sort(routerList);
		Collections.sort(noSNMPList);
		//Print Network informations
		System.out.println("SNMP devices in Network:     "+ Arrays.toString(routerList.toArray()));
		System.out.println("Non SNMP devices in Network: "+ Arrays.toString(noSNMPList.toArray()));

		//-------------------------------------------End of Discovery------------------------------------------------//
		/* APPLICATION PART 2 */						
		/*********************************/
		/**** Router Load Computation ****/
		/*********************************/
		//Print information 
		printer.printSeperator("PRINT HISTOGRAMMS");
		//initialize to avoid null pointer exception
		measureFrequency="3";
		sampleDistance="0";
		//go on till exit
		while(!measureFrequency.equals("exit")){
			try{
				//USER INPUT ....
				printer.printHead("USER INPUT");
				System.out.println("");
				System.out.println("Define the distance between measurements in sec (distance between Histogram creations) or type 'exit'");
				measureFrequency=console.readLine();

				//to avoid execution if exit is typed
				if(!measureFrequency.equals("exit")){
					//More INPUT
					System.out.println("Define the distance between samples in seconds (0=as fast as possible).");
					sampleDistance=console.readLine();
					
					System.out.println("Define the number of bars (bins)");
					nrOfBins=console.readLine();
					
					System.out.println("Define the number of histogramms to print (repetitions of measurement)");
					nrOfMeasurements=console.readLine();
					
					//sampleDistance in seconds therefore convert to ms
					sampleFrequency=new Long(sampleDistance+"000");
					
					

					//How often should be measured go for it if number bigger 1 sleep for defined measureFrequency
					for(int l=0; l<Integer.parseInt(nrOfMeasurements);l++){
						//Print seperator
						printer.printSeperator("MEASUREMENT");
						//as its a loop clear
						routerLoadsList.clear();
						centroidsList.clear();
						//for comparison to work
						biggestCounter=0;
						//Nr of Bins is defined therefore - reset afetr first
						binArray=new ArrayList[Integer.parseInt(nrOfBins)];
						for(int i=0;i<binArray.length;i++){
							binArray[i]=new ArrayList <BigDecimal>();
						}
						//+++++++++++++++++++++Get values from RouterLoadAgent and start timer ++++++++++++++++++++
						startMeasurementTime=System.currentTimeMillis();
						routerLoadsList.addAll(rloadagent.getRouterLoads(routerList,intNrList,sampleFrequency));
						stopMeasurementTime=System.currentTimeMillis();
						//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
						//Calculating the bars and creating a 2DArray
						//find largest and smallest value
						//initialize with a value to avoid 0 lowest value
						lowestOctet=routerLoadsList.get(0);
						biggestOctet=routerLoadsList.get(0);

						//biggest
						for(int i=0;i<routerLoadsList.size();i++ ){
							//if smaller then it returns -1
							if(biggestOctet.compareTo(routerLoadsList.get(i))==-1){
								biggestOctet=routerLoadsList.get(i);
							}
							//the lowest value
							if(lowestOctet.compareTo(routerLoadsList.get(i))==1){
								lowestOctet=routerLoadsList.get(i);
							}
						}//end for

						//First lets sort the fields into the ranges (number of bins)
						binWidth=biggestOctet.subtract(lowestOctet).divide(new BigDecimal(nrOfBins),0,RoundingMode.HALF_UP);
						//Create the arrays needed
						xAxesArray=new BigDecimal[Integer.parseInt(nrOfBins)];
						counterArray=new int[Integer.parseInt(nrOfBins)];
						//centroitsArray=new BigDecimal[Integer.parseInt(nrOfBins)];

						//Fill the xarray (xaxes)
						xAxesArray[0]=lowestOctet;
						for(int i=1;i<xAxesArray.length;i++){
							xAxesArray[i]=xAxesArray[i-1].add(binWidth);
						}

						//Count the matches in the different categories
						for(int i=0;i<routerLoadsList.size();i++){
							for(int j=0;j<xAxesArray.length;j++){
								if(routerLoadsList.get(i).compareTo(xAxesArray[xAxesArray.length-1])>=0){
									counterArray[xAxesArray.length-1]++;
									binArray[xAxesArray.length-1].add(routerLoadsList.get(i));//put them in buckets
									break;
								}else{
									if(routerLoadsList.get(i).compareTo(xAxesArray[j])>=0&&routerLoadsList.get(i).compareTo(xAxesArray[j+1])<0){
										counterArray[j]++;
										binArray[j].add(routerLoadsList.get(i));//sum up all in a bin
										break;
									}
								}
							}//end for j
						}//end for i
						
						//calculate the centroits for error calculation
						for (int i=0;i<Integer.parseInt(nrOfBins)-1;i++){
							//centroitsArray[i]=xAxesArray[i].add(xAxesArray[i]).divide(new BigDecimal("2"),3,RoundingMode.HALF_UP);
							centroidsList.add(xAxesArray[i].add(xAxesArray[i+1]).divide(new BigDecimal("2"),0,RoundingMode.HALF_UP).abs());
						}
						

						//find the biggest Number of matches for dimensioning the 2D array
						for(int i=0;i<counterArray.length;i++){
							if(counterArray[i]>biggestCounter){
								biggestCounter=counterArray[i];
							}
						}
						//Create the 2D Array
						// fill the array with ""
						histArray=new String [biggestCounter][counterArray.length];
						for (int i =0; i < histArray[0].length; i++) {
							for (int j = 0; j < histArray.length; j++) {
								histArray[j][i]=" ";
							}//end for j
						}//end for i
						//set the counters i=column j=row .length =column ;[0].length = rows
						//go through the array horizontal
						for(int i=0;i<histArray[0].length;i++){
							//fill vertical
							for(int j=histArray.length-counterArray[i];j<histArray.length;j++){
								histArray[j][i]="+";
							}
						}

						//PRINT it and some Information !!!
						System.out.println( new java.util.Date() );
						System.out.println("Router in Network   :"+ Arrays.toString(routerList.toArray()));
						System.out.println("Router Loads (bit/s):"+(Arrays.toString(routerLoadsList.toArray())));
						//sort for better comparison in graph
						//sort the route rLoads List
						Collections.sort(routerLoadsList);
						System.out.println("Sorted RLoad (bit/s):"+(Arrays.toString(routerLoadsList.toArray())));
						System.out.println("Sample Distance: "+sampleDistance+"s");
						System.out.println("Highest Value  : "+biggestOctet+" bit/s  "+"Lowest Value: "+lowestOctet+" bit/s");
						printer.printHead("Common Hist.");
						System.out.println("Quantization Error: "+binsAgent.calculateEstimationError(centroidsList, binArray));
						
						BigDecimal errorCommonApproach = new BigDecimal ("0");
						errorCommonApproach = binsAgent.calculateEstimationError(centroidsList, binArray);
					
						System.out.println("Center Values (bits/s): "+Arrays.toString(centroidsList.toArray()));
						System.out.println("Width of Bins: "+binWidth);
						printer.printHistogram(histArray, xAxesArray, biggestOctet);
						//variable bins
						
						getVariableBins(routerLoadsList,binsAgent,Integer.parseInt(nrOfBins),printer,biggestOctet,errorCommonApproach);
						
						//Sleep for defined time to implement time distance between measurements. Not for the last one
						if(Integer.parseInt(nrOfMeasurements)>1&&Integer.parseInt(nrOfMeasurements)-1!=l){
							try {
								//check whether the measurement took longer than the distance between measurements. if yes go immediately
								if(stopMeasurementTime-startMeasurementTime<new Long(measureFrequency+"000")){
									Thread.currentThread();
									Thread.sleep(new Long(measureFrequency+"000")-(stopMeasurementTime-startMeasurementTime));
								}else{
									
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}//end for l
				}//end if 
			}catch(Exception e){
				System.out.println("Error occured, please try again");
				e.printStackTrace();
			}
		}//end while (sampleDistance exit)
		

		//Programme finished
		System.exit(0);
	}//end main
	
	@SuppressWarnings("unchecked")
	static void getVariableBins(List routerLoadsList, VariableBinsAgent binsAgent, int binNumber,Printer printer, BigDecimal biggestOctet, BigDecimal errorCommonApproach){
		printer.printHead("Variable Hist.");
		//For painting later
		BigDecimal[] binWidthArray=null;
		BigDecimal[] minBinValArray=null;
		//for painting
		int[] counterArray=null;//counter for the xaxes
		int biggestCounter=0;//coints matches for the bins
		//2d Array
		String histArray[][]=null;
		
		
			
		List <List<BigDecimal>> finalBinList = new ArrayList <List<BigDecimal>>();;	

		//++++++++++++++++++++++++++++++++++++++++Variable BINS+++++++++++++++++++++++++++++++++++++++++++++++++
		//***************************************optional ****************************************************
		
		finalBinList=binsAgent.getVariableBins(routerLoadsList, binNumber);
		BigDecimal errorVariableApproach = new BigDecimal ("0");
		errorVariableApproach = binsAgent.result;
		while (errorCommonApproach.compareTo(errorVariableApproach) == -1){
			
			System.out.println("Random initial centroids were improperly selected, recompute centroids");
			//finalBinList.clear();
			finalBinList=binsAgent.getVariableBins(routerLoadsList, binNumber);
			errorVariableApproach = binsAgent.result;
			//binsAgent.getVariableBins(routerLoadsList, binNumber);
		}
		//*****************************************************************************************
		
		//create array
		binWidthArray=new BigDecimal[finalBinList.size()];
		minBinValArray=new BigDecimal[finalBinList.size()];
	
		//Fill the counter array to send to printer later
		counterArray=new int[finalBinList.size()];
		for(int i=0;i<finalBinList.size();i++){
			counterArray[i]=finalBinList.get(i).size();
		}
		//------------------------2D Array creation----------------------
		//find the biggest Number of matches for dimensioning the 2D array
		for(int i=0;i<counterArray.length;i++){
			if(counterArray[i]>biggestCounter){
				biggestCounter=counterArray[i];
			}
		}
		//Create the 2D Array
		// fill the array with ""
		histArray=new String [biggestCounter][counterArray.length];
		for (int i =0; i < histArray[0].length; i++) {
			for (int j = 0; j < histArray.length; j++) {
				histArray[j][i]=" ";
			}//end for j
		}//end for i
		//set the counters i=column j=row .length =column ;[0].length = rows
		//go through the array horizontal
		for(int i=0;i<histArray[0].length;i++){
			//fill vertical
			for(int j=histArray.length-counterArray[i];j<histArray.length;j++){
				histArray[j][i]="+";
			}
		}
		//-------------------------------------------------------------------------
		
		
		
		//fill the binwidth array
		if (errorCommonApproach.compareTo(errorVariableApproach) == 1){
		for (int i = 0; i < finalBinList.size(); i++){
		    minBinValArray[i] = Collections.min(finalBinList.get(i));
			binWidthArray[i] = (Collections.max(finalBinList.get(i)).subtract(Collections.min(finalBinList.get(i))));
		}
		
		
		System.out.println("Width of the Bins (bits): "+Arrays.toString(binWidthArray));
		printer.printHistogram(histArray, minBinValArray, biggestOctet);
		}
		//System.out.println("minBin: "+Arrays.toString(minBinValArray));
		
		
	}
}
