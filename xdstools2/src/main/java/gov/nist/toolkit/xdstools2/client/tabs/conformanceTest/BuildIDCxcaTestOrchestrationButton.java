package gov.nist.toolkit.xdstools2.client.tabs.conformanceTest;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import gov.nist.toolkit.actorfactory.client.SimulatorConfig;
import gov.nist.toolkit.actortransaction.client.ActorType;
import gov.nist.toolkit.actortransaction.client.ParamType;
import gov.nist.toolkit.configDatatypes.SimulatorProperties;
import gov.nist.toolkit.configDatatypes.client.TransactionType;
import gov.nist.toolkit.services.client.IdcxcaOrchestrationRequest;
import gov.nist.toolkit.services.client.IdcxcaOrchestrationResponse;
import gov.nist.toolkit.services.client.RawResponse;
import gov.nist.toolkit.simcommon.client.config.SimulatorConfigElement;
import gov.nist.toolkit.sitemanagement.client.SiteSpec;
import gov.nist.toolkit.xdstools2.client.command.command.BuildIIGTestOrchestrationCommand;
import gov.nist.toolkit.xdstools2.client.command.command.BuildIdcxcaTestOrchestrationCommand;
import gov.nist.toolkit.xdstools2.client.util.ClientUtils;
import gov.nist.toolkit.xdstools2.client.widgets.OrchestrationSupportTestsDisplay;
import gov.nist.toolkit.xdstools2.client.widgets.buttons.AbstractOrchestrationButton;
import gov.nist.toolkit.xdstools2.shared.command.request.BuildIdcxcaTestOrchestrationRequest;
import gov.nist.toolkit.xdstools2.shared.command.request.BuildIigTestOrchestrationRequest;

/**
 * Created by smm on 10/9/16.
 */

public class BuildIDCxcaTestOrchestrationButton extends AbstractOrchestrationButton {
    private ConformanceTestTab testTab;
    private Panel initializationPanel;
    private FlowPanel initializationResultsPanel = new FlowPanel();
    private TestContext testContext;
    private TestContextView testContextView;
    private TestRunner testRunner;

    BuildIDCxcaTestOrchestrationButton(ConformanceTestTab testTab, TestContext testContext, TestContextView testContextView, TestRunner testRunner, Panel initializationPanel, String label) {
        this.initializationPanel = initializationPanel;
        this.testTab = testTab;
        this.testContext = testContext;
        this.testContextView = testContextView;


        HTML instructions = new HTML(
                "<p>" +
                        "The System Under Test (SUT) is an Imaging Document Consumer in XCA-I." +
                        "The diagram below shows the test environment with the SUT in orange. " +
                        "The test software creates and configures the simulators in the diagram. " +
                        "</p>" +
                        "<p>" +
                        "You need to configure your Imaging Document Consumer to communicate with " +
                        "the Initiating Imaging Gateway shown in the diagram. " +
                        "The table immediately below describes the environment at a high level with " +
                        "values for homeCommunityID's and repositoryUniqueID's. " +

                        "</p>" +
                        "<table border=\"1\">" +
                        "<tr><th>homeCommunityID</th><th>Imaging Doc Source Repository Unique ID</th></tr>" +
                        "<tr bgcolor=\"#FFA500\"><td colspan=\"2\"><center>Under Test:Initiating Imaging Gateway</center></td></tr>" +
                        "<tr bgcolor=\"#FFA500\"><td>urn:oid:1.3.6.1.4.1.21367.13.70.1</td><td>&nbsp;</td></tr>" +

                        "<tr bgcolor=\"#FFFFFF\"><td colspan=\"2\"><center>Community A: Responding Imaging Gateway</center></td></tr>" +
                        "<tr bgcolor=\"#FFFFFF\"><td>urn:oid:1.3.6.1.4.1.21367.13.70.101</td>" +
                        "<td>1.3.6.1.4.1.21367.13.71.101 <br/>1.3.6.1.4.1.21367.13.71.101.1</td></tr>" +

                        "<tr bgcolor=\"#A0A0A0\"><td colspan=\"2\"><center>Community B: Responding Imaging Gateway</center></td></tr>" +
                        "<tr bgcolor=\"#A0A0A0\"><td>urn:oid:1.3.6.1.4.1.21367.13.70.102</td><td>1.3.6.1.4.1.21367.13.71.102</td></tr>" +

                        "<tr bgcolor=\"#FFFFFF\"><td colspan=\"2\"><center>Community C: Responding Imaging Gateway</center></td></tr>" +
                        "<tr bgcolor=\"#FFFFFF\"><td>urn:oid:1.3.6.1.4.1.21367.13.70.103</td><td>1.3.6.1.4.1.21367.13.71.103</td></tr>" +

                        "<tr bgcolor=\"#A0A0A0\"><td colspan=\"2\">" +
                        "<center>Unregistered Community Represents Error Conditions<br/>Do not configure these in your Initiating Imaging Gateway</center>" +
                        "</td></tr>" +
                        "<tr bgcolor=\"#A0A0A0\"><td>urn:oid:1.3.6.1.4.1.21367.13.70.102.999</td><td>1.3.6.1.4.1.21367.13.71.102.999</td></tr>" +
                        "</table>" +

                        "<p>" +
                        "After you have initialized the test environment, you should see the full set of configuration " +
                        "parameters needed to configure and test your system. " +
                        "</p>" +
                        "<p>" +
                        "Note that your Imaging Document Consumer only communicates with the Initiating Imaging Gateway simulator. " +
                        "Your system will not connect directly to any other simulator." +
                        "</p>"

        );

        initializationPanel.add(instructions);

        setSystemDiagramUrl("diagrams/IIGdiagram.png");

        this.testRunner = testRunner;

        setParentPanel(initializationPanel);
        setLabel(label);
        setResetLabel("Reset");
        build();
        panel().add(initializationResultsPanel);
    }

