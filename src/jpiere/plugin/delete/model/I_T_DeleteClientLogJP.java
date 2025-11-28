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

/** Generated Interface for T_DeleteClientLogJP
 *  @author iDempiere (generated) 
 *  @version Release 12
 */
@SuppressWarnings("all")
public interface I_T_DeleteClientLogJP 
{

    /** TableName=T_DeleteClientLogJP */
    public static final String Table_Name = "T_DeleteClientLogJP";

    /** AD_Table_ID=1000063 */
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

    /** Column name AD_Column_ID */
    public static final String COLUMNNAME_AD_Column_ID = "AD_Column_ID";

	/** Set Column.
	  * Column in the table
	  */
	public void setAD_Column_ID (int AD_Column_ID);

	/** Get Column.
	  * Column in the table
	  */
	public int getAD_Column_ID();

	public org.compiere.model.I_AD_Column getAD_Column() throws RuntimeException;

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

    /** Column name AD_PInstance_ID */
    public static final String COLUMNNAME_AD_PInstance_ID = "AD_PInstance_ID";

	/** Set Process Instance.
	  * Instance of the process
	  */
	public void setAD_PInstance_ID (int AD_PInstance_ID);

	/** Get Process Instance.
	  * Instance of the process
	  */
	public int getAD_PInstance_ID();

	public org.compiere.model.I_AD_PInstance getAD_PInstance() throws RuntimeException;

    /** Column name AD_Reference_ID */
    public static final String COLUMNNAME_AD_Reference_ID = "AD_Reference_ID";

	/** Set Reference.
	  * System Reference and Validation
	  */
	public void setAD_Reference_ID (int AD_Reference_ID);

	/** Get Reference.
	  * System Reference and Validation
	  */
	public int getAD_Reference_ID();

	public org.compiere.model.I_AD_Reference getAD_Reference() throws RuntimeException;

    /** Column name AD_Reference_Value_ID */
    public static final String COLUMNNAME_AD_Reference_Value_ID = "AD_Reference_Value_ID";

	/** Set Reference Key.
	  * Required to specify, if data type is Table or List
	  */
	public void setAD_Reference_Value_ID (int AD_Reference_Value_ID);

	/** Get Reference Key.
	  * Required to specify, if data type is Table or List
	  */
	public int getAD_Reference_Value_ID();

	public org.compiere.model.I_AD_Reference getAD_Reference_Value() throws RuntimeException;

    /** Column name AD_Table_ID */
    public static final String COLUMNNAME_AD_Table_ID = "AD_Table_ID";

	/** Set Table.
	  * Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID);

	/** Get Table.
	  * Database Table information
	  */
	public int getAD_Table_ID();

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException;

    /** Column name AD_Val_Rule_ID */
    public static final String COLUMNNAME_AD_Val_Rule_ID = "AD_Val_Rule_ID";

	/** Set Dynamic Validation.
	  * Dynamic Validation Rule
	  */
	public void setAD_Val_Rule_ID (int AD_Val_Rule_ID);

	/** Get Dynamic Validation.
	  * Dynamic Validation Rule
	  */
	public int getAD_Val_Rule_ID();

	public org.compiere.model.I_AD_Val_Rule getAD_Val_Rule() throws RuntimeException;

    /** Column name AD_Window_ID */
    public static final String COLUMNNAME_AD_Window_ID = "AD_Window_ID";

	/** Set Window.
	  * Data entry or display window
	  */
	public void setAD_Window_ID (int AD_Window_ID);

	/** Get Window.
	  * Data entry or display window
	  */
	public int getAD_Window_ID();

	public org.compiere.model.I_AD_Window getAD_Window() throws RuntimeException;

    /** Column name AccessLevel */
    public static final String COLUMNNAME_AccessLevel = "AccessLevel";

	/** Set Data Access Level.
	  * Access Level required
	  */
	public void setAccessLevel (String AccessLevel);

	/** Get Data Access Level.
	  * Access Level required
	  */
	public String getAccessLevel();

    /** Column name ColumnName */
    public static final String COLUMNNAME_ColumnName = "ColumnName";

	/** Set DB Column Name.
	  * Name of the column in the database
	  */
	public void setColumnName (String ColumnName);

