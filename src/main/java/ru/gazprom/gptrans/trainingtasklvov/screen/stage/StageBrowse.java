package ru.gazprom.gptrans.trainingtasklvov.screen.stage;

import io.jmix.ui.screen.*;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;

@UiController("ttl_Stage.browse")
@UiDescriptor("stage-browse.xml")
@LookupComponent("stagesTable")
public class StageBrowse extends StandardLookup<Stage> {
}