    public void orchestrate() {
        String msg = testContext.verifyTestContext();
        if (msg != null) {
            testContextView.launchDialog(msg);
            return;
        }

        initializationResultsPanel.clear();

        IdcxcaOrchestrationRequest request = new IdcxcaOrchestrationRequest();
        request.setUserName(testTab.getCurrentTestSession());
        request.setEnvironmentName(testTab.getEnvironmentSelection());
        request.setUseExistingState(!isResetRequested());
        SiteSpec siteSpec = new SiteSpec(testContext.getSiteName());
        if (isSaml()) {
            setSamlAssertion(siteSpec);
        }
        request.setSiteUnderTest(siteSpec);

        testTab.setSiteToIssueTestAgainst(siteSpec);

        new BuildIdcxcaTestOrchestrationCommand() {
            @Override
            public void onComplete(RawResponse rawResponse) {
                if (handleError(rawResponse, IdcxcaOrchestrationResponse.class)) return;
                IdcxcaOrchestrationResponse orchResponse = (IdcxcaOrchestrationResponse) rawResponse;
                testTab.setOrchestrationResponse(orchResponse);

                initializationResultsPanel.add(new HTML("Initialization Complete"));

                if (testContext.getSiteUnderTest() != null) {
                    initializationResultsPanel.add(new HTML("<h2>System Under Test Configuration</h2>"));
                    initializationResultsPanel.add(new HTML("Site: " + testContext.getSiteUnderTest().getName()));
                    FlexTable table = new FlexTable();
                    int row = 0;
                    table.setText(row, 0, "Retrieve Img Doc Set: ");
                    try {
                        table.setText(row++, 1,
                                testContext.getSiteUnderTest().getRawEndpoint(TransactionType.RET_IMG_DOC_SET_GW, false, false));
                    } catch (Exception e) {
                    }

                    initializationResultsPanel.add(table);
                }

                initializationResultsPanel.add(new HTML("<h2>Generated Environment</h2>"));

                FlexTable table = new FlexTable();
                int row = 0;
                // Pass through simulators in Orchestra enum order
                for (Orchestra o : Orchestra.values()) {
                    // get matching simulator config
                    SimulatorConfig sim = null;
                    for (SimulatorConfig c : orchResponse.getSimulatorConfigs()) {
                        if (c.getId().getId().equals(o.name())) {
                            sim = c;
                            break;
                        }
                    }
                    if (sim == null) continue;

                    try {
                        // First row: title, sim id, test data and log buttons
                        table.setWidget(row, 0, new HTML("<h3>" + o.title + "</h3>"));
                        table.setText(row++, 1, sim.getId().toString());

                        // Property rows, based on ActorType and Orchestration enum
                        for (String property : o.getDisplayProps()) {
                            table.setWidget(row, 1, new HTML(property));
                            SimulatorConfigElement prop = sim.get(property);
                            String value = prop.asString();
                            if (prop.hasList()) value = prop.asList().toString();
                            table.setWidget(row++, 2, new HTML(value));
                        }
                    } catch (Exception e) {
                        initializationResultsPanel.add(new HTML("<h3>exception " + o.name() + " " + e.getMessage() + "/h3>"));
                    }
                }
                initializationResultsPanel.add(table);

                initializationResultsPanel.add(new HTML("<br />"));

                initializationResultsPanel.add(new OrchestrationSupportTestsDisplay(orchResponse, testContext, testContextView, testRunner ));

                initializationResultsPanel.add(new HTML("<br />"));

                initializationResultsPanel.add(new HTML("<p>Configure your " +
                        "Initiating Imaging Gateway SUT to forward Retrieve Imaging " +
                        "Document Set Requests to these Responding Imaging Gateways<hr/>"));
            }
        }.run(new BuildIdcxcaTestOrchestrationRequest(ClientUtils.INSTANCE.getCommandContext(), request));
    }

