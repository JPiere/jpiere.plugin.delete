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

package jpiere.plugin.delete.factory;

import java.sql.ResultSet;

import jpiere.plugin.delete.model.MCustomDeleteProfile;
import jpiere.plugin.delete.model.MCustomDeleteProfileLine;
import jpiere.plugin.delete.model.MDeleteClientLogJP;
import jpiere.plugin.delete.model.MDeleteProfile;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;


/**
 *  JPiere Plugins(JPPS) Delete Model Factory
 *
 *  @author Hideaki Hagiwara（h.hagiwara@oss-erp.co.jp）
 *
 */
public class JPiereDeleteModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName)
	{

		if(tableName.startsWith("JP"))
		{
			if(tableName.equals(MDeleteProfile.Table_Name)){
				return MDeleteProfile.class;
			}else if(tableName.equals(MCustomDeleteProfile.Table_Name)){
				return MCustomDeleteProfile.class;
			}else if(tableName.equals(MCustomDeleteProfileLine.Table_Name)){
				return MCustomDeleteProfileLine.class;
			}else if(tableName.equals(MDeleteClientLogJP.Table_Name)){
				return MDeleteClientLogJP.class;
			}
		}
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName)
	{

		if(tableName.startsWith("JP"))
		{
			if(tableName.equals(MDeleteProfile.Table_Name)){
				return  new MDeleteProfile(Env.getCtx(), Record_ID, trxName);
			}else if(tableName.equals(MCustomDeleteProfile.Table_Name)){
				return new MCustomDeleteProfile(Env.getCtx(), Record_ID, trxName);
			}else if(tableName.equals(MCustomDeleteProfileLine.Table_Name)){
				return new MCustomDeleteProfileLine(Env.getCtx(), Record_ID, trxName);
			}else if(tableName.equals(MDeleteClientLogJP.Table_Name)){
				return new MDeleteClientLogJP(Env.getCtx(), Record_ID, trxName);
			}
		}
		return null;
	}
	
	
	@Override
	public PO getPO(String tableName, String Record_UU, String trxName)
	{

		if(tableName.startsWith("JP"))
		{
			if(tableName.equals(MDeleteProfile.Table_Name)){
				return  new MDeleteProfile(Env.getCtx(), Record_UU, trxName);
			}else if(tableName.equals(MCustomDeleteProfile.Table_Name)){
				return new MCustomDeleteProfile(Env.getCtx(), Record_UU, trxName);
			}else if(tableName.equals(MCustomDeleteProfileLine.Table_Name)){
				return new MCustomDeleteProfileLine(Env.getCtx(), Record_UU, trxName);
			}else if(tableName.equals(MDeleteClientLogJP.Table_Name)){
				return new MDeleteClientLogJP(Env.getCtx(), Record_UU, trxName);
			}
		}
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName)
	{

		if(tableName.startsWith("JP"))
		{
			if(tableName.equals(MDeleteProfile.Table_Name)){
				return  new MDeleteProfile(Env.getCtx(), rs, trxName);
			}else if(tableName.equals(MCustomDeleteProfile.Table_Name)){
				return new MCustomDeleteProfile(Env.getCtx(), rs, trxName);
			}else if(tableName.equals(MCustomDeleteProfileLine.Table_Name)){
				return new MCustomDeleteProfileLine(Env.getCtx(), rs, trxName);
			}else if(tableName.equals(MDeleteClientLogJP.Table_Name)){
				return new MDeleteClientLogJP(Env.getCtx(), rs, trxName);
			}
		}
		return null;
	}

}
