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
package jpiere.plugin.delete.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for JP_DeleteProfile
 *  @author iDempiere (generated) 
 *  @version Release 12
 */
@SuppressWarnings("all")
public interface I_JP_DeleteProfile 
{

    /** TableName=JP_DeleteProfile */
    public static final String Table_Name = "JP_DeleteProfile";

    /** AD_Table_ID=1000060 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 4 - System 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(4);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Tenant.
	  * Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within tenant
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within tenant
	  */
	public int getAD_Org_ID();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name JP_AD_Org_ID_Where */
    public static final String COLUMNNAME_JP_AD_Org_ID_Where = "JP_AD_Org_ID_Where";

	/** Set AD_Org_ID Where	  */
	public void setJP_AD_Org_ID_Where (String JP_AD_Org_ID_Where);

	/** Get AD_Org_ID Where	  */
	public String getJP_AD_Org_ID_Where();

    /** Column name JP_AD_Role_ID_Where */
    public static final String COLUMNNAME_JP_AD_Role_ID_Where = "JP_AD_Role_ID_Where";

	/** Set AD_Role_ID Where	  */
	public void setJP_AD_Role_ID_Where (String JP_AD_Role_ID_Where);

	/** Get AD_Role_ID Where	  */
	public String getJP_AD_Role_ID_Where();

    /** Column name JP_AD_User_ID_Where */
    public static final String COLUMNNAME_JP_AD_User_ID_Where = "JP_AD_User_ID_Where";

	/** Set AD_User_ID Where	  */
	public void setJP_AD_User_ID_Where (String JP_AD_User_ID_Where);

	/** Get AD_User_ID Where	  */
	public String getJP_AD_User_ID_Where();

    /** Column name JP_C_BPartner_ID_Where */
    public static final String COLUMNNAME_JP_C_BPartner_ID_Where = "JP_C_BPartner_ID_Where";

	/** Set C_BPartner_ID Where	  */
	public void setJP_C_BPartner_ID_Where (String JP_C_BPartner_ID_Where);

	/** Get C_BPartner_ID Where	  */
	public String getJP_C_BPartner_ID_Where();

    /** Column name JP_DeleteProfile_ID */
    public static final String COLUMNNAME_JP_DeleteProfile_ID = "JP_DeleteProfile_ID";

	/** Set Delete Profile	  */
	public void setJP_DeleteProfile_ID (int JP_DeleteProfile_ID);

	/** Get Delete Profile	  */
	public int getJP_DeleteProfile_ID();

    /** Column name JP_DeleteProfile_UU */
    public static final String COLUMNNAME_JP_DeleteProfile_UU = "JP_DeleteProfile_UU";

	/** Set JP_DeleteProfile_UU	  */
	public void setJP_DeleteProfile_UU (String JP_DeleteProfile_UU);

	/** Get JP_DeleteProfile_UU	  */
	public String getJP_DeleteProfile_UU();

    /** Column name JP_Delete_Client */
    public static final String COLUMNNAME_JP_Delete_Client = "JP_Delete_Client";

	/** Set Delete or Initialize Tenant	  */
	public void setJP_Delete_Client (String JP_Delete_Client);

	/** Get Delete or Initialize Tenant	  */
	public String getJP_Delete_Client();

    /** Column name JP_M_Product_ID_Where */
    public static final String COLUMNNAME_JP_M_Product_ID_Where = "JP_M_Product_ID_Where";

	/** Set M_Product_ID Where	  */
	public void setJP_M_Product_ID_Where (String JP_M_Product_ID_Where);

	/** Get M_Product_ID Where	  */
	public String getJP_M_Product_ID_Where();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name Name2 */
    public static final String COLUMNNAME_Name2 = "Name2";

	/** Set Name 2.
	  * Additional Name
	  */
	public void setName2 (String Name2);

	/** Get Name 2.
	  * Additional Name
	  */
	public String getName2();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();
}
