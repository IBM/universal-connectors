/*
* � Copyright IBM Corp. 2021, 2022 All rights reserved.
* SPDX-License-Identifier: Apache-2.0
*/

package com.ibm.neptune.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.script.ScriptEngine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.jsr223.GremlinLangScriptEngine;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal.Admin;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.HasContainerHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.Parameterizing;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.DropStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.AddPropertyStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.step.sideEffect.TinkerGraphStep;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;

import com.ibm.neptune.connector.constant.ApplicationConstants;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * 
 *A parser to parse the Neptune Gremlin queries.
 * 
 */
public class GremlinParser {

	private static final Logger LOG = LogManager.getLogger(GremlinParser.class);
	private static final ScriptEngine ENGINE;

	static {

    TinkerGraph graph = TinkerGraph.open();
		GraphTraversalSource g = AnonymousTraversalSource.traversal().withEmbedded(graph);
		ENGINE = new GremlinLangScriptEngine();
		ENGINE.put("g", g);
	
  }

	public static Map<String, List<Object>> parseGremlin(String script) throws Exception {

		Map<String, List<Object>> map = new HashMap<>();
		List<Object> objectList = new ArrayList<>();
		List<Object> verbList = new ArrayList<>();
		List<Object> idList = new ArrayList<>();

		map.put(ApplicationConstants.OBJECT, objectList);
		map.put(ApplicationConstants.VERB, verbList);
		map.put(ApplicationConstants.IDs, idList);

		try {
			@SuppressWarnings("rawtypes")
			Traversal stmt = (Traversal) ((ScriptEngine) ENGINE).eval(script);

			@SuppressWarnings("rawtypes")
			Admin graphTrav = stmt.asAdmin();
//			System.out.println(script);

			printSteps(graphTrav, map);
//			System.out.println("ObjectList: " + objectList + "\n" +"VerbList: "+ verbList);
//			System.out.println("-------------------------------------------------------------------------------");
			return map;
		} catch (Exception e) {
			LOG.error("Exception occured in parseGremlin method of gremlin parser");
			throw e;

		}
	}

