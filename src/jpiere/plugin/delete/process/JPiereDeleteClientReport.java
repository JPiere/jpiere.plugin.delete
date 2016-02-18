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
import java.util.logging.Level;

import jpiere.plugin.delete.model.MCustomDeleteProfile;
import jpiere.plugin.delete.model.MCustomDeleteProfileLine;
import jpiere.plugin.delete.model.MDeleteClientLogJP;
import jpiere.plugin.delete.model.MDeleteProfile;
import jpiere.plugin.delete.process.JPiereDeleteClientRecords.TableColumn;

import org.adempiere.exceptions.DBException;
import org.compiere.model.MColumn;
import org.compiere.model.MRefList;
import org.compiere.model.MTable;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Msg;
import org.compiere.util.Util;

/**
 * 	Report of Initialize Client and Delete Client.
 *
 *  @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 *
 */
public class JPiereDeleteClientReport extends SvrProcess
{

	//Parameters
	private int p_JP_DeleteProfile_ID = 0;
	private int p_Referenced_Table_ID = 0;

	private boolean p_IsAllowLogging = false; //if true display all Table

	private int AD_PInstance_ID = 0;


	//
	private MDeleteProfile m_DeleteProfile = null;
	private String[] customDeleteTables;
	private String[] tables;
	private JPiereDeleteClientRecords deleteClientRecordsProcess;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if(name.equals("JP_DeleteProfile_ID")){
				p_JP_DeleteProfile_ID = para[i].getParameterAsInt();
			}else if(name.equals("Referenced_Table_ID")){
				p_Referenced_Table_ID = para[i].getParameterAsInt();
			}else if(name.equals("IsAllowLogging")){
					p_IsAllowLogging = para[i].getParameterAsBoolean();
			}else{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}

