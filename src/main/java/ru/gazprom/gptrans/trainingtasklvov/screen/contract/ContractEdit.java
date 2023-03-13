package ru.gazprom.gptrans.trainingtasklvov.screen.contract;

import io.jmix.bpmui.processform.ProcessFormContext;
import io.jmix.bpmui.processform.annotation.Outcome;
import io.jmix.bpmui.processform.annotation.Param;
import io.jmix.bpmui.processform.annotation.ProcessForm;
import io.jmix.bpmui.processform.annotation.ProcessFormParam;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.LoadContext;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gazprom.gptrans.trainingtasklvov.app.ContractManagementService;
import ru.gazprom.gptrans.trainingtasklvov.configuration.VatConfiguration;
import ru.gazprom.gptrans.trainingtasklvov.entity.*;
import ru.gazprom.gptrans.trainingtasklvov.screen.stage.StageEdit;

import java.math.BigDecimal;
import java.util.*;

@UiController("ttl_Contract.edit")
@UiDescriptor("contract-edit.xml")
@EditedEntityContainer("contractDc")
@ProcessForm(
        params = {
                @Param(name = "contract")
        },
        outcomes = {
                @Outcome(id = "approve"),
                @Outcome(id = "reject")
        }
)
public class ContractEdit extends StandardEditor<Contract> {
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
    @Autowired
    private ProcessFormContext processFormContext;
    @Autowired
    private ContractManagementService contractManagementService;
    @Autowired
    private Button approveAndCloseBtn;
    @Autowired
    private Button rejectBtn;
    @Autowired
    private FileMultiUploadField filesField;
    @Autowired
    private TemporaryStorage temporaryStorage;
    @Autowired
    private CollectionContainer<Attachment> attachments;
    @ProcessFormParam(name = "contract")
    private Contract contractVariable;

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
        double amount;
        if (event.getValue() == null) {
            amount = 0;
        } else {
            amount = event.getValue().doubleValue();
        }
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
        Contract contract = getEditedEntity();
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
                                        contractManagementService.createContractWithDefaultStage(contract);
                                    }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .show();
            event.preventCommit();
        }
    }

    private boolean checkRequiredParameters(Contract contract) {
        return contract.getCustomer() != null && contract.getPerformer() != null && contract.getSignedDate() != null &&
                contract.getType() != null && contract.getDateFrom() != null && contract.getDateTo() != null &&
                contract.getAmount() != null && StringUtils.isNotBlank(contract.getCustomerSigner()) &&
                StringUtils.isNotBlank(contract.getNumber()) && StringUtils.isNotBlank(contract.getPerformerSigner());
    }

    @Subscribe(id = "contractDc", target = Target.DATA_CONTAINER)
    public void onContractDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Contract> event) {
        Contract contract = contractDc.getItem();
        createBtn.setEnabled(checkRequiredParameters(contract));
    }

    @Subscribe("approveAndCloseBtn")
    public void onApproveAndCloseBtnClick(Button.ClickEvent event) {
        completeTaskWithOutcome("approve");
    }

    @Subscribe("rejectBtn")
    public void onRejectBtnClick(Button.ClickEvent event) {
        completeTaskWithOutcome("reject");
    }

    private void completeTaskWithOutcome(String outcome) {
        Contract contract = getEditedEntity();
        String statusCode = contractManagementService.getNextStatusCodeByInstanceAndOutcome(contract, outcome);
        commitChanges();
        processFormContext.taskCompletion()
                .withOutcome(outcome)
                .addProcessVariable("statusCode", statusCode)
                .saveInjectedProcessVariables()
                .complete();
        closeWithDefaultAction();
    }

    @Subscribe
    public void onInit(InitEvent event) {
        if (Objects.nonNull(contractVariable)) {
            setEntityToEdit(contractVariable);
            if ("TO_APPROVAL".equalsIgnoreCase(contractVariable.getStatus().getCode())) {
                approveAndCloseBtn.setVisible(true);
                approveAndCloseBtn.setCaption("Согласовать");
                rejectBtn.setVisible(true);
            } else if ("ACTIVE".equalsIgnoreCase(contractVariable.getStatus().getCode())) {
                approveAndCloseBtn.setVisible(true);
                approveAndCloseBtn.setCaption("Согласовать завершение");
            } else {
                approveAndCloseBtn.setVisible(false);
                rejectBtn.setVisible(false);
            }
        }
        filesField.addQueueUploadCompleteListener(queueUploadCompleteEvent -> {
            for (Map.Entry<UUID, String> entry : filesField.getUploadsMap().entrySet()) {
                UUID fileId = entry.getKey();
                String fileName = entry.getValue();
                FileRef fileRef = temporaryStorage.putFileIntoStorage(fileId, fileName);
                Attachment attachment = dataManager.create(Attachment.class);
                attachment.setFile(fileRef);
                attachment.setContract(getEditedEntity());
                dataManager.save(attachment);
            }
            attachments.setItems(attachmentsDlLoadDelegate(null));
            notifications.create()
                    .withCaption("Uploaded files: " + filesField.getUploadsMap().values())
                    .show();
            filesField.clearUploads();
        });
        filesField.addFileUploadErrorListener(queueFileUploadErrorEvent ->
                notifications.create()
                        .withCaption("File upload error")
                        .show());
    }

    @Install(to = "attachmentsDl", target = Target.DATA_LOADER)
    private List<Attachment> attachmentsDlLoadDelegate(LoadContext<Attachment> loadContext) {
        Contract contract = getEditedEntity();
        return dataManager.load(Attachment.class)
                .query("e.contract = :contract")
                .parameter("contract", contract)
                .list();
    }

    @Install(to = "attachments.number", subject = "columnGenerator")
    private Integer attachmentsNumberColumnGenerator(DataGrid.ColumnGeneratorEvent<Attachment> columnGeneratorEvent) {
        List<Attachment> items = attachments.getItems();
        Attachment item = columnGeneratorEvent.getItem();
        for (Attachment attachment : items) {
            if (item.equals(attachment)) {
                return attachments.getItemIndex(attachment) + 1;
            }
        }
        return 0;
    }

    @Install(to = "attachments.fileName", subject = "columnGenerator")
    private String attachmentsFileNameColumnGenerator(DataGrid.ColumnGeneratorEvent<Attachment> columnGeneratorEvent) {
        Attachment attachment = columnGeneratorEvent.getItem();
        return attachment.getFile().getFileName();
    }
}