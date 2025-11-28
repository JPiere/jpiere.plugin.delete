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
import java.util.Properties;

import org.compiere.model.MRefTable;
import org.compiere.model.MTable;
import org.compiere.util.DisplayType;
import org.compiere.util.Msg;

/**
 * 	Delete Profile Line.
 *
 *  @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 *
 */
public class MCustomDeleteProfileLine extends X_JP_CustomDeleteProfileLine {

	private static final long serialVersionUID = 2540907339534081600L;
	
	private MCustomDeleteProfile m_Parent;

	public MCustomDeleteProfileLine(Properties ctx, int JP_CustomDeleteProfileLine_ID, String trxName) {
		super(ctx, JP_CustomDeleteProfileLine_ID, trxName);
	}
	
	public MCustomDeleteProfileLine(Properties ctx, String JP_CustomDeleteProfileLine_UU, String trxName) {
		super(ctx, JP_CustomDeleteProfileLine_UU, trxName);
	}

	public MCustomDeleteProfileLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	@Override
	protected boolean beforeSave(boolean newRecord)
	{
		if(newRecord || is_ValueChanged("AD_Table_ID") || is_ValueChanged("AD_Column_ID"))
		{
			MTable table = MTable.get(getCtx(), getAD_Table_ID());
			if(table.getAccessLevel().equals(String.valueOf(MTable.ACCESSLEVEL_SYSTEM)))
			{
				if(MDeleteProfile.JP_DELETE_CLIENT_CustomDelete.equals(getParent().getParent().getJP_Delete_Client()))
				{
					if(table.columnExistsInDictionary("EntityType"))
					{
						//Tables of Application dictionary can not delete.
						log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_ADTable"));
						return false;
					}
					
				}else{
					
					//Tables of System Only can not Delete
					log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_SystemTable"));
					return false;
				}
			}

			if(!DisplayType.isID(getAD_Column().getAD_Reference_ID()))
			{
				//Irrelevant Column. Not Referenced Table.
				log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotReferencedTable"));
				return false;
			}else{

				//Can not check.
				if(getAD_Column().getAD_Reference_ID() == DisplayType.Account
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Assignment
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Chart
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Color
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Image
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Location
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Locator
						|| getAD_Column().getAD_Reference_ID() == DisplayType.Image
						)
				{
					return true;
				}

			}

			if(getAD_Table_ID() == getParent().getAD_Table_ID()) //Same Table
			{
				//Same Table and Same Column.
				if(getAD_Column().getColumnName().equalsIgnoreCase((getParent().getAD_Table().getTableName()+"_ID")))
				{
					//Table and Column are the same as Referenced Table
					log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_SameTableColumn"));
					return false;
				}

				//Same Table and Difference Column.
				MRefTable refTable = new MRefTable(getCtx(), getAD_Column().getAD_Reference_Value_ID(), null);
				if(refTable == null || refTable.get_ID()== 0)
				{
					//Irrelevant Column. Not Referenced Table.
					log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotReferencedTable"));
					return false;

				}else if(refTable.getAD_Table_ID() != getParent().getAD_Table_ID()){//Not Same Table
					//Irrelevant Column. Not Referenced Table.
					log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotReferencedTable"));
					return false;
				}

			}else{

				//Difference Table and Same PK & FK Column Name
				if(getAD_Column().getColumnName().equalsIgnoreCase((getParent().getAD_Table().getTableName()+"_ID")))
				{
					;//Nothing to do;
				}else{ //Deference Table and Deference Column

					MRefTable refTable = new MRefTable(getCtx(), getAD_Column().getAD_Reference_Value_ID(), null);
					if(refTable == null || refTable.get_ID()== 0)
					{
						//Irrelevant Column. Not Referenced Table.
						log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotReferencedTable"));
						return false;

					}else if(refTable.getAD_Table_ID() != getParent().getAD_Table_ID()){//Not Same Table
						//Irrelevant Column. Not Referenced Table.
						log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotReferencedTable"));
						return false;
					}
				}
			}


		}//if(newRecord || is_ValueChanged("AD_Table_ID") || is_ValueChanged("AD_Column_ID"))

		return true;
	}

	public MCustomDeleteProfile getParent()
	{
		if(m_Parent==null)
			m_Parent = new MCustomDeleteProfile(getCtx(),getJP_CustomDeleteProfile_ID(),null);

		return m_Parent;
	}
}
