package tests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.PositionIMPL;
import implementation.SensorDataIMPL;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.ast.AndBooleanExpr;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.EmptyContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalGather;
import query.ast.GatherQuery;
import query.ast.GreaterOrEqualConditionalExpr;
import query.ast.LesserOrEqualConditionalExpr;
import query.ast.OrBooleanExpr;
import query.ast.RecursiveGather;
import query.ast.SensorRand;

public class queryTest {

    class ProcessingNodeTest extends ProcessingNodeIMPL {
        public ProcessingNodeTest() {
            super();
            this.setPostion(new PositionIMPL(2.0, 5.0));
            this.setNodeId("node1");
            Map sensorData = new HashMap<String, SensorDataI>() {
                {
                    put("vent", new SensorDataIMPL(getNodeIdentifier(), "vent", 15.0, null, null));
                    put("eau", new SensorDataIMPL(getNodeIdentifier(), "eau", 30.0, null, null));
                }
            };
            this.setSensorDataMap(sensorData);
        }
    }

    class ExecutionStateTest extends ExecutionStateIMPL {
        public ExecutionStateTest() {
            super();
            this.updateProcessingNode(new ProcessingNodeTest());
        }
    }

    @Test
    public void testGQueryEtgather() {

        GatherQuery res = new GatherQuery(
                new RecursiveGather("eau", new FinalGather("vent")), new EmptyContinuation());

        ExecutionStateI context = new ExecutionStateTest();
        QueryResultI result = res.eval(context);
        List<SensorDataI> gathered = result.gatheredSensorsValues();
        for (SensorDataI sensor : gathered) {
            if (sensor.getSensorIdentifier().equals("eau")) {
                assertEquals(30.0, (Double) sensor.getValue(), 0.0);
            }
            if (sensor.getSensorIdentifier().equals("vent")) {
                assertEquals(15.0, (Double) sensor.getValue(), 0.0);
            }
        }
    }

    @Test
    public void testBQuery_AndBExp() {
        AndBooleanExpr res = new AndBooleanExpr(
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("vent"), new ConstantRand(15.0))),
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("vent"), new ConstantRand(15.0))));
        BooleanQuery ress = new BooleanQuery(res, new EmptyContinuation());
        ExecutionStateI context = new ExecutionStateTest();

        QueryResultI r = ress.eval(context);
        SensorRand o = new SensorRand("vent");
        ConstantRand j = new ConstantRand(15.0);
        EqualConditionalExpr eqc = new EqualConditionalExpr(o, j);
        o.eval(context);
        j.eval(context);
        eqc.eval(context);
        assertEquals(r.positiveSensorNodes().size(), 1);
        assertEquals(r.positiveSensorNodes().get(0), "node1");
    }

    @Test
    public void testBQuery_GeqCExp() {
        GreaterOrEqualConditionalExpr res = new GreaterOrEqualConditionalExpr(new SensorRand("vent"),
                new ConstantRand(10.0));
        LesserOrEqualConditionalExpr res2 = new LesserOrEqualConditionalExpr(new SensorRand("eau"),
                new ConstantRand(40.0)); // faux
        OrBooleanExpr res3 = new OrBooleanExpr(new ConditionalExprBooleanExpr(res),
                new ConditionalExprBooleanExpr(res2));
        BooleanQuery ress = new BooleanQuery(res3, new EmptyContinuation());
        ExecutionStateI context = new ExecutionStateTest();
        QueryResultI result = ress.eval(context);

        assertEquals(result.positiveSensorNodes().size(), 1);
        assertEquals(result.positiveSensorNodes().get(0), "node1");

    }

}
