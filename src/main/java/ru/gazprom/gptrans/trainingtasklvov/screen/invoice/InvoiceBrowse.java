package ru.gazprom.gptrans.trainingtasklvov.screen.invoice;

import io.jmix.ui.screen.*;
import ru.gazprom.gptrans.trainingtasklvov.entity.Invoice;

@UiController("ttl_Invoice.browse")
@UiDescriptor("invoice-browse.xml")
@LookupComponent("invoicesTable")
public class InvoiceBrowse extends StandardLookup<Invoice> {
}