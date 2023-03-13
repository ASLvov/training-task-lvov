package ru.gazprom.gptrans.trainingtasklvov.screen.stage;

import io.jmix.ui.Notifications;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gazprom.gptrans.trainingtasklvov.configuration.VatConfiguration;
import ru.gazprom.gptrans.trainingtasklvov.entity.Organization;
import ru.gazprom.gptrans.trainingtasklvov.entity.Stage;

import java.math.BigDecimal;

@UiController("ttl_Stage.edit")
@UiDescriptor("stage-edit.xml")
@EditedEntityContainer("stageDc")
public class StageEdit extends StandardEditor<Stage> {
    @Autowired
    private InstanceContainer<Stage> stageDc;
    @Autowired
    private VatConfiguration vatConfiguration;
    @Autowired
    private TextField<BigDecimal> amountField;
    @Autowired
    private TextField<BigDecimal> vatField;
    @Autowired
    private TextField<BigDecimal> totalAmountField;
    @Autowired
    private Notifications notifications;


    @Subscribe("amountField")
    public void onAmountFieldValueChange(HasValue.ValueChangeEvent<BigDecimal> event) {
        Organization performer = stageDc.getItem().getContract().getPerformer();
        if (performer == null) {
            notifications.create()
                    .withCaption("Не выбран поставщик услуг!")
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
}