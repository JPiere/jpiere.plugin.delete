/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package jpiere.plugin.delete.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for JP_DeleteProfile
 *  @author iDempiere (generated)
 *  @version Release 12 - $Id$ */
@org.adempiere.base.Model(table="JP_DeleteProfile")
public class X_JP_DeleteProfile extends PO implements I_JP_DeleteProfile, I_Persistent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20251128L;

    /** Standard Constructor */
    public X_JP_DeleteProfile (Properties ctx, int JP_DeleteProfile_ID, String trxName)
    {
      super (ctx, JP_DeleteProfile_ID, trxName);
      /** if (JP_DeleteProfile_ID == 0)
        {
			setJP_DeleteProfile_ID (0);
			setJP_Delete_Client (null);
// TC
			setName (null);
			setValue (null);
        } */
    }

    /** Standard Constructor */
    public X_JP_DeleteProfile (Properties ctx, int JP_DeleteProfile_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, JP_DeleteProfile_ID, trxName, virtualColumns);
      /** if (JP_DeleteProfile_ID == 0)
        {
			setJP_DeleteProfile_ID (0);
			setJP_Delete_Client (null);
// TC
			setName (null);
			setValue (null);
        } */
    }

    /** Standard Constructor */
    public X_JP_DeleteProfile (Properties ctx, String JP_DeleteProfile_UU, String trxName)
    {
      super (ctx, JP_DeleteProfile_UU, trxName);
      /** if (JP_DeleteProfile_UU == null)
        {
			setJP_DeleteProfile_ID (0);
			setJP_Delete_Client (null);
// TC
			setName (null);
			setValue (null);
        } */
    }

    /** Standard Constructor */
    public X_JP_DeleteProfile (Properties ctx, String JP_DeleteProfile_UU, String trxName, String ... virtualColumns)
    {
      super (ctx, JP_DeleteProfile_UU, trxName, virtualColumns);
      /** if (JP_DeleteProfile_UU == null)
        {
			setJP_DeleteProfile_ID (0);
			setJP_Delete_Client (null);
// TC
			setName (null);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_JP_DeleteProfile (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_JP_DeleteProfile[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set AD_Org_ID Where.
		@param JP_AD_Org_ID_Where AD_Org_ID Where
	*/
	public void setJP_AD_Org_ID_Where (String JP_AD_Org_ID_Where)
	{
		set_Value (COLUMNNAME_JP_AD_Org_ID_Where, JP_AD_Org_ID_Where);
	}

	/** Get AD_Org_ID Where.
		@return AD_Org_ID Where	  */
	public String getJP_AD_Org_ID_Where()
	{
		return (String)get_Value(COLUMNNAME_JP_AD_Org_ID_Where);
	}

	/** Set AD_Role_ID Where.
		@param JP_AD_Role_ID_Where AD_Role_ID Where
	*/
	public void setJP_AD_Role_ID_Where (String JP_AD_Role_ID_Where)
	{
		set_Value (COLUMNNAME_JP_AD_Role_ID_Where, JP_AD_Role_ID_Where);
	}

	/** Get AD_Role_ID Where.
		@return AD_Role_ID Where	  */
	public String getJP_AD_Role_ID_Where()
	{
		return (String)get_Value(COLUMNNAME_JP_AD_Role_ID_Where);
	}

	/** Set AD_User_ID Where.
		@param JP_AD_User_ID_Where AD_User_ID Where
	*/
	public void setJP_AD_User_ID_Where (String JP_AD_User_ID_Where)
	{
		set_Value (COLUMNNAME_JP_AD_User_ID_Where, JP_AD_User_ID_Where);
	}

	/** Get AD_User_ID Where.
		@return AD_User_ID Where	  */
	public String getJP_AD_User_ID_Where()
	{
		return (String)get_Value(COLUMNNAME_JP_AD_User_ID_Where);
	}

	/** Set C_BPartner_ID Where.
		@param JP_C_BPartner_ID_Where C_BPartner_ID Where
	*/
	public void setJP_C_BPartner_ID_Where (String JP_C_BPartner_ID_Where)
	{
		set_Value (COLUMNNAME_JP_C_BPartner_ID_Where, JP_C_BPartner_ID_Where);
	}

	/** Get C_BPartner_ID Where.
		@return C_BPartner_ID Where	  */
	public String getJP_C_BPartner_ID_Where()
	{
		return (String)get_Value(COLUMNNAME_JP_C_BPartner_ID_Where);
	}

	/** Set Delete Profile.
		@param JP_DeleteProfile_ID Delete Profile
	*/
	public void setJP_DeleteProfile_ID (int JP_DeleteProfile_ID)
	{
		if (JP_DeleteProfile_ID < 1)
			set_ValueNoCheck (COLUMNNAME_JP_DeleteProfile_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_JP_DeleteProfile_ID, Integer.valueOf(JP_DeleteProfile_ID));
	}

	/** Get Delete Profile.
		@return Delete Profile	  */
	public int getJP_DeleteProfile_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_DeleteProfile_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set JP_DeleteProfile_UU.
		@param JP_DeleteProfile_UU JP_DeleteProfile_UU
	*/
	public void setJP_DeleteProfile_UU (String JP_DeleteProfile_UU)
	{
		set_ValueNoCheck (COLUMNNAME_JP_DeleteProfile_UU, JP_DeleteProfile_UU);
	}

	/** Get JP_DeleteProfile_UU.
		@return JP_DeleteProfile_UU	  */
	public String getJP_DeleteProfile_UU()
	{
		return (String)get_Value(COLUMNNAME_JP_DeleteProfile_UU);
	}

	/** Custom Delete = CD */
	public static final String JP_DELETE_CLIENT_CustomDelete = "CD";
	/** Delete a Client = DC */
	public static final String JP_DELETE_CLIENT_DeleteAClient = "DC";
	/** Initialize a Client = IC */
	public static final String JP_DELETE_CLIENT_InitializeAClient = "IC";
	/** Delete All Clients Transactions = TA */
	public static final String JP_DELETE_CLIENT_DeleteAllClientsTransactions = "TA";
	/** Delete a Client Transactions = TC */
	public static final String JP_DELETE_CLIENT_DeleteAClientTransactions = "TC";
	/** Set Delete or Initialize Tenant.
		@param JP_Delete_Client Delete or Initialize Tenant
	*/
	public void setJP_Delete_Client (String JP_Delete_Client)
	{

		set_Value (COLUMNNAME_JP_Delete_Client, JP_Delete_Client);
	}

	/** Get Delete or Initialize Tenant.
		@return Delete or Initialize Tenant	  */
	public String getJP_Delete_Client()
	{
		return (String)get_Value(COLUMNNAME_JP_Delete_Client);
	}

	/** Set M_Product_ID Where.
		@param JP_M_Product_ID_Where M_Product_ID Where
	*/
	public void setJP_M_Product_ID_Where (String JP_M_Product_ID_Where)
	{
		set_Value (COLUMNNAME_JP_M_Product_ID_Where, JP_M_Product_ID_Where);
	}

	/** Get M_Product_ID Where.
		@return M_Product_ID Where	  */
	public String getJP_M_Product_ID_Where()
	{
		return (String)get_Value(COLUMNNAME_JP_M_Product_ID_Where);
	}

	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	/** Set Name 2.
		@param Name2 Additional Name
	*/
	public void setName2 (String Name2)
	{
		set_Value (COLUMNNAME_Name2, Name2);
	}

	/** Get Name 2.
		@return Additional Name
	  */
	public String getName2()
	{
		return (String)get_Value(COLUMNNAME_Name2);
	}

	/** Set Search Key.
		@param Value Search key for the record in the format required - must be unique
	*/
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue()
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}