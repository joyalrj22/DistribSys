import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.lang.Math;

// FE, back up data to file in fileparser
// MAKE NON CASE SENSITIVE LATER ON

/* uOp is stored in updateLog as xxx,xxx,xx,xx
 * which can then be converted to String array with split(',')
 */
@SuppressWarnings("deprecation")
public class Server implements ServerInterface{
	
	private int serverNum;
	private FileParser values;
	private ArrayList<TimeStamp> valTS;
	private ArrayList<ArrayList<String>> updateLog;
	private TimeStamp replicaTS;
	private ArrayList<String> exOps;
	private ArrayList<TimeStamp> tsTable;
	private Registry registry;
	private ArrayList<ServerInterface> servers;
	private ServerStatus status;
	
	public Server(int serverNum, int systemServerNum) {
		
		status = ServerStatus.ACTIVE;
		this.serverNum = serverNum;
		values = new FileParser("ratings.csv", "movies.csv");
		try {
			
			registry = LocateRegistry.getRegistry(37001);
			for (int i=0; i<systemServerNum; i++) {
				
				if (i==serverNum) {
					servers.add(null);
				} else {
					servers.add((ServerInterface) registry.lookup(String.valueOf(i)));
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getServerNum() throws RemoteException{
		return serverNum;
	}
	
	public HashMap<TimeStamp, ArrayList<ArrayList<String>>> sendGossip() throws RemoteException{
		System.out.println("Sending gossip...");
		// send updateLog and replicaTS
		HashMap<TimeStamp, ArrayList<ArrayList<String>>> returnVal =  new HashMap<TimeStamp, ArrayList<ArrayList<String>>>();
		returnVal.put(replicaTS, updateLog);
		return returnVal;
	}
	
	private void mergeLog(ArrayList<ArrayList<String>> extLog) {
		for (ArrayList<String> extUpdate: extLog) {
			if 	(new TimeStamp(extUpdate.get(1)).isGreaterThan(replicaTS)) {
				updateLog.add(extUpdate);
			}
		}
	}
	
	private TimeStamp mergeTS(TimeStamp ts1, TimeStamp ts2) {
		ArrayList<Integer> mergedVals = new ArrayList<Integer>();
		for (int i=0; i<ts1.length(); i++) {
			mergedVals.add(Integer.valueOf(Math.max(ts1.getVal(i), ts2.getVal(i))));
		}
		return new TimeStamp(mergedVals);
	}
	
	public void recieveGossip(ArrayList<ArrayList<String>> mLog, TimeStamp mTS, int serverNum) throws RemoteException{
		tsTable.set(serverNum, mTS);
		mergeLog(mLog);
		System.out.println("Receiving gossip from server "+serverNum);
		
		// <serverNum, uTS, uOp, uPrev, uID> = updateLog
		TimeStamp minTS = null;
		TimeStamp tempTS;
		ArrayList<String> nextUpdate = null;
		
		for (ArrayList<String> updateRec: updateLog) {
			tempTS = new TimeStamp(updateRec.get(1));
			if (tempTS.isSmallerThan(minTS)) {
				nextUpdate = updateRec;
				minTS = tempTS;
			}
		}
		
		System.out.println("Applying update from gossip: "+nextUpdate);
		update(nextUpdate.get(2).split(","), new TimeStamp(nextUpdate.get(3)), nextUpdate.get(4));

		ListIterator<ArrayList<String>> updateIter = updateLog.listIterator();
		ArrayList<String> r;
		while(updateIter.hasNext()) {
			r = updateIter.next();
			if (tsTable.get(Integer.valueOf(r.get(0))).isSmallerThan(new TimeStamp(r.get(1)))) {
				updateIter.remove();
			}
		}
		
		// TODO: remove values from exOps
		/*FROM TEXTBOOK:
		 * "In essence, front ends issue acknowledgements to the
		 *  replies to their updates, so replica managers know when
		 *  a front end will stop sending the update. 
		 *  They assume a maximum update propagation delay from that point."
		 */
	}
	
	public HashMap<TimeStamp, String> singleQuery(ArrayList<String> args, q queryType ,TimeStamp qPrev) throws RemoteException{
		System.out.println("Recieved Query");
		for (int i=0; i<valTS.get(serverNum).length(); i++) {
			if (qPrev.getVal(i) > valTS.get(serverNum).getVal(i)) {
				HashMap<TimeStamp, ArrayList<ArrayList<String>>> gossip = servers.get(i).sendGossip();
				TimeStamp tempTS = gossip.keySet().iterator().next();
				recieveGossip(gossip.get(tempTS), tempTS, serverNum);
			}
		}
		
		
		String queryResult = null;
		switch(queryType) {
			case AVG_FOR_MOVIE:
				queryResult = values.avgMovieRating(args.get(0));
				break;
			case AVG_FOR_USER:
				queryResult = values.avgUserRating(args.get(0));
				break;
			case USER_MOVIE:
				queryResult = values.userMovieRating(args.get(0), args.get(1));
				break;
			default:
				break;
		
		}
		HashMap<TimeStamp, String> out = new HashMap<TimeStamp, String>();
		out.put(valTS.get(serverNum), queryResult);
		System.out.println("Query Result: "+out);
		return out;
		
		
	}
	
	public HashMap<TimeStamp, HashMap<String, String>> multiQuery(ArrayList<String> args, q queryType ,TimeStamp qPrev) throws RemoteException{
		System.out.println("Recieved Query");
		for (int i=0; i<valTS.get(serverNum).length(); i++) {
			if (qPrev.getVal(i) > valTS.get(serverNum).getVal(i)) {
				HashMap<TimeStamp, ArrayList<ArrayList<String>>> gossip = servers.get(i).sendGossip();
				TimeStamp tempTS = gossip.keySet().iterator().next();
				recieveGossip(gossip.get(tempTS), tempTS, serverNum);
			}
		}
		
		HashMap<String, String> queryResult = null;
		switch (queryType) {
			case ALL_BY_MOVIE:
				queryResult = values.allRatingsByMovie(args.get(0));
				break;
			case ALL_BY_USER:
				queryResult = values.allRatingsByUser(args.get(0));
				break;
			default:
				break;
		}
		
		HashMap<TimeStamp, HashMap<String, String>> out = new HashMap<TimeStamp, HashMap<String, String>>();
		out.put(valTS.get(serverNum), queryResult);
		System.out.println("Query Result: "+out);
		return out;
	}
	
	public TimeStamp update(String[] uOp, TimeStamp uPrev, String uID) throws RemoteException{
		System.out.println("updating...");
		for (ArrayList<String> r : updateLog) {
			if (r.get(4).equals(uID)){
				return null;
			}
		}
		for (String id: exOps) {
			if (id.equals(uID)) {
				return null;
			}
		}
		replicaTS.incrementVal(serverNum);
		TimeStamp uTS = uPrev.getClone();
		uTS.setVal(serverNum, replicaTS.getVal(serverNum));
				
	
		for (ArrayList<String> r: updateLog) {
			Collections.addAll(r, String.valueOf(serverNum), uTS.toString(), uOp.toString(), uPrev.toString(), uID);
		}
		
		
		if (valTS.get(serverNum).isGreaterThan(uPrev)) {
			values.setUserRating(uOp[0], uOp[1], uOp[2]);
			valTS.set(serverNum, mergeTS(valTS.get(serverNum), replicaTS));
			exOps.add(String.valueOf(uID));
		}
		
		//return uTS to front end for merging with all timestamps from RMs
		return uTS;
		
	}
	
	public ServerStatus getStatus() throws RemoteException{
		return status;
	}
	
	public void setStatus(ServerStatus status) throws RemoteException{
		this.status = status;
	}
	
	public ArrayList<ServerInterface> getServers(){
		return servers;
	}
	
	public static void main(String[] args) {
		
		
			
		try {
			Server s = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(s,0);
			Registry registry = LocateRegistry.getRegistry("localhost",370001);
			registry.bind(String.valueOf(args[0]), stub);
			ArrayList<ServerInterface> servers = s.getServers();
			Runnable gossiper = new Runnable() {
				public void run() {
					for (int i=0; i<servers.size();i++) {
						if (servers.get(i) == null) {
							continue;
						}
						HashMap<TimeStamp, ArrayList<ArrayList<String>>> goss;
						
						try {
							goss = servers.get(i).sendGossip();
							s.recieveGossip(goss.values().iterator().next(),goss.keySet().iterator().next(), i);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
				}
			};
			
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			executor.scheduleAtFixedRate(gossiper, 0, 15, TimeUnit.SECONDS);
		} catch (Exception e) {
			System.out.println("Server Exception: "+e.toString());
			e.printStackTrace();
		}
		
	}
	
}
