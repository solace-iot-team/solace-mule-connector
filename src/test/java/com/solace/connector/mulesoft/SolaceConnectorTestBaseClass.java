package com.solace.connector.mulesoft;

import org.mule.runtime.core.api.construct.Flow;

import com.solace.connector.mulesoft.test.AbstractSolaceArtifactFunctionalTestCase;

public abstract class SolaceConnectorTestBaseClass extends AbstractSolaceArtifactFunctionalTestCase {
	public Flow getFlow(String flowName) {
        return (Flow) registry.lookupByName(flowName).orElse(null);
 }

}
