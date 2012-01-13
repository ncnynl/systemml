package dml.runtime.matrix.operators;

import dml.runtime.functionobjects.IndexFunction;
import dml.runtime.functionobjects.Plus;


public class AggregateUnaryOperator  extends Operator {

	public AggregateOperator aggOp;
	public IndexFunction indexFn;
	
	//TODO: this is a hack to support trace, and it will be removed once selection operation is supported
	public boolean isTrace=false;
	//TODO: this is a hack to support diag, and it will be removed once selection operation is supported
	public boolean isDiagM2V=false;
	
	public AggregateUnaryOperator(AggregateOperator aop, IndexFunction iop)
	{
		aggOp=aop;
		indexFn=iop;
		if(aggOp.increOp.fn instanceof Plus)
			sparseSafe=true;
		else
			sparseSafe=false;
	}
}
