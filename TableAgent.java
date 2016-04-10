
/*gets information from tables 
 * returns lists
 */

import java.util.ArrayList;
import java.util.List;
import com.adventnet.snmp.snmp2.*;

public class TableAgent {
	//Needed Variables
	private SnmpOperator operator;
	private List<SnmpVar> varList= new ArrayList <SnmpVar>(); //for returning values
	private SnmpVar var = null;
	private SnmpPDU responsePDU=null;
	
	//Constructor set the SNMP Operator
	public TableAgent(SnmpOperator operator){
		this.operator=operator;
	}
	
	//returns a column with getnext
	public List<SnmpVar> getColumn(SnmpPDU pdu, SnmpSession session,SnmpOID oid){
		//The OID of the column (for comparison)
		String columnOID=oid.toString();
		//empty VarList
		if(varList!=null){
			varList.clear();
		}
		//First round
		responsePDU=operator.getnext(pdu, session, oid);
		var=responsePDU.getVariable(0);
		//Set OID to next value (get next)
		oid=responsePDU.getObjectID(0);
		//As long as the next oid is bigger (length) then the column oid go on and check whever the column oid does not change
		while(oid.toString().length()>columnOID.length()&&columnOID.equals(oid.toString().substring(0,columnOID.length()))){
			//Add the variable to the List
			varList.add(var);
			//OK get next
			responsePDU=operator.getnext(pdu, session, oid);
			var=responsePDU.getVariable(0);
			//Change OID to be able to jump to next value
			oid=responsePDU.getObjectID(0); 
		}	
		return varList;
	}
	
	//With getbulk (faster)
	public List<SnmpVar> getColumn(SnmpPDU pdu, SnmpSession session,SnmpOID oid,int repetitions){
		varList.clear();
		responsePDU=operator.getbulk(pdu, session, oid, repetitions);
		for(int x=0;x<repetitions;x++){
				varList.add(responsePDU.getVariable(x));
			}
		return varList;
	}
	
	//Get bulk and time (for histograms)
	public List<SnmpVar> getColumnAndTime(SnmpPDU pdu, SnmpSession session,SnmpOID oidTime, SnmpOID oidBulk,int repetitions){
		varList.clear();
		responsePDU=operator.getSeveralBulk(pdu, session, oidTime,oidBulk, repetitions);
		for(int x=1;x<repetitions;x++){
				varList.add(responsePDU.getVariable(x));
			}
		//Ad the Time at the end
		varList.add(responsePDU.getVariable(0));
		return varList;
	}
	

}
