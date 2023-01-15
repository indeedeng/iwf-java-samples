package io.iworkflow.workflow.persistence;

import io.iworkflow.core.StateDef;
import io.iworkflow.core.Workflow;
import io.iworkflow.core.persistence.DataObjectDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.core.persistence.SearchAttributeDef;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static io.iworkflow.gen.models.SearchAttributeValueType.INT;
import static io.iworkflow.gen.models.SearchAttributeValueType.KEYWORD;

@Component
public class BasicPersistenceWorkflow implements Workflow {
    public static final String TEST_DATA_OBJECT_KEY = "data-obj-1";

    public static final String TEST_SEARCH_ATTRIBUTE_KEYWORD = "CustomKeywordField";
    public static final String TEST_SEARCH_ATTRIBUTE_INT = "CustomIntField";

    @Override
    public List<StateDef> getStates() {
        return Arrays.asList(StateDef.startingState(new BasicPersistenceWorkflowState1()));
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataObjectDef.create(String.class, TEST_DATA_OBJECT_KEY),
                SearchAttributeDef.create(INT, TEST_SEARCH_ATTRIBUTE_INT),
                SearchAttributeDef.create(KEYWORD, TEST_SEARCH_ATTRIBUTE_KEYWORD)
        );
    }
}
