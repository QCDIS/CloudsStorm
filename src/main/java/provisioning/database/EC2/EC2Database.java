package provisioning.database.EC2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import provisioning.database.Database;

public class EC2Database extends Database {
	
	private static final Logger logger = Logger.getLogger(EC2Database.class);
	
	//Example, key -> Virginia, value -> ec2.us-east-1.amazonaws.com
	public Map<String, String> domain_endpoint = new HashMap<String, String>();
	
	//Example, OS -> Ubuntu 16.04, domain -> Virginia, AMI -> ami-40d28157
	public ArrayList<AmiInfo> amiInfo = new ArrayList<AmiInfo>();
	
	/**
	 * Load the domain information from file. The content is split with "&&".<br/>
	 * Example: <br/>
	 * Virginia&&ec2.us-east-1.amazonaws.com<br/>
	 * Ohio&&ec2.us-east-2.amazonaws.com<br/>
	 * California&&ec2.us-west-1.amazonaws.com<br/>
	 */
	public boolean loadDomainFromFile(String filePath){
		File conf = new File(filePath);
		try {
			BufferedReader in = new BufferedReader(new FileReader(conf));
			String line = null;
			while((line = in.readLine()) != null){
				String[] infos = line.split("&&");
				domain_endpoint.put(infos[0].trim().toLowerCase(), infos[1]);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("The domain infomation of EC2 cannot be loaded from "+filePath);
		}
		
		return true;
	}
	
	
	/**
	 * Load the AMI information from file. The content is split with "&&".<br/>
	 * Example: <br/>
	 * Ubuntu 14.04&&Virginia&&ami-2d39803a  <br/>
	 * Ubuntu 14.04&&California&&ami-48db9d28
	 */
	public boolean loadAmiFromFile(String filePath){
		File conf = new File(filePath);
		try {
			BufferedReader in = new BufferedReader(new FileReader(conf));
			String line = null;
			while((line = in.readLine()) != null){
				String[] infos = line.split("&&");
				AmiInfo ami = new AmiInfo();
				ami.OS = infos[0].trim().toLowerCase();
				ami.domain = infos[1].trim().toLowerCase();
				ami.AMI = infos[2];
				amiInfo.add(ami);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("The AMI infomation of EC2 cannot be loaded from "+filePath);
		}
		
		return true;
	}

}