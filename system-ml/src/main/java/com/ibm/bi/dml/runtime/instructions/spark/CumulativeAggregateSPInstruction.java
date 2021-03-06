/**
 * (C) Copyright IBM Corp. 2010, 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.ibm.bi.dml.runtime.instructions.spark;


import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.DMLUnsupportedOperationException;
import com.ibm.bi.dml.runtime.controlprogram.context.ExecutionContext;
import com.ibm.bi.dml.runtime.controlprogram.context.SparkExecutionContext;
import com.ibm.bi.dml.runtime.instructions.InstructionUtils;
import com.ibm.bi.dml.runtime.instructions.cp.CPOperand;
import com.ibm.bi.dml.runtime.instructions.spark.utils.RDDAggregateUtils;
import com.ibm.bi.dml.runtime.matrix.MatrixCharacteristics;
import com.ibm.bi.dml.runtime.matrix.data.MatrixBlock;
import com.ibm.bi.dml.runtime.matrix.data.MatrixIndexes;
import com.ibm.bi.dml.runtime.matrix.data.OperationsOnMatrixValues;
import com.ibm.bi.dml.runtime.matrix.operators.AggregateUnaryOperator;

/**
 * 
 */
public class CumulativeAggregateSPInstruction extends AggregateUnarySPInstruction 
{
	
	public CumulativeAggregateSPInstruction(AggregateUnaryOperator op, CPOperand in1, CPOperand out, String opcode, String istr )
	{
		super(op, null, in1, out, null, opcode, istr);
		_sptype = SPINSTRUCTION_TYPE.CumsumAggregate;		
	}

	/**
	 * 
	 * @param str
	 * @return
	 * @throws DMLRuntimeException
	 */
	public static CumulativeAggregateSPInstruction parseInstruction( String str ) 
		throws DMLRuntimeException 
	{
		String[] parts = InstructionUtils.getInstructionPartsWithValueType( str );
		InstructionUtils.checkNumFields ( parts, 2 );
				
		String opcode = parts[0];
		CPOperand in1 = new CPOperand(parts[1]);
		CPOperand out = new CPOperand(parts[2]);
		
		AggregateUnaryOperator aggun = InstructionUtils.parseCumulativeAggregateUnaryOperator(opcode);
		
		return new CumulativeAggregateSPInstruction(aggun, in1, out, opcode, str);	
	}
	
	@Override
	public void processInstruction(ExecutionContext ec) 
		throws DMLRuntimeException, DMLUnsupportedOperationException
	{
		SparkExecutionContext sec = (SparkExecutionContext)ec;
		MatrixCharacteristics mc = sec.getMatrixCharacteristics(input1.getName());
		long rlen = mc.getRows();
		int brlen = mc.getRowsPerBlock();
		int bclen = mc.getColsPerBlock();
		
		//get input
		JavaPairRDD<MatrixIndexes,MatrixBlock> in = sec.getBinaryBlockRDDHandleForVariable( input1.getName() );
		
		//execute unary aggregate (w/ implicit drop correction)
		AggregateUnaryOperator auop = (AggregateUnaryOperator) _optr;
		JavaPairRDD<MatrixIndexes,MatrixBlock> out = 
				in.mapToPair(new RDDCumAggFunction(auop, rlen, brlen, bclen));
		out = RDDAggregateUtils.mergeByKey(out);
		
		//put output handle in symbol table
		sec.setRDDHandleForVariable(output.getName(), out);	
		sec.addLineageRDD(output.getName(), input1.getName());
	}
	
	/**
	 * 
	 * 
	 */
	private static class RDDCumAggFunction implements PairFunction<Tuple2<MatrixIndexes, MatrixBlock>, MatrixIndexes, MatrixBlock> 
	{
		private static final long serialVersionUID = 11324676268945117L;
		
		private AggregateUnaryOperator _op = null;		
		private long _rlen = -1;
		private int _brlen = -1;
		private int _bclen = -1;
		
		public RDDCumAggFunction( AggregateUnaryOperator op, long rlen, int brlen, int bclen )
		{
			_op = op;
			_rlen = rlen;
			_brlen = brlen;
			_bclen = bclen;
		}
		
		@Override
		public Tuple2<MatrixIndexes, MatrixBlock> call( Tuple2<MatrixIndexes, MatrixBlock> arg0 ) 
			throws Exception 
		{			
			MatrixIndexes ixIn = arg0._1();
			MatrixBlock blkIn = arg0._2();

			MatrixIndexes ixOut = new MatrixIndexes();
			MatrixBlock blkOut = new MatrixBlock();
			
			//process instruction
			OperationsOnMatrixValues.performAggregateUnary( ixIn, blkIn, ixOut, blkOut, 
					                            ((AggregateUnaryOperator)_op), _brlen, _bclen);
			if( ((AggregateUnaryOperator)_op).aggOp.correctionExists )
				blkOut.dropLastRowsOrColums(((AggregateUnaryOperator)_op).aggOp.correctionLocation);
			
			//cumsum expand partial aggregates
			long rlenOut = (long)Math.ceil((double)_rlen/_brlen);
			long rixOut = (long)Math.ceil((double)ixIn.getRowIndex()/_brlen);
			int rlenBlk = (int) Math.min(rlenOut-(rixOut-1)*_brlen, _brlen);
			int clenBlk = blkOut.getNumColumns();
			int posBlk = (int) ((ixIn.getRowIndex()-1) % _brlen);
			MatrixBlock blkOut2 = new MatrixBlock(rlenBlk, clenBlk, false);
			blkOut2.copy(posBlk, posBlk, 0, clenBlk-1, blkOut, true);
			ixOut.setIndexes(rixOut, ixOut.getColumnIndex());
			
			//output new tuple
			return new Tuple2<MatrixIndexes, MatrixBlock>(ixOut, blkOut2);
		}
	}
}
