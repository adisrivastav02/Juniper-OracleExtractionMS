package com.iig.gcp.extraction.oracle.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iig.gcp.constants.OracleConstants;
import com.iig.gcp.extraction.oracle.dto.ConnectionMaster;
import com.iig.gcp.extraction.oracle.dto.CountryMaster;
import com.iig.gcp.extraction.oracle.dto.DataDetailBean;
import com.iig.gcp.extraction.oracle.dto.DriveMaster;
import com.iig.gcp.extraction.oracle.dto.ReservoirMaster;
import com.iig.gcp.extraction.oracle.dto.RunFeedsBean;
import com.iig.gcp.extraction.oracle.dto.SourceSystemDetailBean;
import com.iig.gcp.extraction.oracle.dto.SourceSystemMaster;
import com.iig.gcp.extraction.oracle.dto.TargetMaster;
import com.iig.gcp.extraction.oracle.dto.TempDataDetailBean;
import com.iig.gcp.extraction.utils.ConnectionUtils;
import com.iig.gcp.extraction.utils.EncryptionUtil;



@Service
public class ExtractionServiceImpl implements ExtractionService {

	private static String extraction_compute_url;
	@Value("${extraction.compute.url}")
	public void setExtractionUrl(String value) {
		this.extraction_compute_url=value;
	}
	private static String extraction_compute_url1;
	@Value("${extraction.compute.url1}")
	public void setExtractionUrl1(String value) {
		this.extraction_compute_url1=value;
	}
	private static String SCHEDULER_MASTER_TABLE = "JUNIPER_SCH_MASTER_JOB_DETAIL";

	@Override
	public String invokeRest(String json, String url) throws UnsupportedOperationException, Exception {
		String resp = null;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(extraction_compute_url + url);
		postRequest.setHeader("Content-Type", "application/json");
		StringEntity input = new StringEntity(json);
		postRequest.setEntity(input);
		HttpResponse response = httpClient.execute(postRequest);
		String response_string = EntityUtils.toString(response.getEntity(), "UTF-8");
		if (response.getStatusLine().getStatusCode() != 200) {
			resp = "Error" + response_string;
			throw new Exception("Error" + response_string);
		} else {
			resp = response_string;
		}
		return resp;
	}

	@Override
	public String invokeRest1(String json, String url) throws UnsupportedOperationException, Exception {
		String resp = null;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(extraction_compute_url1 + url);
		postRequest.setHeader("Content-Type", "application/json");
		StringEntity input = new StringEntity(json);
		postRequest.setEntity(input);
		HttpResponse response = httpClient.execute(postRequest);
		String response_string = EntityUtils.toString(response.getEntity(), "UTF-8");
		if (response.getStatusLine().getStatusCode() != 200) {
			resp = "Error" + response_string;
			throw new Exception("Error" + response_string);
		} else {
			resp = response_string;
		}
		return resp;
	}

	@Override
	public ArrayList<String> getRunFeeds(String project_id) throws Exception {
		ArrayList<String> arr = new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("SELECT DISTINCT n.FEED_UNIQUE_NAME FROM  JUNIPER_EXT_NIFI_STATUS n LEFT JOIN JUNIPER_PROJECT_MASTER p ON n.PROJECT_SEQUENCE=p.PROJECT_SEQUENCE WHERE p.PROJECT_ID=?");
			pstm.setString(1, project_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}


	@Override
	public ArrayList<RunFeedsBean> getLastRunFeeds(String project_id , String feed) throws Exception {
		ArrayList<RunFeedsBean> arr = new ArrayList<RunFeedsBean>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("SELECT n.FEED_UNIQUE_NAME,n.RUN_ID, TO_CHAR(TO_DATE(n.EXTRACTED_DATE,'DDMMYYYY'),'DD-MON-YY'),f.EXTRACTION_TYPE|| ' ' ||n.PG_TYPE,n.JOB_START_TIME,n.JOB_END_TIME,UPPER(n.STATUS) FROM  JUNIPER_EXT_NIFI_STATUS n LEFT JOIN JUNIPER_EXT_FEED_MASTER f ON n.FEED_UNIQUE_NAME=f.FEED_UNIQUE_NAME and n.PROJECT_SEQUENCE=f.PROJECT_SEQUENCE LEFT JOIN JUNIPER_PROJECT_MASTER p ON f.PROJECT_SEQUENCE=p.PROJECT_SEQUENCE WHERE p.PROJECT_ID=? and n.FEED_UNIQUE_NAME=? ORDER BY n.EXTRACTED_DATE");
			pstm.setString(1, project_id);
			pstm.setString(2, feed);

			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				RunFeedsBean runf = new RunFeedsBean();
				runf.setFeed_name(rs.getString(1));
				runf.setRun_id(rs.getString(2));
				runf.setExtract_date(rs.getString(3));
				runf.setExtraction_type(rs.getString(4));
				runf.setStart_time(rs.getString(5));
				runf.setEnd_time(rs.getString(6));
				runf.setStatus(rs.getString(7));
				arr.add(runf);
			}

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return arr;
	}

