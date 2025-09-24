package MapEditor;

import Level.Map;
import Maps.FirstRoom;
import Maps.SecondRoom;
import Maps.TestMap;
import Maps.TitleScreenMap;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMapNames() {
        return new ArrayList<String>() {{
            add("TestMap");
            add("TitleScreen");
            add("SecondRoom");
            add("FirstRoom");
        }};
    }

    public static Map getMapByName(String mapName) {
        switch(mapName) {
            case "TestMap":
                return new TestMap();
            case "TitleScreen":
                return new TitleScreenMap();
            case "SecondRoom":
                return new SecondRoom();
            case "FirstRoom":
                return new FirstRoom();
            default:
                throw new RuntimeException("Unrecognized map name");
        }
    }
}