	/** Get DB Column Name.
	  * Name of the column in the database
	  */
	public String getColumnName();

    /** Column name ColumnSQL */
    public static final String COLUMNNAME_ColumnSQL = "ColumnSQL";

	/** Set Column SQL.
	  * Virtual Column (r/o)
	  */
	public void setColumnSQL (String ColumnSQL);

	/** Get Column SQL.
	  * Virtual Column (r/o)
	  */
	public String getColumnSQL();

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

    /** Column name DefaultValue */
    public static final String COLUMNNAME_DefaultValue = "DefaultValue";

	/** Set Default Logic.
	  * Default value hierarchy, separated by ;

	  */
	public void setDefaultValue (String DefaultValue);

	/** Get Default Logic.
	  * Default value hierarchy, separated by ;

	  */
	public String getDefaultValue();

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

    /** Column name FKConstraintName */
    public static final String COLUMNNAME_FKConstraintName = "FKConstraintName";

	/** Set Constraint Name	  */
	public void setFKConstraintName (String FKConstraintName);

	/** Get Constraint Name	  */
	public String getFKConstraintName();

    /** Column name FKConstraintType */
    public static final String COLUMNNAME_FKConstraintType = "FKConstraintType";

	/** Set Constraint Type	  */
	public void setFKConstraintType (String FKConstraintType);

	/** Get Constraint Type	  */
	public String getFKConstraintType();

    /** Column name Help */
    public static final String COLUMNNAME_Help = "Help";

	/** Set Comment/Help.
	  * Comment or Hint
	  */
	public void setHelp (String Help);

	/** Get Comment/Help.
	  * Comment or Hint
	  */
	public String getHelp();

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

    /** Column name IsAllowLogging */
    public static final String COLUMNNAME_IsAllowLogging = "IsAllowLogging";

	/** Set Allow Logging.
	  * Determine if a column must be recorded into the change log
	  */
	public void setIsAllowLogging (boolean IsAllowLogging);

	/** Get Allow Logging.
	  * Determine if a column must be recorded into the change log
	  */
	public boolean isAllowLogging();

    /** Column name IsKey */
    public static final String COLUMNNAME_IsKey = "IsKey";

	/** Set Key column.
	  * This column is the key in this table
	  */
	public void setIsKey (boolean IsKey);

	/** Get Key column.
	  * This column is the key in this table
	  */
	public boolean isKey();

    /** Column name IsMandatory */
    public static final String COLUMNNAME_IsMandatory = "IsMandatory";

	/** Set Mandatory.
	  * Data entry is required in this column
	  */
	public void setIsMandatory (boolean IsMandatory);

	/** Get Mandatory.
	  * Data entry is required in this column
	  */
	public boolean isMandatory();

    /** Column name IsParent */
    public static final String COLUMNNAME_IsParent = "IsParent";

	/** Set Parent link column.
	  * This column is a link to the parent table (e.g. header from lines) - incl. Association key columns
	  */
	public void setIsParent (boolean IsParent);

	/** Get Parent link column.
	  * This column is a link to the parent table (e.g. header from lines) - incl. Association key columns
	  */
	public boolean isParent();

    /** Column name IsTruncateJP */
    public static final String COLUMNNAME_IsTruncateJP = "IsTruncateJP";

	/** Set Truncate.
	  * JPIERE-0158:JPPS
	  */
	public void setIsTruncateJP (boolean IsTruncateJP);

	/** Get Truncate.
	  * JPIERE-0158:JPPS
	  */
	public boolean isTruncateJP();

    /** Column name JP_CustomDeleteProfileLine_ID */
    public static final String COLUMNNAME_JP_CustomDeleteProfileLine_ID = "JP_CustomDeleteProfileLine_ID";

	/** Set Custom Delete Profile Line	  */
	public void setJP_CustomDeleteProfileLine_ID (int JP_CustomDeleteProfileLine_ID);

	/** Get Custom Delete Profile Line	  */
	public int getJP_CustomDeleteProfileLine_ID();

	public I_JP_CustomDeleteProfileLine getJP_CustomDeleteProfileLine() throws RuntimeException;

