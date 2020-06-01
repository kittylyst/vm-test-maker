package ocelot;

import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MainMaker {

    private final Map<String,Byte> opcodes = new HashMap<>();

    public static void main(String[] args) {
        MainMaker m = new MainMaker();
        m.init();
        m.run();
    }

    void init() {
        var url = MainMaker.class.getClassLoader().getResource("opcodes.txt");
        try {
            for (var l : Files.readAllLines(Paths.get(url.toURI()))) {
                var symVal = l.split(",");
                var val = Integer.parseInt(symVal[1]);
                opcodes.put(symVal[0], (byte)val);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(opcodes);
    }


    public void run() {
        var tmp = new String[]{"ICONST_1", "ICONST_1", "IRETURN"};

    }

}
