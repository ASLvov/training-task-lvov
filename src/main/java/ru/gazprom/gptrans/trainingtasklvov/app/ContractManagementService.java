package ru.gazprom.gptrans.trainingtasklvov.app;

import io.jmix.core.DataManager;
import io.jmix.email.Emailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gazprom.gptrans.trainingtasklvov.entity.Contract;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;
import ru.gazprom.gptrans.trainingtasklvov.entity.Status;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component("ttl_ContractManagementService")
public class ContractManagementService {
    private static final String DEFAULT_STAGE_NAME = "Начало и конец";
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Emailer emailer;

    public void createContractWithDefaultStage(Contract contract) {
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

    public void changeContractStatus(Contract contract, String statusCode) {
        contract.setStatus(getStatusByStatusCode(statusCode));
        dataManager.save(contract);
    }

    public String getNextStatusCodeByInstanceAndOutcome(Contract contract, String outcome) {
        Status currentStatus = contract.getStatus();
        if ("approve".equalsIgnoreCase(outcome)) {
            if ("TO_APPROVAL".equalsIgnoreCase(currentStatus.getCode())) {
                return "ACTIVE";
            } else if ("ACTIVE".equalsIgnoreCase(currentStatus.getCode())) {
                return "FINISHED";
            }
        } else if ("reject".equalsIgnoreCase(outcome)) {
            if ("TO_APPROVAL".equalsIgnoreCase(currentStatus.getCode())) {
                return "CANCELED";
            }
        }
        return currentStatus.getCode();
    }

    private Status getStatusByStatusCode(String statusCode) {
        Optional<Status> optionalStatus = dataManager.load(Status.class)
                .query(String.format("e.code = '%s'", statusCode.toUpperCase()))
                .optional();
        if (optionalStatus.isPresent()) {
            return optionalStatus.get();
        } else {
            throw new RuntimeException("Unknown status code");
        }
    }
}