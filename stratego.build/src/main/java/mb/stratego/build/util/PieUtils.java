package mb.stratego.build.util;

import java.io.Serializable;
import java.util.function.Function;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.OutputStampers;

@SuppressWarnings("unchecked")
public class PieUtils {
    public static <I extends Serializable, O extends Serializable, P extends Serializable> P requirePartial(
        ExecContext c, TaskDef<I, O> taskDef, I input, SerializableFunction<O, P> mapping) {
        return mapping.apply(c.require(taskDef, input,
            OutputStampers.funcEquals((Function<Serializable, Serializable>) mapping)));
    }

    public static <O extends Serializable, P extends Serializable> P requirePartial(
        ExecContext c, Task<O> task, SerializableFunction<O, P> mapping) {
        return mapping.apply(
            c.require(task, OutputStampers.funcEquals((Function<Serializable, Serializable>) mapping)));
    }

    public static <I extends Serializable, O extends Serializable, P extends Serializable> P requirePartial(
        ExecContext c, STaskDef<I, O> sTaskDef, I input, SerializableFunction<O, P> mapping) {
        return mapping.apply(c.require(sTaskDef, input,
            OutputStampers.funcEquals((Function<Serializable, Serializable>) mapping)));
    }

    public static <O extends Serializable, P extends Serializable> P requirePartial(
        ExecContext c, STask<O> sTask, SerializableFunction<O, P> mapping) {
        return mapping.apply(
            c.require(sTask, OutputStampers.funcEquals((Function<Serializable, Serializable>) mapping)));
    }
}