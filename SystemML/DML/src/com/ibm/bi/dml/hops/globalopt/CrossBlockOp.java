/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2014
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.hops.globalopt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.harmony.luni.util.NotImplementedException;

import com.ibm.bi.dml.hops.FunctionOp;
import com.ibm.bi.dml.hops.Hop;
import com.ibm.bi.dml.hops.HopsException;
import com.ibm.bi.dml.hops.MemoTable;
import com.ibm.bi.dml.hops.globalopt.HopsVisitor.Flag;
import com.ibm.bi.dml.hops.globalopt.enumerate.BlockSizeRewrite;
import com.ibm.bi.dml.hops.globalopt.enumerate.FormatParam;
import com.ibm.bi.dml.hops.globalopt.enumerate.LocationParam;
import com.ibm.bi.dml.hops.globalopt.enumerate.LocationRewrite;
import com.ibm.bi.dml.hops.globalopt.enumerate.ReblockRewrite;
import com.ibm.bi.dml.hops.globalopt.enumerate.Rewrite;
import com.ibm.bi.dml.lops.Lop;
import com.ibm.bi.dml.lops.LopProperties.ExecType;
import com.ibm.bi.dml.lops.LopsException;
import com.ibm.bi.dml.runtime.controlprogram.ProgramBlock;
import com.ibm.bi.dml.sql.sqllops.SQLLops;

/**
 * Meta operator to connect 
 */
public class CrossBlockOp extends Hop implements HopsVisitable 
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2014\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	
	protected Hop output, leftInput;
	
	private ProgramBlock preceedingBlock;
	private ProgramBlock succeedingBlock;
	private Map<String, Rewrite> appliedRewrites = new HashMap<String, Rewrite>();
	
	public CrossBlockOp() {}
	
	public CrossBlockOp(Hop leftInput, Hop output) {
		super(Kind.CrossBlockOp, leftInput.get_name(), leftInput.get_dataType(), leftInput.get_valueType());
		
		this.leftInput = leftInput;
		String name = leftInput.get_name();
		if(leftInput instanceof FunctionOp) {
			name += output.get_name();
		}
		super.set_name(name);
		
		leftInput.setCrossBlockOutput(this);
		this._dim1 = leftInput.get_dim1();
		this._dim2 = leftInput.get_dim2();
		
		this.output = output;
		if(output != null)
			output.setCrossBlockInput(this);
	}
	
	
	/* (non-Javadoc)
	 * @see com.ibm.bi.dml.optimizer.HopsVisitable#accept(com.ibm.bi.dml.optimizer.HopsVisitor)
	 */
	@Override
	public void accept(HopsVisitor visitor) {
		if(visitor.traverseBackwards()) {
			acceptSinkToSource(visitor);
		}else{
			acceptSourceToSink(visitor);
		}
	}

	private void acceptSinkToSource(HopsVisitor visitor)
	{
		//TODO: implement me
		throw new NotImplementedException();
	}
	/**
	 * @param visitor
	 */
	private void acceptSourceToSink(HopsVisitor visitor) {
		Flag flag = visitor.preVisit(this);
		this.hopsVisited.put(visitor, true);
		
		if(flag != Flag.STOP_INPUT 
				&& this.leftInput != null 
				&& !this.leftInput.isHopsVisited(visitor)
				&& visitor.matchesPattern(this.leftInput)) {
			this.leftInput.accept(visitor);
		}
		
		visitor.visit(this);
		
		if(flag != Flag.STOP_OUTPUT 
				&& this.output != null 
				&& !this.output.isHopsVisited(visitor)
				&& visitor.matchesPattern(this.output)) {
			this.output.accept(visitor);
		}
		
		visitor.postVisit(this);
	}

	@Override
	public boolean allowsAllExecTypes() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lop constructLops() throws HopsException, LopsException {
		if(get_lops() != null) 
		{
			return get_lops();
		} else {
			CrossBlockLop cLop = new CrossBlockLop();
			set_lops(cLop);
			return cLop;
		}
	}

	@Override
	public SQLLops constructSQLLOPs() throws HopsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOpString() {
		return "CBH";
	}

	@Override
	protected ExecType optFindExecType() throws HopsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshSizeInformation() {
		this._dim1 = this.leftInput.get_dim1();
		this._dim2 = this.leftInput.get_dim2();
	}


	public Hop getLeftInput() {
		return leftInput;
	}
	
	@Override
	public ArrayList<Hop> getInput() {
		ArrayList<Hop> retVal = new ArrayList<Hop>();
		retVal.add(leftInput);
		return retVal;
	}
	
	public Hop getOutput() {
		return output;
	}
	
	/*
	@Override
	public void propagateBlockSize() {
		long rowsPerBlock = this.get_rows_in_block();
		long colsPerBlock = this.get_cols_in_block();
		
		this.getLeftInput().set_cols_in_block(colsPerBlock);
		this.getLeftInput().set_rows_in_block(rowsPerBlock);
		
		if(this.output != null) {
			this.output.set_cols_in_block(colsPerBlock);
			this.output.set_rows_in_block(rowsPerBlock);
		}
	}
*/

	@Override
	protected double computeIntermediateMemEstimate(long dim1, long dim2,
			long nnz) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	protected double computeOutputMemEstimate(long dim1, long dim2, long nnz) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	protected long[] inferOutputCharacteristics(MemoTable memo) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramBlock getPreceedingBlock() {
		return preceedingBlock;
	}

	public void setPreceedingBlock(ProgramBlock precedingBlock) {
		this.preceedingBlock = precedingBlock;
	}

	public ProgramBlock getSucceedingBlock() {
		return succeedingBlock;
	}

	public void setSucceedingBlock(ProgramBlock succedingBlock) {
		this.succeedingBlock = succedingBlock;
	}

	public Map<String, Rewrite> getAppliedRewrites() {
		return appliedRewrites;
	}

	public void setAppliedRewrites(Map<String, Rewrite> appliedRewrites) {
		this.appliedRewrites = appliedRewrites;
	}

	public void addRewrite(Rewrite rewrite) {
		if(rewrite instanceof ReblockRewrite) {
			this.appliedRewrites.put(FormatParam.NAME, rewrite);
		}
		if(rewrite instanceof BlockSizeRewrite) {
			//FIXME: find the places where this string is used and replace with constant  
			this.appliedRewrites.put("bs", rewrite);
		}
		if(rewrite instanceof LocationRewrite) {
			this.appliedRewrites.put(LocationParam.NAME, rewrite);
		}
	}
	
	public Rewrite getAppliedRewrite(String name) {
		return this.appliedRewrites.get(name);
	}

	@Override
	public boolean compare(Hop that) {
		//do nothing
		return false;
	}
}