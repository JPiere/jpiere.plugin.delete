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

/** Generated Model for JP_CustomDeleteProfileLine
 *  @author iDempiere (generated)
 *  @version Release 12 - $Id$ */
@org.adempiere.base.Model(table="JP_CustomDeleteProfileLine")
public class X_JP_CustomDeleteProfileLine extends PO implements I_JP_CustomDeleteProfileLine, I_Persistent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20251128L;

    /** Standard Constructor */
    public X_JP_CustomDeleteProfileLine (Properties ctx, int JP_CustomDeleteProfileLine_ID, String trxName)
    {
      super (ctx, JP_CustomDeleteProfileLine_ID, trxName);
      /** if (JP_CustomDeleteProfileLine_ID == 0)
        {
			setAD_Column_ID (0);
			setAD_Table_ID (0);
			setJP_CustomDeleteProfileLine_ID (0);
			setJP_CustomDeleteProfile_ID (0);
			setJP_TreatForeignKey (null);
// TSN
			setSeqNo (0);
// @SQL=SELECT COALESCE(MAX(SeqNo),0)+10 AS DefaultValue FROM JP_CustomDeleteProfileLine WHERE JP_CustomDeleteProfile_ID=@JP_CustomDeleteProfile_ID@
        } */
    }

    /** Standard Constructor */
    public X_JP_CustomDeleteProfileLine (Properties ctx, int JP_CustomDeleteProfileLine_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, JP_CustomDeleteProfileLine_ID, trxName, virtualColumns);
      /** if (JP_CustomDeleteProfileLine_ID == 0)
        {
			setAD_Column_ID (0);
			setAD_Table_ID (0);
			setJP_CustomDeleteProfileLine_ID (0);
			setJP_CustomDeleteProfile_ID (0);
			setJP_TreatForeignKey (null);
// TSN
			setSeqNo (0);
// @SQL=SELECT COALESCE(MAX(SeqNo),0)+10 AS DefaultValue FROM JP_CustomDeleteProfileLine WHERE JP_CustomDeleteProfile_ID=@JP_CustomDeleteProfile_ID@
        } */
    }

    /** Standard Constructor */
    public X_JP_CustomDeleteProfileLine (Properties ctx, String JP_CustomDeleteProfileLine_UU, String trxName)
    {
      super (ctx, JP_CustomDeleteProfileLine_UU, trxName);
      /** if (JP_CustomDeleteProfileLine_UU == null)
        {
			setAD_Column_ID (0);
			setAD_Table_ID (0);
			setJP_CustomDeleteProfileLine_ID (0);
			setJP_CustomDeleteProfile_ID (0);
			setJP_TreatForeignKey (null);
// TSN
			setSeqNo (0);
// @SQL=SELECT COALESCE(MAX(SeqNo),0)+10 AS DefaultValue FROM JP_CustomDeleteProfileLine WHERE JP_CustomDeleteProfile_ID=@JP_CustomDeleteProfile_ID@
        } */
    }

    /** Standard Constructor */
    public X_JP_CustomDeleteProfileLine (Properties ctx, String JP_CustomDeleteProfileLine_UU, String trxName, String ... virtualColumns)
    {
      super (ctx, JP_CustomDeleteProfileLine_UU, trxName, virtualColumns);
      /** if (JP_CustomDeleteProfileLine_UU == null)
        {
			setAD_Column_ID (0);
			setAD_Table_ID (0);
			setJP_CustomDeleteProfileLine_ID (0);
			setJP_CustomDeleteProfile_ID (0);
			setJP_TreatForeignKey (null);
// TSN
			setSeqNo (0);
// @SQL=SELECT COALESCE(MAX(SeqNo),0)+10 AS DefaultValue FROM JP_CustomDeleteProfileLine WHERE JP_CustomDeleteProfile_ID=@JP_CustomDeleteProfile_ID@
        } */
    }

    /** Load Constructor */
    public X_JP_CustomDeleteProfileLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_JP_CustomDeleteProfileLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Column getAD_Column() throws RuntimeException
	{
		return (org.compiere.model.I_AD_Column)MTable.get(getCtx(), org.compiere.model.I_AD_Column.Table_ID)
			.getPO(getAD_Column_ID(), get_TrxName());
	}

	/** Set Column.
		@param AD_Column_ID Column in the table
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
	public int getAD_Column_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Column_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException
	{
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_ID)
			.getPO(getAD_Table_ID(), get_TrxName());
	}

	/** Set Table.
		@param AD_Table_ID Database Table information
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
	public int getAD_Table_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set Custom Delete Profile Line.
		@param JP_CustomDeleteProfileLine_ID Custom Delete Profile Line
	*/
	public void setJP_CustomDeleteProfileLine_ID (int JP_CustomDeleteProfileLine_ID)
	{
		if (JP_CustomDeleteProfileLine_ID < 1)
			set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfileLine_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfileLine_ID, Integer.valueOf(JP_CustomDeleteProfileLine_ID));
	}

	/** Get Custom Delete Profile Line.
		@return Custom Delete Profile Line	  */
	public int getJP_CustomDeleteProfileLine_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_CustomDeleteProfileLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set JP_CustomDeleteProfileLine_UU.
		@param JP_CustomDeleteProfileLine_UU JP_CustomDeleteProfileLine_UU
	*/
	public void setJP_CustomDeleteProfileLine_UU (String JP_CustomDeleteProfileLine_UU)
	{
		set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfileLine_UU, JP_CustomDeleteProfileLine_UU);
	}

	/** Get JP_CustomDeleteProfileLine_UU.
		@return JP_CustomDeleteProfileLine_UU	  */
	public String getJP_CustomDeleteProfileLine_UU()
	{
		return (String)get_Value(COLUMNNAME_JP_CustomDeleteProfileLine_UU);
	}

	public I_JP_CustomDeleteProfile getJP_CustomDeleteProfile() throws RuntimeException
	{
		return (I_JP_CustomDeleteProfile)MTable.get(getCtx(), I_JP_CustomDeleteProfile.Table_ID)
			.getPO(getJP_CustomDeleteProfile_ID(), get_TrxName());
	}

	/** Set Custom Delete Profile.
		@param JP_CustomDeleteProfile_ID Custom Delete Profile
	*/
	public void setJP_CustomDeleteProfile_ID (int JP_CustomDeleteProfile_ID)
	{
		if (JP_CustomDeleteProfile_ID < 1)
			set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfile_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_JP_CustomDeleteProfile_ID, Integer.valueOf(JP_CustomDeleteProfile_ID));
	}

	/** Get Custom Delete Profile.
		@return Custom Delete Profile	  */
	public int getJP_CustomDeleteProfile_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_CustomDeleteProfile_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair()
    {
        return new KeyNamePair(get_ID(), String.valueOf(getJP_CustomDeleteProfile_ID()));
    }

	/** Set Foreign Key ID.
		@param JP_ForeignKey_Value Foreign Key ID
	*/
	public void setJP_ForeignKey_Value (int JP_ForeignKey_Value)
	{
		set_Value (COLUMNNAME_JP_ForeignKey_Value, Integer.valueOf(JP_ForeignKey_Value));
	}

	/** Get Foreign Key ID.
		@return Foreign Key ID	  */
	public int getJP_ForeignKey_Value()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_JP_ForeignKey_Value);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Mandatory = true ? Delete : NULL = IDN */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueDeleteNULL = "IDN";
	/** Mandatory = true ? Delete : Value = IDV */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueDeleteValue = "IDV";
	/** Mandatory = true ? Value : Delete = IVD */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueValueDelete = "IVD";
	/** Mandatory = true ? Value : NULL = IVN */
	public static final String JP_TREATFOREIGNKEY_MandatoryEqTrueValueNULL = "IVN";
	/** Delete Records = TDE */
	public static final String JP_TREATFOREIGNKEY_DeleteRecords = "TDE";
	/** Set NULL = TSN */
	public static final String JP_TREATFOREIGNKEY_SetNULL = "TSN";
	/** Set Value = TSV */
	public static final String JP_TREATFOREIGNKEY_SetValue = "TSV";
	/** Set Treat of Foreign Key.
		@param JP_TreatForeignKey Treat of Foreign Key
	*/
	public void setJP_TreatForeignKey (String JP_TreatForeignKey)
	{

		set_Value (COLUMNNAME_JP_TreatForeignKey, JP_TreatForeignKey);
	}

	/** Get Treat of Foreign Key.
		@return Treat of Foreign Key	  */
	public String getJP_TreatForeignKey()
	{
		return (String)get_Value(COLUMNNAME_JP_TreatForeignKey);
	}

	/** Set Sequence.
		@param SeqNo Method of ordering records; lowest number comes first
	*/
	public void setSeqNo (int SeqNo)
	{
		set_Value (COLUMNNAME_SeqNo, Integer.valueOf(SeqNo));
	}

	/** Get Sequence.
		@return Method of ordering records; lowest number comes first
	  */
	public int getSeqNo()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SeqNo);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}