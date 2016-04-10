
/* Printer Class for printing Arrays, 
 * Lists and other output 
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;

import com.adventnet.snmp.snmp2.SnmpVar;

public class Printer {

	public void printTitel(String hostName,String ip,String engineID){
		System.out.println("******************************************************************");
		System.out.println("*                                                                *");
		System.out.println("* "+"Name: "+hostName+"  "+"IP: "+ip+"  "+"EngineID: "+engineID+"  *");
		System.out.println("*                                                                *");
		System.out.println("******************************************************************");
	}
	public void printSeperator(String text){
		System.out.println("");
		System.out.println("-------------------------------------------------------------------------------------------------------");
		System.out.println("|                                         "+text+"                                           |");
		System.out.println("-------------------------------------------------------------------------------------------------------");
		System.out.println("");
	}

	public void printHead(String text){
		System.out.println("**********************");
		System.out.println("*     "+text+"      *");
		System.out.println("**********************");
	}
	public void printDescr(String c1,String c2, String c3){
		System.out.println("");
		System.out.println("---"+c1+"----"+c2+"----------"+c3+"----");
	}
	//For printing ARRAYS with several columns we need a special printer class
	public void print2DArray(SnmpVar twoDArray [][]){
		//Lets print 2D Array .length =rows ;[0].length = columns
		for (int i =0; i < twoDArray.length; i++) {
			for (int j = 0; j < twoDArray[i].length; j++) {
				System.out.print("     " + twoDArray[i][j]);
			}
			System.out.println("");
		}
		System.out.println("");
		System.out.println("");
	}

	//For strings
	public void print2DArray(String twoDArray [][]){
		//Lets print 2D Array .length =rows ;[0].length = columns
		for (int i =0; i < twoDArray.length; i++) {
			for (int j = 0; j < twoDArray[i].length; j++) {
				System.out.print("     " + twoDArray[i][j]);
			}
			System.out.println("");
		}
		System.out.println("");
		System.out.println("");
	}

	//How to print a Histogram
	public void printHistogram(String histArray [][],BigDecimal xAxesArray[],BigDecimal biggestOctet){
		String xScale="";
		//For better scale look at the smallest octet
		if(biggestOctet.compareTo(new BigDecimal("1000"))>0){
			//First vlaue round down rest up
			xAxesArray[0]=xAxesArray[0].divide(new BigDecimal("1000"),0,RoundingMode.DOWN);//round value
			for(int i=1; i<xAxesArray.length;i++){
				//check whetehr the next vlaue would look the same after rounding 
				
				if(i<xAxesArray.length-1&& xAxesArray[i+1].subtract(xAxesArray[i]).abs().compareTo(new BigDecimal("999"))<0){
					xAxesArray[i]=xAxesArray[i].divide(new BigDecimal("1000"),2,RoundingMode.HALF_UP);//round value
					xAxesArray[i+1]=xAxesArray[i+1].divide(new BigDecimal("1000"),2,RoundingMode.HALF_UP);//round value
					i++;
				}else{
				 xAxesArray[i]=xAxesArray[i].divide(new BigDecimal("1000"),0,RoundingMode.HALF_UP);//round value
				}
			
			
			xScale="Kbps";
		}
		}
		if(biggestOctet.compareTo(new BigDecimal("10000000"))>0){
			//First vlaue round down rest up
			xAxesArray[0]=xAxesArray[0].divide(new BigDecimal("10000000"),0,RoundingMode.DOWN);//round value
			for(int i=1; i<xAxesArray.length;i++){
				if(i<xAxesArray.length-1&& xAxesArray[i+1].subtract(xAxesArray[i]).abs().compareTo(new BigDecimal("999"))<0){
					xAxesArray[i]=xAxesArray[i].divide(new BigDecimal("10000000"),2,RoundingMode.HALF_UP);//round value
					xAxesArray[i+1]=xAxesArray[i+1].divide(new BigDecimal("10000000"),2,RoundingMode.HALF_UP);//round value
					i++;
				}else{
				 xAxesArray[i]=xAxesArray[i].divide(new BigDecimal("10000000"),0,RoundingMode.HALF_UP);//round value
				}
			}
			xScale="Mbps";
		}
		// if one number is bigger then three digits then use kbps


		//Printing
		System.out.println("");
		for (int i =0; i < histArray.length; i++) {
			//Numbers for y axes
			if((histArray.length-i)<10){
				System.out.print("  "+Integer.toString(histArray.length-i)+"|  ");
			}else{
				System.out.print(" "+Integer.toString(histArray.length-i)+"|  ");
			}
			//print 2 D array
			for (int j = 0; j < histArray[i].length; j++) {
				System.out.print(histArray[i][j]+"    ");
			}
			System.out.println("");
		}//end for i

		//Print line
		System.out.print("---|");
		for (int i =0; i < xAxesArray.length; i++) {
			System.out.print("----|");		
		}
		System.out.println("");
		//X number line
		System.out.print("   |  ");
		//The Xaxes
		System.out.print(xAxesArray[0]+"  ");
		for (int i =1; i < xAxesArray.length; i++) {
		//new
		
			if(xAxesArray[i].compareTo(new BigDecimal ("10"))<0){
						//For number with more than 2 digits --
				//is it a comma number ?
				if(xAxesArray[i].scale()>0){
					System.out.print(xAxesArray[i]+" ");
				}else{
				    System.out.print(xAxesArray[i]+"    ");
				}
			}else{
				if(xAxesArray[i].compareTo(new BigDecimal ("100"))<0){
					//For number with more than 2 digits --
					System.out.print(xAxesArray[i]+"   ");
					}else{
						if(xAxesArray[i].compareTo(new BigDecimal ("1000"))<0){
							System.out.print(xAxesArray[i]+"  ");
						}else{
							//Standard distabce
						System.out.print(xAxesArray[i]+" ");
						}
					}
				
			}
			
				
			
		}//end for
		System.out.print("  "+xScale);
		System.out.println("");
		System.out.println("");
	}


}



