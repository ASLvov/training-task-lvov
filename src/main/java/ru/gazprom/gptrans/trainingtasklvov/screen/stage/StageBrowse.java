package ru.gazprom.gptrans.trainingtasklvov.screen.stage;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gazprom.gptrans.trainingtasklvov.app.ReportCreationService;
import ru.gazprom.gptrans.trainingtasklvov.entity.Invoice;
import ru.gazprom.gptrans.trainingtasklvov.entity.ServiceCompletionCertificate;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;

import java.text.SimpleDateFormat;

@UiController("ttl_Stage.browse")
@UiDescriptor("stage-browse.xml")
@LookupComponent("stagesTable")
public class StageBrowse extends StandardLookup<Stage> {
    private static final String INVOICE_TITLE = "Счет № %s от %s";
    private static final String CERTIFICATE_TITLE = "Акт № %s от %s";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    @Autowired
    private DataGrid<Stage> stagesTable;
    @Autowired
    private ReportCreationService reportCreationService;
    @Autowired
    private Downloader downloader;

    @Subscribe
    public void onInit(InitEvent event) {
        DataGrid.ClickableTextRenderer<Stage> stagesTableInvoiceRenderer = getApplicationContext().getBean(DataGrid.ClickableTextRenderer.class);
        stagesTableInvoiceRenderer.setRendererClickListener(rendererClickEvent -> {
            ReportOutputDocument invoiceReport = reportCreationService.createInvoiceReport(rendererClickEvent.getItem().getInvoice());
            downloader.download(invoiceReport.getContent(), invoiceReport.getDocumentName());
        });
        stagesTable.getColumn("invoice").setRenderer(stagesTableInvoiceRenderer);
        DataGrid.ClickableTextRenderer<Stage> stagesTableServiceCompletionCertificateRenderer = getApplicationContext().getBean(DataGrid.ClickableTextRenderer.class);
        stagesTableServiceCompletionCertificateRenderer.setRendererClickListener(rendererClickEvent -> {
            ReportOutputDocument serviceCertificateReport = reportCreationService.createServiceCertificateReport(rendererClickEvent.getItem().getServiceCompletionCertificate());
            downloader.download(serviceCertificateReport.getContent(), serviceCertificateReport.getDocumentName());
        });
        stagesTable.getColumn("serviceCompletionCertificate").setRenderer(stagesTableServiceCompletionCertificateRenderer);
    }

    @Install(to = "stagesTable.invoice", subject = "columnGenerator")
    private String stagesTableInvoiceColumnGenerator(DataGrid.ColumnGeneratorEvent<Stage> columnGeneratorEvent) {
        String title = "";
        Stage stage = columnGeneratorEvent.getItem();
        Invoice invoice = stage.getInvoice();
        if (invoice != null) {
            title = String.format(INVOICE_TITLE, invoice.getNumber(), new SimpleDateFormat(DATE_FORMAT).format(invoice.getDate()));
        }
        return title;
    }

    @Install(to = "stagesTable.serviceCompletionCertificate", subject = "columnGenerator")
    private String stagesTableServiceCompletionCertificateColumnGenerator(DataGrid.ColumnGeneratorEvent<Stage> columnGeneratorEvent) {
        String title = "";
        Stage stage = columnGeneratorEvent.getItem();
        ServiceCompletionCertificate serviceCompletionCertificate = stage.getServiceCompletionCertificate();
        if (serviceCompletionCertificate != null) {
            title = String.format(CERTIFICATE_TITLE, serviceCompletionCertificate.getNumber(),
                    new SimpleDateFormat(DATE_FORMAT).format(serviceCompletionCertificate.getDate()));
        }
        return title;
    }
}