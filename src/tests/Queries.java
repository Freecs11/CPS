package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.DirectionContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalDirections;
import query.ast.FinalGather;
import query.ast.FloodingContinuation;
import query.ast.GatherQuery;
import query.ast.RecursiveDirections;
import query.ast.RecursiveGather;
import query.ast.RelativeBase;
import query.ast.SensorRand;

public class Queries {

        // ------------------- Gather Query Test 1 : Flooding continuation , Sync
        // Request , ===> Result : 26 sensors
        // ----------------------
        static GatherQuery query = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new FloodingContinuation(new RelativeBase(), 45.0));

        // -------------------Gather Query Test 2 : flooding continuation , Async
        // Request , ===> Result : 26 sensors
        // -------------------
        // -------------------Gather Query Test 3 : direction continuation , Sync
        // Request , ===> Result : 16 sensors
        // -------------------
        static GatherQuery query2 = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                        new FinalDirections(Direction.NE))));

        // -------------------Gather Query Test 4 : direction continuation , Async
        // Request
        // -------------------
        // ------------------- Boolean Query Test 1 : Flooding continuation , Sync
        // Request
        // -------------------
        static BooleanQuery query3 = new BooleanQuery(
                        new ConditionalExprBooleanExpr(
                                        new EqualConditionalExpr(new SensorRand("humidity"),
                                                        new ConstantRand(70.0))),
                        new FloodingContinuation(new RelativeBase(), 45.0));

        // ------------------- Boolean Query Test 2 : Flooding continuation , Async
        // Request
        // -------------------

        public static List<QueryI> queries = new ArrayList<>(Arrays.asList(query, query2, query3));
}
