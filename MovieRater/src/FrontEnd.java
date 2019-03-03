import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

public class FrontEnd {
	private ArrayList<TimeStamp> prev;
	private int numOfServers;
	private Registry registry;
	private ArrayList<ServerInterface> servers;
	
	public FrontEnd(int systemServerNum) {
		numOfServers = systemServerNum;
		prev = new ArrayList<TimeStamp>();
		servers = new ArrayList<ServerInterface>();
		try {
			registry = LocateRegistry.getRegistry("mira1.dur.ac.uk",37001);
			for (int i=0; i<numOfServers; i++) {
				servers.add((ServerInterface) registry.lookup(String.valueOf(i)));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private TimeStamp mergeTS(TimeStamp ts1, TimeStamp ts2) {
		ArrayList<Integer> mergedVals = new ArrayList<Integer>();
		for (int i=0; i<ts1.length(); i++) {
			mergedVals.add(Integer.valueOf(Math.max(ts1.getVal(i), ts2.getVal(i))));
		}
		return new TimeStamp(mergedVals);
	}
	
	private ServerInterface findActiveServer(int preferredServer) throws RemoteException {
		ServerInterface s = servers.get(preferredServer);
		
		if (s.getStatus() != ServerStatus.ACTIVE) {
			for (ServerInterface si: servers) {
				if (si.getStatus() == ServerStatus.ACTIVE) {
					s = si;
				}
			}
		}
		
		return s;
	}
	
	public String query(boolean singleQuery, ArrayList<String> args, q queryType, int preferredServer) throws RemoteException{
		
		ServerInterface s = findActiveServer(preferredServer);
		
		// server blocked on queries
		if (s.getStatus() != ServerStatus.ACTIVE) { return null; }
		
		int serverNum = s.getServerNum();
		
		if (singleQuery) {
			HashMap<TimeStamp, String> response = s.singleQuery(args, queryType, prev.get(serverNum));
			prev.set(serverNum, mergeTS(response.keySet().iterator().next(), prev.get(serverNum)));
			return response.values().iterator().next();
		} 
		
		HashMap<TimeStamp, HashMap<String, String>> response = s.multiQuery(args, queryType, prev.get(serverNum));
		prev.set(serverNum, mergeTS(response.keySet().iterator().next(), prev.get(serverNum)));
		String outputStr = "";
		HashMap<String, String> unpackedHash = response.values().iterator().next();
		for (String p: unpackedHash.keySet()) {
			outputStr += p + " : " + unpackedHash.get(p) + "\n";
		}
		
		return outputStr;
	}
	/*
	 *   -- Get user request
	 *   
	 *   
	 *   -- Send request to best server
	 *   -- (if all servers down then FE blocked on queries)
	 *   			Queries: send q, prev
	 *   			Updates: send uOp, prev
	 *   
	 *   
	 *   -- If request is update then FE returns to client then
	 *      propagates to server
	 *     		-  Get received update acknowledgement from RM
	 *     		-  Get uID from RM
	 *    
	 *   -- If request is query then RM  replies after coordination
	 *      and execution
	 *      	- Get val, new from RM
	 */
}