	/**
	 * In this printSteps method, if step object contains reference of the AddVertexStep
	 * or AddVertexStartStep Class then "add vertices" will be set in the verb because step
	 * object contains "AddVertex" and vice-versa.
	 * 
	 * 
	 * @param trav
	 * @param map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void printSteps(Admin trav, Map<String, List<Object>> map) {
		trav.getSteps().forEach(new Consumer<Step>() {

			public void accept(Step step) {
//				System.out.println("step : " + step);

				if (step instanceof GraphStep) {

					GraphStep graphStep = (GraphStep) step;
					map.get(ApplicationConstants.IDs).clear();

					String type = graphStep.getReturnClass().getName().contains(ApplicationConstants.VERTEX)
							? ApplicationConstants.VERTICES
							: ApplicationConstants.EDGES;

					map.get(ApplicationConstants.IDs).addAll(Arrays.asList(graphStep.getIds()));
					map.get(ApplicationConstants.OBJECT)
							.add(map.get(ApplicationConstants.IDs).isEmpty() ? Arrays.asList(ApplicationConstants.ALL)
									: Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
					map.get(ApplicationConstants.VERB)
							.add(ApplicationConstants.SELECT + ApplicationConstants.SPACE + type);

				}

				if (step instanceof AddVertexStartStep || step instanceof AddVertexStep
						|| step instanceof AddEdgeStartStep || step instanceof AddEdgeStep) {

					final List<Object> list = ((Parameterizing) step).getParameters().getRaw().get(T.label);

					map.get(ApplicationConstants.OBJECT)
							.add(list.get(0).toString().contains(ApplicationConstants.DOUBLE_COLON)
									? Arrays.asList(list.get(0).toString().split(ApplicationConstants.DOUBLE_COLON))
									: list);
					map.get(ApplicationConstants.VERB)
							.add(step.getClass().toString().contains(ApplicationConstants.ADD_VERTEX)
									? ApplicationConstants.ADD + ApplicationConstants.SPACE
											+ ApplicationConstants.VERTICES
									: ApplicationConstants.ADD + ApplicationConstants.SPACE
											+ ApplicationConstants.EDGES);
				}

				else if (step instanceof HasStep || step instanceof TinkerGraphStep) {

					if (map.get(ApplicationConstants.VERB).get(map.get(ApplicationConstants.VERB).size() - 1).equals(
							ApplicationConstants.SELECT + ApplicationConstants.SPACE + ApplicationConstants.VERTICES)
							|| (map.get(ApplicationConstants.VERB).get(map.get(ApplicationConstants.VERB).size() - 1)
									.equals(ApplicationConstants.SELECT + ApplicationConstants.SPACE
											+ ApplicationConstants.EDGES)
									&& map.get(ApplicationConstants.VERB).size() == 1)) {

						((HasContainerHolder) step).getHasContainers().stream()
								.filter(c -> ((HasContainer) c).getKey().equals("~label")

								).forEach(c -> {
									map.get(ApplicationConstants.OBJECT)
											.remove(map.get(ApplicationConstants.OBJECT).size() - 1);
									final Object object = ((HasContainer) c).getPredicate().getValue();
									map.get(ApplicationConstants.OBJECT).add(
											object instanceof String ? Arrays.asList(object) : (List<Object>) object);
								});

						((HasContainerHolder) step).getHasContainers().stream()
								.filter(c -> ((HasContainer) c).getKey().equals("~id")).forEach(c -> {
									map.get(ApplicationConstants.IDs).add(((HasContainer) c).getPredicate().getValue());
									map.get(ApplicationConstants.OBJECT)
											.remove(map.get(ApplicationConstants.OBJECT).size() - 1);
									map.get(ApplicationConstants.OBJECT)
											.add(map.get(ApplicationConstants.IDs).isEmpty()
													? Arrays.asList(ApplicationConstants.ALL)
													: Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
								});
					}
				}

				else if (step instanceof VertexStep) {

					map.get(ApplicationConstants.OBJECT)
							.add(((VertexStep) step).getEdgeLabels().length > 0
									? Arrays.asList(((VertexStep) step).getEdgeLabels())
									: Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
					map.get(ApplicationConstants.VERB)
							.add(ApplicationConstants.SELECT + ApplicationConstants.SPACE + ApplicationConstants.EDGES);

				}

				else if (step instanceof EdgeVertexStep) {

					map.get(ApplicationConstants.OBJECT).add(Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
					map.get(ApplicationConstants.VERB).add(
							ApplicationConstants.SELECT + ApplicationConstants.SPACE + ApplicationConstants.VERTICES);

				}

				else if (step instanceof AddPropertyStep) {

					checkVerb(map, ApplicationConstants.UPDATE);

				}

				else if (step instanceof DropStep) {

					checkVerb(map, ApplicationConstants.DROP);

				}

			}
		});
	}

	/**
	 * 
	 * 
	 * This method checks the verb in Gremlin query, if last verb value in the list
	 * is "select vertices" and action value is "update" then verb "select vertices" will be
	 * replaced by the verb "update vertices". Same will be applied for edges case also.
	 * 
	 * @param map
	 * @param action
	 */
	private static void checkVerb(final Map<String, List<Object>> map, final String action) {

		final String tempAction = map.get(ApplicationConstants.VERB).get(map.get(ApplicationConstants.VERB).size() - 1)
				.toString();
		if (tempAction.equals(ApplicationConstants.SELECT + ApplicationConstants.SPACE + ApplicationConstants.VERTICES)
				|| (tempAction
						.equals(ApplicationConstants.SELECT + ApplicationConstants.SPACE + ApplicationConstants.EDGES)
						&& map.get(ApplicationConstants.VERB).size() == 1)) {

			map.get(ApplicationConstants.VERB).remove(map.get(ApplicationConstants.VERB).size() - 1);
			map.get(ApplicationConstants.VERB)
					.add(tempAction.contains(ApplicationConstants.VERTICES)
							? action + ApplicationConstants.SPACE + ApplicationConstants.VERTICES
							: action + ApplicationConstants.SPACE + ApplicationConstants.EDGES);

		} else if (tempAction
				.equals(ApplicationConstants.SELECT + ApplicationConstants.SPACE + ApplicationConstants.EDGES)) {
			map.get(ApplicationConstants.OBJECT).add(Arrays.asList(ApplicationConstants.UNKNOWN_STRING));
			map.get(ApplicationConstants.VERB).add(action + ApplicationConstants.SPACE + ApplicationConstants.VERTICES);
		}

	}

}
