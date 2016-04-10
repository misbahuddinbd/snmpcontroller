/* Calculates variable intervals 
 * for histograms and calculates
 * estimation error*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class VariableBinsAgent {
	// Initialize variables
	List <BigDecimal> []binArray; // A list array containing lists that contains centroids and values assigned to them
	int nrOfBins;
	Random rGenerator = new Random();
	BigDecimal result=new BigDecimal("0");
	List <BigDecimal> routerLoadsList=new ArrayList <BigDecimal>();
	List <BigDecimal> compareCentroidsList=new ArrayList <BigDecimal>();
	List <BigDecimal> minDistList =new ArrayList <BigDecimal>();
	List <BigDecimal> centroidsList=new ArrayList <BigDecimal>();
	List <BigDecimal> constantLoadsList=new ArrayList <BigDecimal>();
	List <List<BigDecimal>> finalBinList = new ArrayList <List<BigDecimal>>();

	@SuppressWarnings("unchecked")
	public List<List<BigDecimal>> getVariableBins(List <BigDecimal> routerLoadsList, int nrOfBins){
		
		//make a copy as we modify
		this.routerLoadsList.clear();
		//clear everything
		compareCentroidsList.clear();
		minDistList.clear();
		centroidsList.clear();
		constantLoadsList.clear();
		finalBinList.clear();
		//add the router loads
		this.routerLoadsList.addAll(routerLoadsList);
		//set a fixed list with router loads values
		constantLoadsList.addAll(routerLoadsList);
		this.nrOfBins=nrOfBins;
		//Set the arrays
		
		//create the array
		binArray=new ArrayList[nrOfBins];
		for(int i=0;i<binArray.length;i++){
			binArray[i]=new ArrayList <BigDecimal>();
		}

		//execute getInitialcentroids
		calculateInitialCentroids();

		//Loop through the algortihm until centroids are stable. While max 5000 repeats should be enough for good result. 
		for(int i=0;i<5000&&!compareCentroidsList.containsAll(centroidsList);i++){
			//call methods
			calculateDistance();
			calculateMinValue();
			//clear list
			minDistList.clear();
		}//end for

		// Remove the centroids form the result. If centroid values are not equal to actual values
		// remove them, only actual values are left.
		for (int j = 0; j< centroidsList.size(); j++){

			if (centroidsList.contains(binArray[j].get(0)) && !constantLoadsList.contains(centroidsList.get(j))) {
				binArray[j].remove(0);
			}

		}
		
		//Calculate estimation error as the difference between real and assigned values			
		System.out.println("Quantization Error: " +calculateEstimationError(centroidsList,binArray));
		//before returning the list delete empty buckets
		for(int i=0;i<binArray.length;i++){
			if(!binArray[i].isEmpty()){
				finalBinList.add(binArray[i]);
			}else{
				//if empty delete from centroidslist
			try {
				centroidsList.remove(i);
			}catch(Exception e){
		}
			}
		}
		
		System.out.println("Center values (bits/s): "+Arrays.toString(centroidsList.toArray()));		
				
	
		
		return finalBinList;
	}

	private void calculateInitialCentroids(){
		//used for getting a random centroid from the list
		int randomIndex; 
		
		while(centroidsList.size()<nrOfBins){
			// Get random values as centroid values and put them in the list
			randomIndex = rGenerator.nextInt(routerLoadsList.size());
			if (!centroidsList.contains(routerLoadsList.get(randomIndex))){	                        
				centroidsList.add(routerLoadsList.get(randomIndex));
				// Remove centroids from initial computations as they are identical to actual values
				for (int i =0; i < centroidsList.size(); i ++){
					routerLoadsList.remove(centroidsList.get(i));
				}
			}
		}
	}
	
	/* Calculating difference between values in routerLoadList and values in centroidList. */																			
	private void calculateDistance(){
		minDistList.clear();
		BigDecimal distance = new BigDecimal("0");
		
		for (int i = 0; i < centroidsList.size(); i++){
			for (int j = 0; j < routerLoadsList.size(); j++) {	
				//substract the values and get difference and put it in the minDistList
				distance = centroidsList.get(i).subtract(routerLoadsList.get(j)).abs();
				if (distance.compareTo(new BigDecimal("0"))!=0){
					minDistList.add(distance);	
				}
			}					
		}
	}
	
	/* Sorting the values from routerLoadList with the values from centroidList. */ 
	private void calculateMinValue(){
		// Initializing variables
		List <BigDecimal> minValueList=new ArrayList <BigDecimal>();
		Object obj = null;
		BigDecimal centroid=new BigDecimal("0");
		
		//Clear all lists from binarray lists (Nr of Lists in binarray List = centroidList size) and add centroids.
		for(int i=0;i<nrOfBins;i++){
			binArray[i].clear();
			binArray[i].add(centroidsList.get(i));
		}
		
		// Iterate through values in the minDistList (minDistList: list containing all distances from all centroids)
		// and extract all indexes that belong to difference centroid[j] - value[k] (k=0...routerLoadsList.size()). 
		// Put values that represent centroid[j] - value[k] into a list with minimum distances (minValueList) from all 
		// centroids to a particular value.
		
		for (int j = 0; j < routerLoadsList.size(); j++) {
			for(int i = 0; i < minDistList.size(); i++){
				if (i%routerLoadsList.size()-j==0){
					minValueList.add(minDistList.get(i));	
				}
			}

			//no typecast necessary for an object as we will need only objects index and not its value
			try {
				// Take the minimum value from the minimumValue List 
				obj=Collections.min(minValueList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Add value to the minValue List (here typecast necessary)
			minValueList.add((BigDecimal) obj);
			//Get the index of the minimum value for associating it with particular centroids - find position and fill Bins
			for (int k = 0; k < binArray.length; k++){
				if (minValueList.indexOf(obj) == k){
					binArray[k].add(routerLoadsList.get(j));

				}//end for 	
			}
			//sort the router loads list
			Collections.sort(routerLoadsList);

			//clear the min Value List
			minValueList.clear();		
		}//end for
		//clear the routerLoadsList to fill it from the bins again 
		routerLoadsList.clear();
		//Fill in the list with all the values which were from the beginning
		routerLoadsList.addAll(constantLoadsList);
		Collections.sort(routerLoadsList);
		
	
		//Removing centroids from calculations
		for (int i =0; i < centroidsList.size(); i ++){
			routerLoadsList.remove(centroidsList.get(i));
		}//end for
		
		//clear the compareCentroidslist (List for comparing previous centroids with current centroids) and fill it again
		compareCentroidsList.clear();
		compareCentroidsList.addAll(centroidsList);

		// Calculate new centroids as an average between max and min value in BinsLists	
		for (int i = 0; i < binArray.length; i++){
			try {
				centroid =Collections.max(binArray[i]).add(Collections.min(binArray[i])).divide(new BigDecimal("2"),0,RoundingMode.HALF_UP);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (centroidsList.get(i) != centroid){
				centroidsList.remove(i);
				centroidsList.add(i,centroid);			
			}
		}
		Collections.sort(centroidsList);
		

	}//end method 

	
	// Calculating Estimation error
	public BigDecimal calculateEstimationError (List <BigDecimal> centroidsList,List <BigDecimal> []binArray){
		
		//Initializing Local Variables
		BigDecimal sum =new BigDecimal("0");
		
		List <BigDecimal> estimationErrorList=new ArrayList <BigDecimal>();

		for (int j = 0; j < centroidsList.size(); j++  ){
			for (int k = 0; k < binArray[j].size(); k++){ 
				if (!centroidsList.contains(binArray[j].get(k))){
					result = centroidsList.get(j).subtract(binArray[j].get(k));
					result=result.add(result);
					estimationErrorList.add(result.divide(new BigDecimal(binArray[j].size()),3,RoundingMode.HALF_UP).abs()); 
				}    
			}  
		}
		//sum them up and divide
		for (int i=0;i<estimationErrorList.size();i++){
			sum=sum.add(estimationErrorList.get(i));
		}
		result = sum.divide(new BigDecimal(centroidsList.size()),0,RoundingMode.HALF_UP);
		
		return result;
	}


}//end class



