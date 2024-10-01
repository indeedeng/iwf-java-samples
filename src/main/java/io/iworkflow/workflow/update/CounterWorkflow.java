package io.iworkflow.workflow.update;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.gen.models.PersistenceLoadingType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component

public class CounterWorkflow implements ObjectWorkflow {

    public static final String DA_COUNT = "COUNT";

    @Override
    public List<StateDef> getWorkflowStates() {
        return new ArrayList<>();
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataAttributeDef.create(Integer.class, DA_COUNT)
        );
    }

    // Atomically read/write data attributes in RPC will use Temporal sync update features
    @RPC(
            dataAttributesLoadingType = PersistenceLoadingType.PARTIAL_WITH_EXCLUSIVE_LOCK,
            dataAttributesLockingKeys = {DA_COUNT}
    )
    public int inc(Context context, Persistence persistence, Communication communication) {
        int count = persistence.getDataAttribute(DA_COUNT, Integer.class);
        count++;
        persistence.setDataAttribute(DA_COUNT, count);
        return count;
    }
}
