package io.iworkflow.patterns.workflow.storage;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.gen.models.PersistenceLoadingType;

import java.util.Collections;
import java.util.List;

import io.iworkflow.core.persistence.*;

public class OrderWorkflow implements ObjectWorkflow {
    public static final String STATUS = "status";

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
            DataAttributeDef.create(
                String.class,
                STATUS,
                DbAttributeSync.of(
                    /* dataSource   */ MyDataSources.ORDERS,
                    /* table        */ "orders",          
                    /* pk column    */ "id",
                    /* locator      */ (workflowId, persistence) ->
                        new CellLocation(workflowId, "status_col"))));
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(StateDef.startingState(new DecideState()));
    }
}


public class OrderFlow implements Flow {

    // value is the cancel reason 
    public static final Channel CANCEL_ORDER_CH = Channel.define("cancel_order", String.class);
    public static final Attribute ATTR_ORDER_STATUS = Attribute.define(
        "status", String.class,
        DbAttributeSync.of(
            MyDataSources.ORDERS, 
            "orders",             // table
            "id",                 // primary key column
            // row locator
            (runId, context) ->
                new CellLocation(runId, "status_col") // using runId as orderId
        ));
    );
    public static final Attribute ATTR_CANCEL_REASON = Attribute.define("reason", String.class);
    
    @Override
    public PersistenceSchema getPersistenceSchema() {
        return PersistenceSchema.of(
            list.of( ATTR_ORDER_STATUS, ATTR_CANCEL_REASON)
            list.of( CANCEL_ORDER_CH),
        );
    }
    
    @RPC
    public String cancelOrder(Context context, String reason){
        ATTR_ORDER_STATUS.set(context, "canceled")
        ATTR_CANCEL_REASON.set(context, reason)
        // update attribute and also publish message to the channel, atomically 
        CancelOrderCh.publish(context, reason)
    }
    
    /**
     * Static getter to fetch the singleton workflow id based on the staging level.
     * @return the storage workflow id
     */
    public static String getStorageWorkflowId() {
        return String.format("sample-storage-%s", "test");
    }

    /**
     * Returns the list of states defined in the workflow.
     * @return a list of StateDef objects representing the workflow states.
     */
    @Override
    public List<StateDef> getWorkflowStates() {
        // No states in storage workflow
        return Collections.emptyList();
    }

    /**
     * Returns the persistence schema for the workflow.
     * This schema includes a data attribute for the storage class.
     * @return a list of PersistenceFieldDef objects representing the persistence schema.
     */
    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of(DataAttributeDef.create(Storage.class, DA_STORE));
    }

    /**
     * Adds an item to the storage. Locking the storage data attribute because we are reading and then setting the data
     * in this method.
     * @param context the workflow context.
     * @param request the request data including the storage key and value.
     * @param persistence the persistence interface for managing workflow data.
     * @param communication the communication interface for workflow interactions.
     */
    @RPC(dataAttributesLoadingType = PersistenceLoadingType.PARTIAL_WITH_EXCLUSIVE_LOCK, dataAttributesLockingKeys = {DA_STORE})
    public void addItem(Context context, AddStorageItemRequest request, Persistence persistence, Communication communication) {
        Storage storage = persistence.getDataAttribute(DA_STORE, Storage.class);
        if (storage == null) {
            storage = new Storage();
        }
        storage.addItem(request.key(), request.value());
        persistence.setDataAttribute(DA_STORE, storage);
    }

    /**
     * Retrieves the storage item using the provided key. Do not need to lock the storage data attribute because we are doing
     * read-only.
     * @param context the workflow context.
     * @param itemKey the key of the item to retrieve.
     * @param persistence the persistence interface for managing workflow data.
     * @param communication the communication interface for workflow interactions.
     * @return the value of the item if it exists, null otherwise.
     */
    @RPC
    public String getItem(Context context, String itemKey, Persistence persistence, Communication communication) {
        final Storage storage = persistence.getDataAttribute(DA_STORE, Storage.class);
        return storage == null ? null : storage.getItem(itemKey);
    }


    


    /**
     * Remove an item from the storage. Locking the storage data attribute because we are reading and then setting the data
     * in this method.
     * @param context the workflow context.
     * @param itemKey the key of the item to remove.
     * @param persistence the persistence interface for managing workflow data.
     * @param communication the communication interface for workflow interactions.
     */
    @RPC(dataAttributesLoadingType = PersistenceLoadingType.PARTIAL_WITH_EXCLUSIVE_LOCK, dataAttributesLockingKeys = {DA_STORE})
    public void removeItem(Context context, String itemKey, Persistence persistence, Communication communication) {
        final Storage storage = persistence.getDataAttribute(DA_STORE, Storage.class);
        if (storage != null) {
            storage.removeItem(itemKey);
            persistence.setDataAttribute(DA_STORE, storage);
        }
    }
}
