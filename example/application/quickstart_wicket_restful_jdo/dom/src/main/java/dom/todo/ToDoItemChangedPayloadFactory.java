package dom.todo;

import org.apache.isis.applib.annotation.PublishedObject.ChangeKind;
import org.apache.isis.applib.annotation.PublishedObject.PayloadFactory;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventPayloadForObjectChanged;

public class ToDoItemChangedPayloadFactory implements PayloadFactory{

    public static class ToDoItemPayload extends EventPayloadForObjectChanged<ToDoItem> {

        public ToDoItemPayload(ToDoItem changed) {
            super(changed);
        }
        
        public String getDescription() {
            return getChanged().getDescription();
        }
    }
    @Override
    public EventPayload payloadFor(Object changedObject, ChangeKind changeKind) {
        return new ToDoItemPayload((ToDoItem) changedObject);
    }

}
