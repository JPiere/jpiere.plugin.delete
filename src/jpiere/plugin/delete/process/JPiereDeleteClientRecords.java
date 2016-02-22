/******************************************************************************
 * Product: JPiere                                                            *
 * Copyright (C) Hideaki Hagiwara (h.hagiwara@oss-erp.co.jp)                  *
 *                                                                            *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY.                          *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * JPiere is maintained by OSS ERP Solutions Co., Ltd.                        *
 * (http://www.oss-erp.co.jp)                                                 *
 *****************************************************************************/
package jpiere.plugin.delete.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import jpiere.plugin.delete.model.MCustomDeleteProfile;
import jpiere.plugin.delete.model.MCustomDeleteProfileLine;
import jpiere.plugin.delete.model.MDeleteClientLogJP;
import jpiere.plugin.delete.model.MDeleteProfile;

import org.adempiere.exceptions.DBException;
import org.adempiere.util.IProcessUI;
import org.compiere.db.AdempiereDatabase;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MAcctSchemaElement;
import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MSequence;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;

/**
 * 	Process of Initialize Client and Delete Client.
 *  (PostgreSQL only)
 *
 *  This process was developed in reference to "drop_client.sql".
 *  "drop_client.sql" have Credits:
 *  Credits due to Fernando Lucktemberg(fer_luck) for the main workings
 *  Credit to the guys @ e-Nition.com for improving it :)
 *  Credit to the Tony Snook for change in discovering the relnamespace
 *  Credit to Jesus Garcia and Carlos Ruiz from globalqss for adding changes to reference columns with names <> AD_Client_ID
 *
 *  @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 *
 */
public class JPiereDeleteClientRecords extends SvrProcess
{

	//Parameters
	private int p_LookupClientID = 0;
	private String p_JP_Delete_Client = "TC";
	private int p_JP_DeleteProfile_ID = 0;
	private boolean p_IsTruncateJP = false;
	private boolean p_IsAllowLogging = false; //if true allow to get ZERO SQL Log

	//For debug and logging
	private boolean DEBUG_BULK_UPDATE_LOG = false;

	private int JP_CustomDeleteProfile_ID = 0;
	private int JP_CustomDeleteProfileLine_ID = 0;

	//Type of Process
	public static final String TYPE_DELETE_CLIENT = "DC";
	public static final String TYPE_INITIALIZE_CLIENT = "IC";
	public static final String TYPE_ALL_TRANSACTION = "TA";
	public static final String TYPE_CLIENT_TRANSACTION ="TC";
	public static final String TYPE_CUSTOM_DELETE = "CD";

	//Treat of Foreign key
	public static final String TREAT_SET_NULL = "TSN";
	public static final String TREAT_SET_VALUE = "TSV";
	public static final String TREAT_DELETE = "TDE";
	public static final String TREAT_IF_MANDATORY_DELETE_ELSE_VALUE = "IDV";
	public static final String TREAT_IF_MANDATORY_DELETE_ELSE_NULL = "IDN";
	public static final String TREAT_IF_MANDATORY_VALUE_ELSE_NULL = "IVN";
	public static final String TREAT_IF_MANDATORY_VALUE_ELSE_DELETE = "IVD";

	//Where Clause "NOT IN" or "IN"
	private static final boolean WHERE_IN = true;
	private static final boolean WHERE_NOT_IN = false;

	//List of Table
	private ArrayList<String> TABLELIST_DB;	//Table of Database(PostgreSQL)
	private ArrayList<String> TABLELIST_AD;	//Table of Application Dictionary(Have AD_Client_ID)

	private String[] Tables_DeleteAllRecords ;
	private String[] Tables_Not_DeleteAllRecords;
	private String[] Tables_CustomDelete;

	//Process UI
	private IProcessUI processMonitor = null;