	@Override
	public ArrayList<ConnectionMaster> getConnections(String src_val, String project_id) throws Exception {
		// TODO Auto-generated method stub
		Connection connection;
		ConnectionMaster conn = null;
		ArrayList<ConnectionMaster> arrConnectionMaster = new ArrayList<ConnectionMaster>();
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("SELECT SRC_CONN_SEQUENCE,SRC_CONN_NAME,host_name,port_no,username,password,database_name,service_name,system_sequence,drive_sequence,encrypted_encr_key from JUNIPER_EXT_SRC_CONN_MASTER where SRC_CONN_TYPE=? and project_sequence = (select project_sequence from juniper_project_master where project_id=?)");
			pstm.setString(1, src_val);
			pstm.setString(2, project_id);

			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				conn = new ConnectionMaster();
				conn.setConnection_id(rs.getInt(1));
				conn.setConnection_name(rs.getString(2));
				conn.setHost_name(rs.getString(3));
				conn.setPort_no(rs.getString(4));
				conn.setUsername(rs.getString(5));
				conn.setPassword(rs.getBytes(6));
				conn.setDatabase_name(rs.getString(7));
				conn.setService_name(rs.getString(8));
				conn.setSystem(getSystemName(rs.getInt(9)));
				conn.setDrive_id(rs.getInt(10));
				DriveMaster dm = getDrivesDetails(rs.getInt(10));
				conn.setDrive_name(dm.getDrive_name());
				conn.setDrive_path(dm.getDrive_name());
				conn.setEncrypt(rs.getBytes(11));
				arrConnectionMaster.add(conn);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return arrConnectionMaster;
	}

