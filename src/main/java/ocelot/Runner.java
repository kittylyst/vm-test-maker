package ocelot;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.invoke.MethodHandles.lookup;

public class Runner {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: ");
            System.exit(1);
        }
        final var className = args[0];
        var methodName = "a";
        if (args.length > 1) {
            methodName = args[1];
        }
        var r = new Runner();
        try {
            r.run(className, methodName);
        } catch (IOException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    void run(String className, String methodName) throws Throwable {
        var buffy = Files.readAllBytes(Path.of(className+ ".class"));
        MethodHandles.Lookup l = lookup();
        var clazz = l.defineClass(buffy);
        // FIXME - this is hardcoded to ()I descriptor
        var mt = MethodType.methodType(int.class);
        var h = l.findStatic(clazz, methodName, mt);
        var res = h.invoke();
        System.out.println(res);
    }
}
