package tests;

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
        // Request , ===> we're testing the system under pressure with a query that will
        // do the collection of all sensors in the network
        // ----------------------
        static GatherQuery query1 = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new FloodingContinuation(new RelativeBase(), 1505.0));
        static GatherQuery query2 = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new FloodingContinuation(new RelativeBase(), 1505.0));
        static GatherQuery query3 = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new FloodingContinuation(new RelativeBase(), 1505.0));
        static GatherQuery query4 = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new FloodingContinuation(new RelativeBase(), 1505.0));
        static GatherQuery query5 = new GatherQuery(
                        new RecursiveGather("temperature",
                                        new FinalGather("humidity")),
                        new FloodingContinuation(new RelativeBase(), 1505.0));
        // -------------------Gather Query Test 2 : flooding continuation , Async
        // Request , ===> Result : 26 sensors
        // -------------------
        // -------------------Gather Query Test 3 : direction continuation , Sync
        // Request , ===> Result : 16 sensors
        // -------------------
        static GatherQuery query6 = new GatherQuery(
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
        static BooleanQuery query7 = new BooleanQuery(
                        new ConditionalExprBooleanExpr(
                                        new EqualConditionalExpr(new SensorRand("humidity"),
                                                        new ConstantRand(70.0))),
                        new FloodingContinuation(new RelativeBase(), 45.0));

        // all are the same
        // query , a gather with flooding continuation to test the system under pressure
        // with a query that has assured continuation
        public static List<QueryI> queries1 = Arrays.asList(query1);
        public static List<QueryI> queries2 = Arrays.asList(query2);
        public static List<QueryI> queries3 = Arrays.asList(query3);
        public static List<QueryI> queries4 = Arrays.asList(query4);
        public static List<QueryI> queries5 = Arrays.asList(query5);

}
