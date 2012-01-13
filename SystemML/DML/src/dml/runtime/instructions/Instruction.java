package dml.runtime.instructions;

import dml.lops.Lops;
import dml.utils.DMLRuntimeException;
import dml.utils.DMLUnsupportedOperationException;

public abstract class Instruction {

	public enum INSTRUCTION_TYPE { CONTROL_PROGRAM, MAPREDUCE, EXTERNAL_LIBRARY, MAPREDUCE_JOB };
	
	public static final String OPERAND_DELIM = Lops.OPERAND_DELIMITOR;
	public static final String VALUETYPE_PREFIX = Lops.VALUETYPE_PREFIX;
	public static final String INSTRUCTION_DELIM = Lops.INSTRUCTION_DELIMITOR;
	
	protected INSTRUCTION_TYPE type;
	protected String instString;
	
	public void setType (INSTRUCTION_TYPE tp ) {
		type = tp;
	}
	
	public INSTRUCTION_TYPE getType() {
		return type;
	}
	
	protected static Instruction parseInstruction ( String str ) throws DMLRuntimeException, DMLUnsupportedOperationException {
		throw new DMLRuntimeException("parseInstruction(): should not be invoked from the base class.");
	}

	public abstract byte[] getInputIndexes() throws DMLRuntimeException;
	
	public abstract byte[] getAllIndexes() throws DMLRuntimeException;

	public void printMe() {
		System.out.println(instString);
	}
	public String toString() {
		return instString;
	}
	
	public String getGraphString() {
		return null;
	}
	
}
