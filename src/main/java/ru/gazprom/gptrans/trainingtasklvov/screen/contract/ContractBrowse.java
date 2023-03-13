package ru.gazprom.gptrans.trainingtasklvov.screen.contract;

import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.screen.*;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gazprom.gptrans.trainingtasklvov.entity.Contract;
import ru.gazprom.gptrans.trainingtasklvov.entity.Status;
import ru.gazprom.gptrans.trainingtasklvov.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UiController("ttl_Contract.browse")
@UiDescriptor("contract-browse.xml")
@LookupComponent("contractsTable")
public class ContractBrowse extends StandardLookup<Contract> {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Button bpmMoveStatus;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private DataGrid<Contract> contractsTable;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;

    @Subscribe("contractsTable.bpmMoveStatus")
    public void onContractsTableBpmMoveStatus(Action.ActionPerformedEvent event) {
        Contract contract = contractsTable.getSingleSelected();
        if (contract == null) {
            notifications.create()
                    .withPosition(Notifications.Position.MIDDLE_CENTER)
                    .withCaption("Не выбран договор!")
                    .show();
        } else {
            Set<String> usernames = dataManager.load(RoleAssignmentEntity.class)
                    .query("e.roleCode = 'managers'")
                    .list()
                    .stream()
                    .map(RoleAssignmentEntity::getUsername)
                    .collect(Collectors.toSet());
            List<User> users = dataManager.load(User.class)
                    .condition(PropertyCondition.inList("username", usernames))
                    .list();
            dialogs.createInputDialog(this)
                    .withCaption("Выберите менеджера:")
                    .withParameters(
                            InputParameter.parameter("manager")
                                    .withField(() -> {
                                        EntityComboBox<User> field = uiComponents.create(
                                                EntityComboBox.of(User.class));
                                        field.setOptionsList(users);
                                        field.setCaption("Менеджер");
                                        field.setWidthFull();
                                        return field;
                                    })
                    )
                    .withActions(DialogActions.OK)
                    .withCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(DialogOutcome.OK)) {
                            User manager = closeEvent.getValue("manager");
                            Status statusNew = dataManager.load(Status.class)
                                    .query("e.code = 'NEW'")
                                    .one();
                            if (statusNew.equals(contract.getStatus())) {
                                contract.setStatus(dataManager.load(Status.class).query("e.code = 'TO_APPROVAL'").one());
                                dataManager.save(contract);
                            }
                            Map<String, Object> params = new HashMap<>();
                            params.put("contract", contract);
                            params.put("manager", manager);
                            runtimeService.startProcessInstanceByKey("contractStatusManagement", params);
                        }
                    })
                    .show();
        }
    }

    @Subscribe("contractsTable")
    public void onContractsTableItemClick(DataGrid.ItemClickEvent<Contract> event) {
        Contract contract = event.getItem();
        Status statusNew = dataManager.load(Status.class)
                .query("e.code = 'NEW'")
                .one();
        Status statusActive = dataManager.load(Status.class)
                .query("e.code = 'ACTIVE'")
                .one();
        if (statusNew.equals(contract.getStatus())) {
            bpmMoveStatus.setCaption("Отправить на согласование");
            bpmMoveStatus.setEnabled(true);
            bpmMoveStatus.setVisible(true);
        } else if (statusActive.equals(contract.getStatus())) {
            bpmMoveStatus.setCaption("Запросить завершение");
            bpmMoveStatus.setEnabled(true);
            bpmMoveStatus.setVisible(true);
        } else {
            bpmMoveStatus.setEnabled(false);
            bpmMoveStatus.setVisible(false);
        }
    }
}