    /** Column name JP_CustomDeleteProfile_ID */
    public static final String COLUMNNAME_JP_CustomDeleteProfile_ID = "JP_CustomDeleteProfile_ID";

	/** Set Custom Delete Profile	  */
	public void setJP_CustomDeleteProfile_ID (int JP_CustomDeleteProfile_ID);

	/** Get Custom Delete Profile	  */
	public int getJP_CustomDeleteProfile_ID();

	public I_JP_CustomDeleteProfile getJP_CustomDeleteProfile() throws RuntimeException;

    /** Column name JP_DeleteProfile_ID */
    public static final String COLUMNNAME_JP_DeleteProfile_ID = "JP_DeleteProfile_ID";

	/** Set Delete Profile	  */
	public void setJP_DeleteProfile_ID (int JP_DeleteProfile_ID);

	/** Get Delete Profile	  */
	public int getJP_DeleteProfile_ID();

	public I_JP_DeleteProfile getJP_DeleteProfile() throws RuntimeException;

    /** Column name JP_Delete_Client */
    public static final String COLUMNNAME_JP_Delete_Client = "JP_Delete_Client";

	/** Set Delete or Initialize Tenant	  */
	public void setJP_Delete_Client (String JP_Delete_Client);

	/** Get Delete or Initialize Tenant	  */
	public String getJP_Delete_Client();

    /** Column name JP_TreatForeignKey */
    public static final String COLUMNNAME_JP_TreatForeignKey = "JP_TreatForeignKey";

	/** Set Treat of Foreign Key	  */
	public void setJP_TreatForeignKey (String JP_TreatForeignKey);

	/** Get Treat of Foreign Key	  */
	public String getJP_TreatForeignKey();

    /** Column name LookupClientID */
    public static final String COLUMNNAME_LookupClientID = "LookupClientID";

	/** Set Lookup Tenant ID.
	  * The Client ID or Login submitted to the Lookup URL
	  */
	public void setLookupClientID (int LookupClientID);

	/** Get Lookup Tenant ID.
	  * The Client ID or Login submitted to the Lookup URL
	  */
	public int getLookupClientID();

    /** Column name MandatoryLogic */
    public static final String COLUMNNAME_MandatoryLogic = "MandatoryLogic";

	/** Set Mandatory Logic	  */
	public void setMandatoryLogic (String MandatoryLogic);

	/** Get Mandatory Logic	  */
	public String getMandatoryLogic();

    /** Column name Referenced_Table_ID */
    public static final String COLUMNNAME_Referenced_Table_ID = "Referenced_Table_ID";

	/** Set Referenced Table	  */
	public void setReferenced_Table_ID (int Referenced_Table_ID);

	/** Get Referenced Table	  */
	public int getReferenced_Table_ID();

	public org.compiere.model.I_AD_Table getReferenced_Table() throws RuntimeException;

    /** Column name SQLStatement */
    public static final String COLUMNNAME_SQLStatement = "SQLStatement";

	/** Set SQL Expression/Statement	  */
	public void setSQLStatement (String SQLStatement);

	/** Get SQL Expression/Statement	  */
	public String getSQLStatement();

    /** Column name T_DeleteClientLogJP_ID */
    public static final String COLUMNNAME_T_DeleteClientLogJP_ID = "T_DeleteClientLogJP_ID";

	/** Set Delete Log ID	  */
	public void setT_DeleteClientLogJP_ID (int T_DeleteClientLogJP_ID);

	/** Get Delete Log ID	  */
	public int getT_DeleteClientLogJP_ID();

    /** Column name T_DeleteClientLogJP_UU */
    public static final String COLUMNNAME_T_DeleteClientLogJP_UU = "T_DeleteClientLogJP_UU";

	/** Set T_DeleteClientLogJP_UU	  */
	public void setT_DeleteClientLogJP_UU (String T_DeleteClientLogJP_UU);

	/** Get T_DeleteClientLogJP_UU	  */
	public String getT_DeleteClientLogJP_UU();

    /** Column name TableName */
    public static final String COLUMNNAME_TableName = "TableName";

	/** Set DB Table Name.
	  * Name of the table in the database
	  */
	public void setTableName (String TableName);

	/** Get DB Table Name.
	  * Name of the table in the database
	  */
	public String getTableName();

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
}
