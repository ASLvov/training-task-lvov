package ru.gazprom.gptrans.trainingtasklvov.listener;

import io.jmix.core.DataManager;
import io.jmix.core.EntitySet;
import io.jmix.core.SaveContext;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.gazprom.gptrans.trainingtasklvov.entity.Invoice;
import ru.gazprom.gptrans.trainingtasklvov.entity.ServiceCompletionCertificate;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;

@Component("ttl_StageEventListener")
public class StageEventListener {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Sequences sequences;

    @TransactionalEventListener
    public void onStageChangedAfterCommit(EntityChangedEvent<Stage> event) {
        Stage stage = dataManager.load(event.getEntityId())
                .joinTransaction(false)
                .one();
        Invoice invoice = createOrChangeInvoice(stage);
        ServiceCompletionCertificate serviceCompletionCertificate = createOrChangeServiceCompletionCertificate(stage);
        stage.setInvoice(invoice);
        stage.setServiceCompletionCertificate(serviceCompletionCertificate);
        dataManager.save(new SaveContext()
                .saving(stage)
                .setJoinTransaction(false)
        );
    }

    private Invoice createOrChangeInvoice(Stage stage) {
        Invoice invoice;
        if (stage.getInvoice() == null) {
            invoice = dataManager.create(Invoice.class);
            invoice.setNumber(String.valueOf(sequences.createNextValue(Sequence.withName("invoice_number_seq"))));
        } else {
            invoice = dataManager.load(Invoice.class)
                    .id(stage.getInvoice().getId())
                    .one();
        }
        invoice.setAmount(stage.getAmount());
        invoice.setVat(stage.getVat());
        invoice.setTotalAmount(stage.getTotalAmount());
        invoice.setDate(stage.getDateTo());
        invoice.setDescription(stage.getDescription());
        EntitySet entitySet = dataManager.save(new SaveContext()
                .saving(invoice)
                .setJoinTransaction(false)
        );
        return entitySet.get(Invoice.class, invoice.getId());
    }

    private ServiceCompletionCertificate createOrChangeServiceCompletionCertificate(Stage stage) {
        ServiceCompletionCertificate serviceCompletionCertificate;
        if (stage.getServiceCompletionCertificate() == null) {
            serviceCompletionCertificate = dataManager.create(ServiceCompletionCertificate.class);
            serviceCompletionCertificate.setNumber(String.valueOf(sequences.createNextValue(Sequence.withName("service_completion_cert_number_seq"))));
        } else {
            serviceCompletionCertificate = dataManager.load(ServiceCompletionCertificate.class)
                    .id(stage.getServiceCompletionCertificate().getId())
                    .one();
        }
        serviceCompletionCertificate.setAmount(stage.getAmount());
        serviceCompletionCertificate.setVat(stage.getVat());
        serviceCompletionCertificate.setTotalAmount(stage.getTotalAmount());
        serviceCompletionCertificate.setDate(stage.getDateTo());
        serviceCompletionCertificate.setDescription(stage.getDescription());
        EntitySet entitySet = dataManager.save(new SaveContext()
                .saving(serviceCompletionCertificate)
                .setJoinTransaction(false)
        );
        return entitySet.get(ServiceCompletionCertificate.class, serviceCompletionCertificate.getId());
    }
}