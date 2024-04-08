package tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.PositionIMPL;
import implementation.QueryResultIMPL;
import implementation.SensorDataIMPL;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.abstraction.AbstractRand;
import query.ast.AbsoluteBase;
import query.ast.AndBooleanExpr;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.DirectionContinuation;
import query.ast.EmptyContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalDirections;
import query.ast.FinalGather;
import query.ast.FloodingContinuation;
import query.ast.GatherQuery;
import query.ast.GreaterOrEqualConditionalExpr;
import query.ast.LesserOrEqualConditionalExpr;
import query.ast.NotBooleanExpr;
import query.ast.OrBooleanExpr;
import query.ast.RecursiveDirections;
import query.ast.RecursiveGather;
import query.ast.RelativeBase;
import query.ast.SensorBooleanExpr;
import query.ast.SensorRand;

/**
 * Tests for the query language used in the project.
 */
public class queryLanguageTests {

    private ExecutionStateI executionStateTests1;
    private ExecutionStateI executionStateTests2;

    private PositionI position1;
    private PositionI position2;

    private void initialisation() throws Exception {
        ProcessingNodeIMPL pn1 = new ProcessingNodeIMPL();
        String nodeId1 = "node1";
        pn1.setNodeId(nodeId1);
        position1 = new PositionIMPL(1.0, 1.0);
        pn1.setPostion(position1);
        Map<String, SensorDataI> sensorDataMap = new HashMap<String, SensorDataI>();
        pn1.setSensorDataMap(sensorDataMap);
        pn1.addSensorData("capteur_fumee", 13.5);
        pn1.addSensorData("capteur_temperature", 20.0);
        pn1.addSensorData("capteur_humidite", 10.0);
        pn1.addSensorData("capteur_luminosite", 10.0);
        pn1.addSensorData("capteur_presence", true);

        executionStateTests1 = new ExecutionStateIMPL(pn1);

        ProcessingNodeIMPL pn2 = new ProcessingNodeIMPL();
        String nodeId2 = "node2";
        pn2.setNodeId(nodeId2);
        position2 = new PositionIMPL(2.0, 2.0);
        pn2.setPostion(position2);
        Map<String, SensorDataI> sensorDataMap2 = new HashMap<String, SensorDataI>();
        pn2.setSensorDataMap(sensorDataMap2);
        pn2.addSensorData("capteur_fumee", 12.5);
        pn2.addSensorData("capteur_temperature", 25.0);
        pn2.addSensorData("capteur_humidite", 50.0);
        pn2.addSensorData("capteur_luminosite", 100.0);
        pn2.addSensorData("capteur_presence", false);

        executionStateTests2 = new ExecutionStateIMPL(pn2);
    }

    // Ici on teste la fonction eval de tous notre langage de requête

    /**
     * Test pour la fonction eval de SensorRand qui permet de récupérer
     * la valeur d'un capteur
     * 
     * @throws Exception
     */
    @Test
    public void SensorRandQueryTest() throws Exception {
        initialisation();
        SensorRand sensorRandQuery = new SensorRand("capteur_fumee");
        assertEquals(sensorRandQuery.eval(executionStateTests1), 13.5);
        assertEquals(sensorRandQuery.eval(executionStateTests2), 12.5);
        SensorRand sensorRandQuery2 = new SensorRand("capteur_temperature");
        assertEquals(sensorRandQuery2.eval(executionStateTests1), 20.0);
        assertEquals(sensorRandQuery2.eval(executionStateTests2), 25.0);
    }

    /**
     * Test pour la fonction eval de ConstantRand qui permet de définir
     * une valeur constante de type double
     *
     * @throws Exception
     */
    @Test
    public void ConstantRandTest() throws Exception {
        initialisation();

        ConstantRand constantRand = new ConstantRand(10.0);
        double res = constantRand.eval(executionStateTests1);
        assertEquals(res, 10.0, 0.0);
    }

    /**
     * Test pour la fonction eval de SensorBooleanExpr qui permet de récupérer
     * la valeur d'un capteur de type boolean, et de lever une exception si
     * le capteur n'est pas de type boolean
     *
     * @throws Exception
     */
    @Test
    public void SensorBooleanExprTest() throws Exception {
        initialisation();
        SensorBooleanExpr sensorBooleanExpr = new SensorBooleanExpr("capteur_presence");
        assertEquals(sensorBooleanExpr.eval(executionStateTests1), true);
        assertEquals(sensorBooleanExpr.eval(executionStateTests2), false);

        // test de l'exception si le capteur n'est pas de type boolean
        SensorBooleanExpr sensorBooleanExpr2 = new SensorBooleanExpr("capteur_fumee");
        try {
            sensorBooleanExpr2.eval(executionStateTests1);
        } catch (RuntimeException e) {
            assertEquals(e.getMessage(), "Sensor data is not of type Boolean");
        }
    }

