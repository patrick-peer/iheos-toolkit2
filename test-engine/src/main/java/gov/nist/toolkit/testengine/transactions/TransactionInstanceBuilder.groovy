package gov.nist.toolkit.testengine.transactions

import gov.nist.toolkit.actortransaction.client.TransactionInstance
import gov.nist.toolkit.testengine.engine.FhirSimulatorTransaction
import gov.nist.toolkit.testengine.engine.fhirValidations.SimReference
import gov.nist.toolkit.xdsexception.client.XdsInternalException

// enables writing of test stubs for MhdClientTransaction testing
interface TransactionInstanceBuilder {

    TransactionInstance build(String actor, String eventId, String trans)
    List<FhirSimulatorTransaction> getSimulatorTransactions(SimReference simReference) throws XdsInternalException
}