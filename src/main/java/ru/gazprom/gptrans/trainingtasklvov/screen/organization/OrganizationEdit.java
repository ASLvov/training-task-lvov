package ru.gazprom.gptrans.trainingtasklvov.screen.organization;

import io.jmix.ui.screen.*;
import ru.gazprom.gptrans.trainingtasklvov.entity.Organization;

@UiController("ttl_Organization.edit")
@UiDescriptor("organization-edit.xml")
@EditedEntityContainer("organizationDc")
public class OrganizationEdit extends StandardEditor<Organization> {
}