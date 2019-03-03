import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ServerInterface extends Remote{
	
	public HashMap<TimeStamp, ArrayList<ArrayList<String>>> sendGossip() throws RemoteException;
	public void recieveGossip(ArrayList<ArrayList<String>> mLog, TimeStamp mTS, int serverNum) throws RemoteException;
	public HashMap<TimeStamp, String> singleQuery(ArrayList<String> args, q queryType ,TimeStamp qPrev) throws RemoteException;
	public HashMap<TimeStamp, HashMap<String, String>> multiQuery(ArrayList<String> args, q queryType ,TimeStamp qPrev) throws RemoteException;
	public TimeStamp update(String[] uOp, TimeStamp uPrev, String uID) throws RemoteException;
	public ServerStatus getStatus() throws RemoteException;
	public void setStatus(ServerStatus status) throws RemoteException;
	public int getServerNum() throws RemoteException;
}
