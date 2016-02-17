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
import org.compiere.util.KeyNamePair;

/** Generated Model for T_DeleteClientLogJP
 *  @author iDempiere (generated) 
 *  @version Release 3.1 - $Id$ */
public class X_T_DeleteClientLogJP extends PO implements I_T_DeleteClientLogJP, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20160215L;

    /** Standard Constructor */
    public X_T_DeleteClientLogJP (Properties ctx, int T_DeleteClientLogJP_ID, String trxName)
    {
      super (ctx, T_DeleteClientLogJP_ID, trxName);
      /** if (T_DeleteClientLogJP_ID == 0)
        {
			setAD_PInstance_ID (0);
			setT_DeleteClientLogJP_ID (0);
        } */
    }

    /** Load Constructor */
    public X_T_DeleteClientLogJP (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_T_DeleteClientLogJP[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Column getAD_Column() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Column)MTable.get(getCtx(), org.compiere.model.I_AD_Column.Table_Name)
			.getPO(getAD_Column_ID(), get_TrxName());	}

	/** Set Column.
		@param AD_Column_ID 
		Column in the table
	  */
	public void setAD_Column_ID (int AD_Column_ID)
	{
		if (AD_Column_ID < 1) 
			set_Value (COLUMNNAME_AD_Column_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Column_ID, Integer.valueOf(AD_Column_ID));
	}

	/** Get Column.
		@return Column in the table
	  */
	public int getAD_Column_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Column_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_PInstance getAD_PInstance() throws RuntimeException
    {
		return (org.compiere.model.I_AD_PInstance)MTable.get(getCtx(), org.compiere.model.I_AD_PInstance.Table_Name)
			.getPO(getAD_PInstance_ID(), get_TrxName());	}

	/** Set Process Instance.
		@param AD_PInstance_ID 
		Instance of the process
	  */
	public void setAD_PInstance_ID (int AD_PInstance_ID)
	{
		if (AD_PInstance_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_PInstance_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_PInstance_ID, Integer.valueOf(AD_PInstance_ID));
	}

	/** Get Process Instance.
		@return Instance of the process
	  */
	public int getAD_PInstance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_PInstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Reference getAD_Reference() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Reference)MTable.get(getCtx(), org.compiere.model.I_AD_Reference.Table_Name)
			.getPO(getAD_Reference_ID(), get_TrxName());	}

	/** Set Reference.
		@param AD_Reference_ID 
		System Reference and Validation
	  */
	public void setAD_Reference_ID (int AD_Reference_ID)
	{
		if (AD_Reference_ID < 1) 
			set_Value (COLUMNNAME_AD_Reference_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Reference_ID, Integer.valueOf(AD_Reference_ID));
	}

	/** Get Reference.
		@return System Reference and Validation
	  */
	public int getAD_Reference_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Reference_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Reference getAD_Reference_Value() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Reference)MTable.get(getCtx(), org.compiere.model.I_AD_Reference.Table_Name)
			.getPO(getAD_Reference_Value_ID(), get_TrxName());	}

	/** Set Reference Key.
		@param AD_Reference_Value_ID 
		Required to specify, if data type is Table or List
	  */
	public void setAD_Reference_Value_ID (int AD_Reference_Value_ID)
	{
		if (AD_Reference_Value_ID < 1) 
			set_Value (COLUMNNAME_AD_Reference_Value_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Reference_Value_ID, Integer.valueOf(AD_Reference_Value_ID));
	}

	/** Get Reference Key.
		@return Required to specify, if data type is Table or List
	  */
	public int getAD_Reference_Value_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Reference_Value_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_Name)
			.getPO(getAD_Table_ID(), get_TrxName());	}

	/** Set Table.
		@param AD_Table_ID 
		Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1) 
			set_Value (COLUMNNAME_AD_Table_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get Table.
		@return Database Table information
	  */
	public int getAD_Table_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Val_Rule getAD_Val_Rule() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Val_Rule)MTable.get(getCtx(), org.compiere.model.I_AD_Val_Rule.Table_Name)
			.getPO(getAD_Val_Rule_ID(), get_TrxName());	}

	/** Set Dynamic Validation.
		@param AD_Val_Rule_ID 
		Dynamic Validation Rule
	  */
	public void setAD_Val_Rule_ID (int AD_Val_Rule_ID)
	{
		if (AD_Val_Rule_ID < 1) 
			set_Value (COLUMNNAME_AD_Val_Rule_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Val_Rule_ID, Integer.valueOf(AD_Val_Rule_ID));
	}

	/** Get Dynamic Validation.
		@return Dynamic Validation Rule
	  */
	public int getAD_Val_Rule_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Val_Rule_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Window getAD_Window() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Window)MTable.get(getCtx(), org.compiere.model.I_AD_Window.Table_Name)
			.getPO(getAD_Window_ID(), get_TrxName());	}

	/** Set Window.
		@param AD_Window_ID 
		Data entry or display window
	  */
	public void setAD_Window_ID (int AD_Window_ID)
	{
		if (AD_Window_ID < 1) 
			set_Value (COLUMNNAME_AD_Window_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Window_ID, Integer.valueOf(AD_Window_ID));
	}

	/** Get Window.
		@return Data entry or display window
	  */
	public int getAD_Window_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Window_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** AccessLevel AD_Reference_ID=5 */
	public static final int ACCESSLEVEL_AD_Reference_ID=5;
	/** Organization = 1 */
	public static final String ACCESSLEVEL_Organization = "1";
	/** Client+Organization = 3 */
	public static final String ACCESSLEVEL_ClientPlusOrganization = "3";
	/** System only = 4 */
	public static final String ACCESSLEVEL_SystemOnly = "4";
	/** All = 7 */
	public static final String ACCESSLEVEL_All = "7";
	/** System+Client = 6 */
	public static final String ACCESSLEVEL_SystemPlusClient = "6";
	/** Client only = 2 */
	public static final String ACCESSLEVEL_ClientOnly = "2";
	/** Set Data Access Level.
		@param AccessLevel 
		Access Level required
	  */
	public void setAccessLevel (String AccessLevel)
	{

		set_Value (COLUMNNAME_AccessLevel, AccessLevel);
	}

	/** Get Data Access Level.
		@return Access Level required
	  */
	public String getAccessLevel () 
	{
		return (String)get_Value(COLUMNNAME_AccessLevel);
	}

	/** Set DB Column Name.
		@param ColumnName 
		Name of the column in the database
	  */
	public void setColumnName (String ColumnName)
	{
		set_Value (COLUMNNAME_ColumnName, ColumnName);
	}

	/** Get DB Column Name.
		@return Name of the column in the database
	  */
	public String getColumnName () 
	{
		return (String)get_Value(COLUMNNAME_ColumnName);
	}

	/** Set Column SQL.
		@param ColumnSQL 
		Virtual Column (r/o)
	  */
	public void setColumnSQL (String ColumnSQL)
	{
		set_Value (COLUMNNAME_ColumnSQL, ColumnSQL);
	}

	/** Get Column SQL.
		@return Virtual Column (r/o)
	  */
	public String getColumnSQL () 
	{
		return (String)get_Value(COLUMNNAME_ColumnSQL);
	}

	/** Set Default Logic.
		@param DefaultValue 
		Default value hierarchy, separated by ;
	  */
	public void setDefaultValue (String DefaultValue)
	{
		set_Value (COLUMNNAME_DefaultValue, DefaultValue);
	}

	/** Get Default Logic.
		@return Default value hierarchy, separated by ;
	  */
	public String getDefaultValue () 
	{
		return (String)get_Value(COLUMNNAME_DefaultValue);
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Constraint Name.
		@param FKConstraintName Constraint Name	  */
	public void setFKConstraintName (String FKConstraintName)
	{
		set_Value (COLUMNNAME_FKConstraintName, FKConstraintName);
	}

	/** Get Constraint Name.
		@return Constraint Name	  */
	public String getFKConstraintName () 
	{
		return (String)get_Value(COLUMNNAME_FKConstraintName);
	}

	/** FKConstraintType AD_Reference_ID=200075 */
	public static final int FKCONSTRAINTTYPE_AD_Reference_ID=200075;
	/** Do Not Create = D */
	public static final String FKCONSTRAINTTYPE_DoNotCreate = "D";
	/** No Action = N */
	public static final String FKCONSTRAINTTYPE_NoAction = "N";
	/** Cascade = C */
	public static final String FKCONSTRAINTTYPE_Cascade = "C";
	/** Set Null = S */
	public static final String FKCONSTRAINTTYPE_SetNull = "S";
	/** Model Cascade = M */
	public static final String FKCONSTRAINTTYPE_ModelCascade = "M";
	/** Set Constraint Type.
		@param FKConstraintType Constraint Type	  */
	public void setFKConstraintType (String FKConstraintType)
	{

		set_Value (COLUMNNAME_FKConstraintType, FKConstraintType);
	}

	/** Get Constraint Type.
		@return Constraint Type	  */
	public String getFKConstraintType () 
	{
		return (String)get_Value(COLUMNNAME_FKConstraintType);
	}

	/** Set Comment/Help.
		@param Help 
		Comment or Hint
	  */
	public void setHelp (String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Comment/Help.
		@return Comment or Hint
	  */
	public String getHelp () 
	{
		return (String)get_Value(COLUMNNAME_Help);
	}

	/** Set Allow Logging.
		@param IsAllowLogging 
		Determine if a column must be recorded into the change log
	  */
	public void setIsAllowLogging (boolean IsAllowLogging)
	{
		set_Value (COLUMNNAME_IsAllowLogging, Boolean.valueOf(IsAllowLogging));
	}

	/** Get Allow Logging.
		@return Determine if a column must be recorded into the change log
	  */
	public boolean isAllowLogging () 
	{
		Object oo = get_Value(COLUMNNAME_IsAllowLogging);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Key column.
		@param IsKey 
		This column is the key in this table
	  */
	public void setIsKey (boolean IsKey)
	{
		set_Value (COLUMNNAME_IsKey, Boolean.valueOf(IsKey));
	}

	/** Get Key column.
		@return This column is the key in this table
	  */
	public boolean isKey () 
	{
		Object oo = get_Value(COLUMNNAME_IsKey);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Mandatory.
		@param IsMandatory 
		Data entry is required in this column
	  */
	public void setIsMandatory (boolean IsMandatory)
	{
		set_Value (COLUMNNAME_IsMandatory, Boolean.valueOf(IsMandatory));
	}

	/** Get Mandatory.
		@return Data entry is required in this column
	  */
	public boolean isMandatory () 
	{
		Object oo = get_Value(COLUMNNAME_IsMandatory);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Parent link column.
		@param IsParent 
		This column is a link to the parent table (e.g. header from lines) - incl. Association key columns
	  */
	public void setIsParent (boolean IsParent)
	{
		set_Value (COLUMNNAME_IsParent, Boolean.valueOf(IsParent));
	}

	/** Get Parent link column.
		@return This column is a link to the parent table (e.g. header from lines) - incl. Association key columns
	  */
	public boolean isParent () 
	{
		Object oo = get_Value(COLUMNNAME_IsParent);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Truncate.
		@param IsTruncateJP 
		JPIERE-0158:JPPS
	  */
	public void setIsTruncateJP (boolean IsTruncateJP)
	{
		set_Value (COLUMNNAME_IsTruncateJP, Boolean.valueOf(IsTruncateJP));
	}

	/** Get Truncate.
		@return JPIERE-0158:JPPS
	  */
	public boolean isTruncateJP () 
	{
		Object oo = get_Value(COLUMNNAME_IsTruncateJP);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	public I_JP_CustomDeleteProfileLine getJP_CustomDeleteProfileLine() throws RuntimeException
    {
		return (I_JP_CustomDeleteProfileLine)MTable.get(getCtx(), I_JP_CustomDeleteProfileLine.Table_Name)
			.getPO(getJP_CustomDeleteProfileLine_ID(), get_TrxName());	}

	/** Set Custom Delete Profile Line.
		@param JP_CustomDeleteProfileLine_ID Custom Delete Profile Line	  */
	public void setJP_CustomDeleteProfileLine_ID (int JP_CustomDeleteProfileLine_ID)
	{
		if (JP_CustomDeleteProfileLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfileLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfileLine_ID, Integer.valueOf(JP_CustomDeleteProfileLine_ID));
	}

	/** Get Custom Delete Profile Line.
		@return Custom Delete Profile Line	  */
	public int getJP_CustomDeleteProfileLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_CustomDeleteProfileLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_JP_CustomDeleteProfile getJP_CustomDeleteProfile() throws RuntimeException
    {
		return (I_JP_CustomDeleteProfile)MTable.get(getCtx(), I_JP_CustomDeleteProfile.Table_Name)
			.getPO(getJP_CustomDeleteProfile_ID(), get_TrxName());	}

	/** Set Custom Delete Profile.
		@param JP_CustomDeleteProfile_ID Custom Delete Profile	  */
	public void setJP_CustomDeleteProfile_ID (int JP_CustomDeleteProfile_ID)
	{
		if (JP_CustomDeleteProfile_ID < 1) 
			set_Value (COLUMNNAME_JP_CustomDeleteProfile_ID, null);
		else 
			set_Value (COLUMNNAME_JP_CustomDeleteProfile_ID, Integer.valueOf(JP_CustomDeleteProfile_ID));
	}

	/** Get Custom Delete Profile.
		@return Custom Delete Profile	  */
	public int getJP_CustomDeleteProfile_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_CustomDeleteProfile_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_JP_DeleteProfile getJP_DeleteProfile() throws RuntimeException
    {
		return (I_JP_DeleteProfile)MTable.get(getCtx(), I_JP_DeleteProfile.Table_Name)
			.getPO(getJP_DeleteProfile_ID(), get_TrxName());	}

	/** Set Delete Profile.
		@param JP_DeleteProfile_ID Delete Profile	  */
	public void setJP_DeleteProfile_ID (int JP_DeleteProfile_ID)
	{
		if (JP_DeleteProfile_ID < 1) 
			set_Value (COLUMNNAME_JP_DeleteProfile_ID, null);
		else 
			set_Value (COLUMNNAME_JP_DeleteProfile_ID, Integer.valueOf(JP_DeleteProfile_ID));
	}

	/** Get Delete Profile.
		@return Delete Profile	  */
	public int getJP_DeleteProfile_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_DeleteProfile_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Delete Client = DC */
	public static final String JP_DELETE_CLIENT_DeleteClient = "DC";
	/** All Transaction = TA */
	public static final String JP_DELETE_CLIENT_AllTransaction = "TA";
	/** Client Transaction = TC */
	public static final String JP_DELETE_CLIENT_ClientTransaction = "TC";
	/** Initialize Client = IC */
	public static final String JP_DELETE_CLIENT_InitializeClient = "IC";
	/** Custom Delete = CD */
	public static final String JP_DELETE_CLIENT_CustomDelete = "CD";
	/** Set Delete or Initialize Client.
		@param JP_Delete_Client Delete or Initialize Client	  */
	public void setJP_Delete_Client (String JP_Delete_Client)
	{

		set_Value (COLUMNNAME_JP_Delete_Client, JP_Delete_Client);
	}

	/** Get Delete or Initialize Client.
		@return Delete or Initialize Client	  */
	public String getJP_Delete_Client () 
	{
		return (String)get_Value(COLUMNNAME_JP_Delete_Client);
	}

	/** Set NULL = TSN */
	public static final String JP_TREATFOREIGNKEY_SetNULL = "TSN";
	/** Set Value = TSV */
	public static final String JP_TREATFOREIGNKEY_SetValue = "TSV";
	/** Delete Records = TDE */
	public static final String JP_TREATFOREIGNKEY_DeleteRecords = "TDE";
	/** Mandatory = true ? Delete : Value = IDV */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueDeleteValue = "IDV";
	/** Mandatory = true ? Delete : NULL = IDN */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueDeleteNULL = "IDN";
	/** Mandatory = true ? Value : NULL = IVN */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueValueNULL = "IVN";
	/** Mandatory = true ? Value : Delete = IVD */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueValueDelete = "IVD";
	/** Set Treat of Foreign Key.
		@param JP_TreatForeignKey Treat of Foreign Key	  */
	public void setJP_TreatForeignKey (String JP_TreatForeignKey)
	{

		set_Value (COLUMNNAME_JP_TreatForeignKey, JP_TreatForeignKey);
	}

	/** Get Treat of Foreign Key.
		@return Treat of Foreign Key	  */
	public String getJP_TreatForeignKey () 
	{
		return (String)get_Value(COLUMNNAME_JP_TreatForeignKey);
	}

	/** Set Lookup Client ID.
		@param LookupClientID 
		The ClientID or Login submitted to the Lookup URL
	  */
	public void setLookupClientID (int LookupClientID)
	{
		set_Value (COLUMNNAME_LookupClientID, Integer.valueOf(LookupClientID));
	}

	/** Get Lookup Client ID.
		@return The ClientID or Login submitted to the Lookup URL
	  */
	public int getLookupClientID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LookupClientID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Mandatory Logic.
		@param MandatoryLogic Mandatory Logic	  */
	public void setMandatoryLogic (String MandatoryLogic)
	{
		set_Value (COLUMNNAME_MandatoryLogic, MandatoryLogic);
	}

	/** Get Mandatory Logic.
		@return Mandatory Logic	  */
	public String getMandatoryLogic () 
	{
		return (String)get_Value(COLUMNNAME_MandatoryLogic);
	}

	public org.compiere.model.I_AD_Table getReferenced_Table() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_Name)
			.getPO(getReferenced_Table_ID(), get_TrxName());	}

	/** Set Referenced Table.
		@param Referenced_Table_ID Referenced Table	  */
	public void setReferenced_Table_ID (int Referenced_Table_ID)
	{
		if (Referenced_Table_ID < 1) 
			set_Value (COLUMNNAME_Referenced_Table_ID, null);
		else 
			set_Value (COLUMNNAME_Referenced_Table_ID, Integer.valueOf(Referenced_Table_ID));
	}

	/** Get Referenced Table.
		@return Referenced Table	  */
	public int getReferenced_Table_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Referenced_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set SQLStatement.
		@param SQLStatement SQLStatement	  */
	public void setSQLStatement (String SQLStatement)
	{
		set_Value (COLUMNNAME_SQLStatement, SQLStatement);
	}

	/** Get SQLStatement.
		@return SQLStatement	  */
	public String getSQLStatement () 
	{
		return (String)get_Value(COLUMNNAME_SQLStatement);
	}

	/** Set Delete Client Log Temporary Table.
		@param T_DeleteClientLogJP_ID Delete Client Log Temporary Table	  */
	public void setT_DeleteClientLogJP_ID (int T_DeleteClientLogJP_ID)
	{
		if (T_DeleteClientLogJP_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_T_DeleteClientLogJP_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_T_DeleteClientLogJP_ID, Integer.valueOf(T_DeleteClientLogJP_ID));
	}

	/** Get Delete Client Log Temporary Table.
		@return Delete Client Log Temporary Table	  */
	public int getT_DeleteClientLogJP_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_T_DeleteClientLogJP_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set T_DeleteClientLogJP_UU.
		@param T_DeleteClientLogJP_UU T_DeleteClientLogJP_UU	  */
	public void setT_DeleteClientLogJP_UU (String T_DeleteClientLogJP_UU)
	{
		set_ValueNoCheck (COLUMNNAME_T_DeleteClientLogJP_UU, T_DeleteClientLogJP_UU);
	}

	/** Get T_DeleteClientLogJP_UU.
		@return T_DeleteClientLogJP_UU	  */
	public String getT_DeleteClientLogJP_UU () 
	{
		return (String)get_Value(COLUMNNAME_T_DeleteClientLogJP_UU);
	}

	/** Set DB Table Name.
		@param TableName 
		Name of the table in the database
	  */
	public void setTableName (String TableName)
	{
		set_Value (COLUMNNAME_TableName, TableName);
	}

	/** Get DB Table Name.
		@return Name of the table in the database
	  */
	public String getTableName () 
	{
		return (String)get_Value(COLUMNNAME_TableName);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getTableName());
    }
}