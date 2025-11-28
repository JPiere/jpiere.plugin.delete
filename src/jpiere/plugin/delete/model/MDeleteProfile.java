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

import org.compiere.model.Query;
import org.compiere.util.Util;

/**
 * 	Delete Profile.
 *
 *  @author Hideaki Hagiwara(h.hagiwara@oss-erp.co.jp)
 *
 */
public class MDeleteProfile extends X_JP_DeleteProfile {

	private static final long serialVersionUID = 2402834363374900854L;
	
	protected MCustomDeleteProfile[] 	m_CustomDeleteProfiles = null;

	public MDeleteProfile(Properties ctx, int JP_DeleteProfile_ID, String trxName)
	{
		super(ctx, JP_DeleteProfile_ID, trxName);
	}

	public MDeleteProfile(Properties ctx, String JP_DeleteProfile_UU, String trxName)
	{
		super(ctx, JP_DeleteProfile_UU, trxName);
	}
	
	public MDeleteProfile(Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}

	public MCustomDeleteProfile[] getCustomDeleteProfiles (String whereClause, String orderClause)
	{
		StringBuilder whereClauseFinal = new StringBuilder("IsActive='Y' AND " + MCustomDeleteProfile.COLUMNNAME_JP_DeleteProfile_ID+"=? ");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = MCustomDeleteProfile.COLUMNNAME_SeqNo;
		//
		List<MCustomDeleteProfile> list = new Query(getCtx(), MCustomDeleteProfile.Table_Name, whereClauseFinal.toString(), get_TrxName())
										.setParameters(get_ID())
										.setOrderBy(orderClause)
										.list();

		return list.toArray(new MCustomDeleteProfile[list.size()]);
	}	//


	public MCustomDeleteProfile[] getCustomDeleteProfiles (boolean requery, String orderBy)
	{
		if (m_CustomDeleteProfiles != null && !requery) {
			set_TrxName(m_CustomDeleteProfiles, get_TrxName());
			return m_CustomDeleteProfiles;
		}
		//
		String orderClause = "";
		if (orderBy != null && orderBy.length() > 0)
			orderClause += orderBy;
		else
			orderClause += "SeqNo";
		m_CustomDeleteProfiles = getCustomDeleteProfiles(null, orderClause);
		return m_CustomDeleteProfiles;
	}	//


	public MCustomDeleteProfile[] getCustomDeleteProfiles()
	{
		return getCustomDeleteProfiles(true, null);
	}	//


}