	@Override
	public ArrayList<TargetMaster> getTargets(String project_id) throws Exception {
		ArrayList<TargetMaster> arr = new ArrayList<TargetMaster>();
		TargetMaster tm=null;
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select TARGET_CONN_SEQUENCE,target_unique_name from JUNIPER_EXT_TARGET_CONN_MASTER where project_sequence=(select project_sequence from juniper_project_master where project_id='"+project_id+"')");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				tm=new TargetMaster();
				tm.setTarget_conn_sequence(rs.getInt(1));
				tm.setTarget_unique_name(rs.getString(2));
				arr.add(tm);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	@Override
	public TargetMaster getTargets1(int tgt) throws Exception {
		TargetMaster tm = null;
		Connection connection;
		try {
			String typ=null;
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm1 = connection.prepareStatement("select "
					+ "a.target_type from JUNIPER_EXT_TARGET_CONN_MASTER a where a.TARGET_CONN_SEQUENCE="+tgt);
			ResultSet rs1 = pstm1.executeQuery();
			while (rs1.next()) {
				typ=rs1.getString(1);
			}
			System.out.println(tgt);
			if(typ.equalsIgnoreCase("gcs"))
			{
				PreparedStatement pstm = connection.prepareStatement("select "
						+ "a.target_unique_name,a.target_type,b.gcp_project,b.service_account_name,b.bucket_name,a.system_sequence"
						+ " from JUNIPER_EXT_TARGET_CONN_MASTER a, JUNIPER_EXT_GCP_MASTER b where a.gcp_sequence=b.gcp_sequence and a.target_conn_sequence="+tgt);
				ResultSet rs = pstm.executeQuery();
				while (rs.next()) {
					tm = new TargetMaster();
					tm.setTarget_unique_name(rs.getString(1));
					tm.setTarget_type(rs.getString(2));
					tm.setGcp_project(rs.getString(3));
					tm.setGcp_service_account(rs.getString(4));
					tm.setGcp_bucket(rs.getString(5));
					tm.setSystem_name(getSystemName(rs.getInt(6)));
				}
			}
			else if(typ.equalsIgnoreCase("hdfs"))
			{
				PreparedStatement pstm = connection.prepareStatement("select "
						+ "a.target_unique_name,a.target_type,a.hdp_knox_host,a.hdp_knox_port,a.hdp_user,a.hdp_encrypted_password,a.encrypted_key,"
						+ "a.hdfs_gateway,a.hdp_hdfs_path,a.materialization_flag,a.partition_flag,a.hive_gateway,a.system_sequence"
						+ " from JUNIPER_EXT_TARGET_CONN_MASTER a where a.target_conn_sequence="+tgt);
				ResultSet rs = pstm.executeQuery();
				while (rs.next()) {
					tm = new TargetMaster();
					tm.setTarget_unique_name(rs.getString(1));
					tm.setTarget_type(rs.getString(2));
					tm.setHdp_knox_host(rs.getString(3));
					tm.setHdp_knox_port(rs.getInt(4));
					tm.setHdp_user(rs.getString(5));
					tm.setHdp_encrypted_password(rs.getBytes(6));
					tm.setEncrypted_key(rs.getBytes(7));
					tm.setHdfs_gateway(rs.getString(8));
					tm.setHdp_hdfs_path(rs.getString(9));
					tm.setMaterialization_flag(rs.getString(10));
					tm.setPartition_flag(rs.getString(11));
					tm.setHive_gateway(rs.getString(12));
					tm.setSystem_name(getSystemName(rs.getInt(13)));
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return tm;
	}

	@Override
	public ConnectionMaster getConnections1(String src_val,int src_sys_id) throws Exception {
		// TODO Auto-generated method stub
		Connection connection;
		ConnectionMaster conn = new ConnectionMaster();
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select a.SRC_CONN_SEQUENCE from JUNIPER_EXT_FEED_MASTER b,JUNIPER_EXT_FEED_SRC_TGT_LINK a"
					+ " where a.feed_sequence=b.feed_sequence and a.FEED_SEQUENCE="+src_sys_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				conn.setConnection_id(rs.getInt(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	@Override
	public ConnectionMaster getConnections2(String src_val,int conn_id, String project_id) throws Exception {
		// TODO Auto-generated method stub
		Connection connection;
		ConnectionMaster conn = new ConnectionMaster();
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select src_conn_sequence,src_conn_name,src_conn_type,host_name,port_no,username,password,database_name,service_name,system_sequence from JUNIPER_EXT_SRC_CONN_MASTER where project_sequence=(select project_sequence from juniper_project_master where project_id='"+project_id+"')" + " and src_conn_sequence="+conn_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				conn.setConnection_id(rs.getInt(1));
				conn.setConnection_name(rs.getString(2));
				conn.setConnection_type(rs.getString(3));
				conn.setHost_name(rs.getString(4));
				conn.setPort_no(rs.getString(5));
				conn.setUsername(rs.getString(6));
				conn.setPassword(rs.getBytes(7));
				conn.setDatabase_name(rs.getString(8));
				conn.setService_name(rs.getString(9));
				conn.setSystem(getSystemName(rs.getInt(10)));
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	@Override
	public String getExtType(int src_sys_id) throws Exception {
		// TODO Auto-generated method stub
		String val=null;
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select extraction_type from JUNIPER_EXT_FEED_MASTER where feed_sequence="+src_sys_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				val=rs.getString(1);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return val;
	}

	@Override
	public String getExtType1(String src_unique_name) throws Exception {
		String val=null;
		try {
			Connection connection = ConnectionUtils.getConnection();

			//Check if job is already scheduled for extraction, if yes we dont need to need to schdule this again.
			String checkIfJobIsInMasterQuery="select "
					+ "CASE\r\n" + 
					"    WHEN master.weekly_flag = 'Y'  THEN concat('Weekly on ', master.week_run_day)\r\n" + 
					"    WHEN master.daily_flag = 'Y'   THEN concat('Daily at ', substr(master.job_schedule_time, 1, 5))\r\n" + 
					"    WHEN master.monthly_flag = 'Y' THEN concat('Monthly on ', master.month_run_day)\r\n" + 
					"    WHEN master.yearly_flag = 'Y'  THEN concat('Yearly on month ', master.month_run_val)\r\n" + 
					"END AS consolidated_schedule "
					+ "from "+SCHEDULER_MASTER_TABLE+" master "
					+ "where batch_id='"+src_unique_name+"'";
			PreparedStatement pstm = connection.prepareStatement(checkIfJobIsInMasterQuery);
			ResultSet rs = pstm.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}

			String extractionTypeQuery="select extraction_type from JUNIPER_EXT_FEED_MASTER "
					+ "where feed_unique_name='"+src_unique_name+"'";
			pstm = connection.prepareStatement(extractionTypeQuery);
			rs = pstm.executeQuery();
			while (rs.next()) {
				val=rs.getString(1);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return val;
	}

	@Override
	public ArrayList<String> getTables(String src_val, int conn_id, String schema_name, String project_id,String db_name) throws Exception {
		ArrayList<ConnectionMaster> arrcm = getConnections(src_val, project_id);
		ArrayList<String> arrTbl = new ArrayList<String>();
		String query = null, connectionUrl = null, host = null, port = null, username = null, db = null, service = null;
		byte[] password = null,encrypt = null;
		Connection serverConnection = null;
		Statement st = null;
		for (ConnectionMaster cm : arrcm) {
			if (conn_id == cm.getConnection_id()) {
				host = cm.getHost_name();
				port = cm.getPort_no();
				username = cm.getUsername();
				password = cm.getPassword();
				db = cm.getDatabase_name();
				service = cm.getService_name();
				encrypt = cm.getEncrypt();
			}
		}
		try {
			query = "select distinct table_name from all_tables where owner='"+schema_name+"' order by table_name";
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connectionUrl = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service + "";
			String pass = EncryptionUtil.decyptPassword(encrypt, password);
			serverConnection = DriverManager.getConnection(connectionUrl, username, pass);
			st = serverConnection.createStatement();

			ResultSet rs2 = st.executeQuery(query);
			while (rs2.next()) {
				arrTbl.add(rs2.getString(1));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			st.close();
			serverConnection.close();
		}
		return arrTbl;
	}

	@Override
	public ArrayList<String> getFields(String id, String src_val, String table_name, int conn_id, String schema_name, String project_id,String db_name) throws Exception {
		ArrayList<ConnectionMaster> arrcm = getConnections(src_val, project_id);
		ArrayList<String> arrFld = new ArrayList<String>();
		String query = null, connectionUrl = null, host = null, port = null, username = null, db = null, service = null;
		byte[] password = null,encrypt=null;
		Connection serverConnection = null;
		Statement st = null;
		for (ConnectionMaster cm : arrcm) {
			if (conn_id == cm.getConnection_id()) {
				host = cm.getHost_name();
				port = cm.getPort_no();
				username = cm.getUsername();
				password = cm.getPassword();
				db = cm.getDatabase_name();
				service = cm.getService_name();
				encrypt = cm.getEncrypt();
			}
		}
		String tbls[] = table_name.split(",");
		for (int i = 0; i < tbls.length; i++) {
			try {
				String pass = EncryptionUtil.decyptPassword(encrypt, password);
				serverConnection = DriverManager.getConnection(connectionUrl, username, pass);
				st = serverConnection.createStatement();
				ResultSet rs2 = st.executeQuery(query);
				while (rs2.next()) {
					arrFld.add(rs2.getString(1));
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				st.close();
				serverConnection.close();
			}
		}
		return arrFld;
	}

	@Override
	public ArrayList<SourceSystemMaster> getSources(String src_val, String project_id) throws Exception {
		SourceSystemMaster ssm = null;
		ArrayList<SourceSystemMaster> arrssm = new ArrayList<SourceSystemMaster>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select feed_sequence,feed_unique_name, " + 
					"listagg(table_sequence, ',' ) within group (order by feed_unique_name), " + 
					"listagg(file_sequence, ',' ) within group (order by feed_unique_name), " + 
					"listagg(db_name, ',' ) within group (order by feed_unique_name) " + 
					"from " + 
					"(" + 
					"select a.FEED_SEQUENCE,a.FEED_UNIQUE_NAME,c.table_sequence,d.file_sequence,e.db_name " + 
					"from " + 
					"JUNIPER_EXT_FEED_MASTER a " + 
					"inner join JUNIPER_EXT_FEED_SRC_TGT_LINK b " + 
					"on a.feed_sequence=b.feed_sequence " + 
					"left outer join JUNIPER_EXT_TABLE_MASTER c " + 
					"on a.feed_sequence=c.feed_sequence " + 
					"left outer join JUNIPER_EXT_FILE_MASTER d " + 
					"on a.feed_sequence=d.feed_sequence " + 
					"left outer join JUNIPER_EXT_HIVE_DB_MASTER e " + 
					"on a.feed_sequence=e.feed_id " + 
					"where a.project_sequence=(select project_sequence from juniper_project_master where project_id = '"+project_id+"') " + 
					"and b.src_conn_sequence in (select src_conn_sequence from JUNIPER_EXT_SRC_CONN_MASTER where src_conn_type='"+src_val+"') " + 
					") " + 
					"group by feed_sequence,feed_unique_name order by feed_sequence,feed_unique_name");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				ssm = new SourceSystemMaster();
				ssm.setSrc_sys_id(rs.getInt(1));
				ssm.setSrc_unique_name(rs.getString(2));
				ssm.setTable_list(rs.getString(3));
				ssm.setFile_list(rs.getString(4));
				ssm.setDb_name(rs.getString(5));
				arrssm.add(ssm);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arrssm;
	}

	@Override
	public ArrayList<SourceSystemDetailBean> getSources1(String src_val,int src_sys_id) throws Exception {
		SourceSystemDetailBean ssm = null;
		ArrayList<SourceSystemDetailBean> arrssm = new ArrayList<SourceSystemDetailBean>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select feed_sequence,feed_unique_name,feed_desc,country_code,extraction_type,src_conn_sequence, " + 
					"listagg(target_unique_name,',') within group (order by feed_sequence,feed_unique_name,feed_desc,country_code,extraction_type,src_conn_sequence) " + 
					"from (" + 
					"select a.FEED_SEQUENCE,a.FEED_UNIQUE_NAME,a.FEED_DESC,a.COUNTRY_CODE,a.EXTRACTION_TYPE,c.target_unique_name,b.SRC_CONN_SEQUENCE " + 
					"from " + 
					"JUNIPER_EXT_FEED_MASTER a inner join JUNIPER_EXT_FEED_SRC_TGT_LINK b on a.feed_sequence=b.feed_sequence " + 
					"left outer join JUNIPER_EXT_TARGET_CONN_MASTER c on b.target_sequence=c.target_conn_sequence " + 
					"where a.FEED_SEQUENCE=" +src_sys_id+ ") " + 
					"group by feed_sequence,feed_unique_name,feed_desc,country_code,extraction_type,src_conn_sequence " +
					"order by feed_sequence,feed_unique_name,feed_desc,country_code,extraction_type,src_conn_sequence");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				ssm = new SourceSystemDetailBean();
				ssm.setSrc_sys_id(rs.getInt(1));
				ssm.setSrc_unique_name(rs.getString(2));
				ssm.setSrc_sys_desc(rs.getString(3));
				ssm.setCountry_code(rs.getString(4));
				ssm.setExtraction_type(rs.getString(5));
				ssm.setConnection_id(rs.getInt(6));
				ssm.setTarget(rs.getString(7));
				arrssm.add(ssm);
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			// connection.close();
		}
		// connection.close();
		return arrssm;
	}

	@Override
	public ArrayList<CountryMaster> getCountries() throws Exception {
		CountryMaster cm = null;
		ArrayList<CountryMaster> arrcm = new ArrayList<CountryMaster>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select country_code,country_name from JUNIPER_REGION_COUNTRY_MASTER order by country_name");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				cm = new CountryMaster();
				cm.setCountry_code(rs.getString(1));
				cm.setCountry_name(rs.getString(2));
				arrcm.add(cm);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arrcm;
	}

	@Override
	public ArrayList<ReservoirMaster> getReservoirs() throws Exception {
		ReservoirMaster rm = null;
		ArrayList<ReservoirMaster> arrrm = new ArrayList<ReservoirMaster>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select reservoir_id,reservoir_name,reservoir_desc from reservoir_master where reservoir_status='Y' and lower(reservoir_desc) like '%extraction%'");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				rm = new ReservoirMaster();
				rm.setReservoir_id(rs.getInt(1));
				rm.setReservoir_name(rs.getString(2));
				rm.setReservoir_desc(rs.getString(3));
				arrrm.add(rm);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arrrm;
	}

	@Override
	public ArrayList<DataDetailBean> getData(int src_sys_id,String src_val, int conn_id,String db_name,String schema_name, String project_id) throws Exception {
		DataDetailBean ddb = null;
		ArrayList<DataDetailBean> arrddb = new ArrayList<DataDetailBean>();
		ConnectionMaster conn = getConnections1(src_val, src_sys_id);
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement(
					"select table_name, columns, where_clause, fetch_type, incr_col from JUNIPER_EXT_TABLE_MASTER where feed_sequence="+src_sys_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				ddb = new DataDetailBean();
				ddb.setTable_name(rs.getString(1));
				ddb.setTable_name_short(rs.getString(1).split("\\.")[1]);
				ddb.setSchema(rs.getString(1).split("\\.")[0]);
				if(rs.getString(2).equalsIgnoreCase("all"))
				{
					ArrayList<String> arrs = new ArrayList<String>();
					arrs = getFields("1", src_val, rs.getString(1), conn_id, rs.getString(1).split("\\.")[0], project_id,db_name);
					String fieldString = String.join(",", arrs);
					ddb.setColumn_name(fieldString);
				}
				else
				{
					ddb.setColumn_name(rs.getString(2));
				}
				ddb.setWhere_clause(rs.getString(3));
				ddb.setFetch_type(rs.getString(4));
				ddb.setIncremental_column(rs.getString(5));
				ArrayList<String> agf=getFields("1", src_val, rs.getString(1), conn.getConnection_id(),schema_name, project_id,db_name);
				String cols = agf.toString();
				cols = cols.substring(1, cols.length() - 1).replace(", ", ",");
				ddb.setCols(cols);
				arrddb.add(ddb);
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return arrddb;
	}

	@Override
	public int checkNames(String sun) throws Exception
	{
		Connection connection=null;
		int stat=0;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select FEED_UNIQUE_NAME from JUNIPER_EXT_FEED_MASTER where FEED_UNIQUE_NAME='"+sun+"'");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				stat=1;break;
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		ConnectionUtils.closeQuietly(connection);
		return stat;
	}

	@Override
	public ArrayList<String> getSchema(String src_val,int conn_id, String project_id,String db_name) throws Exception
	{
		ArrayList<ConnectionMaster> arrcm = getConnections(src_val, project_id);
		ArrayList<String> arrTbl = new ArrayList<String>();
		String query = null, connectionUrl = null, host = null, port = null, username = null, db = null, service = null;
		byte[] password = null,encrypt=null;
		Connection serverConnection = null;
		Statement st = null;
		for (ConnectionMaster cm : arrcm) {
			if (conn_id == cm.getConnection_id()) {
				host = cm.getHost_name();
				port = cm.getPort_no();
				username = cm.getUsername();
				password = cm.getPassword();
				db = cm.getDatabase_name();
				service = cm.getService_name();
				encrypt = cm.getEncrypt();
			}
		}
		try {
			query = "select distinct owner from all_tables where upper(owner) not like '%SYS%' and upper(owner) not like '%XDB%'";
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connectionUrl = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service + "";System.out.println(connectionUrl);

			String pass = EncryptionUtil.decyptPassword(encrypt, password);
			serverConnection = DriverManager.getConnection(connectionUrl, username, pass);
			st = serverConnection.createStatement();
			System.out.println("Statement : "+st);
			System.out.println("Query : "+query);
			ResultSet rs2 = st.executeQuery(query);
			while (rs2.next()) {
				arrTbl.add(rs2.getString(1));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				st.close();
				serverConnection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return arrTbl;
	}
	public String getSchemaData(String src_val,int src_sys_id) throws Exception
	{
		ConnectionMaster conn = getConnections1(src_val, src_sys_id);
		String sch="";
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement(
					"select table_name from JUNIPER_EXT_TABLE_MASTER where FEED_SEQUENCE="+src_sys_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				if(src_val.equalsIgnoreCase("Mssql")) {
					sch=rs.getString(1).split("\\.")[1];
				}else {
					sch=rs.getString(1).split("\\.")[0];
				}
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			//connection.close();
		}
		return sch;
	}

	public ArrayList<String> getSystem(String project) throws Exception
	{
		ArrayList<String> sys=new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement(
					"select a.system_name from JUNIPER_SYSTEM_MASTER a");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				sys.add(rs.getString(1));
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			//connection.close();
		}
		return sys;
	}

	public String getSystemName(int system) throws Exception
	{
		String sys=null;
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement(
					"select a.system_name from JUNIPER_SYSTEM_MASTER a where system_sequence="+system);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				sys=rs.getString(1);
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			//connection.close();
		}
		return sys;
	}

	@Override
	public ArrayList<DriveMaster> getDrives(String project_id) throws Exception {
		DriveMaster dm = null;
		ArrayList<DriveMaster> arrdm = new ArrayList<DriveMaster>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();

			//PreparedStatement pstm = connection.prepareStatement("select drive_sequence,drive_name,mounted_path,project_sequence from JUNIPER_EXT_DRIVE_MASTER");

			PreparedStatement pstm = connection.prepareStatement("select drive_sequence,drive_name,mounted_path,project_sequence from JUNIPER_EXT_DRIVE_MASTER where project_sequence=(select project_sequence from juniper_project_master where project_id='"+project_id+"')");
			System.out.println(pstm);

			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				dm = new DriveMaster();
				dm.setDrive_id(rs.getInt(1));
				dm.setDrive_name(rs.getString(2));
				dm.setDrive_path(rs.getString(3));
				dm.setProject_id(rs.getString(4));
				arrdm.add(dm);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arrdm;
	}

	@Override
	public ArrayList<DriveMaster> getDrives1(int src_sys_id) throws Exception {
		DriveMaster dm = null;
		ArrayList<DriveMaster> arrdm = new ArrayList<DriveMaster>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select c.drive_name,c.mounted_path from JUNIPER_EXT_FEED_SRC_TGT_LINK a, JUNIPER_EXT_SRC_CONN_MASTER b, JUNIPER_EXT_DRIVE_MASTER c where a.SRC_CONN_SEQUENCE=b.SRC_CONN_SEQUENCE and b.DRIVE_SEQUENCE=c.DRIVE_SEQUENCE and a.FEED_SEQUENCE="+src_sys_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				dm = new DriveMaster();
				dm.setDrive_name(rs.getString(1));
				dm.setDrive_path(rs.getString(2));
				arrdm.add(dm);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arrdm;
	}

	@Override
	public DriveMaster getDrivesDetails(int drive_id) throws Exception {
		DriveMaster dm = new DriveMaster();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select c.drive_sequence,c.drive_name,c.mounted_path from JUNIPER_EXT_DRIVE_MASTER c where c.DRIVE_SEQUENCE="+drive_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				dm.setDrive_id(rs.getInt(1));
				dm.setDrive_name(rs.getString(2));
				dm.setDrive_path(rs.getString(3));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return dm;
	}

	@Override
	public void updateLoggerTable(String src_unique_name) throws Exception {

		// TODO Auto-generated method stub
		String src_id=null;
		Connection connection;
		PreparedStatement pstm;
		Statement statement;
		ResultSet rs=null;
		String query=null;
		try {
			connection = ConnectionUtils.getConnection();

			//Delete all the entries for specific Src ID
			query="DELETE FROM LOGGER_MASTER WHERE FEED_ID='"+src_unique_name+"'";
			statement = connection.createStatement();
			statement.execute(query);

			//Get Source ID from Src Name
			pstm = connection.prepareStatement("select FEED_SEQUENCE from JUNIPER_EXT_FEED_MASTER where FEED_UNIQUE_NAME='"+src_unique_name+"'");
			System.out.println("1: "+query);
			rs = pstm.executeQuery();
			while (rs.next()) {
				src_id=rs.getString(1);
			}
			System.out.println("unique name: "+src_unique_name+" src id: "+src_id);
			//Get Source Details from Src Id
			query="SELECT SRC_CONN_NAME,SRC_CONN_TYPE,S.SYSTEM_SEQUENCE FROM JUNIPER_EXT_SRC_CONN_MASTER C\r\n" + 
					"INNER JOIN  JUNIPER_EXT_FEED_SRC_TGT_LINK L ON C.SRC_CONN_SEQUENCE=L.SRC_CONN_SEQUENCE \r\n" + 
					"INNER JOIN JUNIPER_SYSTEM_MASTER S ON C.system_sequence=S.system_sequence WHERE l.FEED_SEQUENCE="+src_id;
			System.out.println("2: "+query);
			pstm = connection.prepareStatement(query);
			rs = pstm.executeQuery();
			while (rs.next()) {
				query="INSERT ALL \r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Source','Name','"+rs.getString(1)+"')\r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Source','Type','"+rs.getString(2)+"')\r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Source','EIM','"+rs.getString(3)+"')\r\n" + 
						"SELECT * FROM dual";
				System.out.println("3: "+query);
				statement = connection.createStatement();
				statement.execute(query);
			}

			//Get Target Details from Src ID
			query="SELECT TARGET_UNIQUE_NAME,TARGET_TYPE,S.SYSTEM_SEQUENCE FROM JUNIPER_EXT_TARGET_CONN_MASTER C\r\n" + 
					"INNER JOIN  JUNIPER_EXT_FEED_SRC_TGT_LINK L ON C.TARGET_CONN_SEQUENCE=L.TARGET_SEQUENCE \r\n" + 
					"INNER JOIN JUNIPER_SYSTEM_MASTER S ON C.system_sequence=S.system_sequence WHERE l.FEED_SEQUENCE=" + src_id;
			System.out.println("3: "+query);
			pstm = connection.prepareStatement(query);
			rs = pstm.executeQuery();
			while (rs.next()) {
				query="INSERT ALL \r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Target','Name','"+rs.getString(1)+"')\r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Target','Type','"+rs.getString(2)+"')\r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Target','EIM','"+rs.getString(3)+"')\r\n" + 
						"SELECT * FROM dual";
				System.out.println("4: "+query);
				statement = connection.createStatement();
				statement.execute(query);
			}
			//Get Scheduling Information
			query="SELECT DISTINCT \r\n" + 
					"					CASE WHEN SCHEDULE_TYPE = 'R' AND DAILY_FLAG = 'Y' THEN 'DAILY'\r\n" + 
					"					WHEN SCHEDULE_TYPE = 'R' AND  WEEKLY_FLAG = 'Y' THEN 'WEEKLY'\r\n" + 
					"					WHEN SCHEDULE_TYPE = 'R' AND  MONTHLY_FLAG = 'Y' THEN 'MONTHLY' \r\n" + 
					"					WHEN SCHEDULE_TYPE = 'R' AND  YEARLY_FLAG = 'Y' THEN 'YEARLY'\r\n" + 
					"                    WHEN SCHEDULE_TYPE = 'O' THEN 'AD-HOC'\r\n" + 
					"                    WHEN SCHEDULE_TYPE = 'F' THEN 'FILE-WATCHER'\r\n" + 
					"                    WHEN SCHEDULE_TYPE = 'K' THEN 'KAFKA-WATCHER' \r\n" + 
					"                    WHEN SCHEDULE_TYPE = 'A' THEN 'API-WATCHER'\r\n" + 
					"					ELSE ''\r\n" + 
					"					END AS \"FREQUENCY\",\r\n" + 
					"					JOB_SCHEDULE_TIME \r\n" + 
					"					from JUNIPER_SCH_MASTER_JOB_DETAIL where LOWER(BATCH_ID)='"+src_unique_name.toLowerCase()+"'";
			System.out.println("5: "+query);
			pstm = connection.prepareStatement(query);
			rs = pstm.executeQuery();
			while (rs.next()) {
				query="INSERT ALL \r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Scheduling','Frequency','"+rs.getString(1)+"')\r\n" + 
						" INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Scheduling','Time','"+rs.getString(2)+"')\r\n" + 
						"SELECT * FROM dual";
				System.out.println("6: "+query);
				statement = connection.createStatement();
				statement.execute(query);
			}
			//Get Table List Information
			query="select TABLE_NAME from JUNIPER_EXT_TABLE_MASTER where FEED_SEQUENCE="+src_id+" UNION ALL select FILE_NAME from JUNIPER_EXT_FILE_MASTER where FEED_SEQUENCE="+src_id ;
			System.out.println("7: "+query);
			pstm = connection.prepareStatement(query);
			rs = pstm.executeQuery();
			while (rs.next()) {
				query="INSERT INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Table','Name','"+rs.getString(1)+"')";
				System.out.println("8: "+query);
				statement = connection.createStatement();
				statement.execute(query);
			}
			//Insert Platform Information
			query="INSERT INTO LOGGER_MASTER (FEED_ID,CLASSIFICATION,SUBCLASSIFICATION,VALUE)  values('"+src_unique_name+"','Transfer','Platform','JUNIPER')";
			System.out.println("9: "+query);
			statement = connection.createStatement();
			statement.execute(query);

			ConnectionUtils.closeQuietly(connection);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<String> getGoogleProject(String project_id) throws Exception
	{
		ArrayList<String> arr = new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select gcp_project from JUNIPER_EXT_GCP_MASTER where project_sequence=(select project_sequence from juniper_project_master where project_id='"+project_id+"')");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	@Override
	public ArrayList<String> getServiceBucket(String project, String project_id) throws Exception
	{
		ArrayList<String> arr = new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("select bucket_name,service_account_name from JUNIPER_EXT_GCP_MASTER where project_sequence=(select project_sequence from juniper_project_master where project_id='"+project_id+"')" + "and gcp_project='"+project+"'" );
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1)+"|"+rs.getString(2));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	@Override
	public String getBulkDataTemplate(int src_sys_id) throws Exception {
		// TODO Auto-generated method stub
		String val=null;
		Connection connection;
		String yemi = "field.xls";
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement feed_seq = null;

			System.out.println("feed_sequenc is "+src_sys_id);
			PreparedStatement pstm = connection.prepareStatement("select TABLE_NAME,COLUMNS,WHERE_CLAUSE,FETCH_TYPE,INCR_COL,'Y' as VALIDATION_FLAG,'NA' as ERROR_MESSAGE from JUNIPER_EXT_TABLE_MASTER where feed_sequence="+src_sys_id
					+" union select TABLE_NAME,COLUMNS,WHERE_CLAUSE,FETCH_TYPE,INCR_COL,VALIDATION_FLAG,ERROR_MESSAGE from JUNIPER_EXT_TABLE_MASTER_TEMP where feed_sequence="+src_sys_id);
			ResultSet rs = pstm.executeQuery();

			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("lawix10");
			HSSFRow rowhead = sheet.createRow((short) 0);
			rowhead.createCell((short) 0).setCellValue("TABLE_NAME");
			rowhead.createCell((short) 1).setCellValue("COLUMNS");
			rowhead.createCell((short) 2).setCellValue("WHERE_CLAUSE");
			rowhead.createCell((short) 3).setCellValue("FETCH_TYPE");
			rowhead.createCell((short) 4).setCellValue("INCR_COL");
			rowhead.createCell((short) 5).setCellValue("VALIDATION_FLAG");
			rowhead.createCell((short) 6).setCellValue("ERROR_MESSAGE");

			int i = 1;
			while (rs.next()){
				System.out.println(rs.getString("TABLE_NAME"));
				System.out.println(rs.getString("COLUMNS"));
				HSSFRow row = sheet.createRow((short) i);
				row.createCell((short) 0).setCellValue(rs.getString("TABLE_NAME"));
				row.createCell((short) 1).setCellValue(rs.getString("COLUMNS"));
				row.createCell((short) 2).setCellValue(rs.getString("WHERE_CLAUSE"));
				row.createCell((short) 3).setCellValue(rs.getString("FETCH_TYPE"));
				row.createCell((short) 4).setCellValue(rs.getString("INCR_COL"));
				row.createCell((short) 5).setCellValue(rs.getString("VALIDATION_FLAG"));
				row.createCell((short) 6).setCellValue(rs.getString("ERROR_MESSAGE"));

				i++;
			}

			FileOutputStream fileOut = new FileOutputStream(yemi);
			workbook.write(fileOut);
			fileOut.close();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Data is \n" +yemi);
		return yemi;
	}

	@Override
	public ArrayList<TempDataDetailBean> getTempData(int src_sys_id,String src_val, int conn_id, String project_id) throws Exception {
		TempDataDetailBean ddb = null;
		String db_name=null;
		ArrayList<TempDataDetailBean> arrddb = new ArrayList<TempDataDetailBean>();
		ConnectionMaster conn = getConnections1(src_val, src_sys_id);
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			String query="select table_name, columns, where_clause, fetch_type, incr_col,validation_flag,error_message from JUNIPER_EXT_TABLE_MASTER_TEMP where feed_sequence="+src_sys_id
					+" union "
					+"select table_name, columns, where_clause, fetch_type, incr_col,'Y','null' from JUNIPER_EXT_TABLE_MASTER where feed_sequence="+src_sys_id;
			System.out.println(query);
			PreparedStatement pstm = connection.prepareStatement(query);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				System.out.println("Inside the while loop");
				ddb = new TempDataDetailBean();
				ddb.setTable_name(rs.getString(1));
				//ddb.setTable_name_short(rs.getString(1).split("\\.")[1]);
				ddb.setSchema(rs.getString(1).split("\\.")[0]);


				if(rs.getString(2).equalsIgnoreCase("all"))
				{
					ArrayList<String> arrs = new ArrayList<String>();
					arrs = getFields("1", src_val, rs.getString(1), conn_id, rs.getString(1).split("\\.")[0], project_id,db_name);
					String fieldString = String.join(",", arrs);
					ddb.setColumn_name(fieldString);
				}
				else
				{
					ddb.setColumn_name(rs.getString(2));
				}
				ddb.setWhere_clause(rs.getString(3));
				ddb.setFetch_type(rs.getString(4));
				ddb.setIncremental_column(rs.getString(5));
				ddb.setValidation_flag(rs.getString(6));
				ddb.setError_message(rs.getString(7));
				arrddb.add(ddb);
			}
			connection.close();
			System.out.println("Extracted the complete data");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			//connection.close();
		}

		return arrddb;
	}

	@Override
	public ArrayList<String> getHivedbList(String project_id) throws Exception
	{
		ArrayList<String> arr = new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("SELECT DISTINCT db_name FROM  juniper_ext_hive_table_list");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	@Override
	public ArrayList<String> getKafkaTopic() throws Exception
	{
		ArrayList<String> arr = new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("SELECT kafka_topic FROM  juniper_ext_kafka_topic_master");
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	@Override
	public ArrayList<String> getColList(String table_name) throws Exception {
		ArrayList<String> arr = new ArrayList<String>();
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement("SELECT DISTINCT n.FEED_UNIQUE_NAME FROM  JUNIPER_EXT_NIFI_STATUS n LEFT JOIN JUNIPER_PROJECT_MASTER p ON n.PROJECT_SEQUENCE=p.PROJECT_SEQUENCE WHERE p.PROJECT_ID=?");
			pstm.setString(1, table_name);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				arr.add(rs.getString(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	public String getDatabaseData(String src_val,int src_sys_id) throws Exception
	{
		String sch="";
		Connection connection;
		try {
			connection = ConnectionUtils.getConnection();
			PreparedStatement pstm = connection.prepareStatement(
					"select table_name from JUNIPER_EXT_TABLE_MASTER where FEED_SEQUENCE="+src_sys_id);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				sch=rs.getString(1).split("\\.")[0];
			}
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			//connection.close();
		}
		return sch;
	}

	@Override
	public String getJsonFromFile(File file,String user,String schema_name,String project,String src_sys_id) {
		String json_array_str="";
		if(file!=null) {
			System.out.println("Inside Field Flow");
			String[] col_val = new String[20];
			try {
				POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
				HSSFWorkbook wb = new HSSFWorkbook(fs);
				HSSFSheet sheet1 = wb.getSheetAt(0);
				HSSFRow row1;
				HSSFCell cell1;

				int rows;
				rows = sheet1.getPhysicalNumberOfRows();
				String counter = String.valueOf(rows -1);


				JSONArray array = new JSONArray();
				JSONObject item = new JSONObject();

				JSONArray array_pre = new JSONArray();
				JSONObject item_pre = new JSONObject();

				JSONArray array_first = new JSONArray();
				JSONObject item_first = new JSONObject();


				item.put("projects",project);
				item.put("project",project);
				item.put("user",user);
				item.put("feed_id",src_sys_id);
				item.put("counter",counter);
				item.put("schema_name",schema_name);
				item.put("load_type","bulk_load");




				int cols = 0;
				int tmp = 0;

				for(int i = 0;i < rows; i++) {
					row1 = sheet1.getRow(i);
					if(row1 != null) {
						tmp = sheet1.getRow(i).getPhysicalNumberOfCells();
						if(tmp > cols) cols = tmp;
					}
				}

				for(int r = 1; r < rows; r++) {
					row1 = sheet1.getRow(r);
					if(row1 != null) {
						for(int c = 0; c < cols; c++) {
							cell1 = row1.getCell((short)c);
							if(cell1 != null) {
								// Your code here
								//col_val[c]=cell1;
								col_val[c]=cell1.toString(); 
								System.out.println("Col value : "+col_val[c]);
								String TABLE_NAME="table_name"+r;
								String COLUMNS="columns_name"+r;
								String WHERE_CLAUSE="where_clause"+r;
								String FETCH_TYPE="fetch_type"+r;
								String INCR_COL="incr_col"+r;

								if ( c == 0) item.put(TABLE_NAME,col_val[c] );
								if ( c == 1) item.put(COLUMNS,col_val[c] );
								if ( c == 2) item.put(WHERE_CLAUSE,col_val[c] );
								if ( c == 3) item.put(FETCH_TYPE,col_val[c] );
								if ( c == 4) item.put(INCR_COL,col_val[c] );

							}else {
								col_val[c]="";
								System.out.println("Col value : "+col_val[c]);
								String TABLE_NAME="table_name"+r;
								String COLUMNS="columns_name"+r;
								String WHERE_CLAUSE="where_clause"+r;
								String FETCH_TYPE="fetch_type"+r;
								String INCR_COL="incr_col"+r;

								if ( c == 0) item.put(TABLE_NAME,col_val[c] );
								if ( c == 1) item.put(COLUMNS,col_val[c] );
								if ( c == 2) item.put(WHERE_CLAUSE,col_val[c] );
								if ( c == 3) item.put(FETCH_TYPE,col_val[c] );
								if ( c == 4) item.put(INCR_COL,col_val[c] );

							}
						}
					}
				}
				array.put(item);
				item_pre.put("data", array);
				array_pre.put(item_pre);
				item_first.put("body", array_pre);
				array_first.put(item_first);
				json_array_str = array_first.toString().replace("[", "").replace("]", "");
				System.out.println(json_array_str);
				wb.close();
			}catch(Exception ioe) {
				ioe.printStackTrace();
			}
		}
		else {

			System.out.println("No file details file");
		}
		return json_array_str;
	}
	@Override
	public String getJsonFromFeedSequence(String project,String src_sys_id) {
		JSONArray array_metadata = new JSONArray();
		JSONObject item_metadata = new JSONObject();

		JSONArray array_metadata_pre = new JSONArray();
		JSONObject item_metadata_pre = new JSONObject();

		JSONArray array_metadata_first = new JSONArray();
		JSONObject item_metadata_first = new JSONObject();
		item_metadata.put("feed_sequence", src_sys_id);
		item_metadata.put("project_id",project);
		array_metadata.put(item_metadata);
		item_metadata_pre.put("data", array_metadata);
		array_metadata_pre.put(item_metadata_pre);
		item_metadata_first.put("body", array_metadata_pre);
		array_metadata_first.put(item_metadata_first);
		String json_array_metadata_str = array_metadata_first.toString().replace("[", "").replace("]", "");
		return json_array_metadata_str;
	}

}