	private MDeleteProfile m_DeleteProfile = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("LookupClientID"))
			{
				p_LookupClientID = para[i].getParameterAsInt();
			}else if(name.equals("JP_Delete_Client")){
				p_JP_Delete_Client = para[i].getParameterAsString();
			}else if(name.equals("JP_DeleteProfile_ID")){
				p_JP_DeleteProfile_ID = para[i].getParameterAsInt();
			}else if(name.equals("IsTruncateJP")){
				p_IsTruncateJP = para[i].getParameterAsBoolean();
			}else if(name.equals("IsAllowLogging")){
				p_IsAllowLogging = para[i].getParameterAsBoolean();
			}else{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}	//	prepare

	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt() throws Exception
	{
		//Super User can not delete data.
		int AD_User_ID = Env.getAD_User_ID(Env.getCtx());
		if(AD_User_ID==100)//AD_User_ID == 100 that is SuperUser
		{
			//Super User can not execute this process. Please relogin System user or others that can login System client.
			String msg = Msg.getMsg(getCtx(), "JP_Delete_SuperUser_CanNot");
			addLog(msg);
			createLog("","",msg,"","","", false);
			return msg;
		}

		//System Client do not delete.
		if(p_JP_Delete_Client.equals(TYPE_CLIENT_TRANSACTION)
				|| p_JP_Delete_Client.equals(TYPE_DELETE_CLIENT)
				|| p_JP_Delete_Client.equals(TYPE_INITIALIZE_CLIENT)
				|| p_JP_Delete_Client.equals(TYPE_CUSTOM_DELETE))
		{
			if(p_LookupClientID==0)
			{
				MClient client = new MClient(getCtx(), 0, null);
				String msg = Msg.getMsg(getCtx(), "DeleteError") + " " + client.getName() + " " + Msg.getElement(getCtx(), "AD_Client_ID");
				addLog(msg);
				createLog("","",msg, "","","", false);
				return msg;//"Could not delete record: System Client"
			}
		}else if(p_JP_Delete_Client.equals(TYPE_ALL_TRANSACTION)){
			if(p_LookupClientID != 0)
			{
				//All Transaction Records can delete When Client ID 0
				String msg = Msg.getMsg(getCtx(), "JP_Delete_AllTransactionRecords");
				addLog(msg);
				createLog("","",msg, "","","", false);
				return msg;
			}
		}

		MClient m_Client = MClient.get(getCtx(), p_LookupClientID);
		if(Util.isEmpty(m_Client.getName()))
		{
			String msg =Msg.getMsg(getCtx(), "NoRecordID") +" : " +"AD_Client_ID = " + p_LookupClientID;
			addLog(msg);
			createLog("","",msg, "","","", false);
			return msg;//Record ID doesn't exist in the table. AD_Client_ID =
		}


		if(p_JP_Delete_Client.equals(TYPE_CLIENT_TRANSACTION)
				|| p_JP_Delete_Client.equals(TYPE_ALL_TRANSACTION)
				|| p_JP_Delete_Client.equals(TYPE_INITIALIZE_CLIENT)
				|| p_JP_Delete_Client.equals(TYPE_CUSTOM_DELETE))
		{
			if(p_JP_DeleteProfile_ID != 0)
			{
				m_DeleteProfile = new MDeleteProfile(getCtx(), p_JP_DeleteProfile_ID, null);
			}

			if(p_JP_DeleteProfile_ID==0 && (p_JP_Delete_Client.equals(TYPE_INITIALIZE_CLIENT)
					|| p_JP_Delete_Client.equals(TYPE_CUSTOM_DELETE) ))
			{
				String msg = Msg.getMsg(getCtx(), "FillMandatory")+" "+Msg.getElement(null, "JP_DeleteProfile_ID");
				addLog(msg);
				createLog("","",msg, "","","", false);
				return msg;//Fill mandatory fields: Delete Profile
			}

		}


		addBufferLog(0, null, null, "Process Log", MTable.getTable_ID("AD_PInstance"), getAD_PInstance_ID());

		processMonitor = Env.getProcessUI(getCtx());
		StringBuilder message = new StringBuilder();
		try
		{
			executeUpdateConstraint("D");
			commitEx();

			TABLELIST_DB = createTableList_DB();
			TABLELIST_AD = createTableList_AD();

			doBeforeCheck(p_JP_Delete_Client);

			String msg = beforJPiereDeleteProcess();
			if(!Util.isEmpty(msg))
				message.append("Befor Delete Process : "+ msg);

			if(p_JP_Delete_Client.equals(TYPE_DELETE_CLIENT))
			{
				p_IsTruncateJP = false;
				p_JP_DeleteProfile_ID = 0;
				m_DeleteProfile = null;
				msg = doDeleteClient();
				if(!Util.isEmpty(msg))
					message.append("-->Delete Client : "+ msg);

			}else if(p_JP_Delete_Client.equals(TYPE_ALL_TRANSACTION)|| p_JP_Delete_Client.equals(TYPE_CLIENT_TRANSACTION)){
				msg = doDeleteTransactions(p_JP_Delete_Client);
				if(!Util.isEmpty(msg))
					message.append("-->Delete Transactions : "+ msg);

			}else if(p_JP_Delete_Client.equals(TYPE_INITIALIZE_CLIENT)){
				p_IsTruncateJP = false;
				msg = doInitializeClient();
				if(!Util.isEmpty(msg))
					message.append("-->Initialize Client : "+ msg);
			}else if(p_JP_Delete_Client.equals(TYPE_CUSTOM_DELETE)){
				p_IsTruncateJP = false;
				msg = doCustomDeleteProfile(m_DeleteProfile);
				if(!Util.isEmpty(msg))
					message.append("-->Custom Delete : "+ msg);
			}

			msg = afterJPiereDeleteProcess();
			if(!Util.isEmpty(msg))
				message.append("-->After Delete Process : "+ msg);

			msg = doAfterCheck(p_JP_Delete_Client);
			message.append(msg);

		}catch (Exception e){
			message.append("--------> Plese Check Process Log");
			if(e instanceof DBException)
			{
				DBException dbe = (DBException)e;
				addLog(Msg.getMsg(getCtx(), "Error") +" SQL: "+ dbe.getSQL());
				addLog(dbe.toString());
				createLog("","",dbe.getMessage() +"  SQL: " + dbe.getSQL(),"","","",false);
			}else{
				addLog(e.toString());
				createLog("","",e.toString(), "","","", false);
			}
			throw new Exception(e.toString() + message.toString());
		} finally {
			executeUpdateConstraint("O");
			commitEx();
		}

		if(Util.isEmpty(message.toString()))
			message.append(Msg.getMsg(getCtx(), Msg.getMsg(getCtx(), "Success")));
		else
			message.append("--------> Plese Check Process Log");

		addLog(message.toString());
		createLog("","",message.toString(),"","","", true);


		return message.toString();

	}	//	delete


	/***************Main Logic***************/
	/**
	 *
	 * @throws Exception
	 */
	private String beforJPiereDeleteProcess() throws Exception
	{
		if(p_JP_Delete_Client.equals(TYPE_CUSTOM_DELETE))
			return "";

		createLog("", "", "##### BEFOR PROCESS #####", "", "", "",true);

		createLog("", "", "### DELETE U_RoleMenu Table that data  may be inconsistent ###", "", "", "",false);
		ArrayList<Integer> AD_Role_IDs = getIDList("AD_Role_ID", "AD_Role", "AD_Client_ID = 0", TYPE_ALL_TRANSACTION);
		executeDeleteSQL("U_RoleMenu", createWhereInIDs("AD_Role_ID", AD_Role_IDs, WHERE_NOT_IN), TYPE_ALL_TRANSACTION, false,"BEFORE_PROCESS");
		commitEx();
		createLog("", "", "COMMIT", "", "", "",false);

		createLog("", "", "### DELETE AD_ChangeLog and AD_Session WHERE AD_Client_ID <> 0 ###", "", "", "BEFORE_PROCESS",false);
		executeDeleteSQL("AD_ChangeLog", "AD_Client_ID <> 0", TYPE_ALL_TRANSACTION, false, "BEFORE_PROCESS");
		executeDeleteSQL("AD_ChangeLog", "AD_Client_ID = 0 AND AD_Session_ID IN (SELECT DISTINCT(AD_Session_ID) FROM AD_Session WHERE AD_Client_ID <> 0 )"
																										, TYPE_ALL_TRANSACTION, false, "BEFORE_PROCESS");
		commitEx();

		executeDeleteSQL("AD_Session", "AD_Client_ID <> 0", TYPE_ALL_TRANSACTION, false, "BEFORE_PROCESS");
		commitEx();
		createLog("", "", "COMMIT", "", "", "",false);

		if(p_LookupClientID != 0 &&
				(p_JP_Delete_Client.equals(TYPE_INITIALIZE_CLIENT) ||  p_JP_Delete_Client.equals(TYPE_DELETE_CLIENT)) )
		{
			createLog("", "", "### DELETE AD_Preference Table that data may be inconsistent ###", "", "", "",false);
			ArrayList<Integer> AD_User_IDs = getIDList("AD_User_ID", "AD_User", "AD_Client_ID <> " + p_LookupClientID, TYPE_ALL_TRANSACTION);
			executeDeleteSQL("AD_Preference", createWhereInIDs("AD_User_ID", AD_User_IDs, WHERE_NOT_IN) + " AND AD_Client_ID <> " + p_LookupClientID
																										, TYPE_ALL_TRANSACTION, false, "BEFORE_PROCESS");
			commitEx();
			createLog("", "", "COMMIT", "", "", "",false);
		}

		createLog("", "", "### DELETE IMPORT AND TEMPORARY TABLES ###", "", "", "",false);
		for(String IorT_TABLE : TABLELIST_AD)
		{
			if(IorT_TABLE.startsWith("I_"))
			{
				executeDeleteSQL(IorT_TABLE, null, TYPE_ALL_TRANSACTION, false, "BEFORE_PROCESS");
				continue;
			}

			if(IorT_TABLE.startsWith("T_") && !IorT_TABLE.toUpperCase().equals("T_DeleteClientLogJP".toUpperCase()))
			{
				executeDeleteSQL(IorT_TABLE, null, TYPE_ALL_TRANSACTION, false, "BEFORE_PROCESS");
				continue;
			}
		}

		commitEx();
		createLog("", "", "COMMIT", "", "", "",false);

		return "";
	}

	/**
	 *
	 * @throws Exception
	 */
	private String afterJPiereDeleteProcess() throws Exception
	{
		createLog("", "", "##### AFTER PROCESS #####", "", "", "",true);


		if(m_DeleteProfile == null || !p_JP_Delete_Client.equals(TYPE_INITIALIZE_CLIENT))
			return "";

		commitEx();
		String name2 = m_DeleteProfile.getName2();
		if(!Util.isEmpty(name2))
		{
			createLog("", "", "### RENEAME CLIENT ###", "", "", "",false);
			MClient client = MClient.get(getCtx(), p_LookupClientID);
			client.setName(name2);
			client.saveEx(get_TrxName());
		}

		return "";
	}


	/**
	 * Delete Client Process
	 *
	 * @return
	 * @throws Exception
	 */
	private String doDeleteClient() throws Exception
	{
		addLog("##### DELETE CLIENT #####");
		createLog("", "", "##### DELETE CLIENT #####", "", "", "",true);

		StringBuilder message = new StringBuilder("");

		//Delete Transaction Data
		doDeleteTransactions(TYPE_CLIENT_TRANSACTION);

		String[] exceptionTables ={};
		String[] ExclusionTable = stringArray_Merge(TrxTables, exceptionTables);

		//Delete All Recond belong to Delete Client Except Transaction Tables.
		addLog("### DELETE ALL RECORDS BELONG TO DELETE CLIENT EXCEPT TRANSACTION TABLES ###");
		createLog("", "", "### DELETE ALL RECORDS BELONG TO DELETE CLIENT EXCEPT TRANSACTION TABLES ###", "", "", "",false);
		boolean isContaine = false;
		for(String AD_TABLE : TABLELIST_AD)
		{
			isContaine = false;
			for(int i = 0; i < ExclusionTable.length; i++)
			{
				if(AD_TABLE.toUpperCase().equals(ExclusionTable[i].toUpperCase()))
					isContaine = true;
			}

			if(isContaine)
				continue;

			int deletes = executeDeleteSQL(AD_TABLE, null, TYPE_DELETE_CLIENT, false, "DELETE_CLIENT");
			if(deletes==-1)
			{
				createLog("","","NOT FOUND: "+ AD_TABLE,"","","", true);
			}else if(deletes==-2){
				;
			}else if(deletes==-3){
				;
			}else{
				;
			}
		}

		//Delete Records that refer AD_Client indirectly(In case Column name is Not AD_Client_ID).
		addLog("### DELETE ALL RECORDS REFER TO DELETE CLIENT ###");
		createLog("", "", "###  DELETE ALL RECORDS REFER TO DELETE CLIENT ###", "", "", "",false);
		ArrayList<Integer> clientList = new ArrayList<Integer>();
		clientList.add(p_LookupClientID);
		bulkUpdate_canNotReferTableDirect("AD_Client", clientList, WHERE_IN, TREAT_DELETE, 0, ExclusionTable, WHERE_NOT_IN, TYPE_ALL_TRANSACTION);

		//Rest Table ID
		doResetKeyID(TABLELIST_AD.toArray(new String[TABLELIST_AD.size()]));

		return message.toString();
	}


	/**
	 * Delete Transactions Process
	 *
	 * @param type DELETE_CLIENT / INITIALIZE_CLIENT / ALL_TRANSACTION / CLIENT_TRANSACTION
	 * @return
	 * @throws Exception
	 */
	private String doDeleteTransactions(String type) throws Exception
	{
		addLog("##### DELETE TRANSACTION RECORDS #####");
		createLog("", "", "##### DELETE TRANSACTION RECORDS #####", "", "", "",true);

		StringBuilder message = new StringBuilder("");
		ArrayList<String> NotFoundTableList = new ArrayList<String>();

		//Delete Records in Transaction Tables
		createLog("", "", "### DELETE REDORDS IN TRANSACTION TABLES ###", "", "", "",false);
		for(int i = 0; i< TrxTables.length; i++)
		{

			if(Tables_CustomDelete != null && stringArray_IsIN(Tables_CustomDelete, TrxTables[i]))
				continue;

			int deletes = executeDeleteSQL(TrxTables[i], null, type, p_IsTruncateJP,"TRX_TABLE");

			if(deletes==-1)
			{
				createLog("","","NOT FOUND: "+ TrxTables[i],"","","", true);
				NotFoundTableList.add(TrxTables[i]);
			}else if(deletes==-2){
				;
			}else if(deletes==-3){
				;
			}

		}//
		commitEx();
		createLog("", "", "COMMIT", "", "", "",false);

		//Set null to Records that can refer Transaction Tables Directly(TableName_ID)
		if(!p_JP_Delete_Client.equals(TYPE_DELETE_CLIENT))//Skip when Delete Client
		{

			if(!type.equals(TYPE_ALL_TRANSACTION) && !p_IsTruncateJP)//Skip when truncate
			{
				addLog("##### IF MANDATORY DELETE ELSE SET NULL TO RECORDS THAT CAN REFER TRANSACTION TABLES DIRECTLY #####");
				createLog("", "", "### IF MANDATORY DELETE ELSE SET NULL TO RECORDS THAT CAN REFER TRANSACTION TABLES DIRECTLY ###", "", "", "",false);
				int returnInt = 0;
				for(int i = 0; i< TrxTables.length; i++)
				{
					returnInt = bulkUpdate_canReferTableDirect(TrxTables[i], null, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
							,stringArray_Merge(TrxTables,Tables_CustomDelete), WHERE_NOT_IN, type);
					bulkUpdate_Log(returnInt,TrxTables[i], DEBUG_BULK_UPDATE_LOG);
				}
				commitEx();
				createLog("", "", "COMMIT", "", "", "",false);

				//Set null to Records that can't refer Transaction Tables Directly(Not TableName_ID)
				addLog("### IF MANDATORY DELETE ELSE SET NULL TO RECORDS THAT CAN NOT REFER TRANSACTION TABLES DIRECTLY ###");
				createLog("", "", "### IF MANDATORY DELETE ELSE SET NULL TO RECORDS THAT CAN NOT REFER TRANSACTION TABLES DIRECTLY ###", "", "", "",false);
				for(int i = 0; i< TrxTables.length; i++)
				{
					returnInt = bulkUpdate_canNotReferTableDirect(TrxTables[i], null, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL,0
							,stringArray_Merge(TrxTables,Tables_CustomDelete), WHERE_NOT_IN, type);
					bulkUpdate_Log(returnInt,TrxTables[i], DEBUG_BULK_UPDATE_LOG);
				}
				commitEx();
				createLog("", "", "COMMIT", "", "", "",false);
			}//Skip when truncate

			//Initialize Document Number that are used by Document Type only.
			addLog("### INITIALIZE DOCUMENT NUMBER THAT ARE USED BY DOCUMENT TYPE ONLY ###");
			createLog("", "", "### INITIALIZE DOCUMENT NUMBER THAT ARE USED BY DOCUMENT TYPE ONLY ###", "", "", "",false);
			ArrayList<Integer> DocSequenceList = getIDList("DocNoSequence_ID", "C_DocType", "DocNoSequence_ID is not null", p_JP_Delete_Client);

			for(Integer AD_Sequence_ID:DocSequenceList)
			{
				String updateSequenceSQL = "UPDATE AD_Sequence SET CurrentNext = StartNo WHERE AD_Sequence_ID="+AD_Sequence_ID.toString();
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				int updates = 0;
				try
				{
					pstmt = DB.prepareStatement(updateSequenceSQL, get_TrxName());
					updates = pstmt.executeUpdate();
					if(updates != 1)
					{
						createLog("AD_Sequence", null, "UPDETE Doc Sequence ERROR", updateSequenceSQL, "", "",false);
					}
				}
				catch (SQLException e)
				{
					log.log(Level.SEVERE, updateSequenceSQL, e);
					throw new DBException(e, updateSequenceSQL);
				} finally {
					DB.close(rs, pstmt);
					rs = null; pstmt = null;
				}

				MSequence m_sequence = new MSequence(getCtx(), AD_Sequence_ID.intValue(), null);
				createLog("AD_Sequence", null, "RESET DOCNO - " + m_sequence.getName() + " : " + Msg.getElement(getCtx(), "CurrentNext") +" ---> " + m_sequence.getStartNo()
						, updateSequenceSQL, "", "DOCUMENT_NUMBER",false);

			}

			commitEx();
			createLog("", "", "COMMIT", "", "", "",false);


			for(Integer AD_Sequence_ID:DocSequenceList)
			{
				executeDeleteSQL("AD_Sequence_No", "AD_Sequence_ID=" + AD_Sequence_ID.toString(), p_JP_Delete_Client, false, "DOCUMENT_NUMBER");
			}

			commitEx();
			createLog("", "", "COMMIT", "", "", "",false);

			if(m_DeleteProfile != null && (p_JP_Delete_Client.equals(TYPE_ALL_TRANSACTION) || p_JP_Delete_Client.equals(TYPE_CLIENT_TRANSACTION)))
				doCustomDeleteProfile(m_DeleteProfile);

		}//if(!p_JP_Delete_Client.equals(TYPE_DELETE_CLIENT))

		//Reset Table+_ID
		if(Tables_CustomDelete != null && (p_JP_Delete_Client.equals(TYPE_ALL_TRANSACTION) || p_JP_Delete_Client.equals(TYPE_CLIENT_TRANSACTION)))
			doResetKeyID(stringArray_Merge(TrxTables, Tables_CustomDelete));
		else if(Tables_CustomDelete == null && (p_JP_Delete_Client.equals(TYPE_ALL_TRANSACTION) || p_JP_Delete_Client.equals(TYPE_CLIENT_TRANSACTION)))
			doResetKeyID(TrxTables);
		else
			;//Nothing to do.

		return message.toString();

	}


	/**
	 * Initialize Client Process
	 *
	 * @return
	 * @throws Exception
	 */
	private String doInitializeClient()throws Exception
	{
		addLog("##### INITIALIZE CLIENT START #####");
		createLog("", "", "##### INITIALIZE CLIENT START #####", "", "", "",true);

		//Prepare
		if(m_DeleteProfile == null || m_DeleteProfile.get_ID()==0)
		{
			return Msg.getMsg(getCtx(), "NotFound") +" "+ Msg.getElement(getCtx(), "JP_DeleteProfile_ID");
		}

		//Delete Transaction Table;
		String message = doDeleteTransactions(TYPE_CLIENT_TRANSACTION);
		createLog("","",message, "","","", true);

		//Delete Table Except TrxTables and IniTables, Custom Delete Tables
		addLog("### DELETE TABLES EXCEPT TRXTABLES AND INITABLES, CUSTOM DELETE TABLES ###");
		createLog("","","### DELETE TABLES EXCEPT TRXTABLES AND INITABLES, CUSTOM DELETE TABLES ###","","","", true);
		ArrayList<String> list_of_DeleteTables = new ArrayList<String>();
		for(String AD_TABLE : TABLELIST_AD)
		{

			boolean isContain = false;

			//Check Transaction Table
			for(int i = 0; i < TrxTables.length; i++)
			{
				if(TrxTables[i].toUpperCase().equals(AD_TABLE))
				{
					isContain = true;
					break;
				}
			}

			if(isContain) continue;

			//Check Initialize Table
			for(int i = 0; i < IniTables.length; i++)
			{
				if(IniTables[i].toUpperCase().equals(AD_TABLE))
				{
					isContain = true;

					break;
				}
			}

			if(isContain) continue;

			//Check Custom Delete Table
			MCustomDeleteProfile[] customDPs = m_DeleteProfile.getCustomDeleteProfiles();
			for(int i = 0; i < customDPs.length; i++)
			{
				if(customDPs[i].getAD_Table().getTableName().toUpperCase().equals(AD_TABLE))
				{
					isContain = true;
					break;
				}

				MCustomDeleteProfileLine[] customDPLines= customDPs[i].getCustomDeleteProfileLines();
				for(int j = 0; j < customDPLines.length; j++)
				{
					if(customDPLines[j].getAD_Table().getTableName().toUpperCase().equals(AD_TABLE))
					{
						isContain = true;
						break;
					}
				}

				if(isContain) break;
			}

			if(isContain) continue;

			int deletes =executeDeleteSQL(AD_TABLE, null, TYPE_INITIALIZE_CLIENT, false,"DELETE_OTEHR_TEBLES");
			if(deletes==-1)
			{
				createLog("","","NOT FOUND: "+ AD_TABLE,"","","", true);
			}else if(deletes==-2){
				;
			}else if(deletes==-3){
				;
			}else if(deletes<-3){
				;
			}else{
				list_of_DeleteTables.add(AD_TABLE);
			}

		}//for(String AD_TABLE : TABLELIST_AD)

		commitEx();
		createLog("", "", "COMMIT", "", "", "",false);

		Tables_DeleteAllRecords = stringArray_Merge(list_of_DeleteTables.toArray(new String[list_of_DeleteTables.size()]),TrxTables);
		Tables_Not_DeleteAllRecords = stringArray_Subtraction(TABLELIST_AD.toArray(new String[TABLELIST_AD.size()]), Tables_DeleteAllRecords);

		addLog("### SPECIAL TREATMENT FOR TABLES THAT DELETE ALL DATA. REFERRED RECORDS DELETE. ###");
		createLog("","","### SPECIAL TREATMENT FOR TABLES THAT DELETE ALL DATA. REFERRED RECORDS DELETE. ###","","","", true);
		for(String table : list_of_DeleteTables)
		{
			//for C_AcctSchema_Element
			if(table.equalsIgnoreCase("C_Campaign"))
				executeDeleteSQL("C_AcctSchema_Element", "ElementType = 'MC'", TYPE_INITIALIZE_CLIENT, false);
			else if(table.equalsIgnoreCase("C_Activity"))
				executeDeleteSQL("C_AcctSchema_Element", "ElementType = 'AY'", TYPE_INITIALIZE_CLIENT, false);
			else if(table.equalsIgnoreCase("C_Activity"))
				executeDeleteSQL("C_AcctSchema_Element", "ElementType = 'AY'", TYPE_INITIALIZE_CLIENT, false);
			else if(table.equalsIgnoreCase("C_Project"))
				executeDeleteSQL("C_AcctSchema_Element", "ElementType = 'PJ'", TYPE_INITIALIZE_CLIENT, false);
			else if(table.equalsIgnoreCase("C_SalesRegion"))
				executeDeleteSQL("C_AcctSchema_Element", "ElementType = 'SR'", TYPE_INITIALIZE_CLIENT, false);
		}

		//Test Code
//		String[] excludeTables = {"AD_Table","AD_Column"};
//		String[] targetTables = stringArray_Subtraction(Tables_Not_DeleteAllRecords,excludeTables);

		//Refer Table set Null
		addLog("### GENERAL TREATMENT FOR TABLES THAT DELETE ALL DATA. SET NULL TO COLUMN OF FK ###");
		createLog("","","### GENERAL TREATMENT FOR TABLES THAT DELETE ALL DATA. SET NULL TO COLUMN OF FK ###","","","", true);
		int returnInt = 0;
		for(String table : list_of_DeleteTables)
		{
			if(isAccessLevelSystemTable(table, true))//For example AD_Clumn, Not need to set null to AD_ImpFormat_Row.AD_Column.
				continue;

			returnInt = bulkUpdate_canReferTableDirect(table, null, WHERE_NOT_IN, TREAT_SET_NULL, 0, Tables_Not_DeleteAllRecords, WHERE_IN, TYPE_INITIALIZE_CLIENT);
			bulkUpdate_Log(returnInt, table, DEBUG_BULK_UPDATE_LOG);

			returnInt = bulkUpdate_canNotReferTableDirect(table, null, WHERE_NOT_IN, TREAT_SET_NULL, 0, Tables_Not_DeleteAllRecords, WHERE_IN,TYPE_INITIALIZE_CLIENT);
			bulkUpdate_Log(returnInt, table, DEBUG_BULK_UPDATE_LOG);
		}

		commitEx();
		createLog("", "", "COMMIT", "", "", "",false);

		//Delete Organization
		deleteOrg(m_DeleteProfile.getJP_AD_Org_ID_Where());

		//Delete Role
		deleteRole(m_DeleteProfile.getJP_AD_Role_ID_Where());

		//Delete User
		deleteUser(m_DeleteProfile.getJP_AD_User_ID_Where());

		//Delete BPartner
		deleteBPartner(m_DeleteProfile.getJP_C_BPartner_ID_Where());

		//Delete Product
		deleteProduct(m_DeleteProfile.getJP_M_Product_ID_Where());

		//Delete Tables of Custom Delete Profile
		doCustomDeleteProfile(m_DeleteProfile);

		//Rest Table ID
		doResetKeyID(TABLELIST_AD.toArray(new String[TABLELIST_AD.size()]));

		return "";
	}

	/**
	 * Do Delete Custom Profile
	 *
	 * @param deleteProfile
	 * @return
	 * @throws Exception
	 */
	private String doCustomDeleteProfile(MDeleteProfile deleteProfile) throws Exception
	{
		addLog("##### DELETE CUSTOM PROFILE TABLE #####");
		createLog("", "", "##### DELETE CUSTOM PROFILE TABLE #####", "", "", "",true);

		MCustomDeleteProfile[] customDeleteProfiles = deleteProfile.getCustomDeleteProfiles();
		int returnInt = 0;
		for(int i = 0; i < customDeleteProfiles.length; i++)
		{
			JP_CustomDeleteProfile_ID =customDeleteProfiles[i].get_ID();//for logging
			if(customDeleteProfiles[i].isDeleteDataNotUseJP())
			{

				createLog("","","### DELETE " + customDeleteProfiles[i].getAD_Table().getTableName() + " RECORDS THAT ARE NOTE USED ###", "","","", true);
				returnInt = bulkDelete_NotUseRecords(customDeleteProfiles[i].getAD_Table().getTableName(), deleteProfile.getJP_Delete_Client());
				bulkUpdate_Log(returnInt, customDeleteProfiles[i].getAD_Table().getTableName(), DEBUG_BULK_UPDATE_LOG);

			}else{

				createLog("","","### DELETE " + customDeleteProfiles[i].getAD_Table().getTableName() + " ###", "","","", true);
				MCustomDeleteProfileLine[] m_ProfileLines = customDeleteProfiles[i].getCustomDeleteProfileLines();
				ArrayList<String> list_of_excludeTables = new ArrayList<String>();
				for(int j = 0; j < m_ProfileLines.length; j++)
				{
					list_of_excludeTables.add(m_ProfileLines[j].getAD_Table().getTableName());
				}
				String[] excludeTables=list_of_excludeTables.toArray(new String[list_of_excludeTables.size()]);
				String tableName = customDeleteProfiles[i].getAD_Table().getTableName();
				ArrayList<Integer> IDs = getIDList(tableName+"_ID", tableName, customDeleteProfiles[i].getWhereClause(), deleteProfile.getJP_Delete_Client());
				String treat = customDeleteProfiles[i].getJP_TreatForeignKey();
				int value = customDeleteProfiles[i].getJP_ForeignKey_Value();

				executeDeleteSQL(tableName, createWhereInIDs(tableName+"_ID", IDs, WHERE_NOT_IN), deleteProfile.getJP_Delete_Client(), p_IsTruncateJP,"CUSTOM_TABLE");
				if(deleteProfile.getJP_Delete_Client().equals(TYPE_ALL_TRANSACTION) && p_IsTruncateJP)//skip
				{
					bulkUpdate_canReferTableDirect(tableName, IDs, WHERE_NOT_IN, treat, value, excludeTables, WHERE_NOT_IN, deleteProfile.getJP_Delete_Client());
					bulkUpdate_canNotReferTableDirect(tableName, IDs, WHERE_NOT_IN, treat, value, excludeTables, WHERE_NOT_IN, deleteProfile.getJP_Delete_Client());

					for(int j = 0; j < m_ProfileLines.length; j++)
					{
						JP_CustomDeleteProfileLine_ID = m_ProfileLines[j].get_ID();//for logging
						String lineTableName = m_ProfileLines[j].getAD_Table().getTableName();
						String linkColumn =  m_ProfileLines[j].getAD_Column().getColumnName();
						String lineTreat = m_ProfileLines[j].getJP_TreatForeignKey();
						int linValue =  m_ProfileLines[j].getJP_ForeignKey_Value();
						executeUpdateSQL(lineTableName, linkColumn, lineTreat, linValue
								, createWhereInIDs(linkColumn, IDs, WHERE_NOT_IN), deleteProfile.getJP_Delete_Client(),"CUSTOM_TABLE_LINE");
					}//for
				}
				JP_CustomDeleteProfileLine_ID = 0;//for logging
			}//if
		}//for

		JP_CustomDeleteProfile_ID = 0;//for logging

		return "";
	}


	/**
	 * Reset Table_ID
	 *
	 * @param tableNames
	 * @return
	 */
	private boolean doResetKeyID(String[] tableNames)
	{
		addLog("##### RESET PRIMARY KEY ID #####");
		createLog(null, null, "##### RESET PRIMARY KEY ID #####", "", "", "", true);

		for(int i = 0; i< tableNames.length; i++)
		{
			for(String AD_TABLE : TABLELIST_AD)
			{
				if(tableNames[i].toUpperCase().equals(AD_TABLE))
				{
					if(!hasColumn(tableNames[i]+"_ID", tableNames[i]))
						break;

					if(isAccessLevelSystemTable(tableNames[i],true))
						break;

					if(tableNames[i].toUpperCase().startsWith("AD_"))
						break;

					MSequence seq = MSequence.get(getCtx(), tableNames[i]);
					if(seq == null || seq.get_ID()==0)
						break;

					StringBuilder msg = new StringBuilder("RESET ID - " + tableNames[i]+" : "+Msg.getElement(getCtx(), "CurrentNext")
							+" : "+seq.getCurrentNext()+"----->");

					int maxID = getMaxPrimaryKey_ID(tableNames[i]);
					if(maxID < 1) //maxID = 0;
					{
						seq.setCurrentNext(seq.getStartNo());
						seq.saveEx(get_TrxName());
					}else if(maxID < 1000000){
						;//Noting to do;
					}else{
						seq.setCurrentNext(maxID + seq.getIncrementNo());
						seq.saveEx(get_TrxName());
					}

					msg.append(seq.getCurrentNext());
					createLog(tableNames[i], null, msg.toString(), null, null,
							"Max ID = " + maxID +" # " + Msg.getElement(getCtx(), "StartNo") + " = " + seq.getStartNo() +" # " +
								Msg.getElement(getCtx(), "IncrementNo") + " = " +  seq.getIncrementNo() +" # "  +
									Msg.getElement(getCtx(), "CurrentNext") + " = " +  seq.getCurrentNext(),false);

				}
			}
		}

		return true;
	}//Reset Table_ID


	/***Variables that is used in Check Method only***/
	//Check Records in AD_Table
	MTable[] AD_TABLE_Alls;
	TreeMap<String, Integer> beforDeleteTableInfo = new TreeMap<String, Integer>();

	/**
	 * Before Check
	 *
	 * @param type
	 * @return
	 */
	private String doBeforeCheck(String type)
	{
		addLog("##### CHECK DATA BEFORE DELETE #####");
		createLog("", "", "##### CHECK DATA BEFORE DELETE #####", "", "", "",true);

		//SELECT * FROM AD_Table WHERE IsView='N'
		List<MTable> list = new Query(getCtx(), MTable.Table_Name, "IsView='N'", get_TrxName()).list();
		AD_TABLE_Alls = list.toArray(new MTable[list.size()]);

		for(String AD_TABLE : TABLELIST_AD)
		{
			beforDeleteTableInfo.put(AD_TABLE.toUpperCase(), countRecords(AD_TABLE, null, TYPE_ALL_TRANSACTION));
		}

		ArrayList<String> NotHave_AD_Client_ID = new ArrayList<String>();
		boolean isContain = false;
		for(int i = 0; i < AD_TABLE_Alls.length; i++)
		{
			isContain = false;
			for(String AD_TABLE : TABLELIST_AD)
			{
				if(AD_TABLE_Alls[i].getTableName().toUpperCase().equals(AD_TABLE))
				{
					isContain=true;
					break;
				}
			}

			if(!isContain)
				NotHave_AD_Client_ID.add(AD_TABLE_Alls[i].getTableName());
		}

		ArrayList<String> Not_IN_AD = new ArrayList<String>();
		for(String DB_TABLE : TABLELIST_DB)
		{
			isContain = false;
			for(int i = 0; i < AD_TABLE_Alls.length; i++)
			{
				if(DB_TABLE.equals(AD_TABLE_Alls[i].getTableName().toUpperCase()))
				{
					isContain=true;
					break;
				}
			}

			if(!isContain)
				Not_IN_AD.add(DB_TABLE);
		}

		ArrayList<String> Not_IN_DB = new ArrayList<String>();
		for(int i = 0; i < AD_TABLE_Alls.length; i++)
		{
			isContain = false;
			for(String DB_TABLE : TABLELIST_DB)
			{
				if(AD_TABLE_Alls[i].getTableName().toUpperCase().equals(DB_TABLE))
				{
					isContain=true;
					break;
				}
			}

			if(!isContain)
				Not_IN_DB.add(AD_TABLE_Alls[i].getTableName());
		}

		String msg = new String();
		msg = "There are " + TABLELIST_DB.size() + " tables in Data Base.";
		addLog(msg);
		createLog("", "", msg, "", "", "", false);

		msg = "There are " + AD_TABLE_Alls.length + " tables in Application Dictonary.";
		addLog(msg);
		createLog("", "", msg, "", "", "", false);

		if(Not_IN_AD.size() > 0)
		{
			msg = "Tables that are not registering in Application Dictionary are " + Not_IN_AD.size()
					+ " " + ArrayListToString(Not_IN_AD) +". ";
			addLog(msg);
			createLog("", "", msg, "", "", "", false);
		}

		if(Not_IN_DB.size() > 0)
		{
			msg = "Tables that are not registering in Data Base are " +  Not_IN_DB.size()
					+ " " + ArrayListToString(Not_IN_DB) +". ";
			addLog(msg);
			createLog("", "", msg, "", "", "", false);
		}

		if(NotHave_AD_Client_ID.size() > 0)
		{
			msg = "Tables in AD that have a column of AD_Client_ID are " + TABLELIST_AD.size() +". ";
			addLog(msg);
			createLog("", "", msg, "", "", "", false);

			msg = "Tables in AD that do not have a column of AD_Client_ID are " + NotHave_AD_Client_ID.size()
					+ " " + ArrayListToString(NotHave_AD_Client_ID) +". ";
			addLog(msg);
			createLog("", "", msg, "", "", "", false);
		}

		ArrayList<String> deleteTrxTableList = new ArrayList<String>();
		ArrayList<String> unexpectedTrxTableList = new ArrayList<String>();
		if(type.equals(TYPE_ALL_TRANSACTION) || type.equals(TYPE_CLIENT_TRANSACTION)
				|| type.equals(TYPE_INITIALIZE_CLIENT))
		{
			for(int i = 0; i < TrxTables.length; i++)
			{
				isContain = false;
				for(String table : TABLELIST_AD)
				{
					if(TrxTables[i].toUpperCase().equals(table))
					{
						isContain = true;
						break;
					}
				}

				if(isContain)
					deleteTrxTableList.add(TrxTables[i]);
				else
					unexpectedTrxTableList.add(TrxTables[i]);
			}

			msg = "Transactions Tables that will delete are " + (TrxTables.length-unexpectedTrxTableList.size())+ ". ";
			addLog(msg);
			createLog("", "", msg + " " + ArrayListToString(unexpectedTrxTableList) +". " , "", "", "", false);
		}


		ArrayList<String> deleteIniTableList = new ArrayList<String>();
		ArrayList<String> unexpectedIniTableList = new ArrayList<String>();
		if(type.equals(TYPE_INITIALIZE_CLIENT))
		{
			for(int i = 0; i < IniTables.length; i++)
			{
				isContain = false;
				for(String table : TABLELIST_AD)
				{
					if(IniTables[i].toUpperCase().equals(table))
					{
						isContain = true;
						break;
					}
				}

				if(isContain)
					deleteIniTableList.add(IniTables[i]);
				else
					unexpectedIniTableList.add(IniTables[i]);
			}

			msg = "Initialize Tables are " + (IniTables.length-unexpectedIniTableList.size())+ ". ";
			addLog(msg);
			createLog("", "", msg +" " + ArrayListToString(unexpectedIniTableList) +". " , "", "", "", false);
		}


		if(m_DeleteProfile != null && !type.equals(TYPE_DELETE_CLIENT))
		{
			MCustomDeleteProfile[] customDPs = m_DeleteProfile.getCustomDeleteProfiles();
			ArrayList<String> customDeleteTableList = new ArrayList<String>();
			for(int i = 0; i < customDPs.length; i++)
			{
				customDeleteTableList.add(customDPs[i].getAD_Table().getTableName());
				MCustomDeleteProfileLine[] customDPLines= customDPs[i].getCustomDeleteProfileLines();
				for(int j = 0; j < customDPLines.length; j++)
				{
					customDeleteTableList.add(customDPLines[j].getAD_Table().getTableName());
				}
			}

			Tables_CustomDelete = customDeleteTableList.toArray(new String[customDeleteTableList.size()]);
			msg = "Custom Delete Tables are " + Tables_CustomDelete.length + ". ";
			addLog(msg);
			createLog("", "", msg +" " + ArrayListToString(customDeleteTableList) +". " , "", "", "", false);

			if(type.equals(TYPE_ALL_TRANSACTION) || type.equals(TYPE_CLIENT_TRANSACTION))
			{
				ArrayList<String> commonTables = new ArrayList<String>();
				for(int i = 0; i <TrxTables.length; i++)
				{
					if(stringArray_IsIN(Tables_CustomDelete, TrxTables[i]))
					{
						commonTables.add(TrxTables[i]);
					}else{
						;
					}
				}

				msg = "Tables that are included in Transaction Tables and Custom Tables are " +  commonTables.size()
						+ " " + ArrayListToString(commonTables) +". ";
				addLog(msg);
				createLog("", "", msg, "", "", "", false);


			}else if(type.equals(TYPE_INITIALIZE_CLIENT)){
				ArrayList<String> commonTables = new ArrayList<String>();
				for(int i = 0; i <IniTables.length; i++)
				{
					if(stringArray_IsIN(Tables_CustomDelete, IniTables[i]))
					{
						commonTables.add(IniTables[i]);
					}else{
						;
					}
				}

				msg =  "Tables that are included in Initialize Tables and Custom Tables are "  +  commonTables.size()+". ";
				addLog(msg);
				createLog("", "", msg + " " + ArrayListToString(commonTables) +". ", "", "", "", false);
			}


		}

		return "";
	}

	/**
	 * After Check
	 *
	 * @param type DELETE_CLIENT / INITIALIZE_CLIENT / ALL_TRANSACTION / CLIENT_TRANSACTION
	 * @return
	 */
	private String doAfterCheck(String type)
	{
		addLog("##### CHECK DATA AFTER DELETE #####");
		createLog("", "", "##### CHECK DATA AFTER DELETE #####", "", "", "", true);

		ArrayList<String> RemainRecordsTableList = new ArrayList<String>();
		ArrayList<String> UnexpectedTableList= new ArrayList<String>();

		String[] checkTables = null;
		if(type.equals(TYPE_CLIENT_TRANSACTION) || type.equals(TYPE_ALL_TRANSACTION))
			checkTables = Tables_CustomDelete == null ? TrxTables : stringArray_Merge(TrxTables, Tables_CustomDelete);
		else if(Tables_CustomDelete != null && type.equals(TYPE_CUSTOM_DELETE)  )
			checkTables = Tables_CustomDelete;
		else
			checkTables = TABLELIST_AD.toArray(new String[TABLELIST_AD.size()]);

		for(int i = 0; i < checkTables.length; i++)
		{
			for(String AD_TABLE : TABLELIST_AD)
			{
				if(checkTables[i].toUpperCase().equals(AD_TABLE))
				{
					int countsALL =countRecords(checkTables[i], null, TYPE_ALL_TRANSACTION);
					int countsClient =countRecords(checkTables[i], null, TYPE_DELETE_CLIENT);

					if(countsClient < 0)
					{
						createLog("","","Error: unexpected Table: " + checkTables[i], "","","", true);
						UnexpectedTableList.add(checkTables[i]);
					}else{
						int beforeRecords = beforDeleteTableInfo.get(checkTables[i].toUpperCase()).intValue();
						if(stringArray_IsIN(TrxTables, checkTables[i]) || type.equals(TYPE_DELETE_CLIENT))
						{
							boolean isOK = false;
							if(type.equals(TYPE_ALL_TRANSACTION))
							{
								if(countsALL == 0)
									isOK = true;
							}else{
								if(countsClient==0)
									isOK = true;
							}

							createLog(checkTables[i], null,
									"CHECK - " + checkTables[i] + "  -->"
											+ (type.equals(TYPE_ALL_TRANSACTION) ? "" : "Client Records : " + countsClient)
											+ ", Deleted Records : " + (beforeRecords-countsALL)
											+ ", Before Records : " + beforeRecords
											+ ", Remain Records : " + countsALL
									, null, null, isOK ? "OK" : "NG"
									,false
									);
						}else{
							createLog(checkTables[i], null,
									"CHECK - " + checkTables[i] + "  -->"
											+ (type.equals(TYPE_ALL_TRANSACTION) ? "" : "Client Records : " + countsClient)
											+ ", Deleted Records : " + (beforeRecords-countsALL)
											+ ", Before Records : " + beforeRecords
											+ ", Remain Records : " + countsALL
									, null, null, null,false);
						}

						if(type.equals(TYPE_DELETE_CLIENT) && countsClient > 0)
						{
							RemainRecordsTableList.add(checkTables[i]);
						}
					}

					break;
				}//if
			}//for
		}//for i


		if(UnexpectedTableList.size() > 0)
		{
			String msg = "Unexpected Table is " + ArrayListToString(UnexpectedTableList);
			addLog(msg);
			createLog("","", msg,"","","", false);
		}

		String msg = "Total Tables checked is " + checkTables.length
				+ (type.equals(TYPE_DELETE_CLIENT) ? " and Remain Records Table is " + RemainRecordsTableList.size() : "")
				+ " and Unexpected Table is " + UnexpectedTableList.size();

		addLog(msg);
		createLog("", "", msg, "", "", "", false);

		return "";
	}

	/**
	 *
	 * @param arrayList
	 * @return
	 */
	private String ArrayListToString(ArrayList<String> arrayList)
	{
		StringBuilder stringBuilder = new StringBuilder("");

		if(arrayList.size() == 0)
			return "";

		int i = 0;
		for(String string : arrayList)
		{
			if(i == 0)
				stringBuilder.append("( " + string);
			else
				stringBuilder.append(", " + string);
			i++;
		}

		stringBuilder.append(" )");

		return stringBuilder.toString();
	}


	/**
	 * Delete Organization
	 *
	 * @param where
	 * @throws Exception
	 */
	private String deleteOrg(String where) throws Exception
	{
		addLog("##### DELETE ORGANIZATION #####");
		createLog("", "", "##### DELETE ORGANIZATION #####","","","",true);

		ArrayList<Integer> AD_Org_IDs = getIDList("AD_Org_ID", "AD_Org", where, TYPE_INITIALIZE_CLIENT);
		AD_Org_IDs.add(0);// * Org

		createLog("","","### UPDATE ORG BEFORE DELETE ###", "","","", true);

		//C_BPartner.AD_OrgBP_ID
		executeUpdateSQL("C_BPartner", "AD_OrgBP_ID", TREAT_SET_NULL, 0, createWhereInIDs("AD_OrgBP_ID", AD_Org_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT);

		MAcctSchema[] acctSchemas = MAcctSchema.getClientAcctSchema(getCtx(), p_LookupClientID);
		boolean isContain = false;
		for(int i = 0; i < acctSchemas.length; i++)
		{

			isContain = false;
			//Acct Shema
			if(acctSchemas[i].getAD_OrgOnly_ID() != 0)
			{
				for(Integer AD_Org_ID : AD_Org_IDs)
				{
					if(AD_Org_ID.intValue()==acctSchemas[i].getAD_OrgOnly_ID())
					{
						isContain = true;
						break;
					}
				}

				if(!isContain)
				{
					int as_ID = acctSchemas[i].getC_AcctSchema_ID();
					executeDeleteSQL("C_AcctSchema", "C_AcctSchema_ID = "+as_ID, TYPE_INITIALIZE_CLIENT, false, "DELETE_ACCTSCHEMA");

					ArrayList<Integer>  list_of_as_ID = new ArrayList<Integer> ();
					list_of_as_ID.add(as_ID);
					int returnInt = bulkUpdate_canReferTableDirect("C_AcctSchema", list_of_as_ID, WHERE_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
							,Tables_Not_DeleteAllRecords, WHERE_IN, TYPE_INITIALIZE_CLIENT);
					bulkUpdate_Log(returnInt, "C_AcctSchema", DEBUG_BULK_UPDATE_LOG);

					returnInt = bulkUpdate_canNotReferTableDirect("C_AcctSchema", list_of_as_ID, WHERE_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
							,Tables_Not_DeleteAllRecords, WHERE_IN,TYPE_INITIALIZE_CLIENT);
					bulkUpdate_Log(returnInt, "AD_Org", DEBUG_BULK_UPDATE_LOG);
					continue;
				}
			}

			//Acct Schema Element
			MAcctSchemaElement ase_OO = acctSchemas[i].getAcctSchemaElement(MAcctSchemaElement.ELEMENTTYPE_Organization);
			if(ase_OO != null)
			{
				isContain = false;
				for(Integer AD_Org_ID : AD_Org_IDs)
				{
					if(AD_Org_ID.intValue()==ase_OO.getOrg_ID())
					{
						isContain = true;
						break;
					}
				}

				if(!isContain)
				{
					ase_OO.setOrg_ID(AD_Org_IDs.get(0));
					ase_OO.saveEx(get_TrxName());
				}
			}

			MAcctSchemaElement ase_OT = acctSchemas[i].getAcctSchemaElement(MAcctSchemaElement.ELEMENTTYPE_OrgTrx);
			if(ase_OT != null)
			{
				isContain = false;
				for(Integer AD_Org_ID : AD_Org_IDs)
				{
					if(AD_Org_ID.intValue()==ase_OT.getOrg_ID())
					{
						isContain = true;
						break;
					}
				}

				if(!isContain)
				{
					ase_OT.setOrg_ID(AD_Org_IDs.get(0));
					ase_OT.saveEx(get_TrxName());
				}
			}

			;
		}

		String[] tables = {
			"C_BPartner","C_AcctSchema","C_AcctSchema_Element"
		};

		createLog("","","### DELETE RELATION TABLES OF AD_Org CAN REFER TABLE DIRECT ###", "","","", true);
		int returnInt = 0;
		returnInt = bulkUpdate_canReferTableDirect("AD_Org", AD_Org_IDs, WHERE_NOT_IN, TREAT_DELETE, 0
				,Tables_Not_DeleteAllRecords, WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_Org", DEBUG_BULK_UPDATE_LOG);

		createLog("","","### DELETE RELATION TABLES OF AD_Org CAN NOT REFER TABLE DIRECT ###", "","","", true);
		returnInt = bulkUpdate_canNotReferTableDirect("AD_Org", AD_Org_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				,stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN,TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_Org", DEBUG_BULK_UPDATE_LOG);

		createLog("","","### DELETE AD_Org TABLE ###", "","","", true);
		executeDeleteSQL("AD_Org", createWhereInIDs("AD_Org_ID", AD_Org_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false,"DELETE_ORG");

		return "";
	}


	/**
	 * Delete Role
	 *
	 * @param where
	 * @throws Exception
	 */
	private String deleteRole(String where) throws Exception
	{
		addLog("##### DELETE ROLE #####");
		createLog("", "", "##### DELETE ROLE #####","","","",true);

		ArrayList<Integer> AD_Role_IDs = getIDList("AD_Role_ID", "AD_Role", where, TYPE_INITIALIZE_CLIENT);
		AD_Role_IDs.add(0);//System Administrator

		executeDeleteSQL("PA_DashboardPreference", createWhereInIDs("AD_Role_ID", AD_Role_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false,"DELETE_USER");

		String[] tables = {
				"AD_WF_Responsible","PA_DashboardPreference"
			};

		int returnInt = 0;
		returnInt =bulkUpdate_canReferTableDirect("AD_Role", AD_Role_IDs, WHERE_NOT_IN, TREAT_SET_VALUE, 0 //System Administrator Role
				, tables, WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_Role", DEBUG_BULK_UPDATE_LOG);

		returnInt =bulkUpdate_canReferTableDirect("AD_Role", AD_Role_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				, stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_Role", DEBUG_BULK_UPDATE_LOG);

		returnInt =bulkUpdate_canNotReferTableDirect("AD_Role", AD_Role_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				, stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_Role", DEBUG_BULK_UPDATE_LOG);

		executeDeleteSQL("AD_Role", createWhereInIDs("AD_Role_ID", AD_Role_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false, "DELETE_ROLE");

		return "";
	}

	/**
	 * Delete User
	 *
	 * @param where
	 * @throws Exception
	 */
	private String deleteUser(String where)throws Exception
	{
		addLog("##### DELETE USER #####");
		createLog("", "", "##### DELETE USER #####","","","",true);

		createLog("", "", "### UPDATE All CreatedBy and UpdatedBy COLUMNS TO Super User ###","","","",true);
		for(String AD_TABLE : TABLELIST_AD)
		{
			if(hasColumn("CreatedBy",AD_TABLE))
				executeUpdateSQL(AD_TABLE, "CreatedBy", TREAT_SET_VALUE, 100, null, TYPE_INITIALIZE_CLIENT);
			if(hasColumn("UpdatedBy",AD_TABLE))
				executeUpdateSQL(AD_TABLE, "UpdatedBy", TREAT_SET_VALUE, 100, null, TYPE_INITIALIZE_CLIENT);
		}

		ArrayList<Integer> AD_User_IDs = getIDList("AD_User_ID", "AD_User", where, TYPE_INITIALIZE_CLIENT);
		AD_User_IDs.add(100);//Super User
		AD_User_IDs.add(0);//System

		createLog("", "", "### DELETE PREFERENCE ###","","","",true);
		//AD_Preference
		executeDeleteSQL("AD_Preference", createWhereInIDs("AD_User_ID", AD_User_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false,"DELETE_USER");
		executeDeleteSQL("PA_DashboardPreference", createWhereInIDs("AD_User_ID", AD_User_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false,"DELETE_USER");
		executeDeleteSQL("AD_Password_History", createWhereInIDs("AD_User_ID", AD_User_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false,"DELETE_USER");

		String[] tables = {
				"AD_Preference","PA_DashboardPreference","AD_Password_History"
			};


		createLog("", "", "###  DELETE RELATION TABLES OF AD_User CAN REFER TABLE DIRECT ###","","","",true);
		int returnInt = 0;
		returnInt = bulkUpdate_canReferTableDirect("AD_User", AD_User_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 100
				, stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_User", DEBUG_BULK_UPDATE_LOG);

		createLog("", "", "###  DELETE RELATION TABLES OF AD_User CAN NOT REFER TABLE DIRECT ###","","","",true);
		returnInt = bulkUpdate_canNotReferTableDirect("AD_User", AD_User_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 100
				, stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "AD_User", DEBUG_BULK_UPDATE_LOG);

		createLog("", "", "### DELETE AD_User TABLE ###","","","",true);
		executeDeleteSQL("AD_User", createWhereInIDs("AD_User_ID", AD_User_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false,"DELETE_USER");

		return "";
	}

	private String deleteBPartner(String where)throws Exception
	{
		addLog("### DELETE BUSINESS PARTNER ###");
		createLog("", "", "##### DELETE BUSINESS PARTNER ####","","","",true);

		ArrayList<Integer> C_BP_IDs = getIDList("C_BPartner_ID", "C_BPartner", where, TYPE_INITIALIZE_CLIENT);

		int returnInt = 0;
		returnInt =bulkUpdate_canReferTableDirect("C_BPartner", C_BP_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				, Tables_Not_DeleteAllRecords, WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "C_BPartner", DEBUG_BULK_UPDATE_LOG);

		returnInt =bulkUpdate_canNotReferTableDirect("C_BPartner", C_BP_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				, Tables_Not_DeleteAllRecords, WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "C_BPartner", DEBUG_BULK_UPDATE_LOG);

		executeDeleteSQL("C_BPartner", createWhereInIDs("C_BPartner_ID", C_BP_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false, "DELETE_BP");

		return "";
	}

	private String deleteProduct(String where)throws Exception
	{
		addLog("##### DELETE PRODUCT #####");
		createLog("", "", "##### DELETE PRODUCT #####","","","",true);

		ArrayList<Integer> M_Product_IDs = getIDList("M_Product_ID", "M_Product", where, TYPE_INITIALIZE_CLIENT);

		String[] tables = {
				"",
			};

		int returnInt = 0;
		returnInt =bulkUpdate_canReferTableDirect("M_Product", M_Product_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				, stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "M_Product", DEBUG_BULK_UPDATE_LOG);

		returnInt =bulkUpdate_canNotReferTableDirect("M_Product", M_Product_IDs, WHERE_NOT_IN, TREAT_IF_MANDATORY_DELETE_ELSE_NULL, 0
				, stringArray_Subtraction(Tables_Not_DeleteAllRecords, tables), WHERE_IN, TYPE_INITIALIZE_CLIENT);
		bulkUpdate_Log(returnInt, "M_Product", DEBUG_BULK_UPDATE_LOG);

		executeDeleteSQL("M_Product", createWhereInIDs("M_Product_ID", M_Product_IDs, WHERE_NOT_IN), TYPE_INITIALIZE_CLIENT, false, "DELETE_PRODUCT");

		return "";
	}

	/***************(Execute SQL)***************/
	private int executeDeleteSQL(String table, String where, String type, boolean isTruncate)
	{
		return executeDeleteSQL(table, where, type, isTruncate,"");
	}
	/**
	 * Execute Delete SQL
	 *
	 * @param table : Table Name
	 * @param where : WHERE Clause
	 * @param type	: DELETE_CLIENT / INITIALIZE_CLIENT / ALL_TRANSACTION / CLIENT_TRANSACTION / TYPE_CUSTOM_DELETE
	 * @param isTruncate : Incase true and Type is ALL_TRANSACTION execute TRUNCATE
	 * @return Delete Records or -1(Unexpected Table) , -2(), -3(Not execute for Truncate)
	 */
	private int executeDeleteSQL(String table, String where, String type, boolean isTruncate,String help)
	{
		//Check if the "tableName" exists
		if(table == null || !isAppDictionaryTable(table) || !isDataBaseTable(table))
			return -1;

		StringBuilder DeleteSQL = new StringBuilder();

		//Create From
		if(type.equals(TYPE_ALL_TRANSACTION) && isTruncate)
		{
			DeleteSQL.append("TRUNCATE TABLE "+ table + " CASCADE");
		}else{
			DeleteSQL.append("DELETE FROM "+ table);
		}

		//Add Where
		if(!Util.isEmpty(where))
		{
			if(type.equals(TYPE_ALL_TRANSACTION) && isTruncate)
			{
				;//Nothing to do;
			}else if(type.equals(TYPE_ALL_TRANSACTION) && !isTruncate){
				DeleteSQL.append(" WHERE " + where);
			}else{
				DeleteSQL.append(" WHERE " + where + " AND AD_Client_ID = " + p_LookupClientID );
			}
		}else{
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do;
			}else{
				DeleteSQL.append(" WHERE AD_Client_ID = " + p_LookupClientID);
			}
		}

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int deletes = 0;
		try
		{
			pstmt = DB.prepareStatement(DeleteSQL.toString(), get_TrxName());
			deletes = pstmt.executeUpdate();
			if(deletes == 0 && !p_IsAllowLogging)
			{
				;//Noting to do;
			}else{
				createLog(table, null, "DELETE : " + deletes, DeleteSQL.toString(), null, help,false);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, DeleteSQL.toString(), e);
			throw new DBException(e, DeleteSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return deletes;
	}


	private int executeUpdateSQL(String table, String column_ID, String treat, int value ,String where, String type)
	{
		return executeUpdateSQL(table, column_ID, treat, value ,where, type,null);
	}
	/**
	 * Execute Update SQL
	 *
	 * @param table : Table Name
	 * @param Column_ID : Column Name. That Display type is ID.
	 * @param treat : TREAT_SET_NULL / TREAT_SET_VALUE /TREAT_DELETE /TREAT_IF_MANDATORY_DELETE_ELSE_VALUE
	 * 					TREAT_IF_MANDATORY_DELETE_ELSE_NULL / TREAT_IF_MANDATORY_VALUE_ELSE_NULL / TREAT_IF_MANDATORY_VALUE_ELSE_DELETE
	 * @param value : Update Value
	 * @param where : WHERE Clause
	 * @param type	: TYPE_DELETE_CLIENT / TYPE_INITIALIZE_CLIENT / TYPE_ALL_TRANSACTION / TYPE_CLIENT_TRANSACTION /TYPE_CUSTOM_DELETE
	 * @return Updeate Records or -1(Unexpected Table) , -2(), -3(Not execute for Truncate)
	 */
	private int executeUpdateSQL(String table, String column_ID, String treat, int value ,String where, String type, String help)
	{
		if(type.equals(TYPE_ALL_TRANSACTION) && p_IsTruncateJP)
			return -3 ;


		if(treat == null)
		{
			return -1;
		}else if (treat.equals(TREAT_DELETE)){
			return executeDeleteSQL(table, where, type, false);
		}else if (treat.equals(TREAT_SET_NULL) || treat.equals(TREAT_SET_VALUE)
					|| treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_NULL) || treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_VALUE)
					|| treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_DELETE) || treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_NULL)){
			;//Nothing to do;
		}else{
			return -1;
		}

		//Checking if the "tableName" and "Column_ID" exists
		if(table == null || column_ID ==null)
			return -1;

		if(!isAppDictionaryTable(table) || !isDataBaseTable(table))
			return -1;

		if(!hasColumn(column_ID, table))
			return -1;

		if(!isIDColumn(column_ID, table))
			return -1;

		if (treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_NULL) || treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_VALUE)
				|| treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_DELETE) || treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_NULL))
			treat = treatAutoJudge(table, column_ID, treat, value);


		if (treat.equals(TREAT_DELETE))
			return executeDeleteSQL(table, where, type, false);

		StringBuilder updateSQL = new StringBuilder("UPDATE " + table +" SET " + column_ID);
		if(treat.equals(TREAT_SET_NULL))
			updateSQL.append(" = null ");
		else if(treat.equals(TREAT_SET_VALUE))
			updateSQL.append(" = " + value);
		else
		{
			return -1;
		}

		//Add Where
		if(!Util.isEmpty(where))
		{
			updateSQL.append(" WHERE " + where);
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do;
			}else{
				updateSQL.append(" AND AD_Client_ID = " + p_LookupClientID );
			}
		}else{
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do;
			}else{
				updateSQL.append(" WHERE AD_Client_ID = " + p_LookupClientID);
			}
		}


		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int updates = 0;
		try
		{
			pstmt = DB.prepareStatement(updateSQL.toString(), get_TrxName());
			updates = pstmt.executeUpdate();
			if(updates == 0 && !p_IsAllowLogging)
			{
				;//Nothing to do
			}else{
				createLog(table, column_ID, "UPDATE : " + updates, updateSQL.toString(), treat, help, false);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, updateSQL.toString(), e);
			throw new DBException(e, updateSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return updates;
	}

	/**
	 * Execute Update Constraint
	 *
	 * @param s
	 * @return
	 */
	private int executeUpdateConstraint(String s)
	{
		if(s.equals("D"))
		{
			addLog("##### INVALID FK CONSTRAINT #####");
			createLog("", "", "##### INVALID FK CONSTRAINT #####", "", "", "",true);
		}else if(s.equals("O")){
			addLog("##### VALID FK CONSTRAINT #####");
			createLog("", "", "##### VALID FK CONSTRAINT #####", "", "", "",true);
		}else{
			return -1;
		}

		AdempiereDatabase adempiereDB = DB.getDatabase();
		String schemaName =adempiereDB.getSchema();

		String updateConstraintSQL = "update pg_trigger set tgenabled = ? where oid in ( "
			       +" select tr.oid from pg_class cl, pg_trigger tr, pg_namespace ns "
			         +"   where tr.tgrelid = cl.oid "
			           +"     and cl.relnamespace = ns.oid "
			            +"    and ns.nspname = ?)";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int updates = 0;
		try
		{
			pstmt = DB.prepareStatement(updateConstraintSQL, get_TrxName());
			pstmt.setString(1, s);
			pstmt.setString(2, schemaName);
			updates = pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, updateConstraintSQL, e);
			throw new DBException(e, updateConstraintSQL);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return updates;

		/*Status check SQL*/
		//SELECT * from pg_trigger where oid in ( select tr.oid from pg_class cl, pg_trigger tr, pg_namespace ns where tr.tgrelid = cl.oid and  cl.relnamespace = ns.oid and ns.nspname = 'adempiere')
	}


	/**
	 * Execute Lock Table
	 *
	 * @param table
	 * @param lockMode
	 * @return
	 */
	private int executeLockTable(String table, String lockMode)
	{
		//Checking if the "tableName" and "Column_ID" exists
		if(table == null)
			return -1;

		if(!isAppDictionaryTable(table) || !isDataBaseTable(table))
			return -1;

		StringBuilder lockSQL = new StringBuilder("LOCK TABLE " + table);

		if(!Util.isEmpty(lockMode))
			lockSQL.append(" IN " +lockMode+" MODE " );


		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(lockSQL.toString(), get_TrxName());
			pstmt.execute();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, lockSQL.toString(), e);
			throw new DBException(e, lockSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return 0;
	}

	/**
	 *
	 * @param table
	 * @param where
	 * @param type
	 * @return
	 */
	private int countRecords(String table, String where, String type)
	{
		//Check
		if(!isAppDictionaryTable(table) || !isDataBaseTable(table))
			return -1;

		StringBuilder countSQL = new StringBuilder("SELECT COUNT(*) FROM " + table);

		//Add Where
		if(!Util.isEmpty(where))
		{
			countSQL.append(" WHERE " + where);
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do;
			}else{
				countSQL.append(" AND AD_Client_ID = " + p_LookupClientID );
			}
		}else{
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do;
			}else{
				countSQL.append(" WHERE AD_Client_ID = " + p_LookupClientID);
			}
		}

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int counts = 0;
		try
		{
			pstmt = DB.prepareStatement(countSQL.toString(), get_TrxName());
			rs = pstmt.executeQuery();
			if (rs.next ())
			{
				counts = rs.getInt(1);
			}

		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, countSQL.toString(), e);
			throw new DBException(e, countSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return counts;
	}

	/**
	 *
	 * @param column_ID
	 * @param table
	 * @param where
	 * @param type
	 * @return
	 */
	private ArrayList<Integer> getIDList(String column_ID, String table, String where, String type)
	{
		ArrayList<Integer> listID = new ArrayList<Integer>();

		StringBuilder distinctSQL = new StringBuilder("SELECT DISTINCT "+ column_ID +" FROM " + table);

		//Add Where
		if(!Util.isEmpty(where))
		{
			distinctSQL.append(" WHERE " + where);
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do
			}else{
				distinctSQL.append(" AND AD_Client_ID = " + p_LookupClientID );
			}
		}else{
			if(type.equals(TYPE_ALL_TRANSACTION))
			{
				;//Noting to do
			}else{
				distinctSQL.append(" WHERE AD_Client_ID = " + p_LookupClientID);
			}
		}

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(distinctSQL.toString(), get_TrxName());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				listID.add(rs.getInt(1));
			}//while
		}catch (SQLException e){
			log.log(Level.SEVERE, distinctSQL.toString(), e);
			throw new DBException(e, distinctSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return listID;
	}

	/**
	 *
	 * @param table
	 * @return
	 */
	private int getMaxPrimaryKey_ID(String table)
	{
		StringBuilder selectMaxSQL = new StringBuilder("SELECT MAX("+ table+"_ID)" +" FROM " + table);

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int maxID = 0;
		try
		{
			pstmt = DB.prepareStatement(selectMaxSQL.toString(), get_TrxName());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				maxID = rs.getInt(1);
			}//while
		}catch (SQLException e){
			log.log(Level.SEVERE, selectMaxSQL.toString(), e);
			throw new DBException(e, selectMaxSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return maxID;
	}



	/***************Bulk update Method***************/
	/**
	 * Bulk Update. The Column can refer Table Direct. Column Name is TableName_ID.
	 *
	 * @param tableName
	 * @param IDs
	 * @param isIN_IDs
	 * @param treat
	 * @param value
	 * @param tables
	 * @param isIN_Tables
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private int bulkUpdate_canReferTableDirect(String tableName, ArrayList<Integer> IDs, boolean isIN_IDs
									, String treat, int value, String[] tables, boolean isIN_Tables, String type) throws Exception
	{
		if(type.equals(TYPE_ALL_TRANSACTION) && p_IsTruncateJP)
			return -5;

		//Checking if the "tableName" exists
		if(!isAppDictionaryTable(tableName) || !isDataBaseTable(tableName))
			return -1 ;//tableName + " is not found. Checked at bulkUpdate_canReferTableDirect() method.";

		//Checking if Table has a column of "TableName_ID"
		if(!hasColumn(tableName+"_ID",tableName))
			return -2 ; //tableName + " does not have a column of "+ tableName +"_ID" + ". Checked at bulkUpdate_canReferTableDirect() method.";

		int processed = 0;

		String getTableSQL = "SELECT UPPER(TableName) FROM AD_Table t INNER JOIN AD_Column c ON(t.AD_Table_ID = C.AD_Table_ID)"
				+ " WHERE IsView='N' and UPPER(ColumnName)=?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(getTableSQL, get_TrxName());
			pstmt.setString(1, (tableName+"_ID").toUpperCase());
			rs = pstmt.executeQuery();
			boolean isExecuteTable = false;
			while (rs.next ())
			{
				if(rs.getString(1).equals(tableName.toUpperCase()))
					continue;

				if(tables != null)
				{
					if(isIN_Tables)
						isExecuteTable = false;
					else
						isExecuteTable = true;

					if(isIN_Tables)
					{
						for(int j = 0; j < tables.length; j++)
						{
							if(rs.getString(1).equals(tables[j].toUpperCase()))
							{
								isExecuteTable =true;
								break;
							}
						}
					}else{
						for(int j = 0; j < tables.length; j++)
						{
							if(rs.getString(1).equals(tables[j].toUpperCase()))
							{
								isExecuteTable =false;
								break;
							}
						}
					}

					if(!isExecuteTable)
						continue;
				}//if(tables != null)

				executeUpdateSQL(rs.getString(1), tableName +"_ID", treat, value, createWhereInIDs(tableName +"_ID", IDs, isIN_IDs), type,"REFERENCE_DIRECT");

				processed++;

			}//while

		}catch (SQLException e){
			log.log(Level.SEVERE, getTableSQL.toString(), e);
			throw new DBException(e, getTableSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return processed;
	}

	/**
	 *
	 * Bulk Update. The Column can't refer Table Direct. Column Name is not TableName_ID.
	 *
	 * @param tableName
	 * @param IDs
	 * @param isIN_IDs
	 * @param treat
	 * @param value
	 * @param tables
	 * @param isIN_Tables
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private int bulkUpdate_canNotReferTableDirect(String tableName, ArrayList<Integer> IDs, boolean isIN_IDs
						, String treat, int value,String[] tables, boolean isIN_Tables, String type) throws Exception
	{
		if(type.equals(TYPE_ALL_TRANSACTION) && p_IsTruncateJP)
			return -5;

		//Checking if the "tableName" exists
		if(!isAppDictionaryTable(tableName) || !isDataBaseTable(tableName))
			return -1 ;//tableName + " is not found. Checked at bulkUpdate_canNotReferTableDirect() method.";

		//Checking if Table has a column of "TableName_ID"
		if(!hasColumn(tableName+"_ID",tableName))
			return -2 ; //tableName + " does not have a column of "+ tableName +"_ID" + ". Checked at bulkUpdate_canNotReferTableDirect() method.";

		int processed = 0;

		ArrayList<Integer> referenceList = getReferenceList(tableName) ;
		if(referenceList.size()==0)
			return -3 ; //tableName + " does not have the Reference. Checked at bulkUpdate_canNotReferTableDirect() method.";

		ArrayList<TableColumn> tableColumnList= getIndirectReferTableColumn(tableName+"_ID", referenceList);
		boolean isExecuteTable = false;
		for(TableColumn tableColumn :tableColumnList)
		{
			if(tables != null)
			{
				if(isIN_Tables)
					isExecuteTable = false;
				else
					isExecuteTable = true;

				if(isIN_Tables)
				{
					for(int j = 0; j < tables.length; j++)
					{
						if(tableColumn.tableName.toUpperCase().equals(tables[j].toUpperCase()))
						{
							isExecuteTable =true;
							break;
						}
					}
				}else{
					for(int j = 0; j < tables.length; j++)
					{
						if(tableColumn.tableName.toUpperCase().equals(tables[j].toUpperCase()))
						{
							isExecuteTable =false;
							break;
						}
					}
				}

				if(!isExecuteTable)
					continue;
			}//if(tables != null)


			//Special Column
			if(tableColumn.columnName.toUpperCase().equals("CreatedBy".toUpperCase())
					|| tableColumn.columnName.toUpperCase().equals("UpdatedBy".toUpperCase()))
				continue;


			executeUpdateSQL(tableColumn.tableName, tableColumn.columnName, treat, value, createWhereInIDs(tableColumn.columnName, IDs, isIN_IDs), type,"REFERENCE_INDIRECT");

			processed++;
		}

		return processed;
	}

	private int bulkDelete_NotUseRecords(String tableName, String type) throws Exception
	{
		//Checking if the "tableName" exists
		if(!isAppDictionaryTable(tableName) || !isDataBaseTable(tableName))
			return -1 ;//tableName + " is not found. Checked at bulkUpdate_canReferTableDirect() method.";

		//Checking if Table has a column of "TableName_ID"
		if(!hasColumn(tableName+"_ID",tableName))
			return -2 ; //tableName + " does not have a column of "+ tableName +"_ID" + ". Checked at bulkUpdate_canReferTableDirect() method.";

		TreeSet<Integer> set_of_ID = new TreeSet<Integer>();

		String getTableSQL = "SELECT UPPER(TableName) FROM AD_Table t INNER JOIN AD_Column c ON(t.AD_Table_ID = C.AD_Table_ID)"
				+ " WHERE IsView='N' and UPPER(ColumnName)=?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int processed = 0;
		try
		{
			pstmt = DB.prepareStatement(getTableSQL, get_TrxName());
			pstmt.setString(1, (tableName+"_ID").toUpperCase());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				if(rs.getString(1).equals((tableName).toUpperCase()))
					continue;

				ArrayList<Integer> list = getIDList(tableName+"_ID", rs.getString(1), null, type);
				for(Integer id : list )
				{
					set_of_ID.add(id);
				}

				processed++;

			}//while


			//Can not refere Table Direct
			ArrayList<Integer> referenceList = getReferenceList(tableName) ;
			if(referenceList.size()==0)
				return -3 ; //tableName + " does not have the Reference. Checked at bulkUpdate_canNotReferTableDirect() method.";

			ArrayList<TableColumn> tableColumnList= getIndirectReferTableColumn(tableName+"_ID", referenceList);
			for(TableColumn tableColumn :tableColumnList)
			{
				ArrayList<Integer> list = getIDList(tableColumn.columnName, tableColumn.tableName, null, type);
				for(Integer id : list )
				{
					set_of_ID.add(id);
				}

				processed++;
			}

			ArrayList<Integer> list_of_mainTable_ID = getIDList(tableName+"_ID", tableName, null, type);
			boolean isContain = false;
			for(Integer id_of_mainTable : list_of_mainTable_ID)
			{
				isContain = false;
				for(Integer id : set_of_ID)
				{
					if(id_of_mainTable.intValue() == id.intValue())
					{
						isContain = true;
						break;
					}
				}

				if(isContain)
				{
					;//Nothing to do;
				}else{
					int deletes = executeDeleteSQL(tableName, tableName+"_ID"+ "=" +id_of_mainTable.intValue(), type, false,"NOT_USE_RECORD");
					if(deletes != 1)
						return -4;
				}
			}//for

		}catch (SQLException e){
			log.log(Level.SEVERE, getTableSQL.toString(), e);
			throw new DBException(e, getTableSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return processed;
	}


	/**
	 *
	 * @param returnInt
	 * @param tableName
	 * @param isCreatLog
	 */
	private void bulkUpdate_Log(int returnInt, String tableName, boolean isCreatLog)
	{
		if(returnInt < 0 && isCreatLog)
		{
			if(returnInt == -1)
				createLog("","",tableName + " is not found. Checked at bulk method.", "","","", false);
			else if(returnInt == -2)
				createLog("","",tableName + " does not have a column of " + tableName +"_ID" + ". Checked at bulk method."
																										,"","","", false);
			else if(returnInt == -3)
				createLog("","",tableName + " does not have the Reference. Checked at bulk method.", "","","", false);
			else if(returnInt == -4)
				createLog("","",tableName + " DELETE ERROR Not 1.", "","","", false);
			else if(returnInt == -5)
				createLog("","",tableName + " TRUNCATE ", "","","", false);

		}else if(returnInt == 0 && isCreatLog && p_IsAllowLogging){
			;
		}else if(isCreatLog){
			;
		}
	}

	/**
	 * Auto Judge Treat
	 *
	 * @param tableName
	 * @param columnName
	 * @param treat : TREAT_SET_NULL / TREAT_SET_VALUE /TREAT_DELETE /TREAT_IF_MANDATORY_DELETE_ELSE_VALUE
	 * 					TREAT_IF_MANDATORY_DELETE_ELSE_NULL / TREAT_IF_MANDATORY_VALUE_ELSE_NULL / TREAT_IF_MANDATORY_VALUE_ELSE_DELETE
	 * @param value
	 * @return treat
	 */
	public String treatAutoJudge(String tableName, String columnName, String treat, int value)
	{
		if (treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_NULL) || treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_VALUE)
				|| treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_DELETE) || treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_NULL))
		{
			;//
		}else{
			return treat;
		}


		MTable m_Table = MTable.get(getCtx(), tableName);
		MColumn m_Column = m_Table.getColumn(columnName);

		if(treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_NULL))
		{
			if(m_Column.isMandatory())
			{
				return TREAT_DELETE;
			}else{
				return TREAT_SET_NULL;
			}
		}else if(treat.equals(TREAT_IF_MANDATORY_DELETE_ELSE_VALUE)){
			if(m_Column.isMandatory())
			{
				return TREAT_DELETE;
			}else{
				int count = countRecords(tableName, columnName + "=" + value, TYPE_ALL_TRANSACTION);
				if(count > 0)
					return TREAT_SET_VALUE;
				else
					return TREAT_SET_NULL;
			}
		}else if(treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_DELETE)){
			if(m_Column.isMandatory())
			{
				int count = countRecords(tableName, columnName + "=" + value, TYPE_ALL_TRANSACTION);
				if(count > 0)
					return TREAT_SET_VALUE;
				else
					return TREAT_DELETE;
			}else{
				return TREAT_DELETE;
			}
		}else if(treat.equals(TREAT_IF_MANDATORY_VALUE_ELSE_NULL)){
			if(m_Column.isMandatory())
			{
				int count = countRecords(tableName, columnName + "=" + value, TYPE_ALL_TRANSACTION);
				if(count > 0)
					return TREAT_SET_VALUE;
				else
					return TREAT_DELETE;
			}else{
				return TREAT_SET_NULL;
			}
		}

		return treat;
	}


	/***************Utility Method***************/
	public String[] stringArray_Merge(String[] stringArray1, String[] stringArray2)
	{
		if(stringArray1 == null || stringArray1.length == 0)
		{
			if(stringArray2 != null)
			{
				return stringArray2;
			}
		}

		if(stringArray2 == null || stringArray2.length == 0){
			if(stringArray1 != null)
			{
				return stringArray1;
			}
		}

		if(stringArray1 == null && stringArray2 == null)
			return null;

		ArrayList<String> stringList = new ArrayList<String>();
		for(int i = 0; i < stringArray1.length; i++)
		{
			stringList.add(stringArray1[i]);
		}

		boolean isContain = false;
		for(int i = 0; i < stringArray2.length; i++)
		{
			isContain = false;
			for(int j = 0; j < stringArray1.length; j++)
			{
				if(stringArray2[i].toUpperCase().equals(stringArray1[j].toUpperCase()))
				{
					isContain = true;
					break;
				}
			}

			if(!isContain)
				stringList.add(stringArray2[i]);
		}

		return stringList.toArray(new String[stringList.size()]);
	}

	public String[] stringArray_Subtraction(String[] stringArray1, String[] stringArray2)
	{
		if(stringArray1 == null || stringArray1.length == 0)
		{
			if(stringArray2 != null)
			{
				return stringArray2;
			}
		}

		if(stringArray2 == null || stringArray2.length == 0){
			if(stringArray1 != null)
			{
				return stringArray1;
			}
		}

		if(stringArray1 == null && stringArray2 == null)
			return null;

		ArrayList<String> stringList = new ArrayList<String>();
		boolean isContain = false;
		for(int i = 0; i < stringArray1.length; i++)
		{
			isContain = false;
			for(int j = 0; j < stringArray2.length; j++)
			{
				if(stringArray1[i].toUpperCase().equals(stringArray2[j].toUpperCase()))
				{
					isContain = true;
					break;
				}
			}

			if(!isContain)
				stringList.add(stringArray1[i]);
		}

		return stringList.toArray(new String[stringList.size()]);
	}

	public boolean stringArray_IsIN(String[] stringArray1, String string)
	{
		boolean isContaine = false;
		for(int i = 0; i < stringArray1.length; i++)
		{
			if(stringArray1[i].toUpperCase().equals(string.toUpperCase()))
			{
				isContaine = true;
				break;
			}
		}

		return isContaine;
	}

	private boolean isAccessLevelSystemTable(String tableName, boolean isSystemOnly)
	{
		MTable m_Table =MTable.get(getCtx(), tableName);
		if(m_Table.get_ID() == 0)
			return false;

		if(isSystemOnly)
		{
			if(m_Table.getAccessLevel().equals(MTable.ACCESSLEVEL_SystemOnly))
				return true;
		}else{
			if(m_Table.getAccessLevel().equals(MTable.ACCESSLEVEL_SystemOnly)
					|| m_Table.getAccessLevel().equals(MTable.ACCESSLEVEL_SystemPlusClient)
					|| m_Table.getAccessLevel().equals(MTable.ACCESSLEVEL_All))
				return true;
		}

		return false;
	}

	public boolean hasColumn(String columnName, String tableName)
	{
		MTable m_Table =MTable.get(getCtx(), tableName);
		MColumn[] columns = m_Table.getColumns(false);
		boolean isContain = false;
		for(int i = 0; i < columns.length; i++)
		{
			if(columns[i].getColumnName().toUpperCase().equals(columnName.toUpperCase()))
			{
				if(Util.isEmpty(columns[i].getColumnSQL()))//check virtual column or not
					isContain = true;

				break;
			}
		}

		return isContain;
	}

	private boolean isIDColumn(String columnName, String tableName)
	{
		MTable m_Table =MTable.get(getCtx(), tableName);
		MColumn column = m_Table.getColumn(columnName);

		if(DisplayType.isID(column.getAD_Reference_ID()))
				return true;
		else if(columnName.endsWith("_ID"))
				return true;
		else
			return false;

	}


	/**
	 * Create WHERE clause "Column_ID in (IDs)"
	 *
	 * @param column_ID : Column Name that Display Type is Table, Table Direct, Search, ID.
	 * @param IDs : ID of Records
	 * @return null or String
	 */
	private String createWhereInIDs(String column_ID, ArrayList<Integer> IDs, boolean isIN)
	{
		if(column_ID == null)
			return null;

		if(IDs == null || IDs.size() == 0)
			return null;

		StringBuilder where = new StringBuilder(column_ID);
		if(isIN)
			where.append(" IN (");
		else
			where.append(" Not IN (");

		int i = 0;
		for(Integer ID : IDs)
		{
			if(i==0)
				where.append(ID.toString());
			else
				where.append("," + ID.toString());
			i++;
		}
		where.append(")");

		return where.toString();
	}

	public ArrayList<TableColumn> getIndirectReferTableColumn(String column_ID, ArrayList<Integer> referenceList)
	{
		ArrayList<TableColumn> tableColumnList = new ArrayList<TableColumn>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder getTableColumnSQL = new StringBuilder("SELECT tablename, columnname FROM AD_COLUMN c, AD_TABLE t WHERE ad_reference_value_id in(");
		for(int i = 0; i < referenceList.size(); i++)
		{
			if(i>0)
				getTableColumnSQL.append(",");
			getTableColumnSQL.append(referenceList.get(i).toString());
		}
		getTableColumnSQL.append(") AND UPPER (columnname) <> '" + column_ID.toUpperCase() +"' AND t.ad_table_id = c.ad_table_id AND t.IsView='N' ");

		try
		{
			pstmt = DB.prepareStatement(getTableColumnSQL.toString(), get_TrxName());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				tableColumnList.add(new TableColumn(rs.getString(1),rs.getString(2)));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, getTableColumnSQL.toString(), e);
			throw new DBException(e, getTableColumnSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return tableColumnList;
	}

	class TableColumn
	{
		public String tableName;
		public String columnName;

		public TableColumn(String tableName, String columnName)
		{
			this.tableName = tableName;
			this.columnName= columnName;
		}
	}



	public ArrayList<Integer> getReferenceList(String tableName)
	{
		String getReferenceListSQL = "SELECT r.AD_Reference_ID FROM AD_Reference r"
				+" INNER JOIN adempiere.AD_Ref_Table rt ON(r.AD_Reference_ID=rt.AD_Reference_ID)"
				+" INNER JOIN adempiere.AD_Table t ON(rt.AD_Table_ID=t.AD_Table_ID)"
				+" WHERE UPPER(t.TableName) = '"+ tableName.toUpperCase() +"'" ;

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		ArrayList<Integer> referenceList = new ArrayList<Integer>();
		try
		{
			pstmt = DB.prepareStatement(getReferenceListSQL, get_TrxName());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				referenceList.add(rs.getInt(1));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, getReferenceListSQL, e);
			throw new DBException(e, getReferenceListSQL);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return referenceList;
	}

	private boolean isAppDictionaryTable(String tableName)
	{
		if(TABLELIST_AD == null)
			TABLELIST_AD = createTableList_AD();

		for(String AD_TABLE : TABLELIST_AD)
		{
			if(tableName.toUpperCase().equals(AD_TABLE.toUpperCase()))
				return true;
		}
		return false;
	}

	private boolean isDataBaseTable(String tableName)
	{
		if(TABLELIST_DB == null)
			TABLELIST_DB = createTableList_DB();

		for(String DB_TABLE : TABLELIST_DB)
		{
			if(tableName.toUpperCase().equals(DB_TABLE.toUpperCase()))
				return true;
		}
		return false;
	}



	/**
	 *
	 * @return
	 */
	private ArrayList<String> createTableList_DB()
	{
		AdempiereDatabase adempiereDB = DB.getDatabase();
		String schemaName =adempiereDB.getSchema();

		String getTableListSQL = "SELECT UPPER(tablename) FROM pg_tables WHERE schemaname = ? ORDER BY tablename";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		try
		{
			pstmt = DB.prepareStatement(getTableListSQL, get_TrxName());
			pstmt.setString(1, schemaName);
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				list.add(rs.getString(1));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, getTableListSQL, e);
			throw new DBException(e, getTableListSQL);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return list;
	}

	/**
	 *
	 * @return
	 */
	private ArrayList<String> createTableList_AD()
	{
		String getTableListSQL = "SELECT UPPER(tablename) FROM AD_TABLE a WHERE a.isview = 'N'"
				+" AND EXISTS (SELECT ad_column_id FROM AD_COLUMN c  WHERE a.ad_table_id = c.ad_table_id AND UPPER (c.columnname) = 'AD_CLIENT_ID')"
				+" ORDER BY tablename";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		try
		{
			pstmt = DB.prepareStatement(getTableListSQL, get_TrxName());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				list.add(rs.getString(1));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, getTableListSQL, e);
			throw new DBException(e, getTableListSQL);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return list;
	}


	/**
	 *
	 * @param tableName
	 * @param columnName
	 * @param description
	 * @param SQL
	 * @param treat
	 * @param help
	 */
	private void createLog(String tableName, String columnName, String description, String SQL, String treat,String help, boolean isDisplay)
	{
		if(isDisplay && processMonitor != null)
			processMonitor.statusUpdate(description);

		MDeleteClientLogJP log = new MDeleteClientLogJP(getCtx(), 0, null);
		log.setAD_PInstance_ID(getAD_PInstance_ID());
		log.setJP_Delete_Client(p_JP_Delete_Client);
		if(p_JP_DeleteProfile_ID != 0)
			log.setJP_DeleteProfile_ID(p_JP_DeleteProfile_ID);
		if(JP_CustomDeleteProfile_ID != 0)
			log.setJP_CustomDeleteProfile_ID(JP_CustomDeleteProfile_ID);
		if(JP_CustomDeleteProfileLine_ID != 0)
			log.setJP_CustomDeleteProfileLine_ID(JP_CustomDeleteProfileLine_ID);
		log.setLookupClientID(p_LookupClientID);
		log.setIsTruncateJP(p_IsTruncateJP);
		log.setIsAllowLogging(p_IsAllowLogging);

		if(!Util.isEmpty(description))
			log.setDescription(description);
		if(!Util.isEmpty(SQL))
			log.setSQLStatement(SQL);
		if(!Util.isEmpty(treat))
			log.setJP_TreatForeignKey(treat);
		if(!Util.isEmpty(help))
			log.setHelp(help);

		if(Util.isEmpty(tableName))
		{
			log.saveEx(get_TrxName());
			return ;
		}

		MTable m_Table = MTable.get(getCtx(), tableName);
		if(m_Table.get_ID()==0)
		{
			log.saveEx(get_TrxName());
			return ;
		}

		log.setAD_Table_ID(m_Table.get_ID());
		log.setTableName(m_Table.getTableName());
		log.setAccessLevel(m_Table.getAccessLevel());
		log.setAD_Window_ID(m_Table.getAD_Window_ID());

		MColumn m_Column = null;
		if(!Util.isEmpty(columnName))
		{
			MColumn[] columns = m_Table.getColumns(false);
			for(int i = 0; i < columns.length; i++)
			{
				if(columns[i].getColumnName().toUpperCase().equals(columnName.toUpperCase()))
				{
					m_Column = columns[i];
					break;
				}
			}
		}

		if(m_Column != null)
		{
			log.setAD_Column_ID(m_Column.get_ID());
			log.setColumnName(m_Column.getColumnName());
			log.setAD_Reference_ID(m_Column.getAD_Reference_ID());
			log.setAD_Reference_Value_ID(m_Column.getAD_Reference_Value_ID());
			log.setAD_Val_Rule_ID(m_Column.getAD_Val_Rule_ID());
			log.setFKConstraintName(m_Column.getFKConstraintName());
			log.setFKConstraintType(m_Column.getFKConstraintType());
			log.setIsKey(m_Column.isKey());
			log.setIsParent(m_Column.isParent());
			log.setIsMandatory(m_Column.isMandatory());
			log.setDefaultValue(m_Column.getDefaultValue());
			log.setMandatoryLogic(m_Column.getMandatoryLogic());
			log.setColumnSQL(m_Column.getColumnSQL());
		}

		log.saveEx(get_TrxName());
	}

	public static String[] TrxTables = {
							/**Quote-to-Invoice and Requisition-to-Invoice**/
							//Order
							"C_Order","C_OrderLine","C_OrderTax","C_POSPayment","C_OrderPaySchedule"
							,"C_OrderLandedCost","C_OrderLandedCostAllocation"
							//In Out
							,"M_InOut","M_InOutLine","M_InOutLineMA","M_Package","M_PackageMPS","M_PackageLine"
							//In Out Confirmation
							,"M_InOutConfirm","M_InOutLineConfirm"
							//Invoice
							,"C_Invoice","C_InvoiceLine","C_InvoiceTax","C_InvoicePaySchedule","C_InvoiceBatch"
							,"C_InvoiceBatchLine","C_LandedCost","C_LandedCostAllocation"
							//Match
							,"M_MatchPO","M_MatchInv"
							//Request for Quotations
							,"C_RfQ_Topic","C_RfQ_TopicSubscriber","C_RfQ_TopicSubscriberOnly","C_RfQ","C_RfQLine"
							,"C_RfQLineQty","C_RfQResponse","C_RfQResponseLine","C_RfQResponseLineQty"
							//Requisition
							,"M_Requisition","M_RequisitionLine"

							//Time Expense
							,"S_TimeExpense","S_TimeExpenseLine"
							//Commission
							,"C_CommissionRun","C_CommissionDetail","C_CommissionAmt"

							/**Return**/
							,"M_RMA","M_RMALine","M_RMATax"

							/**Open Items**/
							//Allocation
							,"C_AllocationHdr","C_AllocationLine"
							//Bank Statement
							,"C_BankStatement","C_BankStatementLine","JP_BankStatementTax"
							//Pay Selection
							,"C_PaySelection","C_PaySelectionLine","C_PaySelectionCheck"
							//Payment
							,"C_PaymentBatch","C_Payment","C_PaymentAllocate","C_PaymentTransaction"
							//Cash Journal
							,"C_Cash","C_CashLine","C_CashPlan","C_CashPlanLine"

							/**Material Management**/
							//Matarial Management Journal
							,"M_Transaction","M_Transactionallocation","M_StorageOnHand","M_StorageReservation"
							//Cost
							,"M_CostDetail","M_CostQueue","M_Cost","M_CostHistory"
							//Inventory
							,"M_Inventory","M_InventoryLine","M_InventoryLineMA"
							//Movement
							,"M_Movement","M_MovementLine","M_MovementLineMA"
							//Movement Confirmation
							,"M_MovementConfirm","M_MovementLineConfirm"

							/**Manufacturing**/
							//Production
							,"M_Production","M_ProductionLine","M_ProductionLineMA","M_QualityTestResult"

							/**Accounting**/
							//Accounting
							,"Fact_Acct","Fact_Acct_Summary","Fact_Reconciliation"
							//GL Journal
							,"GL_JournalBatch","GL_Journal","GL_JournalLine"

							/**SFA**/
							,"C_Opportunity","C_ContactActivity"

							/**Request**/
							,"R_Request","R_RequestUpdates","R_RequestAction","R_RequestUpdate"

							/**Other**/
							,"C_RevenueRecognition_Plan","C_RevenueRecognition_Run"
							,"C_ServiceLevel","C_ServiceLevelLine"
							,"S_ResourceAssignment"

							//Project
							,"C_ProjectIssueMA","C_ProjectLine"

							//Recurring
							,"C_Recurring","C_Recurring_Run"

							//,"DD_Order","DD_OrderLine","PP_Cost_Collector","PP_Order"
							//,"PP_MRP"
						};


	public static String[] IniTables = {
							//Client/
							"AD_Client","AD_ClientInfo","AD_ClientShare"

							//Org
							,"AD_Org","AD_OrgInfo","AD_OrgType"

							//Warehouse
							,"M_Warehouse","M_Locator","M_Warehouse_Acct"

							//Role
							,"AD_Role","AD_Role_OrgAccess","AD_User_Roles","AD_Window_Access","AD_Process_Access"
							,"AD_Form_Access","AD_InfoWindow_Access","AD_Workflow_Access","AD_Task_Access","AD_Document_Action_Access"
							,"AD_Role_Included"

							//User
							,"AD_User","AD_User_Substitute","AD_User_OrgAccess"
							,"AD_Preference"


							/*************************************************************************************/
							//Calendar
							,"C_Calendar","C_Year","C_Period","C_PeriodControl","C_NonBusinessDay"

							//Accounting Schema
							,"C_AcctSchema","C_AcctSchema_Element","C_AcctSchema_GL","C_AcctSchema_Default"

							//Account Element
							,"C_Element","C_ElementValue","C_ElementValue_Trl","C_SubAcct"
							,"C_ValidCombination"

							//GL Category
							,"GL_Category","GL_Category_Trl"

							//Tax
							,"C_TaxCategory","C_TaxCategory_Trl","C_Tax","C_Tax_Acct","C_TaxPostal"
							,"C_Tax_Trl","C_TaxProvider","C_TaxProviderCfg"
							,"C_TaxGroup"

							//Document Type
							,"C_DocType","C_DocType_Trl"

							//Charge
							,"C_Charge","C_Charge_Acct","C_Charge_Trl","C_ChargeType","C_ChargeType_DocType"

							//Payment Term & Invoice Schedule
							,"C_PaymentTerm","C_PaymentTerm_Trl","C_PaySchedule","C_PaymentTerm"
							,"C_InvoiceSchedule"

							//Currency
							,"C_Currency","C_Currency_Trl","C_Currency_Acct","C_ConversionType"

							//Bank & Account
							,"C_Bank","C_BankAccount","C_BankAccountDoc","C_BankAccount_Processor","C_BankStatementLoader"
							,"C_BankAccount_Acct"

							/*************************************************************************************/

							//Workflow
							,"AD_Workflow","AD_WF_Node","AD_WF_Node_Para","AD_WF_Node_Trl","AD_WF_Block"
							,"AD_WF_NodeNext","AD_WF_NextCondition","AD_Workflow_Trl","AD_WF_Responsible"

							/*************************************************************************************/
							//BP
							,"C_BP_Group","C_BP_Group_Acct"
							,"C_BPartner","C_BPartner_Location","C_BP_BankAccount","C_BP_ShippingAcct","C_BP_Customer_Acct"
							,"C_BP_Vendor_Acct","C_BP_Relation"

							//SFA
							,"C_SalesStage","C_SalesStage_Trl"

							//Request Management
							,"R_RequestType","R_RequestTypeUpdates","R_Group","R_GroupUpdates","R_Category"
							,"R_CategoryUpdates","R_Resolution","R_Status","R_StatusCategory","R_StandardResponse"
							,"R_InterestArea","R_ContactInterest"

							//Mail Template
							,"R_MailText","R_MailText_Trl"

							//TODO:need confirmation
							,"R_IssueKnown","R_IssueProject","R_IssueRecommendation","R_IssueStatus","R_IssueSystem"
							,"R_IssueUser"

							//Greeting
							,"C_Greeting","C_Greeting_Trl"


							/*************************************************************************************/
							//Product & Product Category
							,"M_Product_Category","M_Product_Category_Trl","M_Product_Category_Acct"
							,"M_Product","M_Product_Trl","M_Product_BOM","M_Substitute","M_RelatedProduct"
							,"M_Replenish","M_Product_PO","C_BPartner_Product","M_Product_Acct"
							//,"S_ExpenseType","S_Resource"

							//Price List
							,"M_PriceList","M_PriceList_Version","M_ProductPrice","M_ProductPriceVendorBreak"
							,"M_PriceList_Trl","M_PriceList_Version_Trl"

							,"M_DiscountSchema","M_DiscountSchemaBreak"

							//Cost
							,"M_CostType","M_CostElement"

							//Attribute
							,"M_Attribute","M_AttributeValue","M_AttributeSet","M_AttributeUse","M_AttributeSetExclude"

							,"C_UOM"

							/*************************************************************************************/
							//Location
							,"C_Location"


							//Country & Region
							,"C_Country","C_Country_Trl","C_Region","C_Region_Trl","C_City"
							,"C_CountryGroupCountry"


							/*************************************************************************************/
							//Report Format
							,"AD_PrintFormat","AD_PrintFormat_Trl","AD_PrintFormatItem","AD_PrintFormatItem_Trl","AD_PrintGraph"
							,"AD_PrintForm","AD_PrintColor","AD_PrintFont","AD_PrintPaper","AD_PrintTableFormat"

							//Import Format
							,"AD_ImpFormat","AD_ImpFormat_Row"


							//Processor
							,"AD_AlertProcessor","AD_LdapProcessor","AD_WorkflowProcessor","C_AcctProcessor","R_RequestProcessor"
							,"R_RequestProcessor_Route","C_PaymentProcessor"

							//Sequence
							,"AD_Sequence"

							//System Config
							,"AD_SysConfig"

							//Menu
							,"AD_Menu","AD_Menu_Trl"

							//Tree
							,"AD_Tree","AD_TreeBar","AD_TreeNode","AD_TreeNodeBP","AD_TreeNodeCMC"
							,"AD_TreeNodeCMM","AD_TreeNodeCMS","AD_TreeNodeCMT","AD_TreeNodeMM","AD_TreeNodePR"
							,"AD_TreeNodeU1","AD_TreeNodeU2","AD_TreeNodeU3","AD_TreeNodeU4"

							//Other
							,"AD_Image","AD_Password_History","AD_CtxHelp","AD_ReplicationStrategy","AD_StorageProvider"

	};

}
