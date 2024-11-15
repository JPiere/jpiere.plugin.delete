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
package jpiere.plugin.delete.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.util.Msg;
import org.compiere.util.Util;

public class MCustomDeleteProfile extends X_JP_CustomDeleteProfile {

	private static final long serialVersionUID = 4596021515164935039L;

	private MDeleteProfile m_Parent;

	private MCustomDeleteProfileLine[] 	m_lines = null;

	public MCustomDeleteProfile(Properties ctx, int JP_CustomDeleteProfile_ID,String trxName) {
		super(ctx, JP_CustomDeleteProfile_ID, trxName);
	}

	public MCustomDeleteProfile(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	@Override
	protected boolean beforeSave(boolean newRecord)
	{
		if(newRecord || is_ValueChanged("AD_Table_ID"))
		{
			MTable table = MTable.get(getCtx(), getAD_Table_ID());
			if(table.getAccessLevel().equals(String.valueOf(MTable.ACCESSLEVEL_SYSTEM)))
			{
				//Tables of System Only can not Delete
				log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_SystemTable"));
				return false;
			}

			MColumn[] columns = table.getColumns(false);
			boolean isContain= false;
			for(int i = 0; i < columns.length; i++)
			{
				if(columns[i].getColumnName().equals(table.getTableName()+"_ID"))
				{
					isContain =true;
					break;
				}
			}

			if(!isContain)
			{
				//The Table don't have a Primary Key of TableName_ID
				log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotHavePK"));
				return false;
			}
		}

		return true;
	}

	public MCustomDeleteProfileLine[] getCustomDeleteProfileLines (String whereClause, String orderClause)
	{
		StringBuilder whereClauseFinal = new StringBuilder("IsActive='Y' AND "+MCustomDeleteProfileLine.COLUMNNAME_JP_CustomDeleteProfile_ID+"=? ");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = MCustomDeleteProfileLine.COLUMNNAME_SeqNo;
		//
		List<MCustomDeleteProfileLine> list = new Query(getCtx(), MCustomDeleteProfileLine.Table_Name, whereClauseFinal.toString(), get_TrxName())
										.setParameters(get_ID())
										.setOrderBy(orderClause)
										.list();

		return list.toArray(new MCustomDeleteProfileLine[list.size()]);
	}	//	getLines


	public MCustomDeleteProfileLine[] getCustomDeleteProfileLines (boolean requery, String orderBy)
	{
		if (m_lines != null && !requery) {
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		//
		String orderClause = "";
		if (orderBy != null && orderBy.length() > 0)
			orderClause += orderBy;
		else
			orderClause += "SeqNo";
		m_lines = getCustomDeleteProfileLines(null, orderClause);
		return m_lines;
	}	//	getLines


	public MCustomDeleteProfileLine[] getCustomDeleteProfileLines()
	{
		return getCustomDeleteProfileLines(true, null);
	}	//	getLines

	public MDeleteProfile getParent()
	{
		if(m_Parent==null)
			m_Parent = new MDeleteProfile(getCtx(),getJP_DeleteProfile_ID(),null);

		return m_Parent;
	}
}
