package ru.gazprom.gptrans.trainingtasklvov.screen.servicecompletioncertificate;

import io.jmix.ui.screen.*;
import ru.gazprom.gptrans.trainingtasklvov.entity.ServiceCompletionCertificate;

@UiController("ttl_ServiceCompletionCertificate.browse")
@UiDescriptor("service-completion-certificate-browse.xml")
@LookupComponent("serviceCompletionCertificatesTable")
public class ServiceCompletionCertificateBrowse extends StandardLookup<ServiceCompletionCertificate> {
}