    @SuppressWarnings("javadoc")

    public enum Orchestra {

        rg_a("Responding Gateway A", ActorType.RESPONDING_GATEWAY, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.homeCommunityId, ParamType.TEXT, "urn:oid:1.3.6.1.4.1.21367.13.70.101")}),

        rig_a("Responding Imaging Gateway A", ActorType.RESPONDING_IMAGING_GATEWAY, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.homeCommunityId, ParamType.TEXT, "urn:oid:1.3.6.1.4.1.21367.13.70.101"),
                new SimulatorConfigElement(SimulatorProperties.imagingDocumentSources, ParamType.SELECTION, new String[]{"${user}__ids_a1", "${user}__ids_a2"}, true)}),

        ids_a1("Imaging Document Source A1", ActorType.IMAGING_DOC_SOURCE, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.idsRepositoryUniqueId, ParamType.TEXT, "1.3.6.1.4.1.21367.13.71.101"),
                new SimulatorConfigElement(SimulatorProperties.idsImageCache, ParamType.TEXT, "xca-dataset-a1")}),

        ids_a2("Imaging Document Source A2", ActorType.IMAGING_DOC_SOURCE, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.idsRepositoryUniqueId, ParamType.TEXT, "1.3.6.1.4.1.21367.13.71.101.1"),
                new SimulatorConfigElement(SimulatorProperties.idsImageCache, ParamType.TEXT, "xca-dataset-a2")}),

        rg_b("Responding Gateway B", ActorType.RESPONDING_GATEWAY, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.homeCommunityId, ParamType.TEXT, "urn:oid:1.3.6.1.4.1.21367.13.70.102")}),

        rig_b("Responding Imaging Gateway B", ActorType.RESPONDING_IMAGING_GATEWAY, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.homeCommunityId, ParamType.TEXT, "urn:oid:1.3.6.1.4.1.21367.13.70.102"),
                new SimulatorConfigElement(SimulatorProperties.imagingDocumentSources, ParamType.SELECTION, new String[]{"${user}__ids_b1"}, true)}),

        ids_b1("Imaging Document Source B1", ActorType.IMAGING_DOC_SOURCE, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.idsRepositoryUniqueId, ParamType.TEXT, "1.3.6.1.4.1.21367.13.71.102"),
                new SimulatorConfigElement(SimulatorProperties.idsImageCache, ParamType.TEXT, "xca-dataset-b")}),

        ig("Initiating Gateway", ActorType.INITIATING_GATEWAY, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.respondingGateways, ParamType.SELECTION, new String[]{"${user}__rg_a", "${user__rg_b"}, true)}),

        iig("Initiating Imaging Gateway", ActorType.INITIATING_IMAGING_GATEWAY, new SimulatorConfigElement[]{
                new SimulatorConfigElement(SimulatorProperties.respondingImagingGateways, ParamType.SELECTION, new String[]{"${user}__rig_a", "${user}__rig_b"}, true)}),

        simulator_idc("Simulated IDC-XCAI SUT", ActorType.IMAGING_DOC_CONSUMER_XCA, new SimulatorConfigElement[]{});

        public final String title;
        public final ActorType actorType;
        public final SimulatorConfigElement[] elements;

        Orchestra(String title, ActorType actorType, SimulatorConfigElement[] elements) {
            this.title = title;
            this.actorType = actorType;
            this.elements = elements;
        }

        public ActorType getActorType() {
            return actorType;
        }

        public SimulatorConfigElement[] getElements() {
            return elements;
        }

        public String[] getDisplayProps() {
            switch (actorType) {
                case RESPONDING_IMAGING_GATEWAY:
                    return new String[]{
                            SimulatorProperties.homeCommunityId,
                            SimulatorProperties.xcirEndpoint,
                            // SimulatorProperties.xcirTlsEndpoint,
                            SimulatorProperties.imagingDocumentSources,
                    };
                case IMAGING_DOC_SOURCE:
                    return new String[]{
                            SimulatorProperties.idsRepositoryUniqueId,
                            SimulatorProperties.idsrEndpoint,
                            //SimulatorProperties.idsrTlsEndpoint,
                            SimulatorProperties.idsImageCache,
                    };
                case INITIATING_IMAGING_GATEWAY:
                    return new String[]{
                            SimulatorProperties.idsrIigEndpoint,
                            //SimulatorProperties.idsrTlsEndpoint,
                            SimulatorProperties.respondingImagingGateways,
                    };
                case IMAGING_DOC_CONSUMER_XCA:
                    return new String[]{
//                            "put relevant IDC_XCA properties here.",
                    };
                default:
            }
            return new String[0];
        }

    } // EO Orchestra enum

}