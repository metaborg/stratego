package mb.stratego.build.strincr.function;

import java.util.ArrayList;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.task.output.GlobalData;

public class GetResolveMessages implements SerializableFunction<GlobalData, ArrayList<Message>> {
    public static final GetResolveMessages INSTANCE = new GetResolveMessages();

    private GetResolveMessages() {
    }

    @Override public ArrayList<Message> apply(GlobalData output) {
        return output.messages;
    }

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
