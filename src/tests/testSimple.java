package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import implementation.PositionIMPL;
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
import query.interfaces.IParamContext;

public class testSimple {

    @Test
    public void testGQueryEtgather() {

        GatherQuery res = new GatherQuery(
                new RecursiveGather("eau", new FinalGather("vent")), new EmptyContinuation());

        IParamContext context = new iparamBouchon(new PositionIMPL(10, 0), "id");
        Map<String, Double> result = res.eval(context);
        // returns a map {vent=25, eau=25}
        assertEquals((Double) 15.0, (Double) result.get("vent"));
        assertEquals((Double) result.get("eau"), (Double) 30.0);
    }

    @Test
    public void testBQuery_AndBExp() {
        AndBooleanExpr res = new AndBooleanExpr(
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("vent"), new ConstantRand(15.0))),
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("vent"), new ConstantRand(15.0))));
        BooleanQuery ress = new BooleanQuery(res, new EmptyContinuation());
        IParamContext context = new iparamBouchon(new PositionIMPL(10, 0), "id");

        Boolean r = (Boolean) ress.eval(context);
        System.out.println(" and " + res.eval(context));
        SensorRand o = new SensorRand("vent");
        ConstantRand j = new ConstantRand(15.0);
        EqualConditionalExpr eqc = new EqualConditionalExpr(o, j);
        System.out.println(o.eval(context));
        System.out.println(j.eval(context));
        System.out.println("eqc : " + eqc.eval(context));
        assertTrue(r);
    }

    @Test
    public void testBQuery_GeqCExp() {
        GreaterOrEqualConditionalExpr res = new GreaterOrEqualConditionalExpr(new SensorRand("vent"),
                new ConstantRand(10.0));
        LesserOrEqualConditionalExpr res2 = new LesserOrEqualConditionalExpr(new SensorRand("eau"),
                new ConstantRand(20.0)); // faux
        OrBooleanExpr res3 = new OrBooleanExpr(new ConditionalExprBooleanExpr(res),
                new ConditionalExprBooleanExpr(res2));
        BooleanQuery ress = new BooleanQuery(res3, new EmptyContinuation());
        IParamContext context = new iparamBouchon(new PositionIMPL(10, 0), "id");
        Boolean r = (Boolean) ress.eval(context);
        System.out.println(" geq " + res.eval(context));
        assertTrue(r);
    }
}