    /**
     * Test pour la fonction eval de RelativeBase qui permet d'utiliser
     * la position du noeud courant ( ProcessingNode ) se trouvant dans
     * l'executionState.
     * 
     * @throws Exception
     */
    @Test
    public void RelativeBaseTest() throws Exception {
        initialisation();
        RelativeBase relativeBase = new RelativeBase();
        assertEquals(relativeBase.eval(executionStateTests1), position1);
        assertEquals(relativeBase.eval(executionStateTests2), position2);
    }

    /**
     * Test pour la fonction eval de AbsoluteBase qui permet d'utiliser
     * la position donnée en paramètre
     * 
     * @throws Exception
     */
    @Test
    public void AbsoluteBaseTest() throws Exception {
        initialisation();
        PositionI position3 = new PositionIMPL(3.0, 3.0);
        AbsoluteBase absoluteBase = new AbsoluteBase(position3);
        assertEquals(absoluteBase.eval(executionStateTests1), position3);
    }

    // ----------------- Tests pour les expressions de directions (construction)
    // -----------------

    /**
     * Test pour la fonction eval de RecursiveDirections qui permet de contruire les
     * directions
     *
     * @throws Exception
     */
    @Test
    public void RecursiveDirectionsTest() throws Exception {
        initialisation();

        // Le cas où on a une direction et le cas de base de la récursion , 2 directions
        RecursiveDirections recursiveDirections = new RecursiveDirections(Direction.NW,
                new FinalDirections(Direction.NE));

        Set<Direction> directions = new HashSet<Direction>();
        directions.add(Direction.NW);
        directions.add(Direction.NE);

        assertEquals(recursiveDirections.eval(executionStateTests1), directions);

        // Le cas où on a une direction et un cas récursif, 4 directions
        RecursiveDirections recursiveDirections2 = new RecursiveDirections(Direction.NW,
                new RecursiveDirections(Direction.NE, new RecursiveDirections(Direction.SE,
                        new FinalDirections(Direction.SW))));
        Set<Direction> directions2 = new HashSet<Direction>();
        directions2.add(Direction.NW);
        directions2.add(Direction.NE);
        directions2.add(Direction.SE);
        directions2.add(Direction.SW);

        assertEquals(recursiveDirections2.eval(executionStateTests1), directions2);
    }

    /**
     * Test pour la fonction eval de FinalDirections qui permet de contruire les
     * directions et qui est le cas de fin de la récursion
     *
     * @throws Exception
     */
    @Test
    public void FinalDirectionsTest() throws Exception {
        initialisation();
        Direction direction = Direction.NW;

        FinalDirections finalDirections = new FinalDirections(direction);
        Set<Direction> directions = new HashSet<Direction>();
        directions.add(direction);

        assertEquals(finalDirections.eval(executionStateTests1), directions);
    }

    // ----------------- Tests pour les expressions conditionnelles
    // -----------------

    /**
     * Test pour la fonction eval de LesserThanConditionalExpr qui permet de
     * comparer si
     * deux valeurs sont inférieures ou non ( < )
     *
     * @throws Exception
     */
    @Test
    public void GreaterThanConditionalExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(5.0);
        AbstractRand rand3 = new ConstantRand(10.0);
        AbstractRand rand4 = new ConstantRand(10.0);

        // rand1 > rand2
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(rand1, rand2);
        assertEquals(greaterOrEqualConditionalExpr.eval(executionStateTests1), true);

        // rand1 > rand3
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr2 = new GreaterOrEqualConditionalExpr(rand1, rand3);
        assertEquals(greaterOrEqualConditionalExpr2.eval(executionStateTests1), true);

        // passing SensorRand objects
        SensorRand sensorRand1 = new SensorRand("capteur_fumee");
        SensorRand sensorRand2 = new SensorRand("capteur_luminosite");

        // sensorRand1 (13.5) > sensorRand2 (10.0)
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr3 = new GreaterOrEqualConditionalExpr(sensorRand1,
                sensorRand2);
        assertEquals(greaterOrEqualConditionalExpr3.eval(executionStateTests1), true);

        // sensorRand and ConstantRand
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr4 = new GreaterOrEqualConditionalExpr(sensorRand1,
                rand4);

