package provisioning.engine.TEngine;

import org.apache.log4j.Logger;

import provisioning.credential.Credential;
import provisioning.credential.UserCredential;
import provisioning.database.Database;
import provisioning.engine.SEngine.SEngine;
import provisioning.engine.SEngine.SEngineCoreMethod;
import topologyAnalysis.dataStructure.SubTopologyInfo;

public class SEngine_autoScaling implements Runnable{

	private static final Logger logger = Logger.getLogger(SEngine_autoScaling.class);
	
	private SubTopologyInfo subTopologyInfo;
	private Credential credential;
	private Database database;
	
	public SEngine_autoScaling(SubTopologyInfo subTopologyInfo, 
			Credential credential, Database database){
		this.subTopologyInfo = subTopologyInfo;
		this.credential = credential;
		this.database = database;
	}
	
	
	public static SubTopologyInfo generateScalingCopy(String CloudProvider, String domain, 
			SubTopologyInfo scalingTemplate, UserCredential userCredential, Database database){
		Object sEngine = null;
		SubTopologyInfo result = null;
		try {
			sEngine = Class.forName(database.toolInfo.get("sengine")).newInstance();
			
			result = ((SEngineCoreMethod)sEngine).generateScalingCopy(domain, scalingTemplate, userCredential);
			
			} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
			logger.error("The S-Engine for 'generateScalingCopy' cannot be found!");
		}
		
		return result;
	}
	
	@Override
	public void run() {
		try {
			Object sEngine = Class.forName(database.toolInfo.get("sengine")).newInstance();
			if(!((SEngine)sEngine).commonRuntimeCheck(subTopologyInfo)){
				logger.error("Some information is missing for provisioning sub-topology '"+subTopologyInfo.topology+"'!");
				return ;
			}
			if(!((SEngineCoreMethod)sEngine).runtimeCheckandUpdate(subTopologyInfo, database)){
				logger.error("Sub-topology '"+subTopologyInfo.topology+"' cannot pass the runtime check before provisioning!");
				return ;
			}
			if(!((SEngineCoreMethod)sEngine).autoScal(subTopologyInfo, credential, database)){
				logger.error("Provisioning for sub-topology '"+subTopologyInfo.topology+"' failed!");
				subTopologyInfo.status = "failed";
				long curTime = System.currentTimeMillis();
				subTopologyInfo.statusInfo = "not provisioned: "+curTime;
				if(!subTopologyInfo.subTopology.overwirteControlOutput()){
					logger.error("Control information of '"+subTopologyInfo.topology+"' cannot be overwritten to the origin file!");
				}
				return ;
			}else
				logger.info("Sub-topology '"+subTopologyInfo.topology+"' has been provisioned!");
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
			logger.error("The S-Engine for sub-topology '"+subTopologyInfo.topology+"' cannot be found!");
		}
	}

}
