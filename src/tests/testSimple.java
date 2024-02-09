package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import cps.ast.AndBExp;
import cps.ast.BQuery;
import cps.ast.CExpBExp;
import cps.ast.CRand;
import cps.ast.ECont;
import cps.ast.EQCexp;
import cps.ast.FGather;
import cps.ast.GQuery;
import cps.ast.GeqCExp;
import cps.ast.LeqCExp;
import cps.ast.OrBExp;
import cps.ast.Position;
import cps.ast.RGather;
import cps.ast.SRand;
import cps.interfaces.IParamContext;

public class testSimple {

    @Test
    public void testGQueryEtgather() {

        GQuery res = new GQuery(
                new RGather("eau", new FGather("vent")), new ECont());

        IParamContext context = new iparamBouchon(new Position(10, 0), "id");
        Map<String, Double> result = res.eval(context);
        // returns a map {vent=25, eau=25}
        assertEquals((Double) 15.0 , (Double) result.get("vent"));
        assertEquals((Double) result.get("eau"),(Double) 30.0);
    }

    @Test
    public void testBQuery_AndBExp() {
        AndBExp res = new AndBExp(new CExpBExp(new EQCexp(new SRand("vent"), new CRand(15.0))),
                new CExpBExp(new EQCexp(new SRand("vent"), new CRand(15.0))));
        BQuery ress = new BQuery(res, new ECont());
        IParamContext context = new iparamBouchon(new Position(10, 0), "id");

        Boolean r = (Boolean) ress.eval(context);
        System.out.println(" and " + res.eval(context));
        SRand o = new SRand("vent");
        CRand j = new CRand(15.0);
        EQCexp eqc = new EQCexp(o, j);
        System.out.println(o.eval(context));
        System.out.println(j.eval(context));
        System.out.println("eqc : " + eqc.eval(context));
        assertTrue(r);
    }

    @Test
    public void testBQuery_GeqCExp() {
        GeqCExp res = new GeqCExp(new SRand("vent"), new CRand(10.0));
        LeqCExp res2 = new LeqCExp(new SRand("eau"), new CRand(20.0)); // faux
        OrBExp res3 = new OrBExp(new CExpBExp(res), new CExpBExp(res2));
        BQuery ress = new BQuery(res3, new ECont());
        IParamContext context = new iparamBouchon(new Position(10, 0), "id");
        Boolean r = (Boolean) ress.eval(context);
        System.out.println(" geq " + res.eval(context));
        assertTrue(r);
    }
}
