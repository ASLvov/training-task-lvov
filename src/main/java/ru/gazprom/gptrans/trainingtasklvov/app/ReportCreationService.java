package ru.gazprom.gptrans.trainingtasklvov.app;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.DataManager;
import io.jmix.reports.runner.ReportRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gazprom.gptrans.trainingtasklvov.entity.Contract;
import ru.gazprom.gptrans.trainingtasklvov.entity.Invoice;
import ru.gazprom.gptrans.trainingtasklvov.entity.ServiceCompletionCertificate;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;

@Component("ttl_ReportCreationService")
public class ReportCreationService {
    @Autowired
    private ReportRunner reportRunner;
    @Autowired
    private DataManager dataManager;

    public ReportOutputDocument createInvoiceReport(Invoice invoice) {
        Stage stage = invoice.getStage();
        Contract contract = dataManager.load(Contract.class)
                .id(stage.getContract().getId())
                .one();
        return reportRunner.byReportCode("invoice")
                .addParam("invoice", invoice)
                .addParam("contract", contract)
                .addParam("performer", contract.getPerformer())
                .addParam("customer", contract.getCustomer())
                .run();
    }

    public ReportOutputDocument createServiceCertificateReport(ServiceCompletionCertificate serviceCompletionCertificate) {
        Stage stage = dataManager.load(Stage.class)
                .id(serviceCompletionCertificate.getStage().getId())
                .one();
        Contract contract = dataManager.load(Contract.class)
                .id(stage.getContract().getId())
                .one();
        return reportRunner.byReportCode("service_completion_certificate")
                .addParam("act", serviceCompletionCertificate)
                .addParam("stage", stage)
                .addParam("contract", contract)
                .addParam("performer", contract.getPerformer())
                .addParam("customer", contract.getCustomer())
                .run();
    }
}