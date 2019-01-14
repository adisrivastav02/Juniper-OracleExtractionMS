package com.iig.gcp.extraction.oracle.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.iig.gcp.extraction.oracle.dto.ConnectionMaster;
import com.iig.gcp.extraction.oracle.dto.CountryMaster;
import com.iig.gcp.extraction.oracle.dto.DataDetailBean;
import com.iig.gcp.extraction.oracle.dto.DriveMaster;
import com.iig.gcp.extraction.oracle.dto.RunFeedsBean;
import com.iig.gcp.extraction.oracle.dto.SourceSystemDetailBean;
import com.iig.gcp.extraction.oracle.dto.SourceSystemMaster;
import com.iig.gcp.extraction.oracle.dto.TargetMaster;
import com.iig.gcp.extraction.oracle.dto.TempDataDetailBean;
import com.iig.gcp.extraction.oracle.service.ExtractionService;

@Controller
public class ExtractionController {

	@Autowired
	private ExtractionService es;
	public String userId="";
	public String project="";
	public String src_val="";
	public String jwt="";

	@RequestMapping(value = {"/"}, method = RequestMethod.GET)
	public ModelAndView extractionHome(@Valid @ModelAttribute("jsonObject") String jsonObject, ModelMap model, HttpServletRequest request) {
		System.out.println("in extraction controller");
		JSONObject jObj = new JSONObject(jsonObject);
		userId=jObj.getString("userId");
		project=jObj.getString("project");
		jwt=jObj.getString("jwt");
		src_val="Oracle";
		
		System.out.println("user->"+userId+" proj-->"+project+" jwt-->"+jwt);
		request.getSession().setAttribute("userId", userId);
		request.getSession().setAttribute("project", project);
		request.getSession().setAttribute("src_val", src_val);
		return new ModelAndView("/index");
		}
		
	@RequestMapping(value = "/extraction/Event", method = RequestMethod.GET)
	public ModelAndView Event() {
		return new ModelAndView("extraction/Event");
	}

