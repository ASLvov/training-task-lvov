package ru.gazprom.gptrans.trainingtasklvov.screen.organization;

import io.jmix.ui.screen.*;
import ru.gazprom.gptrans.trainingtasklvov.entity.Organization;

@UiController("ttl_Organization.browse")
@UiDescriptor("organization-browse.xml")
@LookupComponent("organizationsTable")
public class OrganizationBrowse extends StandardLookup<Organization> {
}