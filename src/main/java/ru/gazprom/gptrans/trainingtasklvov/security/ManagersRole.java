package ru.gazprom.gptrans.trainingtasklvov.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;
import ru.gazprom.gptrans.trainingtasklvov.entity.*;

import javax.annotation.Nonnull;

@Nonnull
@ResourceRole(name = "Managers", code = "managers", scope = "UI")
public interface ManagersRole {
    @MenuPolicy(menuIds = {"bpm_MyTasks.browse", "ttl_Contract.browse", "ttl_Organization.browse", "ttl_Stage.browse", "ttl_Invoice.browse", "ttl_ServiceCompletionCertificate.browse"})
    @ScreenPolicy(screenIds = {"bpm_MyTasks.browse", "ttl_Contract.browse", "ttl_Contract.edit", "ttl_Organization.edit", "ttl_Organization.browse", "ttl_Stage.browse", "ttl_Invoice.browse", "ttl_ServiceCompletionCertificate.browse", "bpm_VariableInstanceData.edit"})
    void screens();

    @EntityAttributePolicy(entityClass = Contract.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Contract.class, actions = EntityPolicyAction.ALL)
    void contract();

    @EntityAttributePolicy(entityClass = Stage.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Stage.class, actions = EntityPolicyAction.ALL)
    void stage();

    @EntityAttributePolicy(entityClass = Invoice.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Invoice.class, actions = EntityPolicyAction.ALL)
    void invoice();

    @EntityAttributePolicy(entityClass = Status.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Status.class, actions = EntityPolicyAction.ALL)
    void status();

    @EntityAttributePolicy(entityClass = ServiceCompletionCertificate.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ServiceCompletionCertificate.class, actions = EntityPolicyAction.ALL)
    void serviceCompletionCertificate();

    @EntityAttributePolicy(entityClass = Organization.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Organization.class, actions = EntityPolicyAction.ALL)
    void organization();
}