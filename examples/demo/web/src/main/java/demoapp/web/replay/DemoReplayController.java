package demoapp.web.replay;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.commandreplay.secondary.spi.ReplayCommandExecutionController;

@DomainService(nature = NatureOfService.VIEW, objectType = "demoapp.web.DemoReplayController")
@Profile("secondary")
public class DemoReplayController implements ReplayCommandExecutionController {

    private State state = State.PAUSED;

    @Override
    public State getState() {
        return state;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(cssClassFa = "fa-play")
    public void resumeReplay() {
        state = State.RUNNING;
    }
    public boolean hideResumeReplay() { return state == State.RUNNING; }


    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(cssClassFa = "fa-pause")
    public void pauseReplay() {
        state = State.PAUSED;
    }
    public boolean hidePauseReplay() { return state == State.PAUSED; }

}