        // sensorRand1 (13.5) > 10.0
        assertEquals(greaterOrEqualConditionalExpr4.eval(executionStateTests1), true);
    }

    /**
     * Test pour la fonction eval de GreaterOrEqualConditionalExpr qui permet de
     * comparer si
     * deux valeurs sont supérieures ou égales ou non ( >= )
     *
     * @throws Exception
     */
    @Test
    public void GreaterThanOrEqualConditionalExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(5.0);
        AbstractRand rand3 = new ConstantRand(10.0);
        AbstractRand rand4 = new ConstantRand(10.0);

        // rand1 > rand2
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(rand1, rand2);
        assertEquals(greaterOrEqualConditionalExpr.eval(executionStateTests1), true);

        // rand1 == rand3
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr2 = new GreaterOrEqualConditionalExpr(rand1, rand3);
        assertEquals(greaterOrEqualConditionalExpr2.eval(executionStateTests1), true);

        // rand1 == rand4
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr3 = new GreaterOrEqualConditionalExpr(rand1, rand4);
        assertEquals(greaterOrEqualConditionalExpr3.eval(executionStateTests1), true);

        // passing SensorRand objects
        SensorRand sensorRand1 = new SensorRand("capteur_fumee"); // 13.5
        SensorRand sensorRand2 = new SensorRand("capteur_luminosite"); // 10.0
        SensorRand sensorRand3 = new SensorRand("capteur_humidite"); // 10.0

        // sensorRand1 (13.5) > sensorRand2 (10.0)
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr4 = new GreaterOrEqualConditionalExpr(sensorRand1,
                sensorRand2);
        assertEquals(greaterOrEqualConditionalExpr4.eval(executionStateTests1), true);

        // sensorRand2 (10.0) == sensorRand3 (10.0)
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr5 = new GreaterOrEqualConditionalExpr(sensorRand2,
                sensorRand3);
        assertEquals(greaterOrEqualConditionalExpr5.eval(executionStateTests1), true);

        // sensorRand and ConstantRand
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr6 = new GreaterOrEqualConditionalExpr(sensorRand1,
                rand4);

        // sensorRand1 (13.5) > 10.0
        assertEquals(greaterOrEqualConditionalExpr6.eval(executionStateTests1), true);

        // sensorRand2 (10.0) == 10.0
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr7 = new GreaterOrEqualConditionalExpr(sensorRand2,
                rand4);
        assertEquals(greaterOrEqualConditionalExpr7.eval(executionStateTests1), true);
    }

    /**
     * Test pour la fonction eval de LesserThanConditionalExpr qui permet de
     * comparer si
     * deux valeurs sont inférieures ou non ( < )
     *
     * @throws Exception
     */
    @Test
    public void LesserThanConditionalExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(20.0);
        AbstractRand rand2 = new ConstantRand(17.0);

        // rand1 ! < rand2 ( renvoie false à l'execution de la requete )
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr = new LesserOrEqualConditionalExpr(rand1, rand2);
        assertEquals(lesserOrEqualConditionalExpr.eval(executionStateTests1), false);
        // rand2 < rand1
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr2 = new LesserOrEqualConditionalExpr(rand2, rand1);
        assertEquals(lesserOrEqualConditionalExpr2.eval(executionStateTests1), true);

        // passing SensorRand objects
        SensorRand sensorRand1 = new SensorRand("capteur_temperature"); // 20.0
        SensorRand sensorRand2 = new SensorRand("capteur_humidite"); // 10.0

        // sensorRand2 (10.0) < sensorRand1 (20.0)
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr3 = new LesserOrEqualConditionalExpr(sensorRand2,
                sensorRand1);
        assertEquals(lesserOrEqualConditionalExpr3.eval(executionStateTests1), true);

        // sensorRand2 (10.0) < 17.0
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr4 = new LesserOrEqualConditionalExpr(sensorRand2,
                rand2);
        assertEquals(lesserOrEqualConditionalExpr4.eval(executionStateTests1), true);

    }

    /**
     * Test pour la fonction eval de GreaterOrEqualConditionalExpr qui permet de
     * comparer si
     * deux valeurs sont supérieures ou égales ou non ( >= )
     *
     * @throws Exception
     */
    @Test
    public void LesserThanOrEqualConditionalExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(15.0);
        AbstractRand rand2 = new ConstantRand(40.0);

        // rand2 ! <= rand1 ( renvoie false à l'execution de la requete )
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr = new LesserOrEqualConditionalExpr(rand2, rand1);
        assertEquals(lesserOrEqualConditionalExpr.eval(executionStateTests1), false);
        // rand1 <= rand2
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr2 = new LesserOrEqualConditionalExpr(rand1, rand2);
        assertEquals(lesserOrEqualConditionalExpr2.eval(executionStateTests1), true);

        // passing SensorRand objects
        SensorRand sensorRand1 = new SensorRand("capteur_temperature"); // 20.0
        SensorRand sensorRand2 = new SensorRand("capteur_humidite"); // 10.0

        // sensorRand2 (10.0) <= sensorRand1 (20.0)
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr3 = new LesserOrEqualConditionalExpr(sensorRand2,
                sensorRand1);
        assertEquals(lesserOrEqualConditionalExpr3.eval(executionStateTests1), true);

        // sensorRand2 (10.0) <= 40.0
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr4 = new LesserOrEqualConditionalExpr(sensorRand2,
                rand2);
        assertEquals(lesserOrEqualConditionalExpr4.eval(executionStateTests1), true);
    }

    /**
     * Test pour la fonction eval de EqualConditionalExpr qui permet de comparer si
     * deux valeurs sont égales ou non ( == )
     * 
     * @throws Exception
     */
    @Test
    public void EqualConditionalExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(10.0);
        AbstractRand rand3 = new ConstantRand(20.0);

        // rand1 == rand2
        EqualConditionalExpr equalConditionalExpr = new EqualConditionalExpr(rand1, rand2);
        assertEquals(equalConditionalExpr.eval(executionStateTests1), true);

        // rand1 != rand3
        EqualConditionalExpr equalConditionalExpr2 = new EqualConditionalExpr(rand1, rand3);
        assertEquals(equalConditionalExpr2.eval(executionStateTests1), false);

        // passing SensorRand objects
        SensorRand sensorRand1 = new SensorRand("capteur_temperature"); // 20.0

        // sensorRand1 == rand3
        EqualConditionalExpr equalConditionalExpr3 = new EqualConditionalExpr(sensorRand1, rand3);
        assertEquals(equalConditionalExpr3.eval(executionStateTests1), true);
    }

    // ----------------- Tests pour les expressions booléennes -----------------

    /**
     * Test pour la fonction eval de ConditionalExprBooleanExpr qui permet de
     * lancer une expression conditionnelle et de retourner un boolean en fonction
     * de l'évaluation de l'expression conditionnelle
     *
     * @throws Exception
     */
    @Test
    public void ConditionalExprBooleanExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(5.0);

        // rand1 > rand2
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(rand1, rand2);

        ConditionalExprBooleanExpr conditionalExprBooleanExpr = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr);
        assertEquals(conditionalExprBooleanExpr.eval(executionStateTests1), true); // 10.0 >= 5.0 = true
    }

    /**
     * Test pour la fonction eval de OrBooleanExpr qui permet de retourner
     * true si l'une des expressions booléennes est vraie
     * 
     * @throws Exception
     */
    @Test
    public void OrBooleanExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(5.0);
        // rand1 > rand2 = true
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(rand1, rand2);
        ConditionalExprBooleanExpr conditionalExprBooleanExpr = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr); // true
        assertEquals(conditionalExprBooleanExpr.eval(executionStateTests1), true);

        // rand1 < rand2 = false
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr = new LesserOrEqualConditionalExpr(rand1, rand2);
        ConditionalExprBooleanExpr conditionalExprBooleanExpr2 = new ConditionalExprBooleanExpr(
                lesserOrEqualConditionalExpr); // false
        assertEquals(conditionalExprBooleanExpr2.eval(executionStateTests1), false);

        // rand1 > rand2 || rand1 < rand2
        OrBooleanExpr orBooleanExpr = new OrBooleanExpr(conditionalExprBooleanExpr, conditionalExprBooleanExpr2); // true
        assertEquals(orBooleanExpr.eval(executionStateTests1), true);

        // rand1 < rand2 || rand1 < rand2
        OrBooleanExpr orBooleanExpr2 = new OrBooleanExpr(conditionalExprBooleanExpr2, conditionalExprBooleanExpr2); // false
        assertEquals(orBooleanExpr2.eval(executionStateTests1), false);
    }

    /**
     * Test pour la fonction eval de AndBooleanExpr qui permet de retourner
     * true si les deux expressions booléennes sont vraies
     * 
     * @throws Exception
     */
    @Test
    public void AndBooleanExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(5.0);
        // rand1 > rand2 = true
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(rand1, rand2);
        ConditionalExprBooleanExpr conditionalExprBooleanExpr = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr); // true
        assertEquals(conditionalExprBooleanExpr.eval(executionStateTests1), true);

        // rand1 < rand2 = false
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr = new LesserOrEqualConditionalExpr(rand1, rand2);
        ConditionalExprBooleanExpr conditionalExprBooleanExpr2 = new ConditionalExprBooleanExpr(
                lesserOrEqualConditionalExpr); // false
        assertEquals(conditionalExprBooleanExpr2.eval(executionStateTests1), false);

        // rand1 > rand2 && rand1 < rand2
        AndBooleanExpr andBooleanExpr = new AndBooleanExpr(conditionalExprBooleanExpr, conditionalExprBooleanExpr2); // false
        assertEquals(andBooleanExpr.eval(executionStateTests1), false);

        // rand1 > rand2 && rand1 > rand2
        AndBooleanExpr andBooleanExpr2 = new AndBooleanExpr(conditionalExprBooleanExpr, conditionalExprBooleanExpr); // true
        assertEquals(andBooleanExpr2.eval(executionStateTests1), true);
    }

    /**
     * Test pour la fonction eval de NotBooleanExpr qui permet de retourner
     * l'inverse d'une expression booléenne
     *
     * @throws Exception
     */
    @Test
    public void NotBooleanExprTest() throws Exception {
        initialisation();

        AbstractRand rand1 = new ConstantRand(10.0);
        AbstractRand rand2 = new ConstantRand(5.0);
        // rand1 > rand2
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(rand1, rand2);
        ConditionalExprBooleanExpr conditionalExprBooleanExpr = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr); // true
        assertEquals(conditionalExprBooleanExpr.eval(executionStateTests1), true);
        NotBooleanExpr notBooleanExpr = new NotBooleanExpr(conditionalExprBooleanExpr); // false
        assertEquals(notBooleanExpr.eval(executionStateTests1), false);

        // rand1 < rand2
        LesserOrEqualConditionalExpr lesserOrEqualConditionalExpr = new LesserOrEqualConditionalExpr(rand1, rand2);
        ConditionalExprBooleanExpr conditionalExprBooleanExpr2 = new ConditionalExprBooleanExpr(
                lesserOrEqualConditionalExpr); // false
        assertEquals(conditionalExprBooleanExpr2.eval(executionStateTests1), false);
        NotBooleanExpr notBooleanExpr2 = new NotBooleanExpr(conditionalExprBooleanExpr2); // true
        assertEquals(notBooleanExpr2.eval(executionStateTests1), true);
    }

    // ----------------- Tests pour les continuations -----------------

    /**
     * Test pour la fonction eval de EmptyContinuation qui marque que la requête
     * est uniquement locale et ne nécessite pas de communication
     * 
     * @throws Exception
     */
    @Test
    public void EmptyContinuationTest() throws Exception {
        // initialisation();
        // la continutation est vide, donc on ne fait rien, dans le eval on retourne un
        // null
        assert (true);
    }

    /**
     * Test pour la fonction eval de FloodingContinuation qui permet de
     * marquer que la requête doit être propagée à tous les voisins avec
     * une certaine distance
     * 
     * @throws Exception
     */
    @Test
    public void FloodingContinuationTest() throws Exception {
        initialisation();

        RelativeBase relativeBase = new RelativeBase();
        double distance = 10.0;
        FloodingContinuation floodingContinuation = new FloodingContinuation(relativeBase, distance);
        floodingContinuation.eval(executionStateTests1);

        // On vérifie que les informations de l'executionState ont bien été modifiées
        ExecutionStateIMPL executionStateIMPL = (ExecutionStateIMPL) executionStateTests1;
        assertEquals(executionStateIMPL.getMaxDistance(), distance, 0.0);
        assertEquals(executionStateIMPL.isFlooding(), true);
        assertEquals(executionStateIMPL.getProcessingNode().getPosition(), position1);
    }

    /**
     * Test pour la fonction eval de DirectionContinuation qui permet de
     * marquer que la requête doit être propagée dans une direction donnée
     *
     * @throws Exception
     */
    @Test
    public void DirectionContinuationTest() throws Exception {
        initialisation();

        int jumps = 5;
        RecursiveDirections recursiveDirections = new RecursiveDirections(Direction.NW,
                new FinalDirections(Direction.NE));
        DirectionContinuation directionContinuation = new DirectionContinuation(jumps, recursiveDirections);
        directionContinuation.eval(executionStateTests1);

        // On vérifie que les informations de l'executionState ont bien été modifiées
        ExecutionStateIMPL executionStateIMPL = (ExecutionStateIMPL) executionStateTests1;
        assertEquals(executionStateIMPL.getMaxHops(), jumps);
        assertEquals(executionStateIMPL.isDirectional(), true);

        Set<Direction> directions = new HashSet<Direction>();
        directions.add(Direction.NW);
        directions.add(Direction.NE);
        assertEquals(executionStateIMPL.getDirections(), directions); // NW, NE
    }

    // ----------------- Tests pour les requêtes -----------------

    // Gather tests

    /**
     * Test pour la fonction eval de FinalGather qui permet de récupérer
     * les valeurs des capteurs
     *
     * @throws Exception
     */
    @Test
    public void FinalGatherTest() throws Exception {
        initialisation();

        // le eval renvoie la liste des sensorData ArrayList<SensorDataI>
        FinalGather finalGather = new FinalGather("capteur_fumee");
        ArrayList<SensorDataI> sensorDataList = finalGather.eval(executionStateTests1);
        // on vérifie que la liste contient bien un seul élément
        assertEquals(sensorDataList.size(), 1);

        // on vérifie que l'élément est bien de type SensorDataIMPL
        SensorDataIMPL sensorDataIMPL = (SensorDataIMPL) sensorDataList.get(0);
        assertEquals(sensorDataIMPL.getSensorIdentifier(), "capteur_fumee");
        assertEquals(sensorDataIMPL.getValue(), 13.5);
        assertEquals(sensorDataIMPL.getType(), Double.class);
    }

    @Test
    public void RecursiveGatherTest() throws Exception {
        initialisation();

        RecursiveGather recursiveGather = new RecursiveGather("capteur_fumee", new FinalGather("capteur_temperature"));
        ArrayList<SensorDataI> sensorDataList = recursiveGather.eval(executionStateTests1);
        // on vérifie que la liste contient bien deux éléments
        assertEquals(sensorDataList.size(), 2);

        // on vérifie que les éléments sont bien de type SensorDataIMPL
        SensorDataIMPL sensorDataIMPL1 = (SensorDataIMPL) sensorDataList.get(1);
        assertEquals(sensorDataIMPL1.getSensorIdentifier(), "capteur_fumee");
        assertEquals(sensorDataIMPL1.getValue(), 13.5);
        assertEquals(sensorDataIMPL1.getType(), Double.class);

        SensorDataIMPL sensorDataIMPL2 = (SensorDataIMPL) sensorDataList.get(0);
        assertEquals(sensorDataIMPL2.getSensorIdentifier(), "capteur_temperature");
        assertEquals(sensorDataIMPL2.getValue(), 20.0);
        assertEquals(sensorDataIMPL2.getType(), Double.class);

        RecursiveGather recursiveGather2 = new RecursiveGather("capteur_fumee",
                new RecursiveGather("capteur_temperature", new FinalGather(
                        "capteur_presence")));
        ArrayList<SensorDataI> sensorDataList2 = recursiveGather2.eval(executionStateTests1);

        // on vérifie que la liste contient bien trois éléments
        assertEquals(sensorDataList2.size(), 3);

        // on vérifie que les éléments sont bien de type SensorDataIMPL
        SensorDataIMPL sensorDataIMPL3 = (SensorDataIMPL) sensorDataList2.get(2);
        assertEquals(sensorDataIMPL3.getSensorIdentifier(), "capteur_fumee");
        assertEquals(sensorDataIMPL3.getValue(), 13.5);
        assertEquals(sensorDataIMPL3.getType(), Double.class);

        SensorDataIMPL sensorDataIMPL4 = (SensorDataIMPL) sensorDataList2.get(1);
        assertEquals(sensorDataIMPL4.getSensorIdentifier(), "capteur_temperature");
        assertEquals(sensorDataIMPL4.getValue(), 20.0);
        assertEquals(sensorDataIMPL4.getType(), Double.class);

        SensorDataIMPL sensorDataIMPL5 = (SensorDataIMPL) sensorDataList2.get(0);
        assertEquals(sensorDataIMPL5.getSensorIdentifier(), "capteur_presence");
        assertEquals(sensorDataIMPL5.getValue(), true);
        assertEquals(sensorDataIMPL5.getType(), Boolean.class);
    }

    // ----------------- Test pour le GatherQuery -----------------

    @Test
    public void GQueryTest() throws Exception {
        initialisation();

        // on teste le GatherQuery avec les 3 types de continuations

        // EmptyContinuation
        RecursiveGather recursiveGather = new RecursiveGather("capteur_fumee", new FinalGather("capteur_temperature"));
        GatherQuery gatherQuery = new GatherQuery(recursiveGather, new EmptyContinuation());
        QueryResultIMPL queryResultIMPL = (QueryResultIMPL) gatherQuery.eval(executionStateTests1);

        // on vérifie que la liste contient bien deux éléments
        assertEquals(queryResultIMPL.getGatheredSensors().size(), 2);

        // on vérifie que les éléments sont bien de type SensorDataIMPL
        SensorDataIMPL sensorDataIMPL1 = (SensorDataIMPL) queryResultIMPL.getGatheredSensors().get(1);
        assertEquals(sensorDataIMPL1.getSensorIdentifier(), "capteur_fumee");
        assertEquals(sensorDataIMPL1.getValue(), 13.5);
        assertEquals(sensorDataIMPL1.getType(), Double.class);

        SensorDataIMPL sensorDataIMPL2 = (SensorDataIMPL) queryResultIMPL.getGatheredSensors().get(0);
        assertEquals(sensorDataIMPL2.getSensorIdentifier(), "capteur_temperature");
        assertEquals(sensorDataIMPL2.getValue(), 20.0);
        assertEquals(sensorDataIMPL2.getType(), Double.class);

        // on vérifie que les autres attributs sont bien à false
        assertEquals(queryResultIMPL.isBooleanRequest(), false);
        assertEquals(queryResultIMPL.isGatherRequest(), true);
        assertEquals(queryResultIMPL.getPositiveSN().size(), 0);

        // FloodingContinuation
        // NOTE: on a déjà testé la fonction eval de DirectionContinuation et de
        // FloodingContinuation et des autres classes
        initialisation();
        RecursiveGather recursiveGather2 = new RecursiveGather("capteur_fumee",
                new FinalGather("capteur_temperature"));
        GatherQuery gatherQuery2 = new GatherQuery(recursiveGather2,
                new FloodingContinuation(new RelativeBase(), 10.0));
        QueryResultIMPL queryResultIMPL2 = (QueryResultIMPL) gatherQuery2.eval(executionStateTests1);

        // on vérifie que la liste contient bien deux éléments
        assertEquals(queryResultIMPL2.getGatheredSensors().size(), 2);

        // on vérifie que les éléments sont bien de type SensorDataIMPL
        SensorDataIMPL sensorDataIMPL3 = (SensorDataIMPL) queryResultIMPL2.getGatheredSensors().get(1);
        assertEquals(sensorDataIMPL3.getSensorIdentifier(), "capteur_fumee");
        assertEquals(sensorDataIMPL3.getValue(), 13.5);
        assertEquals(sensorDataIMPL3.getType(), Double.class);

        SensorDataIMPL sensorDataIMPL4 = (SensorDataIMPL) queryResultIMPL2.getGatheredSensors().get(0);
        assertEquals(sensorDataIMPL4.getSensorIdentifier(), "capteur_temperature");
        assertEquals(sensorDataIMPL4.getValue(), 20.0);
        assertEquals(sensorDataIMPL4.getType(), Double.class);

        // on vérifie que les autres attributs sont bien à false
        assertEquals(queryResultIMPL2.isBooleanRequest(), false);
        assertEquals(queryResultIMPL2.isGatherRequest(), true);
        assertEquals(queryResultIMPL2.getPositiveSN().size(), 0);

        // DirectionContinuation
        initialisation();
        RecursiveGather recursiveGather3 = new RecursiveGather("capteur_fumee",
                new FinalGather("capteur_temperature"));
        GatherQuery gatherQuery3 = new GatherQuery(recursiveGather3,
                new DirectionContinuation(5, new RecursiveDirections(Direction.NW, new FinalDirections(Direction.NE))));
        QueryResultIMPL queryResultIMPL3 = (QueryResultIMPL) gatherQuery3.eval(executionStateTests1);

        // on vérifie que la liste contient bien deux éléments
        assertEquals(queryResultIMPL3.getGatheredSensors().size(), 2);

        // on vérifie que les éléments sont bien de type SensorDataIMPL
        SensorDataIMPL sensorDataIMPL5 = (SensorDataIMPL) queryResultIMPL3.getGatheredSensors().get(1);
        assertEquals(sensorDataIMPL5.getSensorIdentifier(), "capteur_fumee");
        assertEquals(sensorDataIMPL5.getValue(), 13.5);
        assertEquals(sensorDataIMPL5.getType(), Double.class);

        SensorDataIMPL sensorDataIMPL6 = (SensorDataIMPL) queryResultIMPL3.getGatheredSensors().get(0);
        assertEquals(sensorDataIMPL6.getSensorIdentifier(), "capteur_temperature");
        assertEquals(sensorDataIMPL6.getValue(), 20.0);
        assertEquals(sensorDataIMPL6.getType(), Double.class);
    }

    // ----------------- Tests pour les requêtes booléennes -----------------

    @Test
    public void BooleanQueryTest() throws Exception {
        initialisation();

        // on teste les requêtes booléennes avec les 3 types de continuations comme pour
        // les requêtes de type Gather

        // EmptyContinuation
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr = new GreaterOrEqualConditionalExpr(
                new SensorRand("capteur_fumee"), new ConstantRand(10.0));
        ConditionalExprBooleanExpr conditionalExprBooleanExpr = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr); // true
        BooleanQuery booleanQuery = new BooleanQuery(conditionalExprBooleanExpr, new EmptyContinuation());

        QueryResultIMPL queryResultIMPL = (QueryResultIMPL) booleanQuery.eval(executionStateTests1);
        assertEquals(queryResultIMPL.isBooleanRequest(), true);
        assertEquals(queryResultIMPL.isGatherRequest(), false);
        assertEquals(queryResultIMPL.getGatheredSensors().size(), 0);
        assertEquals(queryResultIMPL.getPositiveSN().size(), 1);

        // on vérifie que c'est bien le bon noeud qui a été ajouté
        assertEquals(queryResultIMPL.getPositiveSN().get(0), "node1");

        // FloodingContinuation
        initialisation();
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr2 = new GreaterOrEqualConditionalExpr(
                new SensorRand("capteur_fumee"), new ConstantRand(15.0));
        ConditionalExprBooleanExpr conditionalExprBooleanExpr2 = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr2); // false
        BooleanQuery booleanQuery2 = new BooleanQuery(conditionalExprBooleanExpr2,
                new FloodingContinuation(new RelativeBase(), 10.0));

        QueryResultIMPL queryResultIMPL2 = (QueryResultIMPL) booleanQuery2.eval(executionStateTests2);
        assertEquals(queryResultIMPL2.isBooleanRequest(), true);
        assertEquals(queryResultIMPL2.isGatherRequest(), false);
        assertEquals(queryResultIMPL2.getGatheredSensors().size(), 0);
        assertEquals(queryResultIMPL2.getPositiveSN().size(), 0); // pas de noeud positif

        // DirectionContinuation
        initialisation();
        GreaterOrEqualConditionalExpr greaterOrEqualConditionalExpr3 = new GreaterOrEqualConditionalExpr(
                new SensorRand("capteur_fumee"), new ConstantRand(6.0));
        ConditionalExprBooleanExpr conditionalExprBooleanExpr3 = new ConditionalExprBooleanExpr(
                greaterOrEqualConditionalExpr3); // true
        BooleanQuery booleanQuery3 = new BooleanQuery(conditionalExprBooleanExpr3, new DirectionContinuation(5,
                new RecursiveDirections(Direction.NW, new FinalDirections(Direction.NE))));

        QueryResultIMPL queryResultIMPL3 = (QueryResultIMPL) booleanQuery3.eval(executionStateTests2);
        assertEquals(queryResultIMPL3.isBooleanRequest(), true);
        assertEquals(queryResultIMPL3.isGatherRequest(), false);
        assertEquals(queryResultIMPL3.getGatheredSensors().size(), 0);
        assertEquals(queryResultIMPL3.getPositiveSN().size(), 1);

        // on vérifie que c'est bien le bon noeud qui a été ajouté
        assertEquals(queryResultIMPL3.getPositiveSN().get(0), "node2");

        // les continuations sont déjà testées, mais on peut toute de même vérifier
        // qu'elles ont bien été ajoutées à l'executionState
        ExecutionStateIMPL executionStateIMPL = (ExecutionStateIMPL) executionStateTests2;
        assertEquals(executionStateIMPL.getMaxHops(), 5);
        assertEquals(executionStateIMPL.isFlooding(), false);
        assertEquals(executionStateIMPL.isDirectional(), true);
        assertEquals(executionStateIMPL.getDirections().size(), 2);

        Set<Direction> directions = new HashSet<Direction>();
        directions.add(Direction.NW);
        directions.add(Direction.NE);
        assertEquals(executionStateIMPL.getDirections(), directions); // NW, NE

    }

}