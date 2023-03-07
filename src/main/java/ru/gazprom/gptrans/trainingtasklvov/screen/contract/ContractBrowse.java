package ru.gazprom.gptrans.trainingtasklvov.screen.contract;

import io.jmix.ui.screen.*;
import ru.gazprom.gptrans.trainingtasklvov.entity.Contract;

@UiController("ttl_Contract.browse")
@UiDescriptor("contract-browse.xml")
@LookupComponent("contractsTable")
public class ContractBrowse extends StandardLookup<Contract> {
}