	@RequestMapping(value = "/extraction/ConnectionDetails", method = RequestMethod.POST)
	public ModelAndView ConnectionDetails( ModelMap model, HttpServletRequest request) {
		model.addAttribute("src_val", "Oracle");
		model.addAttribute("usernm",(String)request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		
		ArrayList<String> system;
		try {
			system = es.getSystem(project);
			model.addAttribute("system", system);
			ArrayList<ConnectionMaster> conn_val = es.getConnections(src_val, project);
			model.addAttribute("conn_val", conn_val);
			ArrayList<DriveMaster> drive = es.getDrives((String) request.getSession().getAttribute("project"));
			model.addAttribute("drive", drive);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/ConnectionDetailsOracle");
	}

	@RequestMapping(value = "/extraction/ConnectionDetails1", method = RequestMethod.POST)
	public ModelAndView ConnectionDetails1(@Valid @ModelAttribute("x") String x, @ModelAttribute("src_val") String src_val, @ModelAttribute("button_type") String button_type, ModelMap model,
			HttpServletRequest request) throws UnsupportedOperationException, Exception {
		String resp = null;
		if (button_type.equalsIgnoreCase("add"))
			resp = es.invokeRest(x, "addConnection");
		else if (button_type.equalsIgnoreCase("upd"))
			resp = es.invokeRest(x, "updConnection");
		else if (button_type.equalsIgnoreCase("del"))
			resp = es.invokeRest(x, "delConnection");
		String status0[] = resp.toString().split(":");
		System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
		String status1[] = status0[1].split(",");
		String status = status1[0].replaceAll("\'", "").trim();
		String message0 = status0[2];
		String message = message0.replaceAll("[\'}]", "").trim();
		String final_message = status + ": " + message;
		System.out.println("final: " + final_message);
		if (status.equalsIgnoreCase("Failed")) {
			model.addAttribute("errorString", final_message);
		} else if (status.equalsIgnoreCase("Success")) {
			model.addAttribute("successString", final_message);
			model.addAttribute("next_button_active", "active");
		}
		model.addAttribute("src_val", src_val);
		//UserAccount u = (UserAccount) request.getSession().getAttribute("user");
		model.addAttribute("usernm",request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		ArrayList<String> system = es.getSystem((String) request.getSession().getAttribute("project"));
		model.addAttribute("system", system);
		ArrayList<ConnectionMaster> conn_val = es.getConnections(src_val, (String) request.getSession().getAttribute("project"));
		model.addAttribute("conn_val", conn_val);
		ArrayList<DriveMaster> drive = es.getDrives((String) request.getSession().getAttribute("project"));
		model.addAttribute("drive", drive);
		return new ModelAndView("extraction/ConnectionDetails");
	}

	@RequestMapping(value = "/extraction/ConnectionDetailsEdit", method = RequestMethod.POST)
	public ModelAndView ConnectionDetailsEdit(@Valid @ModelAttribute("conn") int conn, @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		ConnectionMaster conn_val = es.getConnections2(src_val, conn, (String) request.getSession().getAttribute("project"));
		model.addAttribute("conn_val", conn_val);
		model.addAttribute("src_val", src_val);
		ArrayList<DriveMaster> drive = es.getDrives((String) request.getSession().getAttribute("project"));
		model.addAttribute("drive", drive);
		return new ModelAndView("extraction/ConnectionDetailsEditOracle");
	}

	@RequestMapping(value = "/extraction/TargetDetails", method = RequestMethod.GET)
	public ModelAndView TargetDetails(@Valid ModelMap model, HttpServletRequest request) {
		ArrayList<TargetMaster> tgt;
		try {
			tgt = es.getTargets((String) request.getSession().getAttribute("project"));
			model.addAttribute("usernm", request.getSession().getAttribute("user"));
			model.addAttribute("project", (String) request.getSession().getAttribute("project"));
			ArrayList<String> system = es.getSystem((String) request.getSession().getAttribute("project"));
			model.addAttribute("system", system);
			model.addAttribute("tgt_val", tgt);
			ArrayList<DriveMaster> drive = es.getDrives((String) request.getSession().getAttribute("project"));
			model.addAttribute("drive", drive);
			ArrayList<String> tproj = es.getGoogleProject((String) request.getSession().getAttribute("project"));
			model.addAttribute("tproj", tproj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/TargetDetails");
	}

	@RequestMapping(value = "/extraction/TargetDetails0", method = RequestMethod.POST)
	public ModelAndView TargetDetails0(@Valid ModelMap model, @ModelAttribute("project1") String project1, HttpServletRequest request) {
		ArrayList<String> str;
		try {
			str = es.getServiceBucket(project1, (String) request.getSession().getAttribute("project"));
			
			ArrayList<String> sa = new ArrayList<String>();
			ArrayList<String> buck = new ArrayList<String>();
			for (int i = 0; i < str.size(); i++) {
				String y = (String) str.get(i);
				String[] x = y.split("\\|");
				sa.add(x[1]);
				buck.add(x[0]);
			}
			model.addAttribute("sa", sa);
			model.addAttribute("buck", buck);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/TargetDetails0");
	}

	@RequestMapping(value = "/extraction/TargetDetails1", method = RequestMethod.POST)
	public ModelAndView TargetDetails1(@Valid @ModelAttribute("x") String x, @ModelAttribute("button_type") String button_type, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		System.out.println(x);
		String resp = null;
		if (button_type.equalsIgnoreCase("add"))
			resp = es.invokeRest(x, "addTarget");
		else if (button_type.equalsIgnoreCase("upd"))
			resp = es.invokeRest(x, "updTarget");
		else if (button_type.equalsIgnoreCase("del"))
			resp = es.invokeRest(x, "delTarget");
		String status0[] = resp.toString().split(":");
		System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
		String status1[] = status0[1].split(",");
		String status = status1[0].replaceAll("\'", "").trim();
		String message0 = status0[2];
		String message = message0.replaceAll("[\'}]", "").trim();
		String final_message = status + ": " + message;
		System.out.println("final: " + final_message);
		if (status.equalsIgnoreCase("Failed")) {
			model.addAttribute("errorString", final_message);
		} else if (status.equalsIgnoreCase("Success")) {
			model.addAttribute("successString", final_message);
			model.addAttribute("next_button_active", "active");
		}
		ArrayList<TargetMaster> tgt = es.getTargets((String) request.getSession().getAttribute("project"));
		model.addAttribute("usernm", request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		ArrayList<String> system = es.getSystem((String) request.getSession().getAttribute("project"));
		model.addAttribute("system", system);
		model.addAttribute("tgt_val", tgt);
		ArrayList<DriveMaster> drive = es.getDrives((String) request.getSession().getAttribute("project"));
		model.addAttribute("drive", drive);
		ArrayList<String> tproj = es.getGoogleProject((String) request.getSession().getAttribute("project"));
		model.addAttribute("tproj", tproj);
		return new ModelAndView("extraction/TargetDetails");
	}

	@RequestMapping(value = "/extraction/TargetDetailsEdit", method = RequestMethod.POST)
	public ModelAndView TargetDetailsEdit(@Valid @ModelAttribute("tgt") int tgt, ModelMap model, HttpServletRequest request) {
		TargetMaster tgtx;
		try {
			tgtx = es.getTargets1(tgt);
			model.addAttribute("tgtx", tgtx);
			model.addAttribute("project", (String) request.getSession().getAttribute("project"));
			ArrayList<String> tproj = es.getGoogleProject((String) request.getSession().getAttribute("project"));
			model.addAttribute("tproj", tproj);
			ArrayList<DriveMaster> drive = es.getDrives((String) request.getSession().getAttribute("project"));
			model.addAttribute("drive", drive);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/TargetDetailsEdit");
	}

	@RequestMapping(value = "/extraction/SystemHome", method = RequestMethod.GET)
	public ModelAndView SystemHome() {
		return new ModelAndView("extraction/SystemHome");
	}

	@RequestMapping(value = "/extraction/SystemDetails", method = RequestMethod.POST)
	public ModelAndView SystemDetails(@Valid @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request) {
		model.addAttribute("src_val", src_val);
		ArrayList<SourceSystemMaster> src_sys_val;
		try {
			src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
			model.addAttribute("src_sys_val", src_sys_val);
			ArrayList<ConnectionMaster> conn_val = es.getConnections(src_val, (String) request.getSession().getAttribute("project"));
			model.addAttribute("conn_val", conn_val);
			ArrayList<TargetMaster> tgt = es.getTargets((String) request.getSession().getAttribute("project"));
			model.addAttribute("tgt", tgt);
			model.addAttribute("usernm",request.getSession().getAttribute("user"));
			model.addAttribute("project", (String) request.getSession().getAttribute("project"));
			ArrayList<CountryMaster> countries = es.getCountries();
			model.addAttribute("countries", countries);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/SystemDetails");
	}

	@RequestMapping(value = "/extraction/SystemDetails1", method = RequestMethod.POST)
	public ModelAndView SystemDetails1(@Valid @RequestParam(value = "sun", required = true) String sun, ModelMap model) throws UnsupportedOperationException, Exception {
		int stat = es.checkNames(sun);
		model.addAttribute("stat", stat);
		return new ModelAndView("extraction/SystemDetails1");
	}

	@RequestMapping(value = "/extraction/SystemDetails2", method = RequestMethod.POST)
	public ModelAndView SystemDetails2(@Valid @ModelAttribute("src_val") String src_val, @ModelAttribute("x") String x, @ModelAttribute("button_type") String button_type, ModelMap model,
			HttpServletRequest request) throws UnsupportedOperationException, Exception {
//		System.out.println(x);
		String resp = null;
		if (button_type.equalsIgnoreCase("add"))
			resp = es.invokeRest(x, "onboardSystem");
		else if (button_type.equalsIgnoreCase("upd"))
			resp = es.invokeRest(x, "updSystem");
		else if (button_type.equalsIgnoreCase("del"))
			resp = es.invokeRest(x, "delSystem");
		model.addAttribute("usernm", request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		String status0[] = resp.toString().split(":");
		System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
		String status1[] = status0[1].split(",");
		String status = status1[0].replaceAll("\'", "").trim();
		String message0 = status0[2];
		String message = message0.replaceAll("[\'}]", "").trim();
		String final_message = status + ": " + message;
		System.out.println("final: " + final_message);
		if (status.equalsIgnoreCase("Failed")) {
			model.addAttribute("errorString", final_message);
		} else if (status.equalsIgnoreCase("Success")) {
			model.addAttribute("successString", final_message);
			model.addAttribute("next_button_active", "active");
		}
		model.addAttribute("src_val", src_val);
		ArrayList<SourceSystemMaster> src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		model.addAttribute("src_sys_val", src_sys_val);
		ArrayList<ConnectionMaster> conn_val = es.getConnections(src_val, (String) request.getSession().getAttribute("project"));
		model.addAttribute("conn_val", conn_val);
		ArrayList<TargetMaster> tgt = es.getTargets((String) request.getSession().getAttribute("project"));
		model.addAttribute("tgt", tgt);
		/*
		 * ArrayList<String> buckets = DBUtils.getBuckets();
		 * model.addAttribute("buckets", buckets);
		 */
		ArrayList<CountryMaster> countries = es.getCountries();
		model.addAttribute("countries", countries);
		/*
		 * ArrayList<ReservoirMaster> reservoir = es.getReservoirs();
		 * model.addAttribute("reservoir", reservoir);
		 */
		return new ModelAndView("extraction/SystemDetails");
	}

	@RequestMapping(value = "/extraction/SystemDetailsEdit", method = RequestMethod.POST)
	public ModelAndView SystemDetailsEdit(@Valid @ModelAttribute("src_sys") int src_sys, @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		ArrayList<SourceSystemDetailBean> ssm = es.getSources1(src_val, src_sys);
		model.addAttribute("ssm", ssm);
		ArrayList<ConnectionMaster> conn_val = es.getConnections(src_val, (String) request.getSession().getAttribute("project"));
		model.addAttribute("conn_val", conn_val);
		ArrayList<TargetMaster> tgt = es.getTargets((String) request.getSession().getAttribute("project"));
		model.addAttribute("tgt", tgt);
		ArrayList<CountryMaster> countries = es.getCountries();
		model.addAttribute("countries", countries);
		model.addAttribute("src_val", src_val);
		return new ModelAndView("extraction/SystemDetailsEdit");
	}

	@RequestMapping(value = "/extraction/DataHome", method = RequestMethod.GET)
	public ModelAndView DataHome() {
		return new ModelAndView("extraction/DataHome");
	}

	@RequestMapping(value = "/extraction/DataDetails", method = RequestMethod.POST)
	public ModelAndView DataDetails(@Valid @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request) throws IOException {
		try 
		{
			model.addAttribute("src_val", src_val);
			ArrayList<SourceSystemMaster> src_sys_val1 = new ArrayList<SourceSystemMaster>();
			ArrayList<SourceSystemMaster> src_sys_val2 = new ArrayList<SourceSystemMaster>();
			ArrayList<SourceSystemMaster> src_sys_val;
			src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
			
		for (SourceSystemMaster ssm: src_sys_val) {
			  if(ssm.getFile_list() == null && ssm.getTable_list() == null & ssm.getDb_name() == null) //3rd Added for Hive
			  {
				  src_sys_val1.add(ssm);
			  }
			  else
			  {
				  src_sys_val2.add(ssm);
			  }
		}
		ArrayList<String> db_name = es.getHivedbList((String) request.getSession().getAttribute("project"));
		model.addAttribute("db_name", db_name);
		model.addAttribute("src_sys_val1", src_sys_val1);
		model.addAttribute("src_sys_val2", src_sys_val2);
		model.addAttribute("usernm", request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/DataDetails" + src_val);
	}

	@RequestMapping(value = "/extraction/DataDetailsOracle0", method = RequestMethod.POST)
	public ModelAndView DataDetails0(@Valid @ModelAttribute("src_sys_id") int src_sys_id, @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		String db_name=null;
		ConnectionMaster conn_val = es.getConnections1(src_val, src_sys_id);
		model.addAttribute("conn_val", conn_val);
		ArrayList<String> schema_name = es.getSchema(src_val, conn_val.getConnection_id(), (String) request.getSession().getAttribute("project"),db_name);
		model.addAttribute("schema_name", schema_name);
		model.addAttribute("usernm", request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/DataDetailsOracle0");
	}

	@RequestMapping(value = "/extraction/DataDetailsOracle1", method = RequestMethod.POST)
	public ModelAndView DataDetails1(@Valid @ModelAttribute("src_sys_id") int src_sys_id, @ModelAttribute("src_val") String src_val, @ModelAttribute("schema_name") String schema_name, ModelMap model,
		HttpServletRequest request) throws UnsupportedOperationException, Exception {
		String href1=es.getBulkDataTemplate(src_sys_id);
		String db_name=null;
		href1="field.xls";
		model.addAttribute("href1", href1);
		ConnectionMaster conn_val = es.getConnections1(src_val, src_sys_id);
		model.addAttribute("conn_val", conn_val);
		String ext_type = es.getExtType(src_sys_id);
		model.addAttribute("ext_type", ext_type);
		ArrayList<String> tables = es.getTables(src_val, conn_val.getConnection_id(), schema_name, (String) request.getSession().getAttribute("project"),db_name);
		model.addAttribute("tables", tables);
		model.addAttribute("schema_name", schema_name);
		model.addAttribute("src_sys_id", src_sys_id);
		return new ModelAndView("extraction/DataDetailsOracle1");
	}

	@RequestMapping(value = "/extraction/DataDetailsOracle2", method = RequestMethod.POST)
	public ModelAndView DataDetails2(@Valid @ModelAttribute("id") String id, @ModelAttribute("src_val") String src_val, @ModelAttribute("table_name") String table_name,
			@ModelAttribute("connection_id") int connection_id, @ModelAttribute("schema_name") String schema_name, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		String db_name=null;
		ArrayList<String> fields = es.getFields(id, src_val, table_name, connection_id, schema_name, (String) request.getSession().getAttribute("project"),db_name);
//		ConnectionMaster conn_val = es.getConnections1(src_val, src_sys_id);
//		ArrayList<String> src_tbl_sch = es.getSchema(src_val, conn_val.getConnection_id(), (String) request.getSession().getAttribute("project"));
//		model.addAttribute("src_tbl_sch", src_tbl_sch);
		model.addAttribute("fields", fields);
		model.addAttribute("id", id);
		
		return new ModelAndView("extraction/DataDetailsOracle2");
	}

	@RequestMapping(value = "/extraction/DataDetailsOracle3", method = RequestMethod.POST)
	public ModelAndView DataDetails3(@Valid @ModelAttribute("src_val") String src_val, @ModelAttribute("x") String x, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		String resp="";
		String src_sys_id="";
		String project=(String)request.getSession().getAttribute("project");
		
		
		if(x.contains("feed_id1")) {
			x = x.replace("feed_id1", "feed_id");
			System.out.println(x);
			 
			 resp = es.invokeRest(x, "editTempTableInfo");
		}else {
			System.out.println(x);
			resp = es.invokeRest(x, "addTempTableInfo");
		}
		
		String status0[] = resp.toString().split(":");
		System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
		String status1[] = status0[1].split(",");
		String status = status1[0].replaceAll("\'", "").trim();
		String message0 = status0[2];
		String message = message0.replaceAll("[\'}]", "").trim();
		String final_message = status + ": " + message;
		System.out.println("final: " + final_message);
		if (status.equalsIgnoreCase("Failed")) {
			model.addAttribute("errorString", final_message);
		} else if (status.equalsIgnoreCase("Success")) {
			JSONObject jsonObject = new JSONObject(x);
			 src_sys_id= jsonObject.getJSONObject("body").getJSONObject("data").getString("feed_id");
			 System.out.println("src_sys_id is"+src_sys_id);
			String json_array_metadata_str=es.getJsonFromFeedSequence(project,src_sys_id);
			System.out.println(json_array_metadata_str);
			resp = es.invokeRest(json_array_metadata_str, "metaDataValidation");
			if (resp.contains("success")) {
				model.addAttribute("successString", resp);
				model.addAttribute("next_button_active", "active");
			}else {
				model.addAttribute("errorString", resp);
			}	
		}
		ArrayList<SourceSystemMaster> src_sys_val1 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val2 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		for (SourceSystemMaster ssm: src_sys_val) {
			  if(ssm.getFile_list() == null && ssm.getTable_list() == null & ssm.getDb_name() == null) //3rd Added for Hive
			  {
				  src_sys_val1.add(ssm);
			  }
			  else
			  {
				  src_sys_val2.add(ssm);
			  }
			}
		ArrayList<String> db_name = es.getHivedbList((String) request.getSession().getAttribute("project"));
		model.addAttribute("db_name", db_name);
		model.addAttribute("src_sys_val1", src_sys_val1);
		model.addAttribute("src_sys_val2", src_sys_val2);
		model.addAttribute("usernm", request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/DataDetails" + src_val);	
	}

		@RequestMapping(value = "/extraction/DataDetailsEditOracle", method = RequestMethod.POST)
		public ModelAndView DataDetailsEdit(@Valid @ModelAttribute("src_sys_id") int src_sys_id, @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		String href1=es.getBulkDataTemplate(src_sys_id);
		href1="field.xls";
		String db_name=null;
		model.addAttribute("href1", href1);
		ConnectionMaster conn_val = es.getConnections1(src_val, src_sys_id);
		model.addAttribute("conn_val", conn_val);
		String ext_type = es.getExtType(src_sys_id);
		model.addAttribute("ext_type", ext_type);
		String schema_name = es.getSchemaData(src_val, src_sys_id);
		ArrayList<String> tables = es.getTables(src_val, conn_val.getConnection_id(), schema_name, (String) request.getSession().getAttribute("project"),db_name);
		model.addAttribute("tables", tables);
		ArrayList<DataDetailBean> arrddb = es.getData(src_sys_id, src_val, conn_val.getConnection_id(), schema_name, (String) request.getSession().getAttribute("project"),db_name);
		model.addAttribute("schem", schema_name);
		model.addAttribute("arrddb", arrddb);
		model.addAttribute("usernm", (String)request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/DataDetailsEditOracle");
	}

	@RequestMapping(value = "/extraction/ExtractHome", method = RequestMethod.GET)
	public ModelAndView ExtractHome() {
		return new ModelAndView("extraction/ExtractHome");
	}

	@RequestMapping(value = "/extraction/ExtractData", method = RequestMethod.POST)
	public ModelAndView ExtractData(@Valid @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request) throws IOException {
		try {
		model.addAttribute("src_val", src_val);
		ArrayList<SourceSystemMaster> src_sys_val1 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val;
		src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		
		for (SourceSystemMaster ssm: src_sys_val) {
			  if(ssm.getFile_list() == null && ssm.getTable_list() == null && ssm.getDb_name() == null); //3rd Added for Hive
			  else
			  {
				  src_sys_val1.add(ssm);
			  }
			}
			model.addAttribute("src_sys_val", src_sys_val1);
		model.addAttribute("usernm", (String)request.getSession().getAttribute("user"));
		model.addAttribute("project", (String)request.getSession().getAttribute("project"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/ExtractData");
	}

	@RequestMapping(value = "/extraction/ExtractData1", method = RequestMethod.POST)
	public ModelAndView ExtractData1(@Valid @ModelAttribute("feed_name") String feed_name, @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request)
			throws UnsupportedOperationException, Exception {
		String ext_type = es.getExtType1(feed_name);
		if(ext_type.equals("Real")||ext_type.equals("Batch")||ext_type.equals("Event")){
			model.addAttribute("ext_type", ext_type);
		}else {
			model.addAttribute("errorString", "Job already ordered "+ext_type );
		}
		/*String ext_type = es.getExtType1(feed_name);
		model.addAttribute("ext_type", ext_type);*/
		ArrayList<String> kafka_topic = es.getKafkaTopic();
		model.addAttribute("kafka_topic", kafka_topic);
		return new ModelAndView("extraction/ExtractData1");
	}

	@RequestMapping(value = "/extraction/ExtractData2", method = RequestMethod.POST)
	public ModelAndView ExtractData2(@Valid @ModelAttribute("feed_name") String feed_name, @ModelAttribute("src_val") String src_val, @ModelAttribute("x") String x,
			@ModelAttribute("ext_type") String ext_type, ModelMap model, HttpServletRequest request) throws UnsupportedOperationException, Exception {
		String resp = null;
		System.out.println(x);
		//if (ext_type.equalsIgnoreCase("Batch")) {
			resp = es.invokeRest(x, "createDag");
			es.updateLoggerTable(feed_name);
		//} else {
		//	resp = es.invokeRest(x, "extractData");
		//}
		String status0[] = resp.toString().split(":");
		System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
		String status1[] = status0[1].split(",");
		String status = status1[0].replaceAll("\'", "").trim();
		String message0 = status0[2];
		String message = message0.replaceAll("[\'}]", "").trim();
		String final_message = status + ": " + message;
		System.out.println("final: " + final_message);
		if (status.equalsIgnoreCase("Failed")) {
			model.addAttribute("errorString", final_message);
		} else if (status.equalsIgnoreCase("Success")) {
			model.addAttribute("successString", final_message);
			model.addAttribute("next_button_active", "active");
		}
		model.addAttribute("src_val", src_val);
		ArrayList<SourceSystemMaster> src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		model.addAttribute("src_sys_val", src_sys_val);
		model.addAttribute("usernm", (String)request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/ExtractData");
	}
	
	@RequestMapping(value = "/extraction/ExtractData3", method = RequestMethod.POST)
	public ModelAndView ExtractData3(@Valid @ModelAttribute("feed_name") String feed_name, @ModelAttribute("src_val") String src_val, @ModelAttribute("x") String x,
			@ModelAttribute("ext_type") String ext_type, ModelMap model, HttpServletRequest request) throws UnsupportedOperationException, Exception {
		String final_message=null;
		System.out.println(x);
		final_message = es.invokeRest1(x, "feednm/extractData");
		model.addAttribute("successString", final_message);
/*		String status0[] = resp.toString().split(":");
		System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
		String status1[] = status0[1].split(",");
		String status = status1[0].replaceAll("\'", "").trim();
		String message0 = status0[2];
		String message = message0.replaceAll("[\'}]", "").trim();
		String final_message = status + ": " + message;*/
		System.out.println("final: " + final_message);
		/*if (status.equalsIgnoreCase("Failed")) {
			model.addAttribute("errorString", final_message);
		} else if (status.equalsIgnoreCase("Success")) {
			model.addAttribute("successString", final_message);
			model.addAttribute("next_button_active", "active");
		}*/
		model.addAttribute("src_val", src_val);
		ArrayList<SourceSystemMaster> src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		model.addAttribute("src_sys_val", src_sys_val);
		model.addAttribute("usernm",(String)request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/ExtractData");
	}

	@RequestMapping(value = "/extraction/ViewFeedRun", method = RequestMethod.GET)
	public ModelAndView ViewFeedRun(ModelMap model, HttpServletRequest request) {
		ArrayList<String> feedarr;
		try {
			feedarr = es.getRunFeeds((String) request.getSession().getAttribute("project"));
		model.addAttribute("feedarr", feedarr);
		} catch( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/FeedRun");

	}

	@RequestMapping(value = "/extraction/FeedStatus", method = RequestMethod.POST)
	public ModelAndView FeedStatus(@Valid @ModelAttribute("feed_val") String feed_val, ModelMap model, HttpServletRequest request) throws IOException {
		ArrayList<RunFeedsBean> runfeeds;
		try {
			runfeeds = es.getLastRunFeeds((String) request.getSession().getAttribute("project"), feed_val);
		model.addAttribute("runfeeds", runfeeds);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new ModelAndView("extraction/FeedRunStatus");
	}

	public File convert(MultipartFile multiPartFile) throws Exception {
		File convFile = new File(multiPartFile.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(multiPartFile.getBytes());
		fos.close();
		return convFile;

}
	@RequestMapping(value = "/extraction/CreateBulkLoadDetails", method = RequestMethod.POST)
	public ModelAndView CreateBulkLoadDetails(@Valid @ModelAttribute("src_val") String src_val, 
			@Valid @ModelAttribute("feed_id") String src_sys_id, 
			@RequestParam("file") MultipartFile multiPartFile1,
			ModelMap model,HttpServletRequest request,
			Principal principal) throws UnsupportedOperationException, Exception {		
	
		String resp = null;
		String usernm= (String)request.getSession().getAttribute("user");
		String project=(String)request.getSession().getAttribute("project");
		String schema_name=(String)request.getSession().getAttribute("schema_name");
		File file = convert(multiPartFile1);
		String json_array_str=es.getJsonFromFile(file,usernm,schema_name,project,src_sys_id);
		String json_array_metadata_str=es.getJsonFromFeedSequence(project,src_sys_id);
					System.out.println(json_array_str);
					resp = es.invokeRest(json_array_str, "addTempTableInfo");
					String status0[] = resp.toString().split(":");
					System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
					String status1[] = status0[1].split(",");
					String status = status1[0].replaceAll("\'", "").trim();
					String message0 = status0[2];
					String message = message0.replaceAll("[\'}]", "").trim();
					String final_message = status + ": " + message;
					System.out.println("final: " + final_message);
					if (status.equalsIgnoreCase("Failed")) {
						model.addAttribute("errorString", final_message);
					} else if (status.equalsIgnoreCase("Success")) {	
						resp = es.invokeRest(json_array_metadata_str, "metaDataValidation");
						if (resp.contains("success")) {
							model.addAttribute("successString", resp);
							model.addAttribute("next_button_active", "active");
						}else {
							model.addAttribute("errorString", resp);
						}	
					}
		//model.addAttribute("successString", resp);
		ArrayList<SourceSystemMaster> src_sys_val1 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val2 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		for (SourceSystemMaster ssm: src_sys_val) {
			  if(ssm.getFile_list() == null && ssm.getTable_list() == null & ssm.getDb_name() == null) //3rd Added for Hive
			  {
				  src_sys_val1.add(ssm);
			  }
			  else
			  {
				  src_sys_val2.add(ssm);
			  }
			}
		ArrayList<String> db_name = es.getHivedbList((String) request.getSession().getAttribute("project"));
		model.addAttribute("db_name", db_name);
		model.addAttribute("src_sys_val1", src_sys_val1);
		model.addAttribute("src_sys_val2", src_sys_val2);
		model.addAttribute("usernm", usernm);
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/DataDetails" + src_val);	
	}
	
	@RequestMapping(value = "/extraction/EditBulkLoadDetails", method = RequestMethod.POST)
	public ModelAndView EditBulkLoadDetails(@Valid @ModelAttribute("src_val") String src_val, 
			@Valid @ModelAttribute("feed_id1") String src_sys_id, 
			@RequestParam("file") MultipartFile multiPartFile1,
			ModelMap model,HttpServletRequest request,
			Principal principal) throws UnsupportedOperationException, Exception {
		
		String resp = null;
		String usernm= (String)request.getSession().getAttribute("user");
		String project=(String)request.getSession().getAttribute("project");
		String schema_name=(String)request.getSession().getAttribute("schema_name");
		File file = convert(multiPartFile1);
		String json_array_str=es.getJsonFromFile(file,usernm,schema_name,project,src_sys_id);
		String json_array_metadata_str=es.getJsonFromFeedSequence(project,src_sys_id);
					System.out.println(json_array_str);
					resp = es.invokeRest(json_array_str, "editTempTableInfo");
					String status0[] = resp.toString().split(":");
					System.out.println(status0[0] + " value " + status0[1] + " value3: " + status0[2]);
					String status1[] = status0[1].split(",");
					String status = status1[0].replaceAll("\'", "").trim();
					String message0 = status0[2];
					String message = message0.replaceAll("[\'}]", "").trim();
					String final_message = status + ": " + message;
					System.out.println("final: " + final_message);
					if (status.equalsIgnoreCase("Failed")) {
						model.addAttribute("errorString", final_message);
					} else if (status.equalsIgnoreCase("Success")) {	
						resp = es.invokeRest(json_array_metadata_str, "metaDataValidation");
						if (resp.contains("success")) {
							model.addAttribute("successString", resp);
							model.addAttribute("next_button_active", "active");
						}else {
							model.addAttribute("errorString", resp);
						}	
					}
		//model.addAttribute("successString", resp);
		ArrayList<SourceSystemMaster> src_sys_val1 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val2 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		for (SourceSystemMaster ssm: src_sys_val) {
			  if(ssm.getFile_list() == null && ssm.getTable_list() == null & ssm.getDb_name() == null) //3rd Added for Hive
			  {
				  src_sys_val1.add(ssm);
			  }
			  else
			  {
				  src_sys_val2.add(ssm);
			  }
			}
		ArrayList<String> db_name = es.getHivedbList((String) request.getSession().getAttribute("project"));
		model.addAttribute("db_name", db_name);
		model.addAttribute("src_sys_val1", src_sys_val1);
		model.addAttribute("src_sys_val2", src_sys_val2);
		model.addAttribute("usernm", usernm);
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		return new ModelAndView("extraction/DataDetails" + src_val);
	}

	@RequestMapping(value = "/extraction/FeedValidation", method = RequestMethod.GET)
	public ModelAndView FeedValidation() {
		return new ModelAndView("extraction/FeedValidation");
	}

	@RequestMapping(value = "/extraction/FeedDetails", method = RequestMethod.POST)
	public ModelAndView FeedDetails(@Valid @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request) throws IOException {
		try {
		model.addAttribute("src_val", src_val);
		ArrayList<SourceSystemMaster> src_sys_val1 = new ArrayList<SourceSystemMaster>();
		//ArrayList<SourceSystemMaster> src_sys_val2 = new ArrayList<SourceSystemMaster>();
		ArrayList<SourceSystemMaster> src_sys_val;
		src_sys_val = es.getSources(src_val, (String) request.getSession().getAttribute("project"));
		
		for (SourceSystemMaster ssm: src_sys_val) {		  
				  src_sys_val1.add(ssm);		  
			}
		ArrayList<String> db_name = es.getHivedbList((String) request.getSession().getAttribute("project"));
		model.addAttribute("db_name", db_name);
		model.addAttribute("src_sys_val1", src_sys_val1);
//		model.addAttribute("src_sys_val1", src_sys_val2);
		model.addAttribute("usernm", (String)request.getSession().getAttribute("user"));
		model.addAttribute("project", (String) request.getSession().getAttribute("project"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ModelAndView("extraction/FeedDetails" + src_val);
	}
	
	@RequestMapping(value = { "/extraction/FeedValidationDashboard"}, method = RequestMethod.POST)
	public ModelAndView FeedValidationDashboard(@Valid @ModelAttribute("src_sys_id") int src_sys_id, @ModelAttribute("src_val") String src_val, ModelMap model, HttpServletRequest request)
			throws Exception {
		System.out.println("Reached inside the controller");
		System.out.println("Inside Controller : "+src_sys_id);
		System.out.println("Inside Controller src_val : "+src_val);
		//String schema_name = es.getSchemaData(src_val, src_sys_id);
		ConnectionMaster conn_val = es.getConnections1(src_val, src_sys_id);
		ArrayList<TempDataDetailBean> arrddb = es.getTempData(src_sys_id, src_val, conn_val.getConnection_id(), (String) request.getSession().getAttribute("project"));
		model.addAttribute("arrddb", arrddb);
		//model.addAttribute("schem", schema_name);
		
		return  new ModelAndView("/extraction/FeedValidationDashboard");
	}
	
	@RequestMapping(value = "/extraction/BulkLoadTest", method = RequestMethod.POST)
	public ModelAndView BulkLoadTest(@Valid @ModelAttribute("src_sys_id") int src_sys_id, @ModelAttribute("src_val") String src_val, @ModelAttribute("selection") String selection, ModelMap model,
		HttpServletRequest request) throws UnsupportedOperationException, Exception {
		String href1=es.getBulkDataTemplate(src_sys_id);
		href1="field.xls";
		model.addAttribute("href1", href1);
		model.addAttribute("src_sys_id", src_sys_id);
		model.addAttribute("src_val", src_val);
		model.addAttribute("selection", selection);
		return new ModelAndView("extraction/BulkLoadTest");
	}
}
