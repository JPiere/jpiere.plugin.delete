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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.table.TableColumn;

import org.compiere.model.MRefTable;
import org.compiere.model.MTable;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Msg;

public class MCustomDeleteProfileLine extends X_JP_CustomDeleteProfileLine {

	private MCustomDeleteProfile m_Parent;

	public MCustomDeleteProfileLine(Properties ctx, int JP_CustomDeleteProfileLine_ID, String trxName) {
		super(ctx, JP_CustomDeleteProfileLine_ID, trxName);
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
				//Tables of System Only can not Delete
				log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_SystemTable"));
				return false;
			}

			if(getAD_Table_ID() == getParent().getAD_Table_ID()) //Same Table
			{
				//Same Table and Same Column.
				if(getAD_Column().getColumnName().toUpperCase().equals((getParent().getAD_Table().getTableName()+"_ID").toUpperCase()))
				{
					//Table and Column are the same as Referenced Table
					log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_SameTableColumn"));
					return false;
				}

				//Same Table and Difference Column.
				if(DisplayType.isID(getAD_Column().getAD_Reference_ID()))
				{
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
					//Irrelevant Column. Not Referenced Table.
					log.saveError("Error", Msg.getMsg(getCtx(), "JP_Delete_NotReferencedTable"));
					return false;
				}

			}else{

				//Difference Table and Same PK & FK Column Name
				if(getAD_Column().getColumnName().toUpperCase().equals((getParent().getAD_Table().getTableName()+"_ID").toUpperCase()))
				{
					;//Nothing to do;
				}else{ //Deference Table and Deference Column

					if(DisplayType.isID(getAD_Column().getAD_Reference_ID()))
					{
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

	private TableColumn getRefTableColumn(Properties ctx, int Column_ID)
	{

		String ColumnName = "";
		int AD_Reference_Value_ID = 0;
		boolean IsParent = false;
		String ValidationCode = "";
		//
		String sql = "SELECT rt.TableName, rt.ColumnName"
			+ "FROM AD_Column c INNER JOIN AD_Reference rf ON(c.AD_Reference_Value_ID = rf.AD_Reference_ID) "
			+ " INNER JOIN AD_Ref_Table rt ON (rf.AD_Reference_ID = rt.AD_Reference_ID) "
			+ " LEFT OUTER JOIN AD_Val_Rule vr ON (c.AD_Val_Rule_ID=vr.AD_Val_Rule_ID) "
			+ "WHERE c.AD_Column_ID=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, Column_ID);
			//
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				ColumnName = rs.getString(1);
				AD_Reference_Value_ID = rs.getInt(2);
				IsParent = "Y".equals(rs.getString(3));
				ValidationCode = rs.getString(4);
			}
//			else
//				s_log.log(Level.SEVERE, "Column Not Found - AD_Column_ID=" + Column_ID);
		}
		catch (SQLException ex)
		{
//			s_log.log(Level.SEVERE, "create", ex);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		//


		return null;
	}


}
