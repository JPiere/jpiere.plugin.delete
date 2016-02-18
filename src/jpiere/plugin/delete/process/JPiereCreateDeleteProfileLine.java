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
import jpiere.plugin.delete.process.JPiereDeleteClientRecords.TableColumn;

import org.adempiere.exceptions.DBException;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Msg;

/**
 * 	Create Delete Profile Line.
 *
 *  @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 *
 */
public class JPiereCreateDeleteProfileLine extends SvrProcess
{

	//Parameters
	private int p_JP_CustomDeleteProfile_ID = 0;

	//
	private JPiereDeleteClientRecords deleteClientRecordsProcess;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{

		p_JP_CustomDeleteProfile_ID = getRecord_ID();
		deleteClientRecordsProcess = new JPiereDeleteClientRecords();

	}	//	prepare


	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt() throws Exception
	{

		MCustomDeleteProfile customDeleteProfile = new MCustomDeleteProfile(getCtx(), p_JP_CustomDeleteProfile_ID, null);
		String treat = customDeleteProfile.getJP_TreatForeignKey();

		if(customDeleteProfile.getCustomDeleteProfileLines().length != 0)
		{
			String msg = Msg.getElement(getCtx(), "JP_Delete_AlreadySomeLines");//Error : There are some lines already
			throw new Exception(msg);
		}

		MTable parent_Table = MTable.get(getCtx(), customDeleteProfile.getAD_Table().getTableName());
		if(parent_Table.get_ID()==0)
			return Msg.getMsg(getCtx(), "NotFound") +" : " + Msg.getElement(getCtx(), "AD_Table_ID");

		//Can Refer Table Direct
		String getTableSQL = "SELECT UPPER(TableName) FROM AD_Table t INNER JOIN AD_Column c ON(t.AD_Table_ID = C.AD_Table_ID)"
				+ " WHERE IsView='N' and UPPER(ColumnName)=?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int seqNo = 0;
		try
		{
			pstmt = DB.prepareStatement(getTableSQL, get_TrxName());
			pstmt.setString(1, (parent_Table.getTableName()+"_ID").toUpperCase());
			rs = pstmt.executeQuery();
			while (rs.next ())
			{
				if(rs.getString(1).equals(parent_Table.getTableName().toUpperCase()))//Same Table and Same Column
					continue;

				MTable table = MTable.get(getCtx(), rs.getString(1));
				if(table.get_ID()==0)
					continue;

				if(table.getAccessLevel().equals(String.valueOf(MTable.ACCESSLEVEL_SYSTEM)))//System Table
					continue;

//				if(customDeleteProfile.getAD_Table_ID() == table.getAD_Table_ID())//Same Table and Same Column
//					continue;

				MColumn[] columns = table.getColumns(false);
				MColumn col = null;
				for(int i = 0 ; i < columns.length; i++)
				{
					if(columns[i].getColumnName().toUpperCase().equals((parent_Table.getTableName()+"_ID").toUpperCase()))
					{
						col = MColumn.get(getCtx(), columns[i].get_ID());
						break;
					}
				}

				if(col == null)
					continue;

				MCustomDeleteProfileLine cdpl = new MCustomDeleteProfileLine(getCtx(),0,null);
				cdpl.setJP_CustomDeleteProfile_ID(customDeleteProfile.get_ID());

				seqNo = seqNo + 10;
				cdpl.setSeqNo(seqNo);
				cdpl.setAD_Table_ID(table.get_ID());
				cdpl.setAD_Column_ID(col.getAD_Column_ID());
				cdpl.setJP_TreatForeignKey(deleteClientRecordsProcess.treatAutoJudge(table.getTableName(), col.getColumnName()
														, treat, customDeleteProfile.getJP_ForeignKey_Value()));
				cdpl.setJP_ForeignKey_Value(customDeleteProfile.getJP_ForeignKey_Value());

				cdpl.saveEx();

			}//while

		}catch (SQLException e){
			log.log(Level.SEVERE, getTableSQL.toString(), e);
			throw new DBException(e, getTableSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}


		//Can't Refer Table Direct
		ArrayList<Integer> list_of_reference = deleteClientRecordsProcess.getReferenceList(parent_Table.getTableName());
		if(list_of_reference.size()!=0)
		{
			ArrayList<TableColumn> tableColumnList= deleteClientRecordsProcess.getIndirectReferTableColumn(parent_Table.getTableName() +"_ID", list_of_reference);
			for(TableColumn tableColumn :tableColumnList)
			{
				MTable table = MTable.get(getCtx(), tableColumn.tableName);
				if(table.get_ID()==0)
					continue;

				if(table.getAccessLevel().equals(String.valueOf(MTable.ACCESSLEVEL_SYSTEM)))//System Table
				{
					continue;
				}

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

				if(col == null)
					continue;

				MCustomDeleteProfileLine cdpl = new MCustomDeleteProfileLine(getCtx(),0,null);
				cdpl.setJP_CustomDeleteProfile_ID(customDeleteProfile.get_ID());

				seqNo = seqNo + 10;
				cdpl.setSeqNo(seqNo);
				cdpl.setAD_Table_ID(table.get_ID());
				cdpl.setAD_Column_ID(col.getAD_Column_ID());
				cdpl.setJP_TreatForeignKey(deleteClientRecordsProcess.treatAutoJudge(table.getTableName(), col.getColumnName()
														, treat, customDeleteProfile.getJP_ForeignKey_Value()));
				cdpl.setJP_ForeignKey_Value(customDeleteProfile.getJP_ForeignKey_Value());

				cdpl.saveEx();
			}

		}//if(list_of_reference.size()!=0)

		return "";
	}


}
