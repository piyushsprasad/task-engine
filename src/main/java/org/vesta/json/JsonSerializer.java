package org.vesta.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vesta.models.actions.Action;
import org.vesta.models.actions.ActionFactory;
import org.vesta.models.actions.ActionType;
import org.vesta.models.task.Task;
import org.vesta.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSerializer {
    public ObjectMapper mapper;
    public ActionFactory actionFactory;

    public JsonSerializer(ActionFactory actionFactory) {
        mapper = new ObjectMapper();
        this.actionFactory = actionFactory;
    }

    public List<Action> parseActions(String actionFileName) {
        File actionFile = Paths.get(actionFileName).toFile();
        List<Map<String, Object>> dataList;

        try {
            dataList = mapper.readValue(actionFile, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse Actions file. ", e);
        }

        List<Action> actions = new ArrayList<Action>();
        for (Map<String, Object> data : dataList) {
            actions.add(parseAction(data));
        }
        return actions;
    }

    private Action parseAction(Map<String, Object> data) {
        Action a;
        String loanId;
        String borrowerId;
        String field;
        Object value;
        ActionType type = ActionType.getEnum((String) safeGet(data, JsonKeys.ACTION_KEY, String.class));

        switch (type) {
            case CREATE_LOAN:
                loanId = (String) safeGet(data, JsonKeys.LOAN_ID_KEY, String.class);
                a = actionFactory.createLoanAction(loanId);
                break;
            case CREATE_BORROWER:
                loanId = (String) safeGet(data, JsonKeys.LOAN_ID_KEY, String.class);
                borrowerId = (String) safeGet(data, JsonKeys.BORROWER_ID_KEY, String.class);
                a = actionFactory.createBorrowerAction(borrowerId, loanId);
                break;
            case SET_LOAN_FIELD:
                loanId = (String) safeGet(data, JsonKeys.LOAN_ID_KEY, String.class);
                field = (String) safeGet(data, JsonKeys.FIELD_KEY, String.class);
                value = safeGet(data, JsonKeys.VALUE_KEY, Object.class);
                a = actionFactory.setLoanFieldAction(loanId, field, value);
                break;
            case SET_BORROWER_FIELD:
                borrowerId = (String) safeGet(data, JsonKeys.BORROWER_ID_KEY, String.class);
                field = (String) safeGet(data, JsonKeys.FIELD_KEY, String.class);
                value = safeGet(data, JsonKeys.VALUE_KEY, Object.class);
                a = actionFactory.setBorrowerFieldAction(borrowerId, field, value);
                break;
            default:
                throw new IllegalArgumentException("Invalid Action Key provided: " + type);
        }
        return a;
    }

    public List<Task> parseTasks(String taskFileName) {
        File taskFile = Paths.get(taskFileName).toFile();
        List<Task> taskList;

        try {
            taskList = mapper.readValue(taskFile, new TypeReference<List<Task>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse Tasks file. ", e);
        }

        return taskList;
    }

    private Object safeGet(Map<String, Object> data, String key, Class<?> valueClass) {
        if (!data.containsKey(key)) {
            String mapOutput = Utils.printMap(data);
            throw new IllegalArgumentException(
                    String.format("Data does not contain key %s. Data Map: %s", key, mapOutput));
        }

        Object o = data.get(key);
        if (!valueClass.isInstance(o) && o != null) {
            String mapOutput = Utils.printMap(data);
            throw new IllegalArgumentException(
                    String.format("Data at key %s is not desired type %s. Data Map: %s", key, valueClass.getName(), mapOutput));
        }

        return o;
    }
}
