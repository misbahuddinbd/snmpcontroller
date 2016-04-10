
/*Prepares SNMP packets (get, getnext, bulk ....)
 * and returns the response PDU
 */

import com.adventnet.snmp.snmp2.*;

public class SnmpOperator {
	private SnmpPDU responsePDU = null;

	//GET
	public SnmpPDU get(SnmpPDU pdu, SnmpSession session, SnmpOID oid){
		pdu.setCommand( SnmpAPI.GET_REQ_MSG);
		pdu.removeVariableBinding(0);
		pdu.addNull(oid);
		try {
			responsePDU = session.syncSend(pdu);
		} catch (SnmpException e) {
			System.out.println("SNMPOperator get");
			e.printStackTrace();
		}
		return responsePDU;
	}
	
	//GETNEXT
	public SnmpPDU getnext(SnmpPDU pdu, SnmpSession session,SnmpOID oid){
		pdu.setCommand(SnmpAPI.GETNEXT_REQ_MSG);
		pdu.removeVariableBinding(0);
		pdu.addNull(oid);
		try{
			responsePDU = session.syncSend(pdu);
		} catch (SnmpException e) {
			System.out.println("SNMPOperator getnext");
			e.printStackTrace();
		}
		return responsePDU;
	}
	
	//GETBULK
	public SnmpPDU getbulk(SnmpPDU pdu, SnmpSession session,SnmpOID oid, int repetitions){
		pdu.setCommand( SnmpAPI.GETBULK_REQ_MSG);
		pdu.setMaxRepetitions(repetitions);
		pdu.removeVariableBinding(0);
		pdu.addNull(oid);
		try{
			responsePDU = session.syncSend(pdu);
		} catch (SnmpException e) {
			System.out.println("SNMPOperator getbulk");
			e.printStackTrace();
		}
		return responsePDU;
	}
	
	//Extended GETBULK
	public SnmpPDU getSeveralBulk(SnmpPDU pdu, SnmpSession session,SnmpOID oidSingle,SnmpOID oidBulk, int repetitions){
		pdu.setCommand(SnmpAPI.GETBULK_REQ_MSG);
		pdu.setNonRepeaters(1);//which one is executed only once 
		pdu.setMaxRepetitions(repetitions);
		pdu.removeVariableBinding(0);
		pdu.addNull(oidSingle);
		pdu.addNull(oidBulk);
		try{
			responsePDU = session.syncSend(pdu);
		} catch (SnmpException e) {
			System.out.println("SNMPOperator getbulk");
			e.printStackTrace();
		}
		return responsePDU;
	}

}
