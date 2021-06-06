package jpiere.plugin.delete.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.adempiere.exceptions.DBException;
import org.adempiere.util.ProcessUtil;
import org.compiere.db.AdempiereDatabase;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MBPGroup;
import org.compiere.model.MClient;
import org.compiere.model.MProductCategory;
import org.compiere.model.PO;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.Util;

public class JPiereInitializeDefaultAccountOnly extends SvrProcess {

	private int p_LookupClientID = 0;

	private ArrayList<Integer> defaultAccount_List = new ArrayList<Integer>();

	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("LookupClientID"))
			{
				p_LookupClientID = para[i].getParameterAsInt();
			}else {
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}

	private String message = null;

	@Override
	protected String doIt() throws Exception
	{
		//Super User can not delete data.
		int AD_User_ID = Env.getAD_User_ID(Env.getCtx());
		if(AD_User_ID==100)//AD_User_ID == 100 that is SuperUser
		{
			//Super User can not execute this process. Please relogin System user or others that can login System client.
			message = Msg.getMsg(getCtx(), "JP_Delete_SuperUser_CanNot");
			addLog(message);
			return message;
		}

		if(p_LookupClientID==0)
		{
			MClient client = new MClient(getCtx(), 0, null);
			message = Msg.getMsg(getCtx(), "DeleteError") + " " + client.getName() + " " + Msg.getElement(getCtx(), "AD_Client_ID");
			addLog(message);
			return message;//"Could not delete record: System Client"
		}

		MClient m_Client = MClient.get(getCtx(), p_LookupClientID);
		if(Util.isEmpty(m_Client.getName()))
		{
			message = Msg.getMsg(getCtx(), "NoRecordID") +" : " +"AD_Client_ID = " + p_LookupClientID;
			addLog(message);
			return message;//Record ID doesn't exist in the table. AD_Client_ID =
		}

		try
		{
			executeUpdateConstraint("D");
			commitEx();

			//Copy Defaut Account
			doAcctSchemaDefaultCopy();
			doAcctProductCategoryAcctCopy();
			doBPGroupAcctCopyAcctCopy();

			//Delete Relation Data
			deletePA_Report_Tables();
			deleteC_Charge_Tables();
			deleteC_SubAcct_Tables();

			//Get Default Accont
			getAcctSchemaElementAccounts();
			getDefaultGLAccounts();
			getDefaultAccounts();

			//Delete ValidCombination
			deleteC_ValidCombination();

			//Delete Tree node
			deleteTreeNode();

			//Delete C_ElementValue
			deleteC_ElementValue();

			updateBankAccountInfo();



		}catch (Exception e){

			if(e instanceof DBException)
			{
				DBException dbe = (DBException)e;
				addLog(Msg.getMsg(getCtx(), "Error") +" SQL: "+ dbe.getSQL());
				addLog(dbe.toString());
			}else{
				addLog(e.toString());

			}

			throw new Exception(e.toString() + message.toString());

		} finally {
			executeUpdateConstraint("O");
			commitEx();
		}

		return Msg.getMsg(getCtx(), "Success");
	}

	/**
	 * Execute Update Constraint
	 *
	 * @param s
	 * @return
	 */
	private int executeUpdateConstraint(String s)
	{
		if(s.equals("D"))
		{
			addLog("### INVALID FK CONSTRAINT ###");
		}else if(s.equals("O")){
			addLog("### VALID FK CONSTRAINT ###");
		}else{
			return -1;
		}

		AdempiereDatabase adempiereDB = DB.getDatabase();
		String schemaName =adempiereDB.getSchema();

		String updateConstraintSQL = "update pg_trigger set tgenabled = ? where oid in ( "
			       +" select tr.oid from pg_class cl, pg_trigger tr, pg_namespace ns "
			         +"   where tr.tgrelid = cl.oid "
			           +"     and cl.relnamespace = ns.oid "
			            +"    and ns.nspname = ?)";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int updates = 0;
		try
		{
			pstmt = DB.prepareStatement(updateConstraintSQL, get_TrxName());
			pstmt.setString(1, s);
			pstmt.setString(2, schemaName);
			updates = pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, updateConstraintSQL, e);
			throw new DBException(e, updateConstraintSQL);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return updates;

		/*Status check SQL*/
		//SELECT * from pg_trigger where oid in ( select tr.oid from pg_class cl, pg_trigger tr, pg_namespace ns where tr.tgrelid = cl.oid and  cl.relnamespace = ns.oid and ns.nspname = 'adempiere')
	}

	private boolean doAcctSchemaDefaultCopy()
	{
		ProcessInfo pi = new ProcessInfo("OverWrite Default Account", 0);
		String className = "org.compiere.process.AcctSchemaDefaultCopy";
		pi.setClassName(className);
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		pi.setAD_PInstance_ID(getAD_PInstance_ID());

		MAcctSchema[] acctSchemas = MAcctSchema.getClientAcctSchema(getCtx(), p_LookupClientID);
		for(int i = 0; i < acctSchemas.length; i++)
		{
			ArrayList<ProcessInfoParameter> list = new ArrayList<ProcessInfoParameter>();
			list.add (new ProcessInfoParameter("C_AcctSchema_ID", acctSchemas[i].getC_AcctSchema_ID(), null, null, null ));
			list.add (new ProcessInfoParameter("CopyOverwriteAcct", "Y", null, null, null ));
			ProcessInfoParameter[] pars = new ProcessInfoParameter[list.size()];
			list.toArray(pars);
			pi.setParameter(pars);

			if(processUI == null)
			{
				processUI = Env.getProcessUI(getCtx());

			}

			ProcessUtil.startJavaProcess(getCtx(), pi, Trx.get(get_TrxName(), true), false, processUI);
		}

		return true;
	}

	private boolean doAcctProductCategoryAcctCopy()
	{
		ProcessInfo pi = new ProcessInfo("OverWrite Default Product Account", 0);
		String className = "org.compiere.process.ProductCategoryAcctCopy";
		pi.setClassName(className);
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		pi.setAD_PInstance_ID(getAD_PInstance_ID());

		MAcctSchema[] acctSchemas = MAcctSchema.getClientAcctSchema(getCtx(), p_LookupClientID);
		for(int i = 0; i < acctSchemas.length; i++)
		{
			int[] M_ProductCategory_IDs = PO.getAllIDs(MProductCategory.Table_Name, " AD_Client_ID="+p_LookupClientID, get_TrxName());
			for(int j=0 ; j <M_ProductCategory_IDs.length; j++)
			{
				ArrayList<ProcessInfoParameter> list = new ArrayList<ProcessInfoParameter>();
				list.add (new ProcessInfoParameter("C_AcctSchema_ID", acctSchemas[i].getC_AcctSchema_ID(), null, null, null ));
				list.add (new ProcessInfoParameter("M_Product_Category_ID", M_ProductCategory_IDs[j], null, null, null ));
				ProcessInfoParameter[] pars = new ProcessInfoParameter[list.size()];
				list.toArray(pars);
				pi.setParameter(pars);

				if(processUI == null)
				{
					processUI = Env.getProcessUI(getCtx());

				}

				ProcessUtil.startJavaProcess(getCtx(), pi, Trx.get(get_TrxName(), true), false, processUI);
			}
		}

		return true;
	}

	private boolean doBPGroupAcctCopyAcctCopy()
	{
		ProcessInfo pi = new ProcessInfo("OverWrite Default BP Account", 0);
		String className = "org.compiere.process.BPGroupAcctCopy";
		pi.setClassName(className);
		pi.setAD_Client_ID(getAD_Client_ID());
		pi.setAD_User_ID(getAD_User_ID());
		pi.setAD_PInstance_ID(getAD_PInstance_ID());

		MAcctSchema[] acctSchemas = MAcctSchema.getClientAcctSchema(getCtx(), p_LookupClientID);
		for(int i = 0; i < acctSchemas.length; i++)
		{
			int[] C_BP_Group_IDs = PO.getAllIDs(MBPGroup.Table_Name, " AD_Client_ID="+p_LookupClientID, get_TrxName());
			for(int j=0 ; j <C_BP_Group_IDs.length; j++)
			{
				ArrayList<ProcessInfoParameter> list = new ArrayList<ProcessInfoParameter>();
				list.add (new ProcessInfoParameter("C_AcctSchema_ID", acctSchemas[i].getC_AcctSchema_ID(), null, null, null ));
				list.add (new ProcessInfoParameter("C_BP_Group_ID", C_BP_Group_IDs[j], null, null, null ));
				ProcessInfoParameter[] pars = new ProcessInfoParameter[list.size()];
				list.toArray(pars);
				pi.setParameter(pars);

				if(processUI == null)
				{
					processUI = Env.getProcessUI(getCtx());

				}

				ProcessUtil.startJavaProcess(getCtx(), pi, Trx.get(get_TrxName(), true), false, processUI);
			}
		}

		return true;
	}

	private boolean deletePA_Report_Tables()
	{
		//Finincial Report
		String[] PA_Report_Tables = {"PA_Report","PA_ReportLineSet","PA_ReportLine","PA_ReportSource","PA_ReportColumnSet"
		,"PA_ReportColumn","PA_ReportCube","PA_Hierarchy"};

		for(int i = 0; i < PA_Report_Tables.length; i++)
		{
			executeDeleteSQL(PA_Report_Tables[i],null);
		}

		return true;
	}

	private boolean deleteC_Charge_Tables()
	{
		String[] C_Charge_Tables = {"C_Charge","C_Charge_Acct","C_Charge_Trl","C_ChargeType","C_ChargeType_DocType"};
		for(int i = 0; i < C_Charge_Tables.length; i++)
		{
			executeDeleteSQL(C_Charge_Tables[i],null);
		}


		return true;
	}

	private boolean deleteC_SubAcct_Tables()
	{
		String[] C_SubAcct_Tables = {"C_SubAcct"};
		for(int i = 0; i < C_SubAcct_Tables.length; i++)
		{
			executeDeleteSQL(C_SubAcct_Tables[i],null);
		}


		return true;
	}

	private void getAcctSchemaElementAccounts()
	{
		String[] AcctSchemaElement_Accounts =
			{"C_ElementValue_ID"};

		for(int i = 0; i < AcctSchemaElement_Accounts.length; i++)
		{
			String sql = "SELECT ev.C_ElementValue_ID FROM C_AcctSchema_Element ae "
					+" INNER JOIN adempiere.C_ElementValue ev on (ev.C_ElementValue_ID = ae." + AcctSchemaElement_Accounts[i] + ") "
					+ " WHERE ae.AD_Client_ID = ? ";

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, get_TrxName());
				pstmt.setInt(1, p_LookupClientID);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int account_ID =rs.getInt(1);
					if(account_ID > 0)
						defaultAccount_List.add(Integer.valueOf(account_ID));
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}

		}
	}

	private void getDefaultGLAccounts()
	{
		String[] defaultGLAccounts_Column =
			{"CommitmentOffsetSales_Acct"
			,"CommitmentOffset_Acct"
			,"CurrencyBalancing_Acct"
			,"IncomeSummary_Acct"	//Don't use in iDempiere
			,"IntercompanyDueFrom_Acct"
			,"IntercompanyDueTo_Acct"
			,"PPVOffset_Acct"
			,"RetainedEarning_Acct" //Don't use in iDempiere
			,"SuspenseBalancing_Acct"
			,"SuspenseError_Acct"
			};

		for(int i = 0; i < defaultGLAccounts_Column.length; i++)
		{
			String sql = "SELECT vc.Account_ID FROM C_AcctSchema_GL gl "
					+" INNER JOIN adempiere.C_ValidCombination vc on (vc.C_ValidCombination_ID = gl." + defaultGLAccounts_Column[i] + ") "
					+ " WHERE gl.AD_Client_ID = ? ";

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, get_TrxName());
				pstmt.setInt(1, p_LookupClientID);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int account_ID =rs.getInt(1);
					if(account_ID > 0)
						defaultAccount_List.add(Integer.valueOf(account_ID));
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}

		}
	}

	private void getDefaultAccounts()//TODO
	{

		String[] defaultAccounts_Column =
			{"B_Asset_Acct"
			,"B_Expense_Acct"			//Don't use in iDempiere
			,"B_InTransit_Acct"
			,"B_InterestExp_Acct"
			,"B_InterestRev_Acct"
			,"B_PaymentSelect_Acct"
			,"B_RevaluationGain_Acct"	//Don't use in iDempiere
			,"B_RevaluationLoss_Acct"	//Don't use in iDempiere
			,"B_SettlementGain_Acct"	//Don't use in iDempiere
			,"B_SettlementLoss_Acct"	//Don't use in iDempiere
			,"B_UnallocatedCash_Acct"
			,"B_Unidentified_Acct"		//Don't use in iDempiere
			,"CB_Asset_Acct"
			,"CB_CashTransfer_Acct"
			,"CB_Differences_Acct"
			,"CB_Expense_Acct"
			,"CB_Receipt_Acct"
			,"C_Prepayment_Acct"
			,"C_Receivable_Acct"
			,"C_Receivable_Services_Acct" //(20)
			,"Ch_Expense_Acct"
			,"Ch_Revenue_Acct"			//Don't use in iDempiere
			,"E_Expense_Acct"			//Don't use in iDempiere
			,"E_Prepayment_Acct"		//Don't use in iDempiere
			,"NotInvoicedReceipts_Acct"
			,"NotInvoicedReceivables_Acct"	//Don't use in iDempiere
			,"NotInvoicedRevenue_Acct"		//Don't use in iDempiere
			,"PJ_Asset_Acct"
			,"PJ_WIP_Acct"
			,"P_Asset_Acct"
			,"P_AverageCostVariance_Acct"
			,"P_Burden_Acct"	//Don't use in iDempiere
			,"P_COGS_Acct"
			,"P_CostAdjustment_Acct"
			,"P_CostOfProduction_Acct"	//Don't use in iDempiere
			,"P_Expense_Acct"
			,"P_FloorStock_Acct"		//Don't use in iDempiere
			,"P_InventoryClearing_Acct"
			,"P_InvoicePriceVariance_Acct"
			,"P_Labor_Acct"			//(40)
			,"P_LandedCostClearing_Acct"
			,"P_MethodChangeVariance_Acct"	//Don't use in iDempiere
			,"P_MixVariance_Acct"			//Don't use in iDempiere
			,"P_OutsideProcessing_Acct"		//Don't use in iDempiere
			,"P_Overhead_Acct"				//Don't use in iDempiere
			,"P_PurchasePriceVariance_Acct"
			,"P_RateVariance_Acct"
			,"P_Revenue_Acct"
			,"P_Scrap_Acct"					//Don't use in iDempiere
			,"P_TradeDiscountGrant_Acct"
			,"P_TradeDiscountRec_Acct"
			,"P_UsageVariance_Acct"			//Don't use in iDempiere
			,"P_WIP_Acct"					//Don't use in iDempiere
			,"PayDiscount_Exp_Acct"
			,"PayDiscount_Rev_Acct"
			,"RealizedGain_Acct"
			,"RealizedLoss_Acct"
			,"T_Credit_Acct"
			,"T_Due_Acct"
			,"T_Expense_Acct"	//(60)
			,"T_Liability_Acct"		//Don't use in iDempiere
			,"T_Receivables_Acct"	//Don't use in iDempiere
			,"UnEarnedRevenue_Acct"	//Don't use in iDempiere
			,"UnrealizedGain_Acct"
			,"UnrealizedLoss_Acct"
			,"V_Liability_Acct"
			,"V_Liability_Services_Acct"
			,"V_Prepayment_Acct"
			,"W_Differences_Acct"
			,"W_InvActualAdjust_Acct"	//Don't use in iDempiere
			,"W_Inventory_Acct"			//Don't use in iDempiere
			,"W_Revaluation_Acct"		//Don't use in iDempiere
			,"Withholding_Acct"			//Don't use in iDempiere
			,"WriteOff_Acct" //(74)

			};

		for(int i = 0; i < defaultAccounts_Column.length; i++)
		{
			String sql = "SELECT vc.Account_ID FROM C_AcctSchema_Default df "
					+" INNER JOIN adempiere.C_ValidCombination vc on (vc.C_ValidCombination_ID = df." + defaultAccounts_Column[i] + ") "
					+ " WHERE df.AD_Client_ID = ? ";

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, get_TrxName());
				pstmt.setInt(1, p_LookupClientID);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int account_ID =rs.getInt(1);
					if(account_ID > 0)
						defaultAccount_List.add(Integer.valueOf(account_ID));
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}

		}

	}

	private boolean deleteC_ValidCombination()
	{

		StringBuilder where = new StringBuilder(" Account_ID NOT IN ( ");
		int i = 0;
		for(Integer account_ID :defaultAccount_List)
		{
			if(i==0)
			{
				where.append(String.valueOf(account_ID.intValue()));
			}else {
				where.append(",").append(String.valueOf(account_ID.intValue()));
			}

			i++;
		}

		where.append(")");

		executeDeleteSQL("C_ValidCombination", where.toString());

		return true;
	}

	private boolean deleteTreeNode()
	{
		StringBuilder deleteSQL = new StringBuilder("DELETE FROM AD_TreeNode tn USING AD_Tree t WHERE tn.AD_Tree_ID = t.AD_Tree_ID AND t.treetype = 'EV' AND tn.Node_ID NOT IN ( ");
		int i = 0;
		for(Integer account_ID :defaultAccount_List)
		{
			if(i==0)
			{
				deleteSQL.append(String.valueOf(account_ID.intValue()));
			}else {
				deleteSQL.append(",").append(String.valueOf(account_ID.intValue()));
			}

			i++;
		}

		deleteSQL.append(")");
		deleteSQL.append(" AND tn.AD_Client_ID = " + p_LookupClientID);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int deletes = 0;
		try
		{
			pstmt = DB.prepareStatement(deleteSQL.toString(), get_TrxName());
			deletes = pstmt.executeUpdate();
			if (log.isLoggable(Level.FINE)) log.fine("Delete AD_TreeNode =#" + deletes + ":" + deleteSQL.toString());

		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, deleteSQL.toString(), e);
			throw new DBException(e, deleteSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}


		StringBuilder updateSQL = new StringBuilder("UPDATE AD_TreeNode tn set Parent_ID = 0 FROM AD_Tree t WHERE tn.AD_Tree_ID = t.AD_Tree_ID AND t.treetype = 'EV' ");
		updateSQL.append(" AND tn.AD_Client_ID = " + p_LookupClientID);
		int updates = 0;
		try
		{
			pstmt = DB.prepareStatement(updateSQL.toString(), get_TrxName());
			updates = pstmt.executeUpdate();
			if (log.isLoggable(Level.FINE)) log.fine("Update AD_TreeNode =#" + updates + ":" + updateSQL.toString());

		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, updateSQL.toString(), e);
			throw new DBException(e, updateSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return true;
	}

	private boolean deleteC_ElementValue()
	{

		StringBuilder where = new StringBuilder(" C_ElementValue_ID NOT IN ( ");
		int i = 0;
		for(Integer account_ID :defaultAccount_List)
		{
			if(i==0)
			{
				where.append(String.valueOf(account_ID.intValue()));
			}else {
				where.append(",").append(String.valueOf(account_ID.intValue()));
			}

			i++;
		}

		where.append(")");

		executeDeleteSQL("C_ElementValue", where.toString());
		executeDeleteSQL("C_ElementValue_Trl", where.toString());

		return true;
	}

	private boolean updateBankAccountInfo()
	{

		StringBuilder updateSQL = new StringBuilder("UPDATE C_ElementValue ev set C_BankAccount_ID = NULL ");
		updateSQL.append(" WHERE ev.AD_Client_ID = " + p_LookupClientID);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int updates = 0;
		try
		{
			pstmt = DB.prepareStatement(updateSQL.toString(), get_TrxName());
			updates = pstmt.executeUpdate();
			if (log.isLoggable(Level.FINE)) log.fine("Update C_ElementValue =#" + updates + ":" + updateSQL.toString());

		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, updateSQL.toString(), e);
			throw new DBException(e, updateSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return true;
	}

	private boolean executeDeleteSQL(String table,String where)
	{
		StringBuilder DeleteSQL = new StringBuilder();
		DeleteSQL.append("DELETE FROM "+ table);
		if(Util.isEmpty(where))
		{
			DeleteSQL.append(" WHERE AD_Client_ID = " + p_LookupClientID);
		}else {
			DeleteSQL.append(" WHERE " + where).append(" AND AD_Client_ID = " + p_LookupClientID);
		}

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int deletes = 0;
		try
		{
			pstmt = DB.prepareStatement(DeleteSQL.toString(), get_TrxName());
			deletes = pstmt.executeUpdate();
			if (log.isLoggable(Level.FINE)) log.fine("Delete " + table + " =#" + deletes + " : " + DeleteSQL.toString());

		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, DeleteSQL.toString(), e);
			throw new DBException(e, DeleteSQL.toString());
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return true;
	}
}
