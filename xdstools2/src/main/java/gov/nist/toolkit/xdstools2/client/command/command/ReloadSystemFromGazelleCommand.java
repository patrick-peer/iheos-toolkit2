package gov.nist.toolkit.xdstools2.client.command.command;

import gov.nist.toolkit.xdstools2.client.initialization.XdsTools2Presenter;
import gov.nist.toolkit.xdstools2.shared.command.request.ReloadSystemFromGazelleRequest;

/**
 * Created by onh2 on 10/19/16.
 */
public abstract class ReloadSystemFromGazelleCommand extends GenericCommand<ReloadSystemFromGazelleRequest,String>{
    @Override
    public void run(ReloadSystemFromGazelleRequest request) {
        XdsTools2Presenter.data().getToolkitServices().reloadSystemFromGazelle(request,this);
    }
}