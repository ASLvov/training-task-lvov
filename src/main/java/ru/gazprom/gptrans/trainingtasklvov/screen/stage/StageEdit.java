package ru.gazprom.gptrans.trainingtasklvov.screen.stage;

import io.jmix.core.DataManager;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gazprom.gptrans.trainingtasklvov.configuration.VatConfiguration;
import ru.gazprom.gptrans.trainingtasklvov.entity.Invoice;
import ru.gazprom.gptrans.trainingtasklvov.entity.ServiceCompletionCertificate;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;

import java.math.BigDecimal;
import java.util.Objects;

@UiController("ttl_Stage.edit")
@UiDescriptor("stage-edit.xml")
@EditedEntityContainer("stageDc")
public class StageEdit extends StandardEditor<Stage> {
    @Autowired
    private InstanceContainer<Stage> stageDc;
    @Autowired
    private VatConfiguration vatConfiguration;
    @Autowired
    private EntityPicker<ServiceCompletionCertificate> serviceCompletionCertificateField;
    @Autowired
    private TextField<BigDecimal> vatField;
    @Autowired
    private TextField<BigDecimal> totalAmountField;
    @Autowired
    private EntityPicker<Invoice> invoiceField;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Sequences sequences;

    @Subscribe("amountField")
    public void onAmountFieldValueChange(HasValue.ValueChangeEvent<BigDecimal> event) {
        double amount = Objects.requireNonNull(event.getValue()).doubleValue();
        double vatAmount = amount * vatConfiguration.getValue();
        double totalAmount = amount + vatAmount;
        vatField.setValue(BigDecimal.valueOf(vatAmount));
        totalAmountField.setValue(BigDecimal.valueOf(totalAmount));
    }

    @Subscribe("creteClosingDocsBtn")
    public void onCreteClosingDocsBtnClick(Button.ClickEvent event) {
        Stage stage = stageDc.getItem();
        Invoice invoice = createInvoice(stage);
        ServiceCompletionCertificate serviceCompletionCertificate = createServiceCompletionCertificate(stage);
        stage.setInvoice(invoice);
        stage.setServiceCompletionCertificate(serviceCompletionCertificate);
        commitChanges();
    }

    private Invoice createInvoice(Stage stage) {
        Invoice invoice = dataManager.create(Invoice.class);
        invoice.setAmount(stage.getAmount());
        invoice.setVat(stage.getVat());
        invoice.setTotalAmount(stage.getTotalAmount());
        invoice.setDate(stage.getDateTo());
        invoice.setNumber(String.valueOf(sequences.createNextValue(Sequence.withName("invoice_number_seq"))));
        invoice.setDescription(stage.getDescription());
        return dataManager.save(invoice);
    }

    private ServiceCompletionCertificate createServiceCompletionCertificate(Stage stage) {
        ServiceCompletionCertificate serviceCompletionCertificate = dataManager.create(ServiceCompletionCertificate.class);
        serviceCompletionCertificate.setAmount(stage.getAmount());
        serviceCompletionCertificate.setVat(stage.getVat());
        serviceCompletionCertificate.setTotalAmount(stage.getTotalAmount());
        serviceCompletionCertificate.setDate(stage.getDateTo());
        serviceCompletionCertificate.setNumber(String.valueOf(sequences.createNextValue(Sequence.withName("service_completion_cert_number_seq"))));
        serviceCompletionCertificate.setDescription(stage.getDescription());
        return dataManager.save(serviceCompletionCertificate);
    }
}