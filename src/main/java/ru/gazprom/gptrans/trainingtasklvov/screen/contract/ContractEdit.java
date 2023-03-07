package ru.gazprom.gptrans.trainingtasklvov.screen.contract;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gazprom.gptrans.trainingtasklvov.configuration.VatConfiguration;
import ru.gazprom.gptrans.trainingtasklvov.entity.Contract;
import ru.gazprom.gptrans.trainingtasklvov.entity.Organization;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;
import ru.gazprom.gptrans.trainingtasklvov.entity.Status;
import ru.gazprom.gptrans.trainingtasklvov.screen.stage.StageEdit;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@UiController("ttl_Contract.edit")
@UiDescriptor("contract-edit.xml")
@EditedEntityContainer("contractDc")
public class ContractEdit extends StandardEditor<Contract> {
    private static final String DEFAULT_STAGE_NAME = "Начало и конец";
    @Autowired
    private InstanceContainer<Contract> contractDc;
    @Autowired
    private TextField<BigDecimal> totalAmountField;
    @Autowired
    private TextField<BigDecimal> vatField;
    @Autowired
    private TextField<BigDecimal> amountField;
    @Autowired
    private VatConfiguration vatConfiguration;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Button createBtn;

    @Install(to = "contractStagesDl", target = Target.DATA_LOADER)
    private List<Stage> contractStagesDlLoadDelegate(LoadContext<Stage> loadContext) {
        return Objects.nonNull(contractDc.getItem().getStages()) ? contractDc.getItem().getStages() : Collections.emptyList();
    }

    @Subscribe("amountField")
    public void onAmountFieldValueChange(HasValue.ValueChangeEvent<BigDecimal> event) {
        Organization performer = contractDc.getItem().getPerformer();
        if (performer == null) {
            notifications.create()
                    .withCaption("Необходимо выбрать поставщика услуг!")
                    .withPosition(Notifications.Position.MIDDLE_CENTER)
                    .show();
            amountField.setValue(null);
            return;
        }
        double amount = Objects.requireNonNull(event.getValue()).doubleValue();
        double vatAmount;
        double totalAmount;
        if (performer.getEscapeVat()) {
            vatAmount = 0;
            totalAmount = amount;
        } else {
            vatAmount = amount * vatConfiguration.getValue();
            totalAmount = amount + vatAmount;
        }
        vatField.setValue(BigDecimal.valueOf(vatAmount));
        totalAmountField.setValue(BigDecimal.valueOf(totalAmount));
    }

    @Install(to = "contractStages.create", subject = "screenConfigurer")
    private void contractStagesCreateScreenConfigurer(Screen screen) {
        Contract contract = contractDc.getItem();
        if (checkRequiredParameters(contract)) {
            dataManager.save(contract);
            ((StageEdit) screen).getEditedEntity().setContract(contract);
        } else {
            notifications.create()
                    .withCaption("Заполните обязательные поля!")
                    .withPosition(Notifications.Position.MIDDLE_CENTER)
                    .show();
        }
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<Contract> event) {
        event.getEntity().setStatus(dataManager.load(Status.class).query("e.code = 'NEW'").one());
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        Contract contract = contractDc.getItem();
        if (!checkRequiredParameters(contract)) {
            notifications.create()
                    .withCaption("Заполните обязательные поля!")
                    .withPosition(Notifications.Position.MIDDLE_CENTER)
                    .show();
            event.preventCommit();
            return;
        }
        if (CollectionUtils.isEmpty(contract.getStages())) {
            dialogs.createOptionDialog()
                    .withCaption("Внимание!")
                    .withMessage("Если не задать этапы договора, то будут применены настройки по умолчанию. Продолжить?")
                    .withActions(
                            new DialogAction(DialogAction.Type.YES, Action.Status.PRIMARY)
                                    .withHandler(e -> {
                                        event.resume();
                                        createContractWithDefaultStage(contract);
                                    }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .show();
            event.preventCommit();
        }
    }

    private boolean checkRequiredParameters(Contract contract) {
        if (contract.getCustomer() == null || contract.getPerformer() == null || contract.getSignedDate() == null ||
                contract.getType() == null || contract.getDateFrom() == null || contract.getDateTo() == null ||
                contract.getAmount() == null || StringUtils.isBlank(contract.getCustomerSigner()) ||
                StringUtils.isBlank(contract.getNumber()) || StringUtils.isBlank(contract.getPerformerSigner())) {
            return false;
        }
        return true;
    }

    private void createContractWithDefaultStage(Contract contract) {
        Stage stage = dataManager.create(Stage.class);
        stage.setId(UUID.randomUUID());
        stage.setName(DEFAULT_STAGE_NAME);
        stage.setDescription(DEFAULT_STAGE_NAME);
        stage.setDateFrom(contract.getDateFrom());
        stage.setDateTo(contract.getDateTo());
        stage.setAmount(contract.getAmount());
        stage.setVat(contract.getVat());
        stage.setTotalAmount(contract.getTotalAmount());
        stage.setContract(contract);
        contract.setStages(Collections.singletonList(stage));
        dataManager.save(stage, contract);
    }

    @Subscribe(id = "contractDc", target = Target.DATA_CONTAINER)
    public void onContractDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Contract> event) {
        Contract contract = contractDc.getItem();
        if (checkRequiredParameters(contract)) {
            createBtn.setEnabled(true);
        }
    }
}