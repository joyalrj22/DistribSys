import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Server implements ServerInterface{
	
	private int serverNum;
	private String values;
	private ArrayList<TimeStamp> valTS;
	private ArrayList<ArrayList<String>> updateLog;
	private TimeStamp replicaTS;
	private ArrayList<String> exOps;
	private ArrayList<ArrayList<TimeStamp>> tsTable;
	
	
	public Server(int serverNum) {
		this.serverNum = serverNum;
	}
	
	public void sendGossip() throws RemoteException{
		// send updateLog and replicaTS
	}
	
	public void recieveGossip(ArrayList<ArrayList<String>> mLog, TimeStamp mTS) throws RemoteException{
		merge(mLog, updateLog);
			//merge explained:
			/*
			 * add record from mLog if mTS>replicaTS
			 * and collect stable updates from updateLog
			 */
		
		// apply stable updates that have not been executed
		// making sure the order is according to <= in timestamps
		// apply smallest first
		
		// remove (from updateLog and exOps) entries that have been
		// applied everywhere:
			/*
			 * if gossip sent by RM j then tsTable[j] = mTS
			 * if c is the RM that created a record, r in the updateLog
			 * , then for all 
			 * RMs , i, if the following is not true then remove the
			 * corresponding entry in updateLog:
			 * tsTable[i][c] >= r.ts[c]
			 */
	}
	
	public void query(ArrayList<String> q, TimeStamp qPrev) throws Remote Exception{
		for (int i=0; i<valTS.length(); i++) {
			if (qPrev.getVal(i) > valTS.getVal(i)) {
				// TODO get updates from RM i so valTS>=pPrev
			}
		}
		
		// TODO return result of query and TimeStamp valTS
		
		
	}
	
	public void update(ArrayList<String> uOp, TimeStamp uPrev, int uID) throws RemoteException{
		/* check if this update has already been processed by looking in 
		 * exOps and updateLog for uID. If not already done:
		 */
		replicaTS.incrementVal(serverNum);
		TimeStamp uTS = uPrev.getClone();
		uTS.setVal(serverNum, replicaTS.getVal(serverNum));
		updateLog += <serverNum, uTS, uOp, uPrev, uID>;
		// return uTS to front end for merging with all timestamps from RMs
		
		for (int i=0; i<valTS.length(); i++) {
			if (uPrev.getVal(i) > valTS.getVal(i)) {
				// keep checking this when gossip messages arrive
			}
		}
		
		values := apply(values,uOp);
		valTS := merge(valTS,replicaTS);
		exOps += uID;
		
	}

	public static void main(String[] args) {
		try {
			Server obj = new Server(1);
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj,0);
			Registry registry = LocateRegistry.getRegistry("mira1.dur.ac.uk",370001);
			registry.bind("Hello", stub);		
		} catch (Exception e) {
			System.out.println("Server Exception: "+e.toString());
			e.printStackTrace();
		}

	}

	
}
