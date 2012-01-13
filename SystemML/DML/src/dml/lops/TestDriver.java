package dml.lops;

import java.util.ArrayList;
import java.util.HashMap;

import dml.lops.compile.Dag;
import dml.lops.runtime.RunMRJobs;
import dml.parser.Expression.DataType;
import dml.parser.Expression.ValueType;
import dml.runtime.instructions.Instruction;
import dml.runtime.instructions.MRJobInstruction;
import dml.utils.DMLRuntimeException;
import dml.utils.DMLUnsupportedOperationException;
import dml.utils.LopsException;

public class TestDriver {

	public static void aggbinaryop() throws LopsException, DMLRuntimeException,
			DMLUnsupportedOperationException {

		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data data2 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		MMCJ mmcj1 = new MMCJ(data1, data2, DataType.MATRIX, ValueType.DOUBLE);
		Group group3 = new Group(mmcj1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Aggregate agg1 = new Aggregate(group3, Aggregate.OperationTypes.Sum,
				DataType.MATRIX, ValueType.DOUBLE);
		Data data3 = new Data("scripts/C", Data.OperationTypes.WRITE, agg1,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data3.printMe();

		Dag<Lops> dag = new Dag<Lops>();

		data3.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);

		printInstructions(inst);

	}

	public static void aggunaryop() throws LopsException, DMLRuntimeException,
			DMLUnsupportedOperationException {
		Dag<Lops> dag = new Dag<Lops>();

		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		PartialAggregate pagg1 = new PartialAggregate(data1,
				Aggregate.OperationTypes.Sum,
				PartialAggregate.DirectionTypes.Row, DataType.MATRIX,
				ValueType.DOUBLE);

		Group group1 = new Group(pagg1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);

		Aggregate agg1 = new Aggregate(group1, Aggregate.OperationTypes.Sum,
				DataType.MATRIX, ValueType.DOUBLE);

		Data data2 = new Data("scripts/D", Data.OperationTypes.WRITE, agg1,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data2.printMe();

		data2.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);

		printInstructions(inst);
	}

	public static void binaryop() throws LopsException, DMLRuntimeException,
			DMLUnsupportedOperationException {
		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data data2 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		Group group1 = new Group(data1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Group group2 = new Group(data2, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);

		Binary binary = new Binary(group1, group2, Binary.OperationTypes.ADD,
				DataType.MATRIX, ValueType.DOUBLE);
		Data data3 = new Data("scripts/C", Data.OperationTypes.WRITE, binary,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data3.printMe();

		Dag<Lops> dag = new Dag<Lops>();
		data3.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);

		printInstructions(inst);

	}

	public static void unaryop() throws LopsException, DMLRuntimeException,
			DMLUnsupportedOperationException {
		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data var1 = new Data(null, Data.OperationTypes.READ, null, "0.5",
				DataType.SCALAR, ValueType.DOUBLE, false);

		Unary scalar1 = new Unary(data1, var1, Unary.OperationTypes.ADD,
				DataType.MATRIX, ValueType.DOUBLE);

		Data data2 = new Data("scripts/D", Data.OperationTypes.WRITE, scalar1,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data2.printMe();

		Dag<Lops> dag = new Dag<Lops>();

		data2.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);

		printInstructions(inst);
	}

	public static void multiop() throws LopsException, DMLRuntimeException,
			DMLUnsupportedOperationException {
		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		Transform transform1 = new Transform(data1,
				Transform.OperationTypes.Transpose, DataType.MATRIX,
				ValueType.DOUBLE);

		Group group1 = new Group(data1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Group group2 = new Group(transform1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);

		Binary binary = new Binary(group1, group2, Binary.OperationTypes.ADD,
				DataType.MATRIX, ValueType.DOUBLE);

		Data var1 = new Data(null, Data.OperationTypes.READ, null, "0.5",
				DataType.SCALAR, ValueType.DOUBLE, false);

		Unary scalar1 = new Unary(binary, var1, Unary.OperationTypes.MULTIPLY,
				DataType.MATRIX, ValueType.DOUBLE);

		Data data2 = new Data("scripts/D", Data.OperationTypes.WRITE, scalar1,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data2.printMe();

		Dag<Lops> dag = new Dag<Lops>();

		data2.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);

		printInstructions(inst);
	}

	public static void printInstructions(ArrayList<Instruction> inst) {
		for (int i = 0; i < inst.size(); i++) {
			System.out.println("==============Instruction " + i + "\n"
					+ inst.get(i).toString());
		}
	}

	public static void simplestlop() throws LopsException, DMLRuntimeException,
			DMLUnsupportedOperationException {
		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data data2 = new Data("scripts/B", Data.OperationTypes.WRITE, data1,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data2.printMe();

		Dag<Lops> dag = new Dag<Lops>();
		data2.addToDag(dag);
		ArrayList<Instruction> inst = dag.getJobs(true, null);
		printInstructions(inst);
		((MRJobInstruction) (inst.get(0)))
				.setInputLabelValueMapping(new HashMap<String, dml.runtime.instructions.CPInstructions.Data>());
		try {
			RunMRJobs.submitJob((MRJobInstruction) inst.get(0), null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[]) throws LopsException,
			DMLRuntimeException, DMLUnsupportedOperationException {

		simplestlop();
		// unaryop();
		// binaryop();
		// aggunaryop();
		// multiop();
		// aggbinaryop();
		// harderexample1();
		// harderexample1withLabels();

	}

	public static void harderexample1withLabels() throws LopsException,
			DMLRuntimeException, DMLUnsupportedOperationException {
		Data data1 = new Data(null, Data.OperationTypes.READ, "Data1Label",
				null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data var1 = new Data(null, Data.OperationTypes.READ, "Var1Label", null,
				DataType.SCALAR, ValueType.DOUBLE, false);
		Unary scalar1 = new Unary(data1, var1, Unary.OperationTypes.MULTIPLY,
				DataType.MATRIX, ValueType.DOUBLE);

		Data data2 = new Data(null, Data.OperationTypes.READ, "Data2Label",
				null, DataType.MATRIX, ValueType.DOUBLE, false);

		MMCJ mmcj1 = new MMCJ(scalar1, data2, DataType.MATRIX, ValueType.DOUBLE);
		Group group1 = new Group(mmcj1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Aggregate agg1 = new Aggregate(group1, Aggregate.OperationTypes.Sum,
				DataType.MATRIX, ValueType.DOUBLE);

		PartialAggregate pagg1 = new PartialAggregate(data1,
				Aggregate.OperationTypes.Sum,
				PartialAggregate.DirectionTypes.Row, DataType.MATRIX,
				ValueType.DOUBLE);
		Group group2 = new Group(pagg1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Aggregate agg2 = new Aggregate(group2, Aggregate.OperationTypes.Sum,
				DataType.MATRIX, ValueType.DOUBLE);

		Group group3 = new Group(agg1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Group group4 = new Group(agg2, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);

		Binary binary1 = new Binary(group3, group4, Binary.OperationTypes.ADD,
				DataType.MATRIX, ValueType.DOUBLE);

		Data var2 = new Data(null, Data.OperationTypes.READ, "Var2Label", null,
				DataType.SCALAR, ValueType.DOUBLE, false);
		Data var3 = new Data(null, Data.OperationTypes.READ, "Var3Label", null,
				DataType.SCALAR, ValueType.DOUBLE, false);

		BinaryCP binScalar1 = new BinaryCP(var2, var3,
				BinaryCP.OperationTypes.ADD, DataType.SCALAR, ValueType.DOUBLE);

		Unary scalar2 = new Unary(binary1, binScalar1,
				Unary.OperationTypes.MULTIPLY, DataType.MATRIX,
				ValueType.DOUBLE);

		Data data3 = new Data(null, Data.OperationTypes.WRITE, scalar2,
				"Data3Label", null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data var4 = new Data(null, Data.OperationTypes.WRITE, binScalar1,
				"Var4Label", null, DataType.SCALAR, ValueType.DOUBLE, false);

		data3.printMe();
		var4.printMe();

		Dag<Lops> dag = new Dag<Lops>();

		data3.addToDag(dag);
		var4.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);
		printInstructions(inst);

	}

	public static void harderexample1() throws LopsException,
			DMLRuntimeException, DMLUnsupportedOperationException {

		Data data1 = new Data("scripts/example0.1.A", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);
		Data var1 = new Data(null, Data.OperationTypes.READ, null, "0.5",
				DataType.SCALAR, ValueType.DOUBLE, false);

		Unary scalar1 = new Unary(data1, var1, Unary.OperationTypes.MULTIPLY,
				DataType.MATRIX, ValueType.DOUBLE);
		Data data2 = new Data("scripts/example0.1.B", Data.OperationTypes.READ,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		MMCJ mmcj1 = new MMCJ(scalar1, data2, DataType.MATRIX, ValueType.DOUBLE);
		Group group1 = new Group(mmcj1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Aggregate agg1 = new Aggregate(group1, Aggregate.OperationTypes.Sum,
				DataType.MATRIX, ValueType.DOUBLE);

		PartialAggregate pagg1 = new PartialAggregate(data1,
				Aggregate.OperationTypes.Sum,
				PartialAggregate.DirectionTypes.Row, DataType.MATRIX,
				ValueType.DOUBLE);
		Group group2 = new Group(pagg1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Aggregate agg2 = new Aggregate(group2, Aggregate.OperationTypes.Sum,
				DataType.MATRIX, ValueType.DOUBLE);

		Group group3 = new Group(agg1, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);
		Group group4 = new Group(agg2, Group.OperationTypes.Sort,
				DataType.MATRIX, ValueType.DOUBLE);

		Binary binary1 = new Binary(group3, group4, Binary.OperationTypes.ADD,
				DataType.MATRIX, ValueType.DOUBLE);

		Data data3 = new Data("scripts/D", Data.OperationTypes.WRITE, binary1,
				null, null, DataType.MATRIX, ValueType.DOUBLE, false);

		data3.printMe();

		Dag<Lops> dag = new Dag<Lops>();

		data3.addToDag(dag);

		ArrayList<Instruction> inst = dag.getJobs(true, null);
		printInstructions(inst);

	}

}