		AD_PInstance_ID = getAD_PInstance_ID();
	}	//	prepare


	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt() throws Exception
	{

		if(p_JP_DeleteProfile_ID <= 0 && p_Referenced_Table_ID <= 0 )
			return Msg.getMsg(getCtx(), "NotFound");

		deleteClientRecordsProcess = new JPiereDeleteClientRecords();

		try
		{
			if(p_JP_DeleteProfile_ID > 0 )
			{
				createTableList();
			}else if(p_Referenced_Table_ID > 0){
				createReferenceList();
			}

		} catch (Exception e) {
			throw e ;
		}finally{

		}



		return "";

	}	//	delete

	private String createTableList() throws Exception
	{

		m_DeleteProfile = new MDeleteProfile(getCtx(), p_JP_DeleteProfile_ID, null);

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

		customDeleteTables = customDeleteTableList.toArray(new String[customDeleteTableList.size()]);


		//TYPE_of_TRANSACTION = TrxTable + CustomTable
		if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_ALL_TRANSACTION)
				|| m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CLIENT_TRANSACTION))
		{
			tables = deleteClientRecordsProcess.stringArray_Merge(JPiereDeleteClientRecords.TrxTables, customDeleteTables);

		//TYPE_INITIALIZE_CLIENT = IniTable + TrxTable + CustomTable
		}else if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_INITIALIZE_CLIENT)){

			tables = deleteClientRecordsProcess.stringArray_Merge(JPiereDeleteClientRecords.IniTables,
					deleteClientRecordsProcess.stringArray_Merge(JPiereDeleteClientRecords.TrxTables, customDeleteTables));

		//TYPE_CUSTOM_DELETE = Custom Table
		}else if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CUSTOM_DELETE)){
			tables = customDeleteTables;
		}

		for(int i = 0; i < tables.length; i++)
		{
			MDeleteClientLogJP log = new MDeleteClientLogJP(getCtx(), 0, null);
			log.setAD_PInstance_ID(getAD_PInstance_ID());
			log.setJP_Delete_Client(m_DeleteProfile.getJP_Delete_Client());
			log.setJP_DeleteProfile_ID(p_JP_DeleteProfile_ID);
			log.setReferenced_Table_ID(p_Referenced_Table_ID);

			//##### IMPOERT TABLE or TEMPORARY TABLE #####//
			if(tables[i].toUpperCase().startsWith("I_"))
			{
				if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CUSTOM_DELETE))
					log.setDescription("IMPORT TABLE" + " - " + Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
				else
					log.setDescription("IMPORT TABLE" + " - " + Msg.getMsg(getCtx(), "JP_Delete_AllDelete"));
			}

			if(tables[i].toUpperCase().startsWith("T_") && !tables[i].toUpperCase().equals("T_DeleteClientLogJP".toUpperCase()))
			{
				if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CUSTOM_DELETE))
					log.setDescription("TEMPORARY TABLE" + " - " + Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
				else
					log.setDescription("TEMPORARY TABLE" + " - " + Msg.getMsg(getCtx(), "JP_Delete_AllDelete"));
			}

			//##### CUSTOM TABLE #####//
			if(Util.isEmpty(log.getDescription()))
			{
				for(int j = 0 ; j < customDeleteTables.length; j++)
				{
					if(tables[i].toUpperCase().equals(customDeleteTables[j].toUpperCase()))
					{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_CustomDPTable") + " - " + Msg.getMsg(getCtx(), "JP_Delete_FollowDeleteProfile"));
					}
				}
			}

			//##### TRX TABLE #####//
			if(Util.isEmpty(log.getDescription()))
			{

				for(int j = 0 ; j < JPiereDeleteClientRecords.TrxTables.length; j++)
				{
					if(tables[i].toUpperCase().equals(JPiereDeleteClientRecords.TrxTables[j].toUpperCase()))
					{
						if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CUSTOM_DELETE))
							log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_TrxTable") + " - " + Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
						else
							log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_TrxTable") + " - " + Msg.getMsg(getCtx(), "JP_Delete_AllDelete"));
					}
				}
			}

			//##### INI TABLE #####//
			if(Util.isEmpty(log.getDescription()))
			{

				for(int j = 0 ; j < JPiereDeleteClientRecords.IniTables.length; j++)
				{
					if(tables[i].toUpperCase().equals(JPiereDeleteClientRecords.IniTables[j].toUpperCase()))
					{
						if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_INITIALIZE_CLIENT))
							log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_IniTable") + " - " + Msg.getMsg(getCtx(), "JP_Delete_FollowDeleteProfile"));
						else
							log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_IniTable") + " - " + Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
					}
				}
			}

			MTable m_Table = MTable.get(getCtx(), tables[i]);
			if(m_Table.get_ID()==0)
				continue;

			log.setAD_Table_ID(m_Table.get_ID());
			log.setTableName(m_Table.getTableName());
			log.setAccessLevel(m_Table.getAccessLevel());
			log.setAD_Window_ID(m_Table.getAD_Window_ID());
			if(deleteClientRecordsProcess.hasColumn(tables[i]+"_ID", tables[i]))
				log.setColumnName(tables[i]+"_ID");

			log.saveEx();
		}

		if(!p_IsAllowLogging)
		{
			return "";
		}

		ArrayList<String> TABLELIST_AD = createTableList_AD();
		boolean isContain = false;
		for(String TABLE : TABLELIST_AD)
		{
			isContain = false;
			for(int i = 0; i < tables.length; i++)
			{

				if(TABLE.equals(tables[i].toUpperCase()))
				{
					isContain = true;
					break;
				}
			}

			if(!isContain)
			{
				MTable m_Table = MTable.get(getCtx(), TABLE);
				if(m_Table.get_ID()==0)
					continue;

				MDeleteClientLogJP log = new MDeleteClientLogJP(getCtx(), 0, null);
				log.setAD_PInstance_ID(AD_PInstance_ID);
				log.setJP_Delete_Client(m_DeleteProfile.getJP_Delete_Client());
				log.setJP_DeleteProfile_ID(p_JP_DeleteProfile_ID);
				log.setReferenced_Table_ID(p_Referenced_Table_ID);

				if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_ALL_TRANSACTION)
						|| m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CLIENT_TRANSACTION)
						|| m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_CUSTOM_DELETE))
				{
					if(m_Table.getAccessLevel().equals(MTable.ACCESSLEVEL_SystemOnly))
					{

						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_OtherTable")
										+ " ( " +MRefList.get(getCtx(), 5, MTable.ACCESSLEVEL_SystemOnly, null) +" ) - "+Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
					}else{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_OtherTable") +" - "+Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
					}

				}else if(m_DeleteProfile.getJP_Delete_Client().equals(JPiereDeleteClientRecords.TYPE_INITIALIZE_CLIENT)){

					if(m_Table.getAccessLevel().equals(MTable.ACCESSLEVEL_SystemOnly))
					{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_OtherTable")
										+ " ( " + MRefList.get(getCtx(), 5, MTable.ACCESSLEVEL_SystemOnly, null)  +" ) - "+Msg.getMsg(getCtx(), "JP_Delete_NotDelete"));
					}else{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_OtherTable") +" - "+Msg.getMsg(getCtx(), "JP_Delete_AllDelete"));
					}
				}

				log.setAD_Table_ID(m_Table.get_ID());
				log.setTableName(m_Table.getTableName());
				log.setAccessLevel(m_Table.getAccessLevel());
				log.setAD_Window_ID(m_Table.getAD_Window_ID());
				if(deleteClientRecordsProcess.hasColumn(m_Table.getTableName()+"_ID", m_Table.getTableName()))
					log.setColumnName(m_Table.getTableName()+"_ID");

				log.saveEx();

			}//if(!isContain)
		}//for(String TABLE : TABLELIST_AD)


		return "";
	}

	private String createReferenceList() throws Exception
	{

		if(p_Referenced_Table_ID == 0)
			return Msg.getMsg(getCtx(), "NotFound") +" : " + Msg.getElement(getCtx(), "AD_Table_ID");

		MTable m_Table = MTable.get(getCtx(), p_Referenced_Table_ID);
		if(m_Table.get_ID() == 0)
			return Msg.getMsg(getCtx(), "NotFound") +" : " + Msg.getElement(getCtx(), "AD_Table_ID");

		//Can Refer Table Direct
		String getTableSQL = "SELECT UPPER(TableName) FROM AD_Table t INNER JOIN AD_Column c ON(t.AD_Table_ID = C.AD_Table_ID)"
				+ " WHERE IsView='N' and UPPER(ColumnName)=?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(getTableSQL, get_TrxName());
			pstmt.setString(1, (m_Table.getTableName()+"_ID").toUpperCase());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				if(rs.getString(1).equals(m_Table.getTableName().toUpperCase()))
					continue;


				MTable table = MTable.get(getCtx(), rs.getString(1));
				if(table.get_ID()==0)
					continue;

				MDeleteClientLogJP log = new MDeleteClientLogJP(getCtx(), 0, null);
				log.setAD_PInstance_ID(AD_PInstance_ID);
//				log.setJP_Delete_Client(m_DeleteProfile.getJP_Delete_Client());
				log.setJP_DeleteProfile_ID(p_JP_DeleteProfile_ID);

				//Description
				boolean isContain = false;
				for(int i = 0; i < JPiereDeleteClientRecords.TrxTables.length; i++)
				{
					if(JPiereDeleteClientRecords.TrxTables[i].toUpperCase().equals(table.getTableName().toUpperCase()))
					{
						isContain = true;
						break;
					}
				}

				if(isContain)
				{
					log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_TrxTable"));
				}else{
					for(int i = 0; i < JPiereDeleteClientRecords.IniTables.length; i++)
					{
						if(JPiereDeleteClientRecords.IniTables[i].toUpperCase().equals(table.getTableName().toUpperCase()))
						{
							isContain = true;
							break;
						}
					}

					if(isContain)
					{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_IniTable"));
					}else{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_OtherTable"));
					}
				}

				log.setAD_Table_ID(table.get_ID());
				log.setTableName(table.getTableName());
				log.setAccessLevel(table.getAccessLevel());
				log.setAD_Window_ID(table.getAD_Window_ID());
				log.setReferenced_Table_ID(p_Referenced_Table_ID);

				MColumn[] columns = table.getColumns(false);
				MColumn col = null;
				for(int i = 0 ; i < columns.length; i++)
				{
					if(columns[i].getColumnName().toUpperCase().equals((m_Table.getTableName()+"_ID").toUpperCase()))
					{
						col = MColumn.get(getCtx(), columns[i].get_ID());
						break;
					}
				}

				if(col != null)
				{
					log.setAD_Column_ID(col.get_ID());
					log.setColumnName(col.getColumnName());
					log.setAD_Reference_ID(col.getAD_Reference_ID());
					log.setAD_Reference_Value_ID(col.getAD_Reference_Value_ID());
					log.setAD_Val_Rule_ID(col.getAD_Val_Rule_ID());
					log.setFKConstraintName(col.getFKConstraintName());
					log.setFKConstraintType(col.getFKConstraintType());
					log.setIsKey(col.isKey());
					log.setIsParent(col.isParent());
					log.setIsMandatory(col.isMandatory());
					log.setDefaultValue(col.getDefaultValue());
					log.setMandatoryLogic(col.getMandatoryLogic());
					log.setColumnSQL(col.getColumnSQL());
				}

				log.saveEx();

			}//while

		}catch (SQLException e){
			log.log(Level.SEVERE, getTableSQL.toString(), e);
			throw new DBException(e, getTableSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}


		//Can't Refer Table Direct
		ArrayList<Integer> list_of_reference = deleteClientRecordsProcess.getReferenceList(m_Table.getTableName());
		if(list_of_reference.size()!=0)
		{
			ArrayList<TableColumn> tableColumnList= deleteClientRecordsProcess.getIndirectReferTableColumn(m_Table.getTableName() +"_ID", list_of_reference);
			for(TableColumn tableColumn :tableColumnList)
			{
				MTable table = MTable.get(getCtx(), tableColumn.tableName);
				if(table.get_ID()==0)
					continue;

				MDeleteClientLogJP log = new MDeleteClientLogJP(getCtx(), 0, null);
				log.setAD_PInstance_ID(getAD_PInstance_ID());
//				log.setJP_Delete_Client(m_DeleteProfile.getJP_Delete_Client());
				log.setJP_DeleteProfile_ID(p_JP_DeleteProfile_ID);

				//Description
				boolean isContain = false;
				for(int i = 0; i < JPiereDeleteClientRecords.TrxTables.length; i++)
				{
					if(JPiereDeleteClientRecords.TrxTables[i].toUpperCase().equals(table.getTableName().toUpperCase()))
					{
						isContain = true;
						break;
					}
				}

				if(isContain)
				{
					log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_TrxTable"));
				}else{
					for(int i = 0; i < JPiereDeleteClientRecords.IniTables.length; i++)
					{
						if(JPiereDeleteClientRecords.IniTables[i].toUpperCase().equals(table.getTableName().toUpperCase()))
						{
							isContain = true;
							break;
						}
					}

					if(isContain)
					{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_IniTable"));
					}else{
						log.setDescription(Msg.getMsg(getCtx(), "JP_Delete_OtherTable"));
					}
				}

				log.setAD_Table_ID(table.get_ID());
				log.setTableName(table.getTableName());
				log.setAccessLevel(table.getAccessLevel());
				log.setAD_Window_ID(table.getAD_Window_ID());
				log.setReferenced_Table_ID(p_Referenced_Table_ID);

				MColumn[] columns = table.getColumns(false);
				MColumn col = null;
				for(int i = 0 ; i < columns.length; i++)
				{
					if(columns[i].getColumnName().toUpperCase().equals(tableColumn.columnName.toUpperCase()))
					{
						col = MColumn.get(getCtx(), columns[i].get_ID());
						break;
					}
				}

				if(col != null)
				{
					log.setAD_Column_ID(col.get_ID());
					log.setColumnName(col.getColumnName());
					log.setAD_Reference_ID(col.getAD_Reference_ID());
					log.setAD_Reference_Value_ID(col.getAD_Reference_Value_ID());
					log.setAD_Val_Rule_ID(col.getAD_Val_Rule_ID());
					log.setFKConstraintName(col.getFKConstraintName());
					log.setFKConstraintType(col.getFKConstraintType());
					log.setIsKey(col.isKey());
					log.setIsParent(col.isParent());
					log.setIsMandatory(col.isMandatory());
					log.setDefaultValue(col.getDefaultValue());
					log.setMandatoryLogic(col.getMandatoryLogic());
					log.setColumnSQL(col.getColumnSQL());
				}

				log.saveEx();
			}
		}//if(list_of_reference.size()!=0)

		return "";
	}